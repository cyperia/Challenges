package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle;

import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets.ItemTarget;
import net.cyperia.challenges.plugin.challenges.type.abstraction.ForceBattleDisplayGoal;
import net.cyperia.challenges.plugin.spigot.events.PlayerInventoryClickEvent;
import net.cyperia.challenges.plugin.spigot.events.PlayerPickupItemEvent;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import net.cyperia.commons.common.config.Document;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class ForceItemBattleGoal extends ForceBattleDisplayGoal<ItemTarget> {

  private final BooleanSubSetting giveItemSetting;

  public ForceItemBattleGoal() {

    giveItemSetting = registerSetting("give-item", new BooleanSubSetting(
      new ItemStack(Material.CHEST), getSubSettingSpace("give-item"),
      false
    ));
  }

  @Override
  protected ItemTarget[] getTargetsPossibleToFind() {
    List<Material> materials = ItemTarget.getPossibleItems();
    return materials.stream().map(ItemTarget::new).toArray(ItemTarget[]::new);
  }

  @Override
  public ItemTarget getTargetFromDocument(Document document, String path) {
    return new ItemTarget(document.getEnum(path, Material.class));
  }

  @Override
  public List<ItemTarget> getListFromDocument(Document document, String path) {
    return document.getEnumList(path, Material.class).stream().map(ItemTarget::new).collect(Collectors.toList());
  }

  @Override
  public void handleJokerUse(Player player) {
    if (shouldGiveItemOnSkip()) {
      InventoryUtils.dropOrGiveItem(player.getInventory(), player.getLocation(), currentTarget.get(player.getUniqueId()).getTarget());
    }
    super.handleJokerUse(player);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onClick(PlayerInventoryClickEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getClickedInventory() == null) return;
    if (event.getCurrentItem() == null) return;
    Material material = currentTarget.get(event.getPlayer().getUniqueId()).getTarget();
    if (material == null) return;
    if (material == event.getCurrentItem().getType()) {
      handleTargetFound(event.getPlayer());
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPickup(PlayerPickupItemEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    Material material = currentTarget.get(event.getPlayer().getUniqueId()).getTarget();
    if (material == null) return;
    if (material == event.getItem().getItemStack().getType()) {
      handleTargetFound(event.getPlayer());
    }
  }

  private boolean shouldGiveItemOnSkip() {
    return giveItemSetting.getAsBoolean();
  }

}
