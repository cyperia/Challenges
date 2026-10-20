package net.cyperia.challenges.plugin.challenges.implementation.challenge.inventory;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.spigot.events.PlayerInventoryClickEvent;
import net.cyperia.challenges.plugin.spigot.events.PlayerPickupItemEvent;
import net.cyperia.commons.common.collection.pair.Triple;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoDupedItemsChallenge extends Setting {

  public NoDupedItemsChallenge() {
    setUsesGlobalPunish(true);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onInventoryClick(@NotNull PlayerInventoryClickEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    checkInventories();
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntityPickUpItem(@NotNull PlayerPickupItemEvent event) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if (!shouldExecuteEffect(event.getPlayer())) return;
      checkInventories();
    }, 1);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onInteract(@NotNull PlayerInteractEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getClickedBlock() == null) return;
    checkInventories();
  }

  private void checkInventories() {
    Map<Player, List<Material>> blackList = new HashMap<>();

    for (Player player : ChallengeHelper.getIngamePlayers()) {
      Triple<Player, Player, Material> result = checkInventory(player, blackList);
      if (result != null) {
        getChallengeMessageKey("failed").broadcast(
          Prefix.CHALLENGES,
          result.getFirst(),
          result.getSecond(),
          result.getThird()
        );
        ChallengeHelper.punish(result.getFirst());
        ChallengeHelper.punish(result.getSecond());
      }

    }

  }

  private Triple<Player, Player, Material> checkInventory(@NotNull Player player, Map<Player, List<Material>> blacklist) {
    List<Material> localBlacklist = new ArrayList<>();
    List<Material> playerBlacklist = blacklist.getOrDefault(player, new ArrayList<>());
    blacklist.put(player, playerBlacklist);

    for (ItemStack item : player.getInventory().getContents()) {
      if (item == null) continue;
      if (localBlacklist.contains(item.getType())) continue;
      localBlacklist.add(item.getType());

      for (Map.Entry<Player, List<Material>> entry : blacklist.entrySet()) {
        List<Material> currentBlacklist = entry.getValue();
        if (currentBlacklist.contains(item.getType())) {
          return new Triple<>(player, entry.getKey(), item.getType());
        }

      }
      playerBlacklist.add(item.getType());
    }

    return null;
  }

}
