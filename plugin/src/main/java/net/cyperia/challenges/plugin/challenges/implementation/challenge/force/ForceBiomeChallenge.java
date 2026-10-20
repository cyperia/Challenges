package net.cyperia.challenges.plugin.challenges.implementation.challenge.force;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.CompletableForceChallenge;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.server.scoreboard.ChallengeBossBar.BossBarInstance;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.commons.common.config.Document;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;

public class ForceBiomeChallenge extends CompletableForceChallenge {

  private Biome biome;

  public ForceBiomeChallenge() {
    super(2, 20, 5);
    setValueUnit(ValueUnit.MINUTES.scaled(3).jitter(60));
  }

  @NotNull
  @Override
  public ItemStack getSettingsItemPreset() {
    return DefaultItems.createEnabledValuePreset(getValue() * 3);
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
      bossbar.setTitle(getChallengeMessageKey("bossbar-instruction"), biome, ChallengeAPI.formatTime(getSecondsLeftUntilNextActivation()));
    };
  }

  @Override
  protected void broadcastFailedMessage() {
    getChallengeMessageKey("fail").broadcast(Prefix.CHALLENGES, biome);
  }

  @Override
  protected void broadcastSuccessMessage(@NotNull Player player) {
    getChallengeMessageKey("success").broadcast(Prefix.CHALLENGES, player, biome);
  }

  @Override
  protected void chooseForcing() {
    Biome[] biomes = Arrays.stream(Biome.values())
      .filter(biome -> !biome.name().contains("END"))
      .filter(biome -> !biome.name().contains("MUSHROOM"))
      .filter(biome -> !biome.name().contains("VOID"))
      .filter(biome -> !biome.name().equals("CUSTOM"))
      .toArray(Biome[]::new);

    biome = globalRandom.choose(biomes);
  }

  @Override
  protected int getForcingTime() {
    return globalRandom.around(getRarity(biome) * 60 * 6, 60);
  }

  private int getRarity(@NotNull Biome biome) {
    Object[][] mapping = {
      {"BADLANDS", 5},
      {"JUNGLE", 4},
      {"BAMBOO", 4},
      {"MODIFIED", 3},
      {"TALL", 3},
      {"SWAMP", 2},
    };

    for (Object[] pair : mapping) {
      String key = (String) pair[0];
      if (biome.name().contains(key))
        return (int) pair[1];
    }
    return 1;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMove(@NotNull PlayerMoveEvent event) {
    if (!shouldExecuteEffect()) return;
    if (event.getTo() == null) return;
    if (event.getTo().getBlock().getBiome() != biome) return;
    completeForcing(event.getPlayer());
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    super.loadGameState(document);
    if (document.contains("target")) {
      String biomeName = document.getString("target");
      biome = Registry.BIOME.get(NamespacedKey.minecraft(Objects.requireNonNull(biomeName).toLowerCase()));
      setState(biome == null ? WAITING : COUNTDOWN);
    }
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    super.writeGameState(document);
    document.set("target", biome);
  }

}
