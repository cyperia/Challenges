package net.cyperia.challenges.plugin.challenges.implementation.challenge.inventory;

import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.utils.misc.BlockUtils;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class MovementItemRemovingChallenge extends SettingModifier {

  public static final int BLOCK = 1;

  public MovementItemRemovingChallenge() {
    super(1, 2, 2);
    setValueUnit(namedValues("block", "chunk"));
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerMove(@NotNull PlayerMoveEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getTo() == null) return;

    if (getValue() == BLOCK) {
      if (BlockUtils.isSameBlockLocationIgnoreHeight(event.getFrom(), event.getTo())) return;
    } else {
      if (BlockUtils.isSameChunk(event.getFrom().getChunk(), event.getTo().getChunk()))
        return;
    }

    InventoryUtils.removeRandomItem(event.getPlayer().getInventory());
  }

}
