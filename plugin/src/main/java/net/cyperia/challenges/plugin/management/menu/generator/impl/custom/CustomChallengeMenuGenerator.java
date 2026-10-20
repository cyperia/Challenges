package net.cyperia.challenges.plugin.management.menu.generator.impl.custom;

import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.ToString;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.CustomChallenge;
import net.cyperia.challenges.plugin.challenges.custom.settings.SettingType;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.ChallengeAction;
import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.menu.generator.SinglePageMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.choose.CustomChooseMaterialMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.choose.CustomMainSettingsMenuGenerator;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.chat.ChatInputHandler;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import net.cyperia.commons.common.collection.IRandom;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@ToString
public class CustomChallengeMenuGenerator extends SinglePageMenuGenerator implements IParentCustomGenerator {

  public static final int SIZE = 5 * 9;

  public static final int DELETE_SLOT = 19 + 9;
  public static final int SAVE_SLOT = 25 + 9;
  public static final int CONDITION_SLOT = 21 + 9;
  public static final int ACTION_SLOT = 23 + 9;
  public static final int MATERIAL_SLOT = 14;
  public static final int NAME_SLOT = 12;

  private static final boolean savePlayerChallenges;

  // TODO
  static {
    savePlayerChallenges = Challenges.getInstance().getConfigDocument().getBoolean(ConfigManager.Keys.SAVE_PLAYER_CUSTOM_CHALLENGES);
  }

  private final CustomChooseMaterialMenuGenerator chooseMaterialGenerator = new CustomChooseMaterialMenuGenerator(this);

  private final UUID uuid;
  private String name;
  private Material material;
  private ChallengeTrigger trigger;
  private Map<String, String[]> subTriggers;
  private ChallengeAction action;
  private Map<String, String[]> subActions;

  public CustomChallengeMenuGenerator(@NotNull CustomChallenge customChallenge) {
    this.menuType = MenuType.CUSTOM;
    this.uuid = customChallenge.getUniqueId();
    this.material = customChallenge.getMaterial();
    this.name = customChallenge.getDisplayNameValue();
    this.trigger = customChallenge.getTrigger();
    this.subTriggers = customChallenge.getSubTriggers();
    this.action = customChallenge.getAction();
    this.subActions = customChallenge.getSubActions();
  }

  /**
   * Default Settings for new Custom Challenges
   */
  public CustomChallengeMenuGenerator() {
    this.menuType = MenuType.CUSTOM;
    this.trigger = null;
    this.action = null;
    this.subTriggers = new HashMap<>();
    this.subActions = new HashMap<>();
    this.uuid = UUID.randomUUID();
    this.material = IRandom.threadLocal().choose(CustomChooseMaterialMenuGenerator.MATERIALS);
    this.name = "&7Custom &e#" + (Challenges.getInstance().getCustomChallengesLoader().getCustomChallenges().size() + 1);
  }

  @NotNull
  @Override
  public LocalizableMessage getMenuName() {
    return createSubMenuTitle(MessageKey.of("menu.custom.menu-short"), MessageKey.of("menu.custom.edit.menu"));
  }

  @NotNull
  @Override
  public GeneratorMenuPosition createMenuPosition(@NotNull Player player) {
    return new CustomChallengeMenuPosition();
  }

  @Override
  public void updateInventoryContent(@NotNull Inventory inventory, @NotNull Locale locale) {
    // Save / Delete Item
    inventory.setItem(DELETE_SLOT, createEditorItem(Material.BARRIER, "delete").build(locale));
    inventory.setItem(SAVE_SLOT, createEditorItem(Material.LIME_DYE, "save").build(locale));

    // Trigger Item
    ItemFormatter triggerItem = createEditorItem(Material.WITHER_SKELETON_SKULL, "trigger")
      .states(CustomChallenge.createSettingState("trigger", trigger, subTriggers));
    inventory.setItem(CONDITION_SLOT, triggerItem.build(locale));

    // Action Item
    ItemFormatter actionItem = createEditorItem(Material.NETHER_STAR, "action")
      .states(CustomChallenge.createSettingState("action", action, subActions));
    inventory.setItem(ACTION_SLOT, actionItem.build(locale));

    // Display Item
    inventory.setItem(MATERIAL_SLOT, createEditorItem(material == null ? Material.BARRIER : material, "material")
      .state(MessageKey.of("menu.custom.info.selected").withArgs(material != null ? material : MessageKey.of("generic.none")))
      .build(locale));

    // Name Item
    int maxNameLength = Challenges.getInstance().getCustomChallengesLoader().getMaxNameLength();
    inventory.setItem(NAME_SLOT, createEditorItem(Material.NAME_TAG, "rename", maxNameLength)
      .state(MessageKey.of("menu.custom.info.selected").withArgs(CustomChallenge.formatChallengeName(name)))
      .build(locale));
  }

  /**
   * @param settingKey the {@code menu.custom.<key>} document holding the item's name, its description and
   *                   the description of the click it handles
   */
  @NotNull
  protected ItemFormatter createEditorItem(@NotNull Material material, @NotNull String settingKey, @NotNull Object... descArgs) {
    return new ItemFormatter(material)
      .name(MessageKey.of("menu.custom." + settingKey + ".name"))
      .description(MessageKey.of("menu.custom." + settingKey + ".desc").withArgs(descArgs))
      .tooltip(MenuClickType.LEFT, MessageKey.of("menu.custom.interact." + settingKey));
  }

  @Override
  public int getInventorySize() {
    return SIZE;
  }

