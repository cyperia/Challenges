package net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer;

import lombok.RequiredArgsConstructor;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class RandomEventChallenge extends TimedChallenge {

  private final Event[] events;

  public RandomEventChallenge() {
    super(1, 10, 3, false);
    events = new Event[]{
      new SpeedEvent(),
      new SpawnEntitiesEvent(),
      new HoleEvent(),
      new FlyEvent(),
      new CobWebEvent(),
      new ReplaceOresEvent(),
      new SicknessEvent()
    };
    setValueUnit(ValueUnit.INTERVAL_MINUTES.jitter(30));
  }

  @Override
  protected void onTimeActivation() {
    restartTimer();
    Event event = globalRandom.choose(events);
    Logger.debug("Running random event {}", event.getClass().getSimpleName());
    event.getActivationMessage().broadcast(Prefix.CHALLENGES);
    broadcastFiltered(event::run);
  }

  public interface Event {

    @NotNull
    MessageKey getActivationMessage();

    void run(@NotNull Player player);

  }

  @RequiredArgsConstructor
  public abstract class AbstractEvent implements Event {

    protected final String eventKey;

    @NotNull
    @Override
    public MessageKey getActivationMessage() {
      return getChallengeMessageKey(eventKey);
    }
  }

  public class SpeedEvent extends AbstractEvent {

    public SpeedEvent() {
      super("speed");
    }

    @Override
    public void run(@NotNull Player player) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 99));
    }

  }

  public class HoleEvent extends AbstractEvent {

    public HoleEvent() {
      super("hole");
    }

    @Override
    public void run(@NotNull Player player) {
      Location location = player.getLocation().getBlock().getLocation();
      for (int x = -1; x <= 1; x++) {
        for (int z = -1; z <= 1; z++) {
          for (int y = 1; y >= -8; y--) {
            location.clone().add(x, y, z).getBlock().setType(Material.AIR, true);
          }
        }
      }
    }

  }

  public class FlyEvent extends AbstractEvent {

    public FlyEvent() {
      super("fly");
    }

    @Override
    public void run(@NotNull Player player) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 3 * 20, 5));
    }

  }

  public class ReplaceOresEvent extends AbstractEvent {

    public ReplaceOresEvent() {
      super("ores");
    }

    @Override
    public void run(@NotNull Player player) {
      Location location = player.getLocation();
      Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        for (int x = -100; x <= 100; x++) {
          for (int z = -100; z <= 100; z++) {
            int finalZ = z;
            int finalX = x;
            Bukkit.getScheduler().runTask(plugin, () -> {
              if (location.getWorld() == null) return;
              for (int y = location.getWorld().getMinHeight(); y < 80; y++) {
                Location current = location.clone().add(finalX, 0, finalZ);
                current.setY(y);
                Block block = current.getBlock();
                if (block.getType().name().contains("ORE")) {
                  Environment environment = location.getWorld().getEnvironment();
                  block.setType(environment == Environment.NETHER ? Material.NETHERRACK : environment == Environment.THE_END ? Material.END_STONE : Material.STONE, true);
                }
              }
            });
          }
        }
      });
    }

  }

  public class SicknessEvent extends AbstractEvent {

    public SicknessEvent() {
      super("sickness");
    }

    @Override
    public void run(@NotNull Player player) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 7 * 20, 0));
      player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 3 * 20, 1));
      player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 5 * 20, 1));
    }
  }

  public class SpawnEntitiesEvent extends AbstractEvent {

    public SpawnEntitiesEvent() {
      super("entities");
    }

    @Override
    public void run(@NotNull Player player) {
      EntityType type = globalRandom.choose(EntityType.PIG, EntityType.CHICKEN, EntityType.CAT, EntityType.SILVERFISH, EntityType.WOLF);

      for (int i = 0; i < globalRandom.nextInt(5) + 5; i++) {
        Location randomLocation = player.getLocation().add(globalRandom.nextInt(10) - 5, -10, globalRandom.nextInt(10 - 5));
        if (randomLocation.getWorld() == null) return;
        while (!randomLocation.getBlock().isPassable() && randomLocation.getBlockY() < randomLocation.getWorld().getMaxHeight())
          randomLocation.add(0, 1, 0);

        randomLocation.getWorld().spawnEntity(randomLocation, type);
      }
    }

  }

  public class CobWebEvent extends AbstractEvent {

    public CobWebEvent() {
      super("webs");
    }

    @Override
    public void run(@NotNull Player player) {
      for (int i = 0; i < 13; i++) {
        Location randomLocation = player.getLocation().add(globalRandom.nextInt(10) - 5, -20, globalRandom.nextInt(10 - 5));
        if (randomLocation.getWorld() == null) return;
        while (!randomLocation.getBlock().isPassable() && randomLocation.getBlockY() < randomLocation.getWorld().getMaxHeight())
          randomLocation.add(0, 1, 0);

        randomLocation.getBlock().setType(Material.COBWEB, false);
      }
    }

  }

}
