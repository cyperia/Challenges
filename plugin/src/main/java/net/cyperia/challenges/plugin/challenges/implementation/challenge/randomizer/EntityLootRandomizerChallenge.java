package net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer;

import net.cyperia.challenges.plugin.challenges.type.abstraction.RandomizerSetting;
import net.cyperia.challenges.plugin.utils.misc.ListBuilder;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EntityLootRandomizerChallenge extends RandomizerSetting {

  // which entity's loot each entity drops
  protected final Map<EntityType, EntityType> randomization = new LinkedHashMap<>();

  @Override
  protected void reloadRandomization() {
    randomization.clear();

    List<EntityType> from = new ArrayList<>(getLootableEntities());
    from.removeIf(type -> getLootTable(type) == null);
    List<EntityType> to = new ArrayList<>(from);
    random.shuffle(to);

    for (int i = 0; i < from.size(); i++) {
      randomization.put(from.get(i), to.get(i));
    }

    // blaze rods are the one thing the end needs that no villager trades
    keepReachable(random, randomization, NATURAL_MOBS, List.of(EntityType.BLAZE));
  }

  @NotNull
  @Override
  public List<Pairing> getPairings() {
    return pairingsOf(randomization);
  }

  public List<EntityType> getLootableEntities() {
    return new ListBuilder<>(EntityType.values())
      .removeIf(type -> !type.isSpawnable())
      .removeIf(type -> !type.isAlive())
      .remove(EntityType.ENDER_DRAGON)
      .remove(EntityType.GIANT)
      .remove(EntityType.ILLUSIONER)
      .remove(EntityType.ZOMBIE_HORSE)
      .build();
  }

  @Nullable
  private static LootTable getLootTable(@NotNull EntityType type) {
    try {
      return LootTables.valueOf(type.name()).getLootTable();
    } catch (IllegalArgumentException exception) {
      return null;
    }
  }

  @Override
  protected void onDisable() {
    randomization.clear();
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onEntityDeath(EntityDeathEvent event) {
    if (!isEnabled()) return;
    LivingEntity entity = event.getEntity();
    if (!getLootableEntities().contains(entity.getType())) return;
    event.getDrops().clear();
    EntityType source = randomization.get(entity.getType());
    if (source == null) return;

    LootContext.Builder builder = new LootContext.Builder(entity.getLocation())
      .lootedEntity(entity);

    if (entity.getKiller() == null) {
      builder.lootingModifier(0);
    } else {
      builder.killer(entity.getKiller());
    }

    Collection<ItemStack> newDrops = Objects.requireNonNull(getLootTable(source)).populateLoot(ThreadLocalRandom.current(), builder.build());
    event.getDrops().addAll(newDrops);
  }

}
