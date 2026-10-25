package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.ChecklistGoal;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import net.cyperia.challenges.plugin.utils.entity.KillableMob;
import net.cyperia.challenges.plugin.utils.misc.VariantIds;
import net.cyperia.commons.common.config.Document;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public abstract class KillMobsGoal extends SharedCollectionGoal implements ChecklistGoal {

  // Shared by every mob goal, by KillableMob id, so switching between them keeps the kills
  private static Set<String> killed;
  private static Map<String, UUID> killers;
  // Timer seconds per kill
  private static Map<String, Long> killedAt;

  private final List<EntityType> types;
  @Nullable
  private final BooleanSubSetting mobAttributesSetting;
  @Nullable
  private final BooleanSubSetting mobEquipmentSetting;
  private List<KillableMob> mobsToKill;
  private Map<EntityType, List<KillableMob>> mobsByType;
  private Map<String, KillableMob> mobsById;
  // The random order's target from before the pool changed, kept until it is killed
  @Nullable
  private String keptTarget;
  private long seed;
  private List<KillableMob> order;

  public KillMobsGoal(@NotNull List<EntityType> types) {
    this(types, false);
  }

  /**
   * @param variantsSetting whether the goal offers the settings to kill every variant of its mobs
   */
  public KillMobsGoal(@NotNull List<EntityType> types, boolean variantsSetting) {
    super(false);
    this.types = types;
    mobAttributesSetting = variantsSetting ? registerMobAttributesSetting(false, this::onMobVariantsChange) : null;
    mobEquipmentSetting = variantsSetting ? registerMobEquipmentSetting(false, this::onMobVariantsChange) : null;
    loadMobs();
    resetKills();
    reseed(ThreadLocalRandom.current().nextLong());
  }

  private void loadMobs() {
    mobsToKill = KillableMob.pool(types, mobAttributesSetting != null && mobAttributesSetting.getAsBoolean(),
      mobEquipmentSetting != null && mobEquipmentSetting.getAsBoolean());
    mobsByType = KillableMob.byType(mobsToKill);
    mobsById = new HashMap<>();
    mobsToKill.forEach(mob -> mobsById.put(mob.id(), mob));
  }

  // A baby zombie killed with variants on is a zombie all the same once they are off
  private void inheritBaseKills() {
    VariantIds.forEachCovered(killed, mobsById.keySet(), (id, target) -> {
      if (!killed.add(target)) return;
      if (killers.containsKey(id)) killers.put(target, killers.get(id));
      if (killedAt.containsKey(id)) killedAt.put(target, killedAt.get(id));
    });
  }

  // Kills are kept by id, so a variant killed before still counts once the setting brings it back
  private void onMobVariantsChange() {
    KillableMob previous = getCurrentTarget();
    loadMobs();
    inheritBaseKills();
    reseed(seed);
    keptTarget = previous == null ? null : previous.id();
    if (!isEnabled()) return;
    bossbar.update();
    updateRanking();
    if (killedCount() == mobsToKill.size() && ChallengeAPI.isStarted()) {
      resetKills();
      ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED);
    }
  }

  private void reseed(long seed) {
    this.seed = seed;
    order = seededOrder(mobsToKill, seed);
  }

  @Nullable
  private KillableMob getCurrentTarget() {
    if (keptTarget != null) {
      KillableMob kept = mobsById.get(keptTarget);
      if (kept != null && !isKilled(kept)) return kept;
      keptTarget = null;
    }
    for (KillableMob mob : order) {
      if (!isKilled(mob)) return mob;
    }
    return null;
  }

  private boolean isKilled(@NotNull KillableMob mob) {
    return killed.contains(mob.id());
  }

  private int killedCount() {
    return (int) mobsToKill.stream().filter(this::isKilled).count();
  }

  @NotNull
  protected MessageKey getBossbarMessage() {
    return getChallengeMessageKey("bossbar");
  }

  private static void resetKills() {
    killed = new LinkedHashSet<>();
    killers = new HashMap<>();
    killedAt = new HashMap<>();
  }

  @Override
  public void getWinnersOnEnd(@NotNull List<Player> winners) {

  }

  @Override
  protected void enableGoal() {
    bossbar.setContent((bar, _) -> {
      int killedCount = killedCount();
      KillableMob current = getCurrentTarget();
      boolean showsNextTarget = isRandomOrder() && current != null;
      styleBossbar(bar, showsNextTarget, (float) killedCount / mobsToKill.size());
      if (showsNextTarget) {
        bar.setTitle(getChallengeMessageKey("bossbar-next"), current.displayName(), killedCount + 1, mobsToKill.size());
      } else {
        bar.setTitle(getBossbarMessage(), killedCount, mobsToKill.size());
      }
    });
    bossbar.show();
  }

  @Override
  protected void disableGoal() {
    bossbar.hide();
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onDeath(@NotNull EntityDeathEvent event) {
    if (!shouldExecuteEffect()) return;
    LivingEntity entity = event.getEntity();
    EntityType type = entity.getType();
    Player killer = entity.getKiller();
    // Bosses often die to an explosion or a trident nobody threw last, they count all the same
    if (killer == null && type != EntityType.WITHER && type != EntityType.ENDER_DRAGON && type != EntityType.ELDER_GUARDIAN) return;

    KillableMob current = isRandomOrder() ? getCurrentTarget() : null;
    boolean found = false;
    for (KillableMob mob : KillableMob.killed(entity, mobsByType)) {
      if (isKilled(mob) || (isRandomOrder() && !mob.equals(current))) continue;
      killed.add(mob.id());
      if (killer != null) killers.put(mob.id(), killer.getUniqueId());
      killedAt.put(mob.id(), Challenges.getInstance().getChallengeTimer().getTime());
      announceFind(FindType.MOB, mob.displayName(), killer);
      found = true;
    }
    // A mob this goal does not ask for is kept as well, so it counts after switching to a goal that does
    String typeId = KillableMob.of(type).id();
    if (!mobsById.containsKey(typeId) && killed.add(typeId)) {
      if (killer != null) killers.put(typeId, killer.getUniqueId());
      killedAt.put(typeId, Challenges.getInstance().getChallengeTimer().getTime());
    }
    if (!found) return;
    bossbar.update();
    updateRanking();
    if (killedCount() < mobsToKill.size()) return;
    resetKills();
    ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED);
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    super.writeGameState(document);

    document.set("entities", List.copyOf(killed));
    document.set("seed", seed);
    Document killerDocument = document.getDocument("killers");
    killers.forEach((id, uuid) -> killerDocument.set(id, uuid.toString()));
    Document killedAtDocument = document.getDocument("killed-at");
    killedAt.forEach(killedAtDocument::set);
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    super.loadGameState(document);

    resetKills();
    document.getStringList("entities").stream().map(KillableMob::normalizeId).forEach(killed::add);
    reseed(document.contains("seed") ? document.getLong("seed") : ThreadLocalRandom.current().nextLong());
    keptTarget = null;
    Document killerDocument = document.getDocument("killers");
    for (String key : killerDocument.keys()) {
      UUID killer = killerDocument.getUUID(key);
      if (killer != null) killers.put(KillableMob.normalizeId(key), killer);
    }
    Document killedAtDocument = document.getDocument("killed-at");
    for (String key : killedAtDocument.keys()) {
      killedAt.put(KillableMob.normalizeId(key), killedAtDocument.getLong(key));
    }
    inheritBaseKills();
  }

  @Nullable
  @Override
  protected Object skipCurrentTarget() {
    KillableMob skipped = getCurrentTarget();
    if (skipped == null) return null;
    killed.add(skipped.id());
    killedAt.put(skipped.id(), Challenges.getInstance().getChallengeTimer().getTime());
    if (killedCount() == mobsToKill.size()) {
      resetKills();
      ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED);
    }
    return skipped.displayName();
  }

  @NotNull
  @Override
  protected Collection<UUID> getFinders() {
    List<UUID> finders = new ArrayList<>();
    for (KillableMob mob : mobsToKill) {
      UUID killer = killers.get(mob.id());
      if (killer != null) finders.add(killer);
    }
    return finders;
  }

  @NotNull
  @Override
  public List<ChecklistEntry> getChecklist(@NotNull Player viewer) {
    return mobsToKill.stream()
      .map(mob -> new ChecklistEntry(mob.displayName(), mob.icon(), isKilled(mob),
        Optional.ofNullable(killers.get(mob.id())).stream().toList(), killedAt.get(mob.id())))
      .toList();
  }

  @NotNull
  @Override
  public MessageKey getFinderMessage() {
    return MessageKey.of("menu.goal.killed-by");
  }

}
