package net.cyperia.challenges.plugin.challenges.implementation.preset;

import net.cyperia.challenges.catalog.Challenge;
import net.cyperia.challenges.plugin.challenges.custom.TriggerActionChallenge;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.ChallengeAction;
import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A preset whose difficulty the player can tune, by driving one sub-setting of its trigger or action from
 * the modifier value. The sub-setting to drive is the one declared as {@link #VALUE}.
 */
public class PresetModifierChallenge extends SettingModifier implements TriggerActionChallenge {

  /**
   * Placeholder for a sub-setting value that follows the modifier, in the quantity its
   * {@link ValueUnit} describes: a value of 5 on a minute unit resolves to {@code 300}.
   */
  public static final String VALUE = "${value}";

  private final ChallengeTrigger trigger;
  private final Map<String, String[]> subTriggers;
  private final ChallengeAction action;
  private final Map<String, String[]> subActions;

  public PresetModifierChallenge(@NotNull Challenge catalogEntry, @NotNull ValueUnit valueUnit, int min, int max, int defaultValue,
                                 @NotNull ChallengeTrigger trigger, @NotNull Map<String, String[]> subTriggers,
                                 @NotNull ChallengeAction action, @NotNull Map<String, String[]> subActions) {
    super(catalogEntry, min, max, defaultValue);
    setValueUnit(valueUnit);
    this.trigger = trigger;
    this.subTriggers = subTriggers;
    this.action = action;
    this.subActions = subActions;
  }

  @Nullable
  @Override
  public ChallengeTrigger getTrigger() {
    return trigger;
  }

  @NotNull
  @Override
  public Map<String, String[]> getSubTriggers() {
    return resolveValue(subTriggers);
  }

  @Nullable
  @Override
  public ChallengeAction getAction() {
    return action;
  }

  @NotNull
  @Override
  public Map<String, String[]> getSubActions() {
    return resolveValue(subActions);
  }

  @NotNull
  private Map<String, String[]> resolveValue(@NotNull Map<String, String[]> subSettings) {
    Map<String, String[]> resolved = null;

    for (Map.Entry<String, String[]> entry : subSettings.entrySet()) {
      String[] values = entry.getValue();
      if (values.length != 1) continue;

      String value = resolveValue(values[0]);
      if (value == null) continue;

      if (resolved == null) resolved = new LinkedHashMap<>(subSettings);
      resolved.put(entry.getKey(), new String[]{value});
    }

    return resolved == null ? subSettings : resolved;
  }

  @Nullable
  private String resolveValue(@NotNull String declared) {
    return VALUE.equals(declared) ? String.valueOf(getValueUnit().quantityOf(getValue())) : null;
  }

}
