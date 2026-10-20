package net.cyperia.challenges.plugin.challenges.implementation;

import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeExecutionData;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.ChallengeAction;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class LockedAction extends ChallengeAction implements LockedEntry {

  public LockedAction(@NotNull String name) {
    super(name);
  }

  // Never chosen, so never executed: selecting it opens the transfer dialog instead
  @Override
  public void execute(ChallengeExecutionData executionData, Map<String, String[]> subActions) {
  }

}