  @Override
  public void accept(@NotNull Player player, @NotNull SettingType type, @NotNull Map<String, String[]> data) {
    openMenu(player);

    switch (type) {
      case CONDITION:
        trigger = Challenges.getInstance().getCustomSettingsLoader().getTriggerByName(data.remove("trigger")[0]);
        this.subTriggers = data;
        break;
      case ACTION:
        action = Challenges.getInstance().getCustomSettingsLoader().getActionByName(data.remove("action")[0]);
        this.subActions = data;
        break;
      case MATERIAL:
        material = Material.valueOf(data.remove("material")[0]);
        break;
    }

    updatePages();
  }

  @Override
  public void decline(@NotNull Player player) {
    openMenu(player);
  }

  public void setName(@NotNull String name) {
    this.name = name;
    updatePages();
  }

  @NotNull
  public CustomChallenge save() {
    return Challenges.getInstance().getCustomChallengesLoader()
      .registerCustomChallenge(uuid, material, name, trigger, subTriggers, action, subActions, true);
  }

  @Override
  protected void handleNavigateOutOfMenu(@NotNull Player player) {
    navigateToChallengeList(player);
  }

  private void navigateToChallengeList(@NotNull Player player) {
    CustomHomeMenuGenerator home = MenuType.CUSTOM.getMenuGenerator();
    CustomChallenge challenge = Challenges.getInstance().getCustomChallengesLoader().getCustomChallenges().get(uuid);
    if (challenge == null) {
      home.openMenu(player);
      return;
    }
    CustomListMenuGenerator list = home.getListGenerator();
    int page = Math.clamp(list.getPageOfChallenge(challenge), 0, list.getPageCount() - 1);
    list.openMenu(player, page);
  }

  public class CustomChallengeMenuPosition extends SinglePageGeneratorMenuPosition {

    @Override
    public boolean handleMenuClick(@NotNull MenuClickInfo info) {
      Player player = info.getPlayer();
      switch (info.getSlot()) {
        case MATERIAL_SLOT -> {
          chooseMaterialGenerator.openMenu(player);
          SoundSample.CLICK.play(player);
          return true;
        }
        case CONDITION_SLOT -> {
          new CustomMainSettingsMenuGenerator(CustomChallengeMenuGenerator.this, SettingType.CONDITION, "trigger",
            MessageKey.of("menu.custom.trigger.menu"), ChallengeTrigger.getMenuItems(),
            key -> Challenges.getInstance().getCustomSettingsLoader().getTriggerByName(key))
            .openMenu(player);
          SoundSample.CLICK.play(player);
          return true;
        }
        case ACTION_SLOT -> {
          new CustomMainSettingsMenuGenerator(CustomChallengeMenuGenerator.this, SettingType.ACTION, "action",
            MessageKey.of("menu.custom.action.menu"), ChallengeAction.getMenuItems(),
            key -> Challenges.getInstance().getCustomSettingsLoader().getActionByName(key))
            .openMenu(player);
          SoundSample.CLICK.play(player);
          return true;
        }
        case NAME_SLOT -> {
          MessageKey.of("menu.custom.rename.chat").send(player, Prefix.CUSTOM);
          player.closeInventory();
          ChatInputHandler.set(player, new NameChatInputHandler());
          return true;
        }
        case DELETE_SLOT -> {
          if (!Challenges.getInstance().getCustomChallengesLoader().getCustomChallenges().containsKey(uuid)) {
            MessageKey.of("menu.custom.delete.new").send(player, Prefix.CUSTOM);
            SoundSample.BASS_OFF.play(player);
            return true;
          }
          MessageKey.of("menu.custom.delete.done").send(player, Prefix.CUSTOM, CustomChallenge.formatChallengeName(name));
          navigateToChallengeList(player);
          Challenges.getInstance().getCustomChallengesLoader().unregisterCustomChallenge(uuid);
          new SoundSample().addSound(Sound.ENTITY_WITHER_BREAK_BLOCK, 0.4f).play(player);
          return true;
        }
        case SAVE_SLOT -> {
          String defaults = new CustomChallengeMenuGenerator().toString();
          String current = CustomChallengeMenuGenerator.this.toString();
          if (defaults.equals(current)) {
            MessageKey.of("menu.custom.save.no-changes").send(player, Prefix.CUSTOM);
            SoundSample.BASS_OFF.play(player);
            return true;
          }
          if (trigger == null || action == null) {
            MessageKey.of("menu.custom.save.missing-settings").send(player, Prefix.CUSTOM);
            SoundSample.BASS_OFF.play(player);
            return true;
          }
          save();
          navigateToChallengeList(player);
          MessageKey.of("menu.custom.save.done").send(player, Prefix.CUSTOM);
          if (savePlayerChallenges) {
            MessageKey.of("menu.custom.save.done-db").send(player, Prefix.CUSTOM);
          }
          SoundSample.LEVEL_UP.play(player);
          return true;
        }
      }

      return false;
    }

  }

  public class NameChatInputHandler implements ChatInputHandler {

    @Override
    public void handleChatInput(@NotNull AsyncChatEvent event, @NotNull String input) {
      int maxNameLength = Challenges.getInstance().getCustomChallengesLoader().getMaxNameLength();
      if (input.length() > maxNameLength) {
        MessageKey.of("custom.chars-exceeded").send(event.getPlayer(), Prefix.CUSTOM, maxNameLength);
        return;
      }
      Bukkit.getScheduler().runTask(Challenges.getInstance(), () -> {
        setName(input);
        openMenu(event.getPlayer());
      });
    }

    @Override
    public void handleCancel(@NotNull Player player) {
      openMenu(player);
    }
  }

}
