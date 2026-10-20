package net.cyperia.challenges.plugin.spigot.events;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class InventoryClickEventWrapper extends Event {

  private final InventoryClickEvent event;

  public InventoryClickEventWrapper(@NotNull InventoryClickEvent event) {
    this.event = event;
  }

  @Nullable
  public Inventory getClickedInventory() {
    return event.getClickedInventory();
  }

  @NotNull
  public Inventory getInventory() {
    return event.getInventory();
  }

  @NotNull
  public InventoryView getView() {
    return event.getView();
  }

  @NotNull
  public ClickType getClick() {
    return event.getClick();
  }

  @NotNull
  public HumanEntity getWhoClicked() {
    return event.getWhoClicked();
  }

  public int getSlot() {
    return event.getSlot();
  }

  public int getRawSlot() {
    return event.getRawSlot();
  }

  @NotNull
  public InventoryAction getAction() {
    return event.getAction();
  }

  @Nullable
  public ItemStack getCursor() {
    return event.getCursor();
  }

  public int getHotbarButton() {
    return event.getHotbarButton();
  }

  @NotNull
  public SlotType getSlotType() {
    return event.getSlotType();
  }

  @NotNull
  public Result getResult() {
    return event.getResult();
  }

  public void setResult(@NotNull Result result) {
    event.setResult(result);
  }

  @Nullable
  public ItemStack getCurrentItem() {
    return event.getCurrentItem();
  }

  public void setCurrentItem(@Nullable ItemStack item) {
    event.setCurrentItem(item);
  }

  @NotNull
  public List<HumanEntity> getViewers() {
    return event.getViewers();
  }

  @NotNull
  public InventoryClickEvent getEvent() {
    return event;
  }

  public boolean isCancelled() {
    return event.isCancelled();
  }

  public void setCancelled(boolean cancel) {
    event.setCancelled(cancel);
  }

  public boolean isRightClick() {
    return event.isRightClick();
  }

  public boolean isLeftClick() {
    return event.isLeftClick();
  }

  public boolean isShiftClick() {
    return event.isShiftClick();
  }

}
