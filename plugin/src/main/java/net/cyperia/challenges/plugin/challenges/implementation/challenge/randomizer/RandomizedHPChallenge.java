package net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer;

import org.bukkit.attribute.Attribute;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RandomizedHPChallenge extends SettingModifier {

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

    if (!isEnabled()) {
      attribute.setBaseValue(attribute.getDefaultValue());
      entity.setHealth(attribute.getDefaultValue());
      return;
    }

    int health = random.nextInt(getValue() * 100) + 1;
    attribute.setBaseValue(health);
    entity.setHealth(health);
  }

  private void randomizeExistingEntityHealth() {
    for (World world : ChallengeAPI.getGameWorlds()) {
      for (LivingEntity entity : world.getLivingEntities()) {
        randomizeEntityHealth(entity);
      }
    }
  }

  private void resetExistingEntityHealth() {
    Map<EntityType, Double> entityDefaultHealth = new HashMap<>();
    for (World world : ChallengeAPI.getGameWorlds()) {
      for (LivingEntity entity : world.getLivingEntities()) {
        if (entity instanceof Player) continue;
        EntityType type = entity.getType();
        double health = entityDefaultHealth.getOrDefault(type, getDefaultHealth(type));
        entityDefaultHealth.put(type, health);

        AttributeInstance attribute = entity.getAttribute(Attribute.MAX_HEALTH);
        if (attribute == null) return;
        attribute.setBaseValue(health);
        entity.setHealth(health);
      }
    }
  }

  private double getDefaultHealth(@NotNull EntityType entityType) {
    World world = ChallengeAPI.getGameWorld(Environment.NORMAL);
    Entity entity = world.spawnEntity(new Location(world, 0, 0, 0), entityType);
    entity.remove();
    if (!(entity instanceof LivingEntity)) return 0;
    AttributeInstance attribute = ((LivingEntity) entity).getAttribute(Attribute.MAX_HEALTH);
    if (attribute == null) return 10;
    return attribute.getBaseValue();
  }

  @NotNull
  @Override
  public ItemStack getSettingsItemPreset() {
    return DefaultItems.createEnabledValuePreset(getValue() * 10);
  }

}
