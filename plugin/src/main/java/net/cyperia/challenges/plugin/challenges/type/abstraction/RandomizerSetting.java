package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.commons.common.collection.IRandom;
import net.cyperia.commons.common.config.Document;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class RandomizerSetting extends Setting {

  // the mobs that spawn on their own in the overworld or the nether, so a player can always find them
  public static final List<EntityType> NATURAL_MOBS = List.of(
    EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK, EntityType.DROWNED, EntityType.SKELETON,
    EntityType.STRAY, EntityType.BOGGED, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.CREEPER,
    EntityType.ENDERMAN, EntityType.WITCH, EntityType.SLIME, EntityType.PHANTOM, EntityType.GUARDIAN,
    EntityType.PILLAGER, EntityType.COW, EntityType.PIG, EntityType.SHEEP, EntityType.CHICKEN, EntityType.HORSE,
    EntityType.DONKEY, EntityType.RABBIT, EntityType.WOLF, EntityType.FOX, EntityType.LLAMA, EntityType.POLAR_BEAR,
    EntityType.PANDA, EntityType.PARROT, EntityType.OCELOT, EntityType.CAT, EntityType.MOOSHROOM, EntityType.TURTLE,
    EntityType.GOAT, EntityType.FROG, EntityType.AXOLOTL, EntityType.SQUID, EntityType.GLOW_SQUID, EntityType.DOLPHIN,
    EntityType.COD, EntityType.SALMON, EntityType.TROPICAL_FISH, EntityType.PUFFERFISH, EntityType.BAT, EntityType.BEE,
    EntityType.ARMADILLO, EntityType.VILLAGER, EntityType.ZOMBIFIED_PIGLIN, EntityType.PIGLIN, EntityType.HOGLIN,
    EntityType.GHAST, EntityType.MAGMA_CUBE, EntityType.STRIDER, EntityType.WITHER_SKELETON, EntityType.BLAZE
  );

  protected IRandom random = IRandom.create();

  public RandomizerSetting() {
  }

  public RandomizerSetting(boolean enabledByDefault) {
    super(enabledByDefault);
  }

  protected abstract void reloadRandomization();

  // what each randomized thing currently turns into, empty while the randomizer is disabled
  @NotNull
  public abstract List<Pairing> getPairings();

  @NotNull
  protected static List<Pairing> pairingsOf(@NotNull Map<? extends Enum<?>, ? extends Enum<?>> randomization) {
    return randomization.entrySet().stream().map(entry -> new Pairing(entry.getKey(), List.of(entry.getValue()))).toList();
  }

  @NotNull
  public LocalizableMessage describe(@NotNull Pairing pairing) {
    Object to = pairing.to().size() == 1 ? pairing.to().get(0) : LocalizableMessage.joinList(pairing.to());
    return getChallengeMessageKey("pairing").withArgs(to);
  }

  @NotNull
  public MessageKey getMenuName() {
    return getChallengeMessageKey("menu");
  }

  // moves each required value onto a random reachable key, so the shuffle can never hide it behind a key nobody finds
  protected static <K, V> void keepReachable(@NotNull IRandom random, @NotNull Map<K, V> randomization, @NotNull List<K> reachableKeys, @NotNull Collection<V> required) {
    for (V value : required) {
      K holder = randomization.entrySet().stream()
        .filter(entry -> value.equals(entry.getValue()))
        .map(Map.Entry::getKey)
        .findFirst().orElse(null);
      if (holder == null || reachableKeys.contains(holder)) continue;

      List<K> free = reachableKeys.stream()
        .filter(key -> randomization.containsKey(key) && !required.contains(randomization.get(key)))
        .toList();
      if (free.isEmpty()) continue;

      randomization.put(holder, randomization.put(random.choose(free), value));
    }
  }

  protected int getMatches(int pairs, int matchesRemaining) {
    if ((pairs * 3) <= matchesRemaining - 3) return 3;
    if ((pairs * 2) <= matchesRemaining - 2) return 2;
    return 1;
  }

  // Rewinds the random to its seed first, so toggling the setting rebuilds the same randomization
  private void rebuildRandomization() {
    random.setSeed(random.getSeed());
    reloadRandomization();
  }

  @Override
  protected void onEnable() {
    rebuildRandomization();
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    super.loadGameState(document);
    if (!document.contains("seed")) {
      random = IRandom.create();
    } else {
      long seed = document.getLong("seed");
      if (seed == random.getSeed()) return;
      random = IRandom.create(seed);
    }

    if (!isEnabled()) return;
    rebuildRandomization();
  }

  public record Pairing(@NotNull Enum<?> from, @NotNull List<? extends Enum<?>> to) {
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    super.writeGameState(document);
    document.set("seed", random.getSeed());
  }

}
