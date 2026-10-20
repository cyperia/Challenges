package net.cyperia.challenges.plugin.challenges.implementation.challenge.effect;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.menu.MenuSetting;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.commons.common.collection.IRandom;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RandomPotionEffectChallenge extends MenuSetting {

  private int currentTime = 0;

  public RandomPotionEffectChallenge() {
    registerSetting("time", new NumberSubSetting(
      new ItemStack(Material.CLOCK), getSubSettingSpace("time"),
      1, 60, 30
    ).unit(ValueUnit.INTERVAL_SECONDS));
    registerSetting("length", new NumberSubSetting(
      new ItemStack(Material.ARROW), getSubSettingSpace("length"),
      1, 20, 10
    ).unit(ValueUnit.SECONDS));
    registerSetting("amplifier", new NumberSubSetting(
      new ItemStack(Material.STONE_SWORD), getSubSettingSpace("amplifier"),
      1, 8, 3
    ).unit(ValueUnit.STRENGTH));
  }

  @Nullable
  public static PotionEffectType getNewRandomEffect(@NotNull LivingEntity entity) {
    List<PotionEffectType> activeEffects = entity.getActivePotionEffects().stream().map(PotionEffect::getType).collect(Collectors.toList());

    ArrayList<PotionEffectType> possibleEffects = new ArrayList<>(Arrays.asList(PotionEffectType.values()));
    possibleEffects.removeAll(activeEffects);
    possibleEffects.remove(PotionEffectType.INSTANT_HEALTH);
    possibleEffects.remove(PotionEffectType.INSTANT_DAMAGE);
    return possibleEffects.get(IRandom.threadLocal().nextInt(possibleEffects.size()));
  }

  @ScheduledTask(ticks = 20, async = false)
  public void onSecond() {
    currentTime++;

    if (currentTime > getSetting("time").getAsInt()) {
      currentTime = 0;
      applyRandomEffect();
    }

  }

  private void applyRandomEffect() {
    broadcastFiltered(this::applyRandomEffect);
  }

  private void applyRandomEffect(@NotNull Player entity) {
    PotionEffectType effect = getNewRandomEffect(entity);
    if (effect == null) return;
    applyEffect(entity, effect);
  }

  private void applyEffect(@NotNull Player player, @NotNull PotionEffectType effectType) {
    PotionEffect potionEffect = new PotionEffect(effectType, (getSetting("length").getAsInt() + 1) * 20, getSetting("amplifier").getAsInt() - 1);
    player.addPotionEffect(potionEffect);
  }

}
