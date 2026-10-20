package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets;

import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.ExtremeForceBattleGoal;
import net.cyperia.challenges.plugin.utils.misc.ExperimentalUtils;
import net.cyperia.commons.bukkit.utils.item.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemTarget extends ForceTarget<Material> {

  public ItemTarget(Material target) {
    super(target);
  }

  @Override
  public boolean check(Player player) {
    return player.getInventory().contains(target);
  }

  public static List<Material> getPossibleItems() {
    List<Material> materials = new ArrayList<>(Arrays.asList(ExperimentalUtils.getMaterials()));
    materials.removeIf(material -> !material.isItem());
    materials.removeIf(material -> !ItemUtils.isObtainableInSurvival(material));
    return materials;
  }

  @Override
  public ExtremeForceBattleGoal.TargetType getType() {
    return ExtremeForceBattleGoal.TargetType.ITEM;
  }

  @Override
  public String toString() {
    return target.name();
  }

}
