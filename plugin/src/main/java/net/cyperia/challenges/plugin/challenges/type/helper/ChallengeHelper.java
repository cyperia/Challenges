package net.cyperia.challenges.plugin.challenges.type.helper;

import lombok.Getter;
import lombok.Setter;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.implementation.setting.ChallengePenaltySetting;
import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import net.cyperia.challenges.plugin.challenges.type.IGoal;
import net.cyperia.challenges.plugin.challenges.type.IModifier;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.challenges.type.abstraction.menu.MenuSetting.SubSetting;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.format.InteractionTooltips;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.*;

import java.util.Collection;
import java.util.List;

public final class ChallengeHelper {

  /** amount a shift click adds to / subtracts from a modifier value */
  public static final int SHIFT_STEP = 10;

  @Getter
  private static volatile boolean inInstantKill = false;

  @Getter
  @Setter
  private static ChallengePenalty penalty = ChallengePenalty.kill();

  private ChallengeHelper() {
  }

  public static void punish(@NotNull Player player) {
    runSync(() -> penalty.punish(player));
  }

  public static void punish(@NotNull Player player, int delay) {
    Bukkit.getScheduler().runTaskLater(Challenges.getInstance(), () -> punish(player), delay);
  }

  /** use {@link #punish(Player)} where applicable instead */
  @ApiStatus.Internal
  public static void kill(@NotNull Player player) {
    runSync(() -> {
      try {
        inInstantKill = true;
        player.setNoDamageTicks(0);
        player.damage(Integer.MAX_VALUE);

        // the server silently swallows damage while a player is invulnerable, most notably during the
        // window after a cross world teleport (extra world challenges) where only fall and void damage
        // get through. an insta kill has to land regardless, so force it when the damage did not apply
        if (!player.isDead() && player.getHealth() > 0) {
          player.setHealth(0);
        }
      } finally {
        inInstantKill = false;
      }
    });
  }

  /** use {@link #punish(Player, int)} where applicable instead */
  @ApiStatus.Internal
  public static void kill(@NotNull Player player, int delay) {
    Bukkit.getScheduler().runTaskLater(Challenges.getInstance(), () -> kill(player), delay);
  }

  @CheckReturnValue
  public static boolean runSyncIfConcurrent(Runnable task) {
    if (Bukkit.isPrimaryThread()) {
      return false;
    }
    Bukkit.getScheduler().runTask(Challenges.getInstance(), task);
    return true;
  }

  public static void runSync(@NotNull Runnable task) {
    if (Bukkit.isPrimaryThread()) {
      task.run();
      return;
    }
    Bukkit.getScheduler().runTask(Challenges.getInstance(), task);
  }

  public static void openPunishmentMenu(@NotNull Player player) {
    AbstractChallenge.getFirstInstance(ChallengePenaltySetting.class).getMenuGenerator().openMenu(player);
  }

  public static void updateItems(@NotNull IChallenge challenge) {
    challenge.getMenu().getMenuGenerator().updateElementDisplay(challenge);
    Challenges.getInstance().getMenuManager().invalidateMainMenu();
  }

  public static void handleModifierClick(@NotNull MenuClickInfo info, @NotNull IModifier modifier) {
    modifier.setValue(getClickResult(modifier, info.isRightClick(), info.isShiftClick()));
    modifier.playValueChangeTitle();
    SoundSample.CLICK.play(info.getPlayer());
  }

  /**
   * The value a click leads to. Overshooting a bound stops at it, clicking further wraps around to the
   * opposite one.
   *
   * @implSpec {@link #createModifierTooltips(IModifier)} describes the clicks by calling this, so both stay
   * in sync; do not reimplement the stepping anywhere else.
   */
  @Contract(pure = true)
  public static int getClickResult(@NotNull IModifier modifier, boolean rightClick, boolean shiftClick) {
    int value = modifier.getValue();
    int min = modifier.getMinValue(), max = modifier.getMaxValue();

    int step = shiftClick ? getShiftStep(value, min, rightClick) : 1;
    int newValue = rightClick ? value - step : value + step;

    if (newValue > max) return value < max ? max : min;
    if (newValue < min) return value > min ? min : max;
    return newValue;
  }

