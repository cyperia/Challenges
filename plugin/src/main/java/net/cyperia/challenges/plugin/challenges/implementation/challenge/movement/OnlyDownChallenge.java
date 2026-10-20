package net.cyperia.challenges.plugin.challenges.implementation.challenge.movement;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class OnlyDownChallenge extends Setting {

  public OnlyDownChallenge() {
    setUsesGlobalPunish(true);
  }

  @EventHandler
  public void onPlayerMove(@NotNull PlayerMoveEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getTo() == null) return;
    if (event.getTo().getBlockY() <= event.getFrom().getBlockY()) return;
    getChallengeMessageKey("failed").broadcast(Prefix.CHALLENGES, event.getPlayer());
    ChallengeHelper.punish(event.getPlayer());
  }

}
