package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ChecklistGoal;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import net.cyperia.commons.common.config.Document;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class KillMobsGoal extends SettingGoal implements ChecklistGoal {

  protected static List<EntityType> entitiesKilled;
  private static Map<EntityType, UUID> killers;

  protected final List<EntityType> entitiesToKill;

  public KillMobsGoal(@NotNull List<EntityType> entitiesKilled) {
    this.entitiesToKill = entitiesKilled;
    resetEntitiesToKill();
  }

  @NotNull
  protected MessageKey getBossbarMessage() {
    return getChallengeMessageKey("bossbar");
  }

  private void resetEntitiesToKill() {
    entitiesKilled = new LinkedList<>();
    killers = new EnumMap<>(EntityType.class);
  }

  @Override
  public void getWinnersOnEnd(@NotNull List<Player> winners) {

  }

  @Override
  protected void onEnable() {
    bossbar.setContent((bar, _) -> {
      float i = 1 - ((float) getEntitiesLeftToKill().size() / (float) entitiesToKill.size());
      bar.setProgress(i);
      bar.setColor(BossBar.Color.GREEN);
      bar.setTitle(getBossbarMessage(), getEntitiesKilled().size(), entitiesToKill.size());
    });
    bossbar.show();
  }

  @Override
  protected void onDisable() {
    bossbar.hide();
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onDeath(@NotNull EntityDeathEvent event) {
    if (!shouldExecuteEffect()) return;
    if (event.getEntityType() != EntityType.WITHER && event.getEntityType() != EntityType.ENDER_DRAGON && event.getEntityType() != EntityType.ELDER_GUARDIAN) {
      if (event.getEntity().getKiller() == null) return;
    }
    if (entitiesKilled.contains(event.getEntityType())) return;
    entitiesKilled.add(event.getEntityType());
    Player killer = event.getEntity().getKiller();
    if (killer != null) killers.put(event.getEntityType(), killer.getUniqueId());
    if (entitiesToKill.contains(event.getEntityType())) {
      MessageKey.of("challenge.generic.mob-killed").broadcast(Prefix.CHALLENGES, event.getEntityType(), getEntitiesKilled().size(), entitiesToKill.size());
      bossbar.update();
      if (!getEntitiesLeftToKill().isEmpty()) return;
      resetEntitiesToKill();
      ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED);
    }
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    super.writeGameState(document);

    document.set("entities", entitiesKilled);
    Document killerDocument = document.getDocument("killers");
    killers.forEach((type, uuid) -> killerDocument.set(type.name(), uuid.toString()));
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    super.loadGameState(document);

    entitiesKilled = document.getEnumList("entities", EntityType.class);
    killers = new EnumMap<>(EntityType.class);
    Document killerDocument = document.getDocument("killers");
    for (EntityType type : entitiesKilled) {
      UUID killer = killerDocument.getUUID(type.name());
      if (killer != null) killers.put(type, killer);
    }
  }

  public List<EntityType> getEntitiesKilled() {
    LinkedList<EntityType> entityTypes = new LinkedList<>(entitiesKilled);
    entityTypes.removeIf(type -> !entitiesToKill.contains(type));
    return entityTypes;
  }

  public List<EntityType> getEntitiesLeftToKill() {
    LinkedList<EntityType> entityTypes = new LinkedList<>(entitiesToKill);
    entityTypes.removeAll(entitiesKilled);
    return entityTypes;
  }

  @NotNull
  @Override
  public List<ChecklistEntry> getChecklist(@NotNull Player viewer) {
    return entitiesToKill.stream()
      .map(type -> ChecklistEntry.of(type, entitiesKilled.contains(type), Optional.ofNullable(killers.get(type)).stream().toList()))
      .toList();
  }

  @NotNull
  @Override
  public MessageKey getFinderMessage() {
    return MessageKey.of("menu.goal.killed-by");
  }

}
