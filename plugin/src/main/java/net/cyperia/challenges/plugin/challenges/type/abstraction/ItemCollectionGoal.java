package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.helper.GoalLeaderboard;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.spigot.events.PlayerInventoryClickEvent;
import net.cyperia.challenges.plugin.spigot.events.PlayerPickupItemEvent;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public abstract class ItemCollectionGoal extends CollectionGoal {

  public ItemCollectionGoal(@NotNull Material... target) {
    super(target);
  }

  public ItemCollectionGoal(boolean enabledByDefault, @NotNull Material... target) {
    super(enabledByDefault, target);
  }

  protected void handleCollect(@NotNull Player player, @NotNull Material material) {
    collect(player, material, () -> {
      MessageKey.of("challenge.generic.item-collected").send(player, Prefix.CHALLENGES, material);
      SoundSample.PLING.play(player);
    });
  }

  @Override
  protected void onEnable() {
    scoreboard.setContent(GoalLeaderboard.scoreboardContent(this::getLeaderboard,
      _ -> Collections.singletonList(MessageKey.of("challenge.generic.items-to-collect").withArgs(target.length))));
    scoreboard.show();
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPickupItem(@NotNull PlayerPickupItemEvent event) {
    if (!shouldExecuteEffect()) return;
    Material material = event.getItem().getItemStack().getType();
    Player player = event.getPlayer();
    handleCollect(player, material);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerInventoryClick(@NotNull PlayerInventoryClickEvent event) {
    if (!shouldExecuteEffect()) return;
    if (event.getClickedInventory() == null) return;
    if (event.getClickedInventory().getHolder() != event.getPlayer()) return;
    if (event.getCurrentItem() == null) return;
    Player player = event.getPlayer();
    Material material = event.getCurrentItem().getType();
    handleCollect(player, material);
  }

  // An item shift-clicked out of a chest or smithing table and equipped by right-click never passes
  // a pickup or a click in the player's own inventory, so the inventory is scanned as well.
  @ScheduledTask(ticks = 20, async = false)
  public void scanInventories() {
    for (Player player : ChallengeAPI.getIngamePlayers()) {
      if (!shouldExecuteEffect()) return;
      for (ItemStack item : player.getInventory().getContents()) {
        if (item == null) continue;
        handleCollect(player, item.getType());
      }
    }
  }

}
