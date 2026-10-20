package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Modifier;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RegenerationSetting extends Modifier {

  public static final int DISABLED = 1, ENABLED = 2, NOT_NATURAL = 3;

  public RegenerationSetting() {
    super(1, 3, ENABLED);
    setValueUnit(ValueUnit.named(value -> switch (value) {
      case ENABLED -> ChallengeHelper.getChallengeEnabledName();
      case DISABLED -> ChallengeHelper.getChallengeDisabledName();
      default -> getChallengeMessageKey("not_natural");
    }));
  }

  @NotNull
  @Override
  public ItemStack getSettingsItemPreset() {
    if (getValue() == ENABLED) {
      return DefaultItems.createEnabledPreset();
    }
    return new ItemStack(Material.ORANGE_DYE);
  }

  @Override
  public boolean isEnabled() {
    return getValue() != DISABLED;
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityRegainHealth(EntityRegainHealthEvent event) {
    if (!(event.getEntity() instanceof Player)) return;

    if (getValue() == DISABLED) {
      event.setAmount(0);
      event.setCancelled(true);
      return;
    }

    if (getValue() == NOT_NATURAL) {
      if (event.getRegainReason() == RegainReason.SATIATED || event.getRegainReason() == RegainReason.REGEN) {
        event.setAmount(0);
        event.setCancelled(true);
      }
    }

  }

}
