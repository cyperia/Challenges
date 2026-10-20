package net.cyperia.challenges.plugin.challenges.custom.settings.action;

import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeExecutionData;
import net.cyperia.commons.common.collection.IRandom;

import java.util.Map;

public interface IChallengeAction {

  IRandom random = IRandom.create();

  void execute(ChallengeExecutionData executionData, Map<String, String[]> subActions);

}
