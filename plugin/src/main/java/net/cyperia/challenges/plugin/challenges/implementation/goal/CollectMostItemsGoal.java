package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.abstraction.CollectionGoal;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.misc.ExperimentalUtils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.item.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CollectMostItemsGoal extends CollectionGoal {

  public CollectMostItemsGoal() {
    super(ExperimentalUtils.getMaterials());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPickUp(@NotNull EntityPickupItemEvent event) {
    if (!isEnabled()) return;
    if (!(event.getEntity() instanceof Player player)) return;

    ItemStack item = event.getItem().getItemStack();
    handleNewItem(item.getType(), player);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onClick(@NotNull InventoryClickEvent event) {
    if (!shouldExecuteEffect()) return;
    if (!(event.getWhoClicked() instanceof Player player)) return;

    ItemStack item = event.getCurrentItem();
    if (item == null) return;
    if (!ItemUtils.isObtainableInSurvival(item.getType())) return;

    handleNewItem(item.getType(), player);
  }

  protected void handleNewItem(@NotNull Material material, @NotNull Player player) {
    collect(player, material, () -> {
      MessageKey.of("challenge.generic.item-collected").send(player, Prefix.CHALLENGES, material);
      SoundSample.PLING.play(player);
    });
  }

}
