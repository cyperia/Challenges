package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeExecutionData;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.ChallengeAction;

import java.util.Map;

public class CancelEventAction extends ChallengeAction {

  // Set and read on the thread that fires the trigger, which runs the actions inline
  private static final ThreadLocal<Boolean> inCanceling = ThreadLocal.withInitial(() -> false);

  public CancelEventAction(String name) {
    super(name);
  }

  public static void onPreTrigger() {
    inCanceling.set(false);
  }

  public static boolean shouldCancel() {
    if (inCanceling.get()) {
      inCanceling.set(false);
      return true;
    }
    return false;
  }

  @Override
  public void execute(ChallengeExecutionData executionData, Map<String, String[]> subActions) {
    inCanceling.set(true);
  }

}
