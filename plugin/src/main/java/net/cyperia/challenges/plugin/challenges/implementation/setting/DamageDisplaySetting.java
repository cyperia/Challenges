package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.ArgumentFormat;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;

public class DamageDisplaySetting extends Setting {

  public DamageDisplaySetting() {
    super(true);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onDamage(@NotNull EntityDamageEvent event) {
    if (ChallengeAPI.isPaused() || event.getCause() == DamageCause.CUSTOM || !isEnabled())
      return;
    if (!(event.getEntity() instanceof Player)) return;
    if (ChallengeHelper.finalDamageIsNull(event)) return;

    LocalizableMessage damageDisplay = ArgumentFormat.HEARTS_LIMITED.apply(event.getFinalDamage());
    LocalizableMessage causeDisplay = ArgumentFormat.DAMAGE_CAUSE.apply(event);
    getChallengeMessageKey("message").broadcast(Prefix.DAMAGE, event.getEntity(), damageDisplay, causeDisplay);
  }
}
