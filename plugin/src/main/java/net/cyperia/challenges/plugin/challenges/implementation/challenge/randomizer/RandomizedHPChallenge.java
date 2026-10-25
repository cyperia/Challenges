package net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer;

import org.bukkit.attribute.Attribute;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.function.Consumer;

public class RandomizedHPChallenge extends SettingModifier {

  private static final NamespacedKey ORIGINAL_HEALTH = new NamespacedKey(Challenges.getInstance(), "randomized_hp_original");

  private final Random random = new Random();

  public RandomizedHPChallenge() {
    super(5);
    randomizeExistingEntityHealth();
    setValueUnit(ValueUnit.MAX_HEALTH.scaled(100));
  }

  @Override
  protected void onDisable() {
    resetExistingEntityHealth();
  }

  @Override
  public void onEnable() {
    randomizeExistingEntityHealth();
  }

  @Override
  protected void onValueChange() {
    randomizeExistingEntityHealth();
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onSpawn(@NotNull EntitySpawnEvent event) {
    if (!shouldExecuteEffect()) return;
    if (!(event.getEntity() instanceof LivingEntity entity)) return;
    randomizeEntityHealth(entity);
  }

  private void randomizeEntityHealth(@NotNull LivingEntity entity) {
    if (entity instanceof Player) return;

    AttributeInstance attribute = entity.getAttribute(Attribute.MAX_HEALTH);
    if (attribute == null) return;

    // The health the mob had before, kept on the mob so it can be restored even when it is far from every player
    PersistentDataContainer data = entity.getPersistentDataContainer();
    if (!data.has(ORIGINAL_HEALTH)) data.set(ORIGINAL_HEALTH, PersistentDataType.DOUBLE, attribute.getBaseValue());

    int health = random.nextInt(getValue() * 100) + 1;
    attribute.setBaseValue(health);
    entity.setHealth(health);
  }

  private void restoreEntityHealth(@NotNull LivingEntity entity) {
    Double original = entity.getPersistentDataContainer().get(ORIGINAL_HEALTH, PersistentDataType.DOUBLE);
    if (original == null) return;
    entity.getPersistentDataContainer().remove(ORIGINAL_HEALTH);

    AttributeInstance attribute = entity.getAttribute(Attribute.MAX_HEALTH);
    if (attribute == null) return;
    attribute.setBaseValue(original);
    entity.setHealth(original);
  }

  // Mobs in chunks nobody was near when the challenge ended get their health back as the chunk loads
  @EventHandler
  public void onEntitiesLoad(@NotNull EntitiesLoadEvent event) {
    if (isEnabled() || !ChallengeAPI.getGameWorlds().contains(event.getWorld())) return;
    for (Entity entity : event.getEntities()) {
      if (entity instanceof LivingEntity living) Regions.nextTick(plugin, living, () -> restoreEntityHealth(living));
    }
  }

  // A disabled challenge leaves no randomized health behind, which also cleans up mobs saved by an earlier run
  private void randomizeExistingEntityHealth() {
    forEachMob(entity -> {
      if (isEnabled()) randomizeEntityHealth(entity);
      else restoreEntityHealth(entity);
    });
  }

  private void resetExistingEntityHealth() {
    forEachMob(this::restoreEntityHealth);
  }

  private void forEachMob(@NotNull Consumer<LivingEntity> action) {
    for (World world : ChallengeAPI.getGameWorlds()) {
      Regions.forEachRegion(plugin, world, () -> world.getLivingEntities().forEach(action));
    }
  }

  @NotNull
  @Override
  public ItemStack getSettingsItemPreset() {
    return DefaultItems.createEnabledValuePreset(getValue() * 10);
  }

}
