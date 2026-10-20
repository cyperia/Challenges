package net.cyperia.challenges.plugin.challenges.custom.settings.sub.builder;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SubSettingsBuilder;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.ValueSetting;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.impl.BooleanSetting;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.impl.ModifierSetting;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.MessageKeySpace;
import net.cyperia.challenges.plugin.management.menu.generator.AbstractMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.IParentCustomGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.choose.SubSettingValueMenuGenerator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class ValueSubSettingsBuilder extends GeneratorSubSettingsBuilder {

  private final LinkedHashMap<ValueSetting, String> defaultSettings = new LinkedHashMap<>();

  public ValueSubSettingsBuilder() {
    super("value");
  }

  public ValueSubSettingsBuilder(SubSettingsBuilder parent) {
    super("value", parent);
  }

  @Override
  public AbstractMenuGenerator getGenerator(Player player, IParentCustomGenerator parentGenerator, LocalizableMessage title) {
    return new SubSettingValueMenuGenerator(parentGenerator, new LinkedHashMap<>(getDefaultSettings()), title);
  }

  @NotNull
  @Override
  public Collection<SubSettingDisplay> getCurrentDisplayFor(@NotNull Map<String, String[]> activated) {
    List<SubSettingDisplay> display = Lists.newLinkedList();

    // TODO logic ported from legacy code; overhaul system
    for (ValueSetting setting : defaultSettings.keySet()) {
      String[] values = activated.get(setting.getKey());
      if (values == null || values.length == 0) continue;

      String value = values[0];
      display.add(new SubSettingDisplay(setting.getSubName(), setting.getSettingsName(value)));
    }

    return display;
  }

  public ValueSubSettingsBuilder addBooleanSetting(@NotNull String key, @NotNull ItemStack displayItemPreset,
                                                   @NotNull MessageKeySpace nameSpace, boolean defaultValue) {
    defaultSettings.put(new BooleanSetting(key, displayItemPreset, nameSpace),
      defaultValue ? "enabled" : "disabled");
    return this;
  }

  public ValueSubSettingsBuilder addModifierSetting(@NotNull String key, @NotNull ItemStack displayItemPreset,
                                                    @NotNull MessageKeySpace nameSpace, int defaultValue, int min, int max) {
    defaultSettings.put(new ModifierSetting(key, displayItemPreset, nameSpace, min, max),
      String.valueOf(defaultValue));
    return this;
  }

  public ValueSubSettingsBuilder addModifierSetting(@NotNull String key, @NotNull ItemStack displayItemPreset,
                                                    @NotNull MessageKeySpace nameSpace, int defaultValue, int min, int max,
                                                    @NotNull ValueUnit valueUnit) {
    defaultSettings.put(new ModifierSetting(key, displayItemPreset, nameSpace, min, max, valueUnit),
      String.valueOf(defaultValue));
    return this;
  }

  public ValueSubSettingsBuilder fill(Consumer<ValueSubSettingsBuilder> actions) {
    actions.accept(this);
    return this;
  }

  public boolean hasSettings() {
    return !defaultSettings.isEmpty();
  }

}
