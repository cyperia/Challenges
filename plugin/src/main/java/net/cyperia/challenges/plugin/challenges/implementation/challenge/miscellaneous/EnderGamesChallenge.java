package net.cyperia.challenges.plugin.challenges.implementation.challenge.miscellaneous;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class EnderGamesChallenge extends TimedChallenge {

  public EnderGamesChallenge() {
    super(1, 10, 5, false);
    setValueUnit(ValueUnit.INTERVAL_MINUTES.jitter(20));
  }

  @Override
  protected void onTimeActivation() {
    restartTimer();
    broadcastFiltered(this::teleportRandom);
    SoundSample.TELEPORT.broadcast();
  }

  private void teleportRandom(@NotNull Player player) {

    List<Entity> list = player.getWorld().getNearbyEntities(player.getLocation(), 200, 200, 200).stream()
      .filter(entity -> !(entity instanceof Player))
      .filter(entity -> entity instanceof LivingEntity)
      .collect(Collectors.toList());

    Entity targetEntity = list.get(globalRandom.nextInt(list.size()));

    Location playerLocation = player.getLocation().clone();
    player.teleportAsync(targetEntity.getLocation()).thenAccept(success -> {
      getChallengeMessageKey("teleport").send(player, Prefix.CHALLENGES, targetEntity.getType());
      Regions.entity(plugin, targetEntity, () -> targetEntity.teleportAsync(playerLocation));
    });

  }

}
