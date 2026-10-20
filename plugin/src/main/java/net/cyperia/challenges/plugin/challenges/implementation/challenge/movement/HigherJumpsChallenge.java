package net.cyperia.challenges.plugin.challenges.implementation.challenge.movement;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.spigot.events.PlayerJumpEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class HigherJumpsChallenge extends Setting {

  @EventHandler(priority = EventPriority.NORMAL)
  public void onJump(@NotNull PlayerJumpEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;

    int jumps = getPlayerData(event.getPlayer()).getInt("jumps") + 1;
    getPlayerData(event.getPlayer()).set("jumps", jumps);

    float y = jumps / 7f;
    event.getPlayer().setVelocity(new Vector().setY(y));
  }

}
