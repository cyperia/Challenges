package net.cyperia.challenges.plugin.challenges.type.helper;

import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SelectableKey;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SubSettingsBuilder;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.builder.ChooseItemSubSettingsBuilder;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.builder.ChooseMultipleItemSubSettingBuilder;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKeySpace;
import net.cyperia.challenges.plugin.utils.misc.ExperimentalUtils;
import net.cyperia.challenges.plugin.utils.misc.StructureUtils;
import net.cyperia.commons.bukkit.utils.item.ItemBuilder;
import net.cyperia.commons.common.misc.StringUtils;
import org.bukkit.Material;
import org.bukkit.StructureType;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public final class SubSettingsHelper {

  private SubSettingsHelper() {
  }

  public static final String
    ENTITY_TYPE = "entity_type",
    BLOCK = "block",
    ANY = "any",
    ITEM = "item",
    LIQUID = "liquid",
    TARGET_ENTITY = "target_entity",
    STRUCTURE = "structure";

  public static ChooseMultipleItemSubSettingBuilder createEntityTypeSettingsBuilder(boolean any, boolean player) {
    return SubSettingsBuilder.createChooseMultipleItem(ENTITY_TYPE).fill(builder -> {

      if (any) {
        builder.addSetting(SelectableKey.ofName(ANY, Material.NETHER_STAR, "entity_type.any"));
      }
      if (player) {
        builder.addSetting(SelectableKey.ofName("PLAYER", Material.PLAYER_HEAD, "entity_type.player"));
      }
      for (EntityType type : EntityType.values()) {
        if (!type.isSpawnable() || !type.isAlive()) continue;
        Material displayMaterial;
        try {
          displayMaterial = Material.valueOf(type.name() + "_SPAWN_EGG");
        } catch (Exception ex) {
          displayMaterial = Material.STRUCTURE_VOID;
        }

        builder.addSetting(SelectableKey.ofName(type.name(), displayMaterial, "entity_type.format", type));
      }
    });
  }

  public static ChooseMultipleItemSubSettingBuilder createBlockSettingsBuilder() {
    return SubSettingsBuilder.createChooseMultipleItem(BLOCK).fill(builder -> {
      builder.addSetting(SelectableKey.ofName(ANY, Material.NETHER_STAR, "block.any"));
      for (Material material : ExperimentalUtils.getMaterials()) {
        if (material.isBlock() && material.isItem() && !material.isAir()) {
          builder.addSetting(SelectableKey.ofMaterial(material.name(), material));
        }
      }
    });
  }

  public static ChooseMultipleItemSubSettingBuilder createItemSettingsBuilder() {
    return SubSettingsBuilder.createChooseMultipleItem(ITEM).fill(builder -> {
      builder.addSetting(SelectableKey.ofName(ANY, Material.NETHER_STAR, "item.any"));
      for (Material material : ExperimentalUtils.getMaterials()) {
        if (material.isItem() && !material.isAir()) {
          builder.addSetting(SelectableKey.ofMaterial(material.name(), material));
        }
      }
    });
  }

  public static SubSettingsBuilder createEntityTargetSettingsBuilder(boolean everyMob) {
    return createEntityTargetSettingsBuilder(everyMob, false);
  }

  public static SubSettingsBuilder createEntityTargetSettingsBuilder(boolean everyMob, boolean onlyPlayer) {
    return createEntityTargetSettingsBuilder(everyMob, onlyPlayer, false);
  }

  public static SubSettingsBuilder createEntityTargetSettingsBuilder(boolean everyMob, boolean onlyPlayer, boolean console) {
    ChooseItemSubSettingsBuilder builder = SubSettingsBuilder.createChooseItem(TARGET_ENTITY);

    if (console) {
      builder.addSetting(SelectableKey.ofNameDesc("console", Material.COMMAND_BLOCK_MINECART, "target_entity.console"));
    }

    if (!onlyPlayer) {
      builder.addSetting(SelectableKey.ofNameDesc("current", Material.DRAGON_HEAD, "target_entity.current"));
    }

    builder.addSetting(SelectableKey.ofNameDesc("current_player", Material.PLAYER_HEAD, "target_entity.current_player"));
    builder.addSetting(SelectableKey.ofNameDesc("random_player", Material.ZOMBIE_HEAD, "target_entity.random_player"));
    builder.addSetting(SelectableKey.ofNameDesc("every_player", Material.PLAYER_HEAD, "target_entity.every_player"));

    if (everyMob && !onlyPlayer) {
      builder.addSetting(SelectableKey.ofNameDesc("every_mob", Material.WITHER_SKELETON_SKULL, "target_entity.every_mob"));
      builder.addSetting(SelectableKey.ofNameDesc("every_mob_except_current", Material.SKELETON_SKULL, "target_entity.every_mob_except_current"));
      builder.addSetting(SelectableKey.ofNameDesc("every_mob_except_players", Material.SKELETON_SKULL, "target_entity.every_mob_except_players"));
    }
    return builder;
  }

  public static SubSettingsBuilder createPotionSettingsBuilder(boolean potionType, boolean potionTime) {
    SubSettingsBuilder potionSettings = SubSettingsBuilder.createValueItem().fill(builder -> {

      if (potionTime) {
        builder.addModifierSetting("length",
          new ItemStack(Material.CLOCK), MessageKeySpace.of("custom.action.potion_effect.sub.length"),
          30, 1, 60, ValueUnit.SECONDS.bare()
        );
      }
      builder.addModifierSetting("amplifier",
        new ItemStack(Material.STONE_SWORD), MessageKeySpace.of("custom.action.potion_effect.sub.amplifier"),
        3, 1, 8, ValueUnit.STRENGTH
      );
    });

    if (potionType) {
      potionSettings = potionSettings.createChooseItemChild("potion_type").fill(builder -> {
        for (PotionEffectType effectType : PotionEffectType.values()) {
          ItemStack displayItemPreset = new ItemBuilder.PotionBuilder(Material.POTION)
            .addEffect(effectType.createEffect(1, 0))
            .color(effectType.getColor()).build();

          // TODO custom centralized translation?
          builder.addSetting(SelectableKey.of(effectType.getName(), displayItemPreset, LocalizableMessage.wrap(StringUtils.getEnumName(effectType.getName()))));
        }
      });
    }


    return potionSettings;
  }

  public static SubSettingsBuilder createStructureSettingsBuilder() {
    return SubSettingsBuilder.createChooseItem(STRUCTURE).fill(builder -> {
      builder.addSetting(SelectableKey.ofName("random_structure", Material.STRUCTURE_BLOCK, "structure.random"));
      for (StructureType structure : StructureType.getStructureTypes().values()) {
        builder.addSetting(SelectableKey.ofName(structure.getName(), StructureUtils.getStructureIcon(structure),
          "structure.format", StringUtils.getEnumName(structure.getName())));
      }
    });
  }

}
