package net.cyperia.challenges.plugin.challenges.implementation.challenge.inventory;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.spigot.events.PlayerInventoryClickEvent;
import net.cyperia.challenges.plugin.spigot.events.PlayerPickupItemEvent;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import net.cyperia.commons.common.collection.pair.Triple;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NoDupedItemsChallenge extends Setting {

  public NoDupedItemsChallenge() {
    setUsesGlobalPunish(true);
  }

  // What each player holds, kept by that player's own thread: an inventory can only be read there
  private final Map<UUID, Set<Material>> held = new ConcurrentHashMap<>();

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onInventoryClick(@NotNull PlayerInventoryClickEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    refresh(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntityPickUpItem(@NotNull PlayerPickupItemEvent event) {
    Regions.later(plugin, event.getPlayer(), () -> {
      if (!shouldExecuteEffect(event.getPlayer())) return;
      refresh(event.getPlayer());
    }, 1);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onInteract(@NotNull PlayerInteractEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getClickedBlock() == null) return;
    refresh(event.getPlayer());
  }

  @EventHandler
  public void onQuit(@NotNull PlayerQuitEvent event) {
    held.remove(event.getPlayer().getUniqueId());
  }

  // Runs on the player's thread
  private void refresh(@NotNull Player player) {
    Set<Material> types = EnumSet.noneOf(Material.class);
    for (ItemStack item : player.getInventory().getContents()) {
      if (item != null) types.add(item.getType());
    }
    held.put(player.getUniqueId(), types);
    Regions.global(plugin, this::checkInventories);
  }

  private void checkInventories() {
    List<Player> checked = new ArrayList<>();

    for (Player player : ChallengeHelper.getIngamePlayers()) {
      Triple<Player, Player, Material> result = findDuplicate(player, checked);
      checked.add(player);
      if (result != null) {
        getChallengeMessageKey("failed").broadcast(
          Prefix.CHALLENGES,
          result.getFirst(),
          result.getSecond(),
          result.getThird()
        );
        // Both are rebuilt from their next inventory change, so a punished player is not reported again
        held.remove(result.getFirst().getUniqueId());
        held.remove(result.getSecond().getUniqueId());
        ChallengeHelper.punish(result.getFirst());
        ChallengeHelper.punish(result.getSecond());
      }
    }
  }

  @Nullable
  private Triple<Player, Player, Material> findDuplicate(@NotNull Player player, @NotNull List<Player> others) {
    Set<Material> types = held.get(player.getUniqueId());
    if (types == null) return null;

    for (Material type : types) {
      for (Player other : others) {
        Set<Material> otherTypes = held.get(other.getUniqueId());
        if (otherTypes != null && otherTypes.contains(type)) return new Triple<>(player, other, type);
      }
    }
    return null;
  }

}
