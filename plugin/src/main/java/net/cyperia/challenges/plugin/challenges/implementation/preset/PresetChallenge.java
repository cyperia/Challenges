package net.cyperia.challenges.plugin.challenges.implementation.preset;

import net.cyperia.challenges.catalog.Challenge;
import lombok.Getter;
import net.cyperia.challenges.plugin.challenges.custom.TriggerActionChallenge;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.ChallengeAction;
import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Getter
public class PresetChallenge extends Setting implements TriggerActionChallenge {

  private final ChallengeTrigger trigger;
  private final Map<String, String[]> subTriggers;
  private final ChallengeAction action;
  private final Map<String, String[]> subActions;

  public PresetChallenge(@NotNull Challenge catalogEntry, @NotNull ChallengeTrigger trigger, @NotNull Map<String, String[]> subTriggers,
                         @NotNull ChallengeAction action, @NotNull Map<String, String[]> subActions) {
    super(catalogEntry, false);
    this.trigger = trigger;
    this.subTriggers = subTriggers;
    this.action = action;
    this.subActions = subActions;
  }

}