  /**
   * Shift steps land on round numbers: leaving the lower bound (or returning to it) is one short, so that
   * a modifier starting at 1 steps 1 → 10 → 20 instead of 1 → 11 → 21.
   */
  @Contract(pure = true)
  private static int getShiftStep(int value, int min, boolean rightClick) {
    boolean atRoundedBound = value == min || rightClick && value == SHIFT_STEP - min + 1;
    return atRoundedBound ? SHIFT_STEP - 1 : SHIFT_STEP;
  }

  private static final MenuClickType[] CLICK_TYPES = MenuClickType.values();

  /**
   * Describes every click of a modifier that leads somewhere new. On a modifier with few steps several clicks
   * end up at the same bound, and listing those again would read as if they differed. Which of them survives
   * follows the {@link MenuClickType} order, so an unmodified click is preferred over a shift click.
   */
  @NotNull
  @Contract(pure = true)
  public static InteractionTooltips createModifierTooltips(@NotNull IModifier modifier) {
    ValueUnit unit = modifier.getValueUnit();
    int value = modifier.getValue();
    int[] targets = new int[CLICK_TYPES.length];

    InteractionTooltips tooltips = new InteractionTooltips();
    for (int index = 0; index < CLICK_TYPES.length; index++) {
      MenuClickType click = CLICK_TYPES[index];
      targets[index] = getClickResult(modifier, click.isRightClick(), click.isShiftClick());
      if (isAlreadyReachable(targets, index)) continue;
      tooltips.on(click, describeClick(unit, value, targets[index], !click.isRightClick()));
    }
    return tooltips;
  }

  @Contract(pure = true)
  private static boolean isAlreadyReachable(int[] targets, int index) {
    for (int earlier = 0; earlier < index; earlier++) {
      if (targets[earlier] == targets[index]) return true;
    }
    return false;
  }

  @NotNull
  @Contract(pure = true)
  private static LocalizableMessage describeClick(@NotNull ValueUnit unit, int from, int to, boolean increase) {
    // the compact form, because the item name right above already states the value in full
    // a delta says nothing about a named or stepped value, and is misleading for a click that wraps around a bound
    if (unit.isNamed() || !unit.isLinear() || increase != (to > from)) return Interaction.SET.withArgs(unit.compact(to));
    return (increase ? Interaction.ADD : Interaction.SUBTRACT).withArgs(unit.compact(Math.abs(to - from)));
  }

  public static void breakBlock(@NotNull Block block, @Nullable ItemStack tool) {
    breakBlock(block, tool, null);
  }

  public static void breakBlock(@NotNull Block block, @Nullable ItemStack tool, @Nullable Inventory targetInventory) {
    if (!ChallengeAPI.getDropChance(block.getType())) return;
    boolean putIntoInventory = ChallengeAPI.getItemsDirectIntoInventory() && targetInventory != null;

    Collection<ItemStack> customDrops = ChallengeAPI.getCustomDrops(block, tool, null);
    Location location = block.getLocation().clone().add(0.5, 0, 0.5);
    if (customDrops != null) {
      if (putIntoInventory) {
        customDrops.forEach(drop -> InventoryUtils.dropOrGiveItem(targetInventory, location, drop));
      } else {
        customDrops.forEach(drop -> block.getWorld().dropItem(location, drop));
      }
      block.setType(Material.AIR);
      return;
    }

    if (putIntoInventory) {
      block.getDrops(tool).forEach(drop -> InventoryUtils.dropOrGiveItem(targetInventory, location, drop));
      block.setType(Material.AIR);
      return;
    }

    block.breakNaturally(tool);

  }

  public static void dropItem(@NotNull ItemStack itemStack, @NotNull Location dropLocation, @NotNull Inventory inventory) {
    boolean directIntoInventory = Challenges.getInstance().getBlockDropManager().isItemsDirectIntoInventory();

    if (directIntoInventory) {
      InventoryUtils.dropOrGiveItem(inventory, dropLocation, itemStack);
      return;
    }

    if (dropLocation.getWorld() == null) return;
    dropLocation.getWorld().dropItemNaturally(dropLocation, itemStack);

  }

  public static boolean ignoreDamager(@NotNull Entity damager) {
    Player damagerPlayer = getDamagerPlayer(damager);
    if (damagerPlayer == null) return false;
    return AbstractChallenge.ignorePlayer(damagerPlayer);
  }

