package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import net.cyperia.challenges.plugin.spigot.events.PlayerInventoryClickEvent;
import net.cyperia.challenges.plugin.spigot.events.PlayerPickupItemEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public abstract class FindItemGoal extends SettingGoal {

  private final Material searchedItem;

  public FindItemGoal(@NotNull Material searchedItem) {
    this.searchedItem = searchedItem;
  }

  @Override
  public void getWinnersOnEnd(@NotNull List<Player> winners) {

  }

  private void checkItem(ItemStack itemStack, @NotNull Player player) {
    if (itemStack == null) return;
    if (itemStack.getType() != searchedItem) return;
    ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED, () -> Collections.singletonList(player));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPickup(PlayerPickupItemEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    checkItem(event.getItem().getItemStack(), event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onInventoryClick(PlayerInventoryClickEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getClickedInventory() == null) return;
    if (event.getClickedInventory().getHolder() != event.getPlayer()) return;
    checkItem(event.getCurrentItem(), event.getPlayer());
  }

}
