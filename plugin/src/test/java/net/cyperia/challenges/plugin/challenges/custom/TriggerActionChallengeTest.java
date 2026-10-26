package net.cyperia.challenges.plugin.challenges.custom;

import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeExecutionData;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.ChallengeAction;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.Map;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TriggerActionChallengeTest {

  private final Challenges plugin = mock(Challenges.class);
  private final GlobalRegionScheduler global = mock(GlobalRegionScheduler.class);
  private final ChallengeAction action = mock(ChallengeAction.class);
  private final ChallengeExecutionData data = mock(ChallengeExecutionData.class);
  private final Map<String, String[]> subActions = Map.of();
  private final TriggerActionChallenge challenge = mock(TriggerActionChallenge.class, CALLS_REAL_METHODS);
  private MockedStatic<Bukkit> bukkit;
  private MockedStatic<Challenges> challenges;

  @BeforeEach
  void mockServer() {
    bukkit = mockStatic(Bukkit.class);
    bukkit.when(Bukkit::getGlobalRegionScheduler).thenReturn(global);
    challenges = mockStatic(Challenges.class);
    challenges.when(Challenges::getInstance).thenReturn(plugin);
    when(challenge.getAction()).thenReturn(action);
    when(challenge.getSubActions()).thenReturn(subActions);
  }

  @AfterEach
  void closeServer() {
    challenges.close();
    bukkit.close();
  }

  @Test
  void executeAction_runsInlineOnATickThread() {
    bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);

    challenge.executeAction(data);

    verify(action).execute(data, subActions);
    verify(global, never()).run(any(), any());
  }

  @Test
  @SuppressWarnings("unchecked")
  void executeAction_fromAnAsyncTrigger_runsOnTheNextTick() {
    bukkit.when(Bukkit::isPrimaryThread).thenReturn(false);
    ArgumentCaptor<Consumer<ScheduledTask>> task = ArgumentCaptor.forClass(Consumer.class);

    challenge.executeAction(data);

    verify(action, never()).execute(any(), any());
    verify(global).run(eq(plugin), task.capture());
    task.getValue().accept(mock(ScheduledTask.class));
    verify(action).execute(data, subActions);
  }

}
