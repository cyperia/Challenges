package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeExecutionData;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.ChallengeAction;

import java.util.Map;

public class CancelEventAction extends ChallengeAction {

  public static boolean inCanceling;

  public CancelEventAction(String name) {
    super(name);
  }

  public static void onPreTrigger() {
    inCanceling = false;
  }

  public static boolean shouldCancel() {
    if (inCanceling) {
      inCanceling = false;
      return true;
    }
    return false;
  }

  @Override
  public void execute(ChallengeExecutionData executionData, Map<String, String[]> subActions) {
    inCanceling = true;
  }

}
