package net.cyperia.challenges.plugin.challenges.implementation.challenge.miscellaneous;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class NoExpChallenge extends Setting {

  public NoExpChallenge() {
    setUsesGlobalPunish(true);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onExp(PlayerExpChangeEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    if (event.getAmount() <= 0) return;
    getChallengeMessageKey("picked-up").broadcast(Prefix.CHALLENGES, event.getPlayer());
    ChallengeHelper.punish(event.getPlayer());
  }

}
