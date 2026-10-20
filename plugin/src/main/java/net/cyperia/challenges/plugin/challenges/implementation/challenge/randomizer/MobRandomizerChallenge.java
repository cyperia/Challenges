package net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.abstraction.RandomizerSetting;
import net.cyperia.challenges.plugin.management.scheduler.policy.PlayerCountPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import net.cyperia.challenges.plugin.utils.misc.ExperimentalUtils;
import net.cyperia.challenges.plugin.utils.misc.ListBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MobRandomizerChallenge extends RandomizerSetting {

  // the type a mob had before it was randomized, so restoring never depends on the current randomization
  private static final NamespacedKey ORIGINAL_TYPE = new NamespacedKey(Challenges.getInstance(), "mob_randomizer_original");

  private final Map<EntityType, EntityType> entityRandomizer = new LinkedHashMap<>();

  private boolean inSpawn = false;

  @Override
  protected void onEnable() {
    super.onEnable();
    if (!shouldExecuteEffect()) return;
    forEachLoadedEntity(this::randomize);
  }

  @Override
  protected void onDisable() {
    super.onDisable();
    forEachLoadedEntity(this::restore);
  }

  @TimerTask(status = TimerStatus.RUNNING, async = false, playerPolicy = PlayerCountPolicy.ALWAYS)
  public void onResume() {
    forEachLoadedEntity(this::randomize);
  }

  @TimerTask(status = TimerStatus.PAUSED, async = false, playerPolicy = PlayerCountPolicy.ALWAYS)
  public void onPause() {
    forEachLoadedEntity(this::restore);
  }

  // mobs in unloaded chunks miss every toggle, so they catch up when their chunk loads
  @EventHandler(priority = EventPriority.NORMAL)
  public void onEntitiesLoad(@NotNull EntitiesLoadEvent event) {
    if (!ChallengeAPI.getGameWorlds().contains(event.getWorld())) return;
    Consumer<LivingEntity> action;
    if (shouldExecuteEffect()) action = this::randomize;
    else if (!isEnabled() || !ChallengeAPI.isStarted()) action = this::restore;
    else return;

    List<Entity> entities = event.getEntities();
    // replacing entities while the chunk is still loading them is not safe
    Bukkit.getScheduler().runTask(Challenges.getInstance(), () -> {
      for (Entity entity : entities) {
        if (entity instanceof LivingEntity living && living.isValid()) action.accept(living);
      }
    });
  }

  private void forEachLoadedEntity(@NotNull Consumer<LivingEntity> action) {
    for (World world : ChallengeAPI.getGameWorlds()) {
      world.getLivingEntities().forEach(action);
    }
  }

  private void randomize(@NotNull LivingEntity entity) {
    if (entity.getPersistentDataContainer().has(ORIGINAL_TYPE)) return;
    EntityType type = entityRandomizer.get(entity.getType());
    if (type == null) return;
    replace(entity, type, entity.getType());
  }

  private void restore(@NotNull LivingEntity entity) {
    String original = entity.getPersistentDataContainer().get(ORIGINAL_TYPE, PersistentDataType.STRING);
    if (original == null) return;
    NamespacedKey key = NamespacedKey.fromString(original);
    EntityType type = key == null ? null : Registry.ENTITY_TYPE.get(key);
    if (type == null) return;
    replace(entity, type, null);
  }

  private void replace(@NotNull Entity entity, @NotNull EntityType type, @Nullable EntityType original) {
    spawn(entity.getLocation(), type, original);
    entity.remove();
  }

  private void spawn(@NotNull Location location, @NotNull EntityType type, @Nullable EntityType original) {
    inSpawn = true;
    try {
      Entity spawned = location.getWorld().spawnEntity(location, type);
      if (original != null)
        spawned.getPersistentDataContainer().set(ORIGINAL_TYPE, PersistentDataType.STRING, original.getKey().toString());
    } finally {
      inSpawn = false;
    }
  }

  @Override
  protected void reloadRandomization() {
    entityRandomizer.clear();
    List<EntityType> entityTypes = getSpawnAbleEntities();
    List<EntityType> randomEntityTypes = new ArrayList<>(entityTypes);
    random.shuffle(randomEntityTypes);

    for (int i = 0; i < entityTypes.size(); i++) {
      entityRandomizer.put(entityTypes.get(i), randomEntityTypes.get(i));
    }
    // blaze rods are the one thing the end needs that no villager trades
    keepReachable(random, entityRandomizer, NATURAL_MOBS, List.of(EntityType.BLAZE));
  }

  @NotNull
  @Override
  public List<Pairing> getPairings() {
    if (!isEnabled()) return List.of();
    return pairingsOf(entityRandomizer);
  }

  public List<EntityType> getSpawnAbleEntities() {
    ListBuilder<EntityType> builder = new ListBuilder<>(ExperimentalUtils.getEntityTypes())
      .removeIf(type -> !type.isSpawnable())
      .removeIf(type -> !type.isAlive())
      .remove(EntityType.ARMOR_STAND)
      .remove(EntityType.ENDER_DRAGON)
      .remove(EntityType.WITHER)
      .remove(EntityType.GIANT)
      .remove(EntityType.ILLUSIONER);

    return builder.build();
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntitySpawn(@NotNull EntitySpawnEvent event) {
    if (!shouldExecuteEffect()) return;
    if (inSpawn) return;
    EntityType type = entityRandomizer.get(event.getEntityType());
    if (type == null) return;

    event.setCancelled(true);

    Location location = event.getLocation();
    if (location.getWorld() == null) return;
    if (!maySpawn(type, location.getWorld())) return;
    spawn(location, type, event.getEntityType());
  }

  private boolean maySpawn(@NotNull EntityType newType, @NotNull World world) {
    EntityCategory category = getEntityCategory(newType);
    int currentMobCount = getCurrentMobCount(category, world);
    int spawnLimit = getEntityCategory(newType).getSpawnLimit(world);
    return currentMobCount < spawnLimit;
  }

  private int getCurrentMobCount(@NotNull EntityCategory entityState, @NotNull World world) {

    int mobCount = 0;

    for (Entity entity : world.getLivingEntities()) {
      if (!(entity instanceof LivingEntity)) continue;
      EntityCategory entityTypeState = getEntityCategory(entity.getType());
      if (entityState == entityTypeState) {
        mobCount++;
      }

    }

    return mobCount;
  }

  private EntityCategory getEntityCategory(@NotNull EntityType type) {
    Class<? extends Entity> entity = type.getEntityClass();
    if (entity == null) return EntityCategory.OTHER;

    if (Animals.class.isAssignableFrom(entity)) {
      return EntityCategory.ANIMAL;
    }
    if (Drowned.class.isAssignableFrom(entity) || Guardian.class.isAssignableFrom(entity)) {
      return EntityCategory.WATER_AMBIENT;
    }
    if (Monster.class.isAssignableFrom(entity)) {
      return EntityCategory.HOSTILE;
    }
    if (WaterMob.class.isAssignableFrom(entity)) {
      return EntityCategory.WATER_ANIMAL;
    }

    return EntityCategory.AMBIENT;
  }

  public enum EntityCategory {

    HOSTILE,
    AMBIENT,
    ANIMAL,
    WATER_ANIMAL,
    WATER_AMBIENT,
    OTHER;

    private int getSpawnLimit(@NotNull World world) {
      return switch (this) {
        case AMBIENT -> world.getSpawnLimit(SpawnCategory.AMBIENT);
        case HOSTILE -> world.getSpawnLimit(SpawnCategory.MONSTER);
        case ANIMAL -> world.getSpawnLimit(SpawnCategory.ANIMAL);
        case WATER_AMBIENT -> world.getSpawnLimit(SpawnCategory.WATER_AMBIENT);
        case WATER_ANIMAL -> world.getSpawnLimit(SpawnCategory.WATER_ANIMAL);
        default -> 0;
      };
    }

  }

}
