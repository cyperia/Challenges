package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets;

import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.ExtremeForceBattleGoal;
import net.cyperia.challenges.plugin.utils.misc.EntityUtils;
import net.cyperia.challenges.plugin.utils.misc.Utils;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MobTarget extends ForceTarget<EntityType> {

  public MobTarget(EntityType target) {
    super(target);
  }

  @Override
  public boolean check(Player player) {
    return false;
  }

  public static List<EntityType> getPossibleMobs() {
    List<EntityType> entityTypes = new ArrayList<>(Arrays.asList(EntityType.values()));
    entityTypes.removeIf(entityType -> !entityType.isSpawnable());
    entityTypes.removeIf(entityType -> !entityType.isAlive());
    Utils.removeEnums(entityTypes, "ILLUSIONER", "ARMOR_STAND", "MANNEQUIN", "ZOMBIE_HORSE", "GIANT");

    return entityTypes;
  }

  @Override
  public ExtremeForceBattleGoal.TargetType getType() {
    return ExtremeForceBattleGoal.TargetType.MOB;
  }

  @Override
  public void updateDisplayStand(@NotNull ArmorStand armorStand) {
    Material spawnEgg = EntityUtils.getSpawnEgg(target);
    if (spawnEgg == null) {
      Objects.requireNonNull(armorStand.getEquipment()).setHelmet(null);
    } else {
      Objects.requireNonNull(armorStand.getEquipment()).setHelmet(new ItemStack(spawnEgg));
    }
  }

}