  @Nullable
  public static Player getDamagerPlayer(@NotNull Entity damager) {
    return switch (damager) {
      case Player player -> player;
      case Projectile projectile when projectile.getShooter() instanceof Player shooter -> shooter;
      case TNTPrimed tnt when tnt.getSource() instanceof Player source -> source;
      default -> null;
    };
  }

  @NotNull
  public static List<Player> getIngamePlayers() {
    return Challenges.getInstance().getPlayerManager().getChallengePlayers();
  }

  public static void playChallengeToggleTitle(@NotNull AbstractChallenge challenge) {
    playChallengeToggleTitle(challenge, challenge.isEnabled());
  }

  public static void playChallengeToggleTitle(@NotNull AbstractChallenge challenge, boolean enabled) {
    Challenges.getInstance().getTitleManager().sendChallengeToggleTitle(challenge, enabled);
  }

  public static void playGoalToggleTitle(@NotNull IGoal goal) {
    playGoalToggleTitle(goal, goal.isEnabled());
  }

  public static void playGoalToggleTitle(@NotNull IGoal goal, boolean enabled) {
    Challenges.getInstance().getTitleManager().sendGoalToggleTitle(goal, enabled);
  }

  public static void playSubSettingToggleTitle(@NotNull AbstractChallenge challenge, @NotNull SubSetting setting) {
    playSubSettingToggleTitle(challenge, setting.getName(), setting.getAsBoolean());
  }

  public static void playSubSettingToggleTitle(@NotNull AbstractChallenge challenge, @NotNull Object name, boolean enabled) {
    Challenges.getInstance().getTitleManager().sendSubSettingToggleTitle(challenge, name, enabled);
  }

  public static <T extends AbstractChallenge & IModifier> void playChallengeValueTitle(@NotNull T modifier) {
    LocalizableMessage description = modifier.getSettingsDescription();
    playChallengeValueTitle(modifier, description != null ? description : modifier.getSettingsName()); // TODO or #getValue instead of#getSettingsName
  }

  public static void playChallengeValueTitle(@NotNull AbstractChallenge challenge, @NotNull Object value) {
    Challenges.getInstance().getTitleManager().sendChallengeValueTitle(challenge, value);
  }

  public static void playSubSettingValueTitle(@NotNull AbstractChallenge challenge, @NotNull SubSetting setting) {
    LocalizableMessage description = setting.getSettingsDescription();
    playSubSettingValueTitle(challenge, setting.getName(), description != null ? description : setting.getSettingsName());
  }

  public static void playSubSettingValueTitle(@NotNull AbstractChallenge challenge, @NotNull Object name, @NotNull Object value) {
    Challenges.getInstance().getTitleManager().sendSubSettingValueTitle(challenge, name, value);
  }

  /**
   * The unit of a modifier that switches between applying its effect to everyone and to the player who
   * triggered it.
   *
   * @param everyoneValue the value standing for everyone, the other one for the single player
   */
  @NotNull
  @Contract(pure = true)
  public static ValueUnit createAudienceUnit(int everyoneValue) {
    return ValueUnit.named(value -> MessageKey.of(value == everyoneValue ? "generic.everyone" : "generic.player"));
  }

  @NotNull
  @Contract(pure = true)
  public static LocalizableMessage getChallengeEnabledName() {
    return MessageKey.of("generic.enabled");
  }

  @NotNull
  @Contract(pure = true)
  public static LocalizableMessage getChallengeDisabledName() {
    return MessageKey.of("generic.disabled");
  }

  public static boolean finalDamageIsNull(@NotNull EntityDamageEvent event) {
    return getFinalDamage(event) == 0;
  }

  // Capped at what the entity had left: /kill deals Float.MAX_VALUE and kill() Integer.MAX_VALUE
  public static double getFinalDamage(@NotNull EntityDamageEvent event) {
    double damage = event.getFinalDamage() + event.getDamage(DamageModifier.ABSORPTION);
    if (!(event.getEntity() instanceof LivingEntity entity)) return damage;
    return Math.min(damage, entity.getHealth() + entity.getAbsorptionAmount());
  }

}
