package net.cyperia.challenges.plugin.challenges.implementation.material;

import net.cyperia.challenges.catalog.Challenge;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.spigot.events.PlayerInventoryClickEvent;
import net.cyperia.challenges.plugin.spigot.events.PlayerPickupItemEvent;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class BlockMaterialSetting extends Setting {

  private final List<Material> materials;

  public BlockMaterialSetting(@NotNull Challenge catalogEntry, @NotNull Material... materials) {
    super(catalogEntry, true);
    this.materials = Arrays.asList(materials);
  }

  @NotNull
  @Override
  public LocalizableMessage getDescription() {
    return super.getDescription().withArgs(LocalizableMessage.joinList(2, materials));
  }

  private boolean isMaterialAllowed(Material material) {
    return !materials.contains(material);
  }

  @Override
  protected void onDisable() {
    broadcastFiltered(player -> dropMaterial(player.getLocation(), player.getInventory()));
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
    if (isEnabled()) return;
    if (ChallengeAPI.isWorldInUse()) return;
    if (ignorePlayer(event.getPlayer())) return;
    if (isMaterialAllowed(event.getMaterial())) {
      if (event.getClickedBlock() == null) return;
      if (isMaterialAllowed(event.getClickedBlock().getType())) return;
      event.setCancelled(true);
      return;
    }
    event.setCancelled(true);

    dropMaterial(event.getPlayer().getLocation(), event.getPlayer().getInventory());
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerInventoryClick(@NotNull PlayerInventoryClickEvent event) {
    if (isEnabled()) return;
    if (ignorePlayer(event.getPlayer())) return;
    if (ChallengeAPI.isWorldInUse()) return;
    if (event.getCurrentItem() == null) return;
    if (event.getClickedInventory() == null) return;
    if (event.getClickedInventory().getHolder() != event.getPlayer()) return;
    if (isMaterialAllowed(event.getCurrentItem().getType())) return;
    event.setCancelled(true);

    dropMaterial(event.getPlayer().getLocation(), event.getClickedInventory());
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerPickupItem(@NotNull PlayerPickupItemEvent event) {
    if (isEnabled()) return;
    if (ignorePlayer(event.getPlayer())) return;
    if (ChallengeAPI.isWorldInUse()) return;
    if (isMaterialAllowed(event.getItem().getItemStack().getType())) return;
    event.setCancelled(true);
  }

  public void dropMaterial(@NotNull Location location, @NotNull Inventory inventory) {
    for (int slot = 0; slot < inventory.getSize(); slot++) {
      ItemStack item = inventory.getItem(slot);
      if (item == null) continue;
      if (isMaterialAllowed(item.getType())) continue;
      InventoryUtils.dropItemByPlayer(location, item);
      inventory.setItem(slot, null);
    }
  }

}
