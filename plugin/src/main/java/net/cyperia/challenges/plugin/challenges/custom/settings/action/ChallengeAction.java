package net.cyperia.challenges.plugin.challenges.custom.settings.action;

import net.cyperia.challenges.catalog.CustomSetting;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeSetting;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SelectableKey;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SubSettingsBuilder;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.commons.common.collection.IRandom;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public abstract class ChallengeAction extends ChallengeSetting implements IChallengeAction {

  protected static final IRandom random = IRandom.create();

  @NotNull
  @Override
  public CustomSetting getCatalogEntry() {
    return Challenges.getInstance().getCatalog().action(getUniqueName())
      .orElseThrow(() -> new IllegalStateException("No action " + getUniqueName() + " in the challenge catalog"));
  }

  public ChallengeAction(String name, SubSettingsBuilder subSettingsBuilder) {
    super(name, subSettingsBuilder);
  }

  public ChallengeAction(String name) {
    super(name);
  }

  public ChallengeAction(String name, Supplier<SubSettingsBuilder> builderSupplier) {
    super(name, builderSupplier);
  }

  @NotNull
  public static Map<String, SelectableKey> getMenuItems() {
    return Collections.unmodifiableMap(Challenges.getInstance().getCustomSettingsLoader().getActions());
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
    return MessageKey.of("custom.action." + getRelativeMessageKey() + "." + keySuffix);
  }
}
