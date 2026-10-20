package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.Challenges;
import org.jetbrains.annotations.NotNull;

public abstract class OneEnabledSetting extends Setting {

  private final String typeId;

  public OneEnabledSetting(@NotNull String typeId) {
    this.typeId = typeId;
  }

  public OneEnabledSetting(boolean enabledByDefault, @NotNull String typeId) {
    super(enabledByDefault);
    this.typeId = typeId;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (enabled) disableOthers();
    super.setEnabled(enabled);
  }

  protected final void disableOthers() {
    Challenges.getInstance().getChallengeManager().getChallenges().stream()
      .filter(challenge -> challenge != this)
      .filter(challenge -> challenge instanceof OneEnabledSetting)
      .map(challenge -> (OneEnabledSetting) challenge)
      .filter(setting -> setting.typeId.equals(typeId))
      .forEach(setting -> setting.setEnabled(false));
  }

}
