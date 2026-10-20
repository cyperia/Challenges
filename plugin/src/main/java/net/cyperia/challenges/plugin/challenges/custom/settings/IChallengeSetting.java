package net.cyperia.challenges.plugin.challenges.custom.settings;

import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SelectableKey;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SubSettingsBuilder;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import org.jetbrains.annotations.NotNull;

public interface IChallengeSetting extends SelectableKey {

  @NotNull
  SubSettingsBuilder getSubSettingsBuilder();

  @NotNull
  String getUniqueName();

  @NotNull
  LocalizableMessage getSettingName();

  @NotNull
  LocalizableMessage getSettingDescription();

}
