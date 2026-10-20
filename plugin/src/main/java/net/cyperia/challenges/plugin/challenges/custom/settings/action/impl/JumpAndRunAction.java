package net.cyperia.challenges.plugin.challenges.custom.settings.action.impl;

import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeExecutionData;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.ChallengeAction;
import net.cyperia.challenges.plugin.challenges.implementation.challenge.extraworld.JumpAndRunChallenge;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;

import java.util.Map;

public class JumpAndRunAction extends ChallengeAction {

  public JumpAndRunAction(String name) {
    super(name);
  }

  @Override
  public void execute(
    ChallengeExecutionData executionData,
    Map<String, String[]> subActions) {
    AbstractChallenge.getFirstInstance(JumpAndRunChallenge.class).startWorldChallenge();
  }

}
