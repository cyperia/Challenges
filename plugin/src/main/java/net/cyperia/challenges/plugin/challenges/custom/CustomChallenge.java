package net.cyperia.challenges.plugin.challenges.custom;

import lombok.Getter;
import lombok.ToString;
import net.cyperia.challenges.plugin.challenges.custom.settings.IChallengeSetting;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.ChallengeAction;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SubSettingsBuilder;
import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.impl.format.ComponentFormatter;
import net.cyperia.challenges.plugin.management.menu.SettingCategory;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.commons.common.config.Document;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@ToString
public class CustomChallenge extends Setting implements TriggerActionChallenge {

  private final UUID uuid;
  private Material material;
  private String customName;
  private ChallengeTrigger trigger;
  private Map<String, String[]> subTriggers;
  private ChallengeAction action;
  private Map<String, String[]> subActions;

  private Component cachedNameFormatted;

  public CustomChallenge(UUID uuid, Material displayItem, String displayName, ChallengeTrigger trigger,
                         Map<String, String[]> subTriggers, ChallengeAction action, Map<String, String[]> subActions) {
    this.uuid = uuid;
    this.material = displayItem;
    this.customName = displayName;
    this.trigger = trigger;
    this.subTriggers = subTriggers;
    this.action = action;
    this.subActions = subActions;
  }

  // Made by a player, so it has no catalog entry to take these from
  @NotNull
  @Override
  public SettingCategory getCategory() {
    return SettingCategory.of("misc_custom");
  }

  @NotNull
  @Override
  public String getNameMessageKey() {
    return "custom-challenge";
  }

  @NotNull
  @Override
  public ItemStack getDisplayItemPreset() {
    return new ItemStack(material == null ? Material.BARRIER : material);
  }

  @NotNull
  @Override
  public ItemFormatter getDisplayItem() {
    // no description: what a custom challenge does is exactly what its trigger and action state say
    return new ItemFormatter(getDisplayItemPreset())
      .name(this.getName())
      .tooltips(getDisplayTooltips())
      .states(createSettingState("trigger", trigger, subTriggers))
      .state(LocalizableMessage.empty())
      .states(createSettingState("action", action, subActions));
  }

  @NotNull
  public static List<LocalizableMessage> createSettingState(@NotNull String settingKey, @Nullable IChallengeSetting setting,
                                                            @NotNull Map<String, String[]> subSettings) {
    List<LocalizableMessage> lines = new ArrayList<>(3);
    lines.add(MessageKey.of("menu.custom.info." + settingKey)
      .withArgs(setting == null ? MessageKey.of("generic.none") : setting.getSettingName()));
    if (setting == null) return lines;

    for (SubSettingsBuilder.SubSettingDisplay display : setting.getSubSettingsBuilder().collectCurrentDisplayFor(subSettings)) {
      lines.add(MessageKey.of("menu.custom.info-format").withArgs(display.keyName(), display.valueFormatted()));
    }
    return lines;
  }

  @NotNull
  @Override
  public LocalizableMessage getName() {
    return LocalizableMessage.wrap(getDisplayName());
  }

  @NotNull
  @Override
  public LocalizableMessage getDescription() {
    List<LocalizableMessage> lines = new ArrayList<>(6);
    lines.addAll(createSettingState("trigger", trigger, subTriggers));
    lines.add(LocalizableMessage.empty());
    lines.addAll(createSettingState("action", action, subActions));
    return LocalizableMessage.wrap(lines);
  }

  @Override
  public void writeSettings(@NotNull Document document) {
    super.writeSettings(document);

    document.set("material", material == null ? null : material.name());
    document.set("name", customName);
    document.set("trigger", trigger == null ? null : trigger.getUniqueName());
    document.set("subTrigger", subTriggers);
    document.set("action", action == null ? null : action.getUniqueName());
    document.set("subActions", subActions);
  }

  public void applySettings(@NotNull Material material, @NotNull String name, @NotNull ChallengeTrigger trigger,
                            Map<String, String[]> subTriggers, ChallengeAction action, Map<String, String[]> subActions) {
    this.material = material;
    this.customName = name;
    this.trigger = trigger;
    this.subTriggers = subTriggers;
    this.action = action;
    this.subActions = subActions;
    this.cachedNameFormatted = null;
  }

  public UUID getUniqueId() {
    return uuid;
  }

  @NotNull
  public String getDisplayNameValue() {
    return customName;
  }

  @NotNull
  public Component getDisplayName() {
    if (cachedNameFormatted != null) return cachedNameFormatted;
    return cachedNameFormatted = formatChallengeName(customName);
  }

  @NotNull
  @Override
  public String getUniqueName() {
    return uuid.toString();
  }

  @NotNull
  public static Component formatChallengeName(@NotNull String rawName) {
    try {
      return ComponentFormatter.LEGACY_AMPERSAND.deserialize(rawName).colorIfAbsent(NamedTextColor.WHITE);
    } catch (Exception _) {
      // malformatted
      return Component.text(rawName).colorIfAbsent(NamedTextColor.WHITE);
    }
  }

}
