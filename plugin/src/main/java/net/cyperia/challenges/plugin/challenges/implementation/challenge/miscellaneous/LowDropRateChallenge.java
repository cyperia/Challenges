package net.cyperia.challenges.plugin.challenges.implementation.challenge.miscellaneous;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.management.blocks.BlockDropManager.DropPriority;
import net.cyperia.challenges.plugin.utils.misc.ExperimentalUtils;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Random;

public class LowDropRateChallenge extends SettingModifier {

  private final Random random = new Random();

  public LowDropRateChallenge() {
    super(9);
    setValueUnit(ValueUnit.PERCENT.scaled(10).describedBy(getChallengeMessageKey("settings-desc")));
  }

  @Override
  protected void onValueChange() {
    reloadChances();
  }

  @Override
  protected void onEnable() {
    reloadChances();
  }

  @Override
  protected void onDisable() {
    Challenges.getInstance().getBlockDropManager().resetDropChance(DropPriority.CHANCE);
  }

  protected void reloadChances() {
    Arrays.stream(ExperimentalUtils.getMaterials()).filter(Material::isBlock).forEach(block -> {
      Challenges.getInstance().getBlockDropManager().setDropChance(block, DropPriority.CHANCE, () -> random.nextInt(10) < getValue());
    });
  }

}
