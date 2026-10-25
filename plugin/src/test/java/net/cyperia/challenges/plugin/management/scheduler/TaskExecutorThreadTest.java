package net.cyperia.challenges.plugin.management.scheduler;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskExecutorThreadTest {

  private final Plugin plugin = mock(Plugin.class);
  private final GlobalRegionScheduler global = mock(GlobalRegionScheduler.class);
  private final AsyncScheduler async = mock(AsyncScheduler.class);
  private MockedStatic<Bukkit> bukkit;

  @BeforeEach
  void mockServer() {
    bukkit = org.mockito.Mockito.mockStatic(Bukkit.class);
    bukkit.when(Bukkit::getGlobalRegionScheduler).thenReturn(global);
    bukkit.when(Bukkit::getAsyncScheduler).thenReturn(async);
    when(plugin.isEnabled()).thenReturn(true);
  }

  @AfterEach
  void closeServer() {
    bukkit.close();
  }

  @Test
  void syncScheduledTask_ticksOnTheGlobalRegionStartingAtTheNextTick() {
    new ScheduledTaskExecutor(plugin, new ScheduledTaskConfig(20, false)).start();

    verify(global).runAtFixedRate(eq(plugin), any(), eq(1L), eq(20L));
    verify(async, never()).runAtFixedRate(any(), any(), org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong(), any());
  }

  @Test
  void asyncScheduledTask_ticksOnTheAsyncSchedulerWithTheSameRate() {
    new ScheduledTaskExecutor(plugin, new ScheduledTaskConfig(20, true)).start();

    verify(async).runAtFixedRate(eq(plugin), any(), eq(50L), eq(1000L), eq(TimeUnit.MILLISECONDS));
    verify(global, never()).runAtFixedRate(any(), any(), org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong());
  }

  @Test
  void stop_cancelsTheRunningTask() {
    io.papermc.paper.threadedregions.scheduler.ScheduledTask running = mock(io.papermc.paper.threadedregions.scheduler.ScheduledTask.class);
    when(global.runAtFixedRate(any(), any(), org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong())).thenReturn(running);
    ScheduledTaskExecutor executor = new ScheduledTaskExecutor(plugin, new ScheduledTaskConfig(20, false));
    executor.start();

    executor.stop();

    verify(running).cancel();
  }

  @Test
  void syncTimerTask_runsInlineOnTheGlobalThread() {
    bukkit.when(Bukkit::isGlobalTickThread).thenReturn(true);
    TimerTaskExecutor executor = executor(false);

    executor.execute();

    verify(executor).run();
    verify(global, never()).execute(any(), any());
  }

  @Test
  void syncTimerTask_hopsToTheGlobalRegionFromAnyOtherThread() {
    bukkit.when(Bukkit::isGlobalTickThread).thenReturn(false);
    TimerTaskExecutor executor = executor(false);

    executor.execute();

    verify(executor, never()).run();
    verify(global).execute(plugin, executor);
  }

  @Test
  void asyncTimerTask_alwaysGoesToTheAsyncScheduler() {
    bukkit.when(Bukkit::isGlobalTickThread).thenReturn(true);
    TimerTaskExecutor executor = executor(true);

    executor.execute();

    verify(async).runNow(eq(plugin), any());
    verify(executor, never()).run();
  }

  private TimerTaskExecutor executor(boolean async) {
    TimerTaskExecutor executor = spy(new TimerTaskExecutor(plugin, new TimerTaskConfig(new TimerStatus[]{TimerStatus.RUNNING}, async)));
    doNothing().when(executor).run();
    return executor;
  }

}
