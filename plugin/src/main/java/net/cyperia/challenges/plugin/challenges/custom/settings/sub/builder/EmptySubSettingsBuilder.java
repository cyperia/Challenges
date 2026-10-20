package net.cyperia.challenges.plugin.challenges.custom.settings.sub.builder;

import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SubSettingsBuilder;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.IParentCustomGenerator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class EmptySubSettingsBuilder extends SubSettingsBuilder {

  public EmptySubSettingsBuilder() {
    super("none");
  }

  @Override
  public @NotNull Collection<SubSettingDisplay> getCurrentDisplayFor(@NotNull Map<String, String[]> activated) {
    return Collections.emptyList();
  }

  @Override
  public boolean hasSettings() {
    return false;
  }

  @Override
  public boolean open(Player player, IParentCustomGenerator parentGenerator, LocalizableMessage title) {
    return false;
  }

}
