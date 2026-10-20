package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.challenges.type.abstraction.CollectionGoal;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

public class CollectMostDeathsGoal extends CollectionGoal {

  public CollectMostDeathsGoal() {
    super(DamageCause.values());
  }

  @EventHandler
  public void onDeath(@NotNull PlayerDeathEvent event) {
    if (!shouldExecuteEffect()) return;

    EntityDamageEvent lastCause = event.getEntity().getLastDamageCause();
    if (lastCause == null) return;

    DamageCause cause = lastCause.getCause();
    if (cause == DamageCause.CUSTOM) return;

    collect(event.getEntity(), cause, () -> {
      getChallengeMessageKey("collected").send(event.getEntity(), Prefix.CHALLENGES, cause);
      SoundSample.PLING.play(event.getEntity());
    });
  }

  @NotNull
  @Override
  public MessageKey getFinderMessage() {
    return MessageKey.of("menu.goal.died-by");
  }

}
