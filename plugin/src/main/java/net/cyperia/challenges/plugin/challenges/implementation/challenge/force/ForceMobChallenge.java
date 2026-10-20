package net.cyperia.challenges.plugin.challenges.implementation.challenge.force;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.CompletableForceChallenge;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.server.scoreboard.ChallengeBossBar.BossBarInstance;
import net.cyperia.challenges.plugin.utils.misc.Utils;
import net.cyperia.commons.common.config.Document;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class ForceMobChallenge extends CompletableForceChallenge {

  private EntityType entity;

  public ForceMobChallenge() {
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
      bossbar.setTitle(getChallengeMessageKey("bossbar-instruction"), entity, ChallengeAPI.formatTime(getSecondsLeftUntilNextActivation()));
    };
  }

  @Override
  protected void broadcastFailedMessage() {
    getChallengeMessageKey("fail").broadcast(Prefix.CHALLENGES, entity);
  }

  @Override
  protected void broadcastSuccessMessage(@NotNull Player player) {
    getChallengeMessageKey("success").broadcast(Prefix.CHALLENGES, player, entity);
  }

  @Override
  protected void chooseForcing() {
    List<EntityType> entities = new ArrayList<>(Arrays.asList(EntityType.values()));
    entities.removeIf(type -> !type.isSpawnable());
    entities.removeIf(type -> !type.isAlive());
    Utils.removeEnums(entities, "ENDER_DRAGON", "ILLUSIONER", "ARMOR_STAND", "ZOMBIE_HORSE", "SKELETON_HORSE", "SHULKER", "WITHER", "GIANT");

    entity = globalRandom.choose(entities);
  }

  @Override
  protected int getForcingTime() {
    return globalRandom.range(5 * 60, 8 * 60);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onKill(@NotNull EntityDeathEvent event) {
    LivingEntity entity = event.getEntity();
    if (entity.getType() != this.entity) return;
    Player killer = entity.getKiller();
    if (killer == null) return;
    completeForcing(killer);
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    super.loadGameState(document);
    if (document.contains("target")) {
      entity = document.getEnum("target", EntityType.class);
      setState(entity == null ? WAITING : COUNTDOWN);
    }
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    super.writeGameState(document);
    document.set("target", entity);
  }

}
