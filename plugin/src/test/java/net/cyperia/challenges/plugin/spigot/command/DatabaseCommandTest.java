package net.cyperia.challenges.plugin.spigot.command;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseCommandTest {

  private final Queue<Runnable> async = new ArrayDeque<>();
  private final Queue<Runnable> global = new ArrayDeque<>();
  private final List<String> events = new ArrayList<>();

  private final Executor asyncExecutor = async::add;
  private final Executor globalExecutor = global::add;

  @Test
  void runsTheDatabaseCallOnlyOnTheAsyncExecutor() {
    DatabaseCommand.offThread(asyncExecutor, globalExecutor, () -> {
      events.add("db");
      return "doc";
    }, doc -> events.add("apply " + doc), ex -> events.add("error"));

    assertTrue(events.isEmpty());
    async.poll().run();
    assertEquals(List.of("db"), events);
  }

  @Test
  void appliesTheResultOnTheGlobalExecutor() {
    DatabaseCommand.offThread(asyncExecutor, globalExecutor, () -> "doc",
      doc -> events.add("apply " + doc), ex -> events.add("error"));

    async.poll().run();
    assertTrue(events.isEmpty());
    global.poll().run();
    assertEquals(List.of("apply doc"), events);
  }

  @Test
  void reportsAFailureWithoutApplying() {
    DatabaseCommand.offThread(asyncExecutor, globalExecutor, () -> {
      throw new IOException("down");
    }, doc -> events.add("apply"), ex -> events.add("error " + ex.getMessage()));

    async.poll().run();
    assertEquals(List.of("error down"), events);
    assertTrue(global.isEmpty());
  }

}
