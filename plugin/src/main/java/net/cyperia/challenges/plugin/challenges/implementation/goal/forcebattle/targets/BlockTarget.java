package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets;

import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.ExtremeForceBattleGoal;
import net.cyperia.challenges.plugin.utils.misc.EntityUtils;
import net.cyperia.challenges.plugin.utils.misc.ExperimentalUtils;
import net.cyperia.commons.bukkit.utils.item.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockTarget extends ForceTarget<Material> {

  public BlockTarget(Material target) {
    super(target);
  }

  @Override
  public boolean check(Player player) {
    return EntityUtils.isStandingOnBlock(player, target);
  }

  public static List<Material> getPossibleBlocks() {
    List<Material> materials = new ArrayList<>(Arrays.asList(ExperimentalUtils.getMaterials()));
    materials.removeIf(material -> !ItemUtils.isBlockAvailableInSurvival(material));
    return materials;
  }

  @Override
  public ExtremeForceBattleGoal.TargetType getType() {
    return ExtremeForceBattleGoal.TargetType.BLOCK;
  }

  @Override
  public String toString() {
    return target.name();
  }

}
