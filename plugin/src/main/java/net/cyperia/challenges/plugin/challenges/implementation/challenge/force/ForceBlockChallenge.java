package net.cyperia.challenges.plugin.challenges.implementation.challenge.force;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.EndingForceChallenge;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.server.scoreboard.ChallengeBossBar.BossBarInstance;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import net.cyperia.challenges.plugin.utils.misc.ExperimentalUtils;
import net.cyperia.commons.bukkit.utils.item.ItemUtils;
import net.cyperia.commons.common.config.Document;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.BiConsumer;

public class ForceBlockChallenge extends EndingForceChallenge {

  private Material block;

  public ForceBlockChallenge() {
    super(2, 15);
    setValueUnit(ValueUnit.MINUTES.jitter(30));
  }

  @NotNull
  @Override
  protected BiConsumer<BossBarInstance, Player> setupBossbar() {
    return (bossbar, player) -> {
      if (getState() == WAITING) {
        bossbar.setTitle(getChallengeMessageKey("bossbar-waiting"));
        return;
      }

      bossbar.setColor(BossBar.Color.GREEN);
      bossbar.setProgress(getProgress());
      bossbar.setTitle(getChallengeMessageKey("bossbar-instruction"), block, ChallengeAPI.formatTime(getSecondsLeftUntilNextActivation()));
    };
  }

  @Override
  protected boolean isFailing(@NotNull Player player) {
    for (int x = -1; x <= 1; x++) {
      for (int z = -1; z <= 1; z++) {
        for (int y = -1; y <= 1; y++) {
          Material type = player.getLocation().add(x, y, z).getBlock().getType();
          if (type == block) return false;
        }
      }
    }
    return true;
  }

  @Override
  protected void broadcastFailedMessage(@NotNull Player player) {
    getChallengeMessageKey("fail").broadcast(Prefix.CHALLENGES, player, player.getLocation().subtract(0, 1, 0).getBlock().getType());
  }

  @Override
  protected void broadcastSuccessMessage() {
    getChallengeMessageKey("success").broadcast(Prefix.CHALLENGES);
  }

  @Override
  protected void chooseForcing() {
    Material[] materials = Arrays.stream(ExperimentalUtils.getMaterials())
      .filter(Material::isBlock)
      .filter(ItemUtils::isObtainableInSurvival)
      .filter(material -> !BlockUtils.isTooHardToGet(material))
      .filter(material -> !material.name().contains("WALL"))
      .toArray(length -> new Material[length]);
    block = globalRandom.choose(materials);
  }

  @Override
  protected int getForcingTime() {
    return globalRandom.range(4 * 60, 6 * 60);
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    super.loadGameState(document);
    if (document.contains("target")) {
      block = document.getEnum("target", Material.class);
      setState(block == null ? WAITING : COUNTDOWN);
    }
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    super.writeGameState(document);
    document.set("target", block);
  }

}
