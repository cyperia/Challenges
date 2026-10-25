package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets;

import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.ExtremeForceBattleGoal;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.utils.item.CollectableItem;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ItemTarget extends ForceTarget<CollectableItem> {

  public ItemTarget(CollectableItem target) {
    super(target);
  }

  // Saves written before item variants hold the bare material name, which parse() still reads
  @Nullable
  public static ItemTarget parse(@Nullable String saved) {
    CollectableItem item = saved == null ? null : CollectableItem.parse(saved);
    return item == null ? null : new ItemTarget(item);
  }

  @Override
  public boolean check(Player player) {
    for (ItemStack item : player.getInventory().getContents()) {
      if (item != null && target.matches(item)) return true;
    }
    return false;
  }

  public boolean matches(@NotNull ItemStack item) {
    return target.matches(item);
  }

  public static List<CollectableItem> getPossibleItems(boolean variants) {
    return CollectableItem.obtainableInSurvival(variants);
  }

  @NotNull
  @Override
  public LocalizableMessage getDisplayName() {
    return target.displayName();
  }

  @Override
  public void updateDisplayStand(@NotNull ArmorStand armorStand) {
    Objects.requireNonNull(armorStand.getEquipment()).setHelmet(target.icon());
  }

  @Override
  public ExtremeForceBattleGoal.TargetType getType() {
    return ExtremeForceBattleGoal.TargetType.ITEM;
  }

  @Override
  public Object getTargetSaveObject() {
    return target.id();
  }

}
