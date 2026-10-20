package net.cyperia.challenges.plugin.challenges.implementation.setting;

import org.bukkit.potion.PotionEffectType;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class SoupSetting extends Setting {

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    if (!shouldExecuteEffect()) return;
    if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
      return;
    if (event.getItem() == null) return;
    if (event.getItem().getType() != Material.MUSHROOM_STEW) return;

    Player player = event.getPlayer();
    if (player.getGameMode() == GameMode.CREATIVE) return;
    if (player.getHealth() == player.getMaxHealth()) return;

    player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 1));
    player.getInventory().setItemInMainHand(new ItemStack(Material.BOWL));
    player.updateInventory();
    SoundSample.EAT.play(player);

  }

}
