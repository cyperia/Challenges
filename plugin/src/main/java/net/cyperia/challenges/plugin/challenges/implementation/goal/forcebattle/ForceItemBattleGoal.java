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
import java.util.Objects;
import java.util.stream.Collectors;

public class ForceItemBattleGoal extends ForceBattleDisplayGoal<ItemTarget> {

  private final BooleanSubSetting giveItemSetting;
  private final BooleanSubSetting itemVariantsSetting;

  public ForceItemBattleGoal() {

    giveItemSetting = registerSetting("give-item", new BooleanSubSetting(
      new ItemStack(Material.CHEST), getSubSettingSpace("give-item"),
      false
    ));
    // Only future targets change, the current ones stay
    itemVariantsSetting = registerItemVariantsSetting(false, () -> {
      if (isEnabled()) targetsPossibleToFind = getTargetsPossibleToFind();
    });
  }

  @Override
  protected ItemTarget[] getTargetsPossibleToFind() {
    return ItemTarget.getPossibleItems(itemVariantsSetting.getAsBoolean()).stream().map(ItemTarget::new).toArray(ItemTarget[]::new);
  }

  @Override
  public ItemTarget getTargetFromDocument(Document document, String path) {
    return ItemTarget.parse(document.getString(path));
  }

  @Override
  public List<ItemTarget> getListFromDocument(Document document, String path) {
    return document.getStringList(path).stream().map(ItemTarget::parse).filter(Objects::nonNull).collect(Collectors.toList());
  }

  @Override
  public void handleJokerUse(Player player) {
    if (shouldGiveItemOnSkip()) {
      InventoryUtils.dropOrGiveItem(player.getInventory(), player.getLocation(), currentTarget.get(player.getUniqueId()).getTarget().icon());
    }
    super.handleJokerUse(player);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onClick(PlayerInventoryClickEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getClickedInventory() == null) return;
    if (event.getCurrentItem() == null) return;
    ItemTarget target = currentTarget.get(event.getPlayer().getUniqueId());
    if (target != null && target.matches(event.getCurrentItem())) {
      handleTargetFound(event.getPlayer());
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPickup(PlayerPickupItemEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    ItemTarget target = currentTarget.get(event.getPlayer().getUniqueId());
    if (target != null && target.matches(event.getItem().getItemStack())) {
      handleTargetFound(event.getPlayer());
    }
  }

  private boolean shouldGiveItemOnSkip() {
    return giveItemSetting.getAsBoolean();
  }

}
