package net.cyperia.challenges.plugin.utils.bukkit.container;

import lombok.Data;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@Data
public final class PlayerData {

  private final GameMode gamemode;
  private final Location location;
  private final ItemStack[] inventory;
  private final ItemStack[] armor;
  private final Collection<PotionEffect> effects;

  private final double health;
  private final int food;
  private final float saturation;
  private final int heldItemSlot;
  private final boolean allowedFlight;
  private final boolean flying;

  public PlayerData(@NotNull Player player) {
    this(
      player.getGameMode(),
      player.getLocation(),
      player.getInventory().getContents(),
      player.getInventory().getArmorContents(),
      player.getActivePotionEffects(),
      player.getHealth(),
      player.getFoodLevel(),
      player.getSaturation(),
      player.getInventory().getHeldItemSlot(),
      player.getAllowFlight(),
      player.isFlying()
    );
  }

  public PlayerData(
    @NotNull GameMode gamemode,
    @NotNull Location location,
    @NotNull ItemStack[] inventory,
    @NotNull ItemStack[] armor,
    @NotNull Collection<PotionEffect> effects,
    double health,
    int food,
    float saturation,
    int heldItemSlot,
    boolean allowedFlight,
    boolean flying
  ) {
    this.gamemode = gamemode;
    this.location = location;
    this.inventory = inventory;
    this.armor = armor;
    this.effects = effects;
    this.health = health;
    this.food = food;
    this.saturation = saturation;
    this.heldItemSlot = heldItemSlot;
    this.allowedFlight = allowedFlight;
    this.flying = allowedFlight && flying;
  }

  public void apply(@NotNull Player player) {
    for (PotionEffect effect : player.getActivePotionEffects()) {
      player.removePotionEffect(effect.getType());
    }
    player.setGameMode(gamemode);
    player.teleport(location);
    player.setFallDistance(0); // a teleport keeps it, so a fall in the extra world would land here
    player.setHealth(health);
    player.setFoodLevel(food);
    player.setSaturation(saturation);
    player.getInventory().setContents(inventory);
    player.getInventory().setArmorContents(armor);
    player.getInventory().setHeldItemSlot(heldItemSlot);
    player.addPotionEffects(effects);
    player.setAllowFlight(allowedFlight);
    player.setFlying(flying);
  }

}
