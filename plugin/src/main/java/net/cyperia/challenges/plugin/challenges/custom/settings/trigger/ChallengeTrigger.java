package net.cyperia.challenges.plugin.challenges.custom.settings.trigger;

import net.cyperia.challenges.catalog.CustomSetting;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeSetting;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SelectableKey;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SubSettingsBuilder;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public abstract class ChallengeTrigger extends ChallengeSetting implements IChallengeTrigger {

  @NotNull
  @Override
  public CustomSetting getCatalogEntry() {
    return Challenges.getInstance().getCatalog().trigger(getUniqueName())
      .orElseThrow(() -> new IllegalStateException("No trigger " + getUniqueName() + " in the challenge catalog"));
  }

  public ChallengeTrigger(String name, SubSettingsBuilder subSettingsBuilder) {
    super(name, subSettingsBuilder);
  }

  public ChallengeTrigger(String name) {
    super(name);
  }

  public ChallengeTrigger(String name, Supplier<SubSettingsBuilder> builderSupplier) {
    super(name, builderSupplier);
  }

  @NotNull
  public static Map<String, SelectableKey> getMenuItems() {
    return Collections.unmodifiableMap(Challenges.getInstance().getCustomSettingsLoader().getTriggers());
  }

  @NotNull
  @Override
  public LocalizableMessage getSettingName() {
    return getSettingMessageKey("name");
  }

  @NotNull
  @Override
  public LocalizableMessage getSettingDescription() {
    return getSettingMessageKey("desc");
  }

  @NotNull
  protected LocalizableMessage getSettingMessageKey(@NotNull String keySuffix) {
    return MessageKey.of("custom.trigger." + getRelativeMessageKey() + "." + keySuffix);
  }

}
