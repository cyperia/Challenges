package net.cyperia.challenges.plugin.utils.entity;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Llama;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Salmon;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.ZombieNautilus;
import org.bukkit.entity.ZombieVillager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static net.cyperia.challenges.plugin.utils.entity.MobTrait.*;

/**
 * Every variant of a mob that can come up in survival, from the Minecraft wiki: coats, colours, biomes, professions,
 * babies, what a mob holds or wears, what it rides. A type on its own is not one of them, see {@link KillableMob#pool}.
 * A variant is about either the mob itself or its equipment and riders, never both: a mob matching both would hardly ever spawn.
 */
final class MobVariants {

  private static final List<String> BIOMES = List.of("plains", "desert", "jungle", "savanna", "snow", "swamp", "taiga");
  private static final List<String> PROFESSIONS = List.of("none", "armorer", "butcher", "cartographer", "cleric", "farmer",
    "fisherman", "fletcher", "leatherworker", "librarian", "mason", "nitwit", "shepherd", "toolsmith", "weaponsmith");
  private static final List<String> CLIMATES = List.of("temperate", "warm", "cold");
  private static final List<String> ARMOR = List.of("LEATHER", "COPPER", "IRON", "CHAINMAIL", "DIAMOND", "GOLDEN");
  private static final List<Material> ZOMBIE_WEAPONS = List.of(Material.IRON_SHOVEL, Material.IRON_SWORD, Material.IRON_SPEAR);
  private static final List<Material> FOX_ITEMS = List.of(Material.EMERALD, Material.RABBIT_FOOT, Material.RABBIT_HIDE,
    Material.EGG, Material.WHEAT, Material.LEATHER, Material.FEATHER);
  // The named tropical fish in the order of their vanilla names entity.minecraft.tropical_fish.predefined.<index>
  private static final List<FishLook> NAMED_FISH = List.of(
    new FishLook(TropicalFish.Pattern.STRIPEY, DyeColor.ORANGE, DyeColor.GRAY),
    new FishLook(TropicalFish.Pattern.FLOPPER, DyeColor.GRAY, DyeColor.GRAY),
    new FishLook(TropicalFish.Pattern.FLOPPER, DyeColor.GRAY, DyeColor.BLUE),
    new FishLook(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.GRAY),
    new FishLook(TropicalFish.Pattern.SUNSTREAK, DyeColor.BLUE, DyeColor.GRAY),
    new FishLook(TropicalFish.Pattern.KOB, DyeColor.ORANGE, DyeColor.WHITE),
    new FishLook(TropicalFish.Pattern.SPOTTY, DyeColor.PINK, DyeColor.LIGHT_BLUE),
    new FishLook(TropicalFish.Pattern.BLOCKFISH, DyeColor.PURPLE, DyeColor.YELLOW),
    new FishLook(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.RED),
    new FishLook(TropicalFish.Pattern.SPOTTY, DyeColor.WHITE, DyeColor.YELLOW),
    new FishLook(TropicalFish.Pattern.GLITTER, DyeColor.WHITE, DyeColor.GRAY),
    new FishLook(TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.ORANGE),
    new FishLook(TropicalFish.Pattern.DASHER, DyeColor.CYAN, DyeColor.PINK),
    new FishLook(TropicalFish.Pattern.BRINELY, DyeColor.LIME, DyeColor.LIGHT_BLUE),
    new FishLook(TropicalFish.Pattern.BETTY, DyeColor.RED, DyeColor.WHITE),
    new FishLook(TropicalFish.Pattern.SNOOPER, DyeColor.GRAY, DyeColor.RED),
    new FishLook(TropicalFish.Pattern.BLOCKFISH, DyeColor.RED, DyeColor.WHITE),
    new FishLook(TropicalFish.Pattern.FLOPPER, DyeColor.WHITE, DyeColor.YELLOW),
    new FishLook(TropicalFish.Pattern.KOB, DyeColor.RED, DyeColor.WHITE),
    new FishLook(TropicalFish.Pattern.SUNSTREAK, DyeColor.GRAY, DyeColor.WHITE),
    new FishLook(TropicalFish.Pattern.DASHER, DyeColor.CYAN, DyeColor.YELLOW),
    new FishLook(TropicalFish.Pattern.FLOPPER, DyeColor.YELLOW, DyeColor.YELLOW)
  );

  private record FishLook(@NotNull TropicalFish.Pattern pattern, @NotNull DyeColor body, @NotNull DyeColor patternColor) {

    boolean matches(@NotNull Entity entity) {
      return entity instanceof TropicalFish fish
        && fish.getPattern() == pattern && fish.getBodyColor() == body && fish.getPatternColor() == patternColor;
    }
  }

  // Types whose variants cover every mob of the type, so the type on its own would be found twice
  private static final Set<EntityType> COVERED = EnumSet.of(EntityType.SALMON, EntityType.SLIME, EntityType.MAGMA_CUBE,
    EntityType.SULFUR_CUBE, EntityType.COW, EntityType.PIG, EntityType.CHICKEN, EntityType.SHEEP, EntityType.MOOSHROOM,
    EntityType.WOLF, EntityType.CAT, EntityType.HORSE, EntityType.LLAMA, EntityType.TRADER_LLAMA, EntityType.PARROT,
    EntityType.RABBIT, EntityType.FOX, EntityType.PANDA, EntityType.AXOLOTL, EntityType.FROG, EntityType.COPPER_GOLEM,
    EntityType.VILLAGER, EntityType.ZOMBIE_VILLAGER, EntityType.TROPICAL_FISH, EntityType.ZOMBIE_NAUTILUS);

  private static Map<EntityType, List<KillableMob>> variants;

  private MobVariants() {
  }

  @NotNull
  static List<KillableMob> of(@NotNull EntityType type) {
    if (variants == null) variants = load();
    return variants.getOrDefault(type, List.of());
  }

  static boolean coversEveryMob(@NotNull EntityType type) {
    return COVERED.contains(type);
  }

  @NotNull
  static Map<EntityType, List<KillableMob>> load() {
    Map<EntityType, List<KillableMob>> mobs = new EnumMap<>(EntityType.class);

    // Villagers
    for (String profession : PROFESSIONS) {
      for (String biome : BIOMES) {
        add(mobs, EntityType.VILLAGER, ADULT, profession(profession), biome(biome));
        add(mobs, EntityType.ZOMBIE_VILLAGER, ADULT, profession(profession), biome(biome));
      }
    }
    for (String biome : BIOMES) {
      add(mobs, EntityType.VILLAGER, BABY, biome(biome));
      add(mobs, EntityType.ZOMBIE_VILLAGER, BABY, biome(biome));
    }
    add(mobs, EntityType.ZOMBIE_VILLAGER, riding(EntityType.CHICKEN));

    // Zombies
    for (EntityType zombie : List.of(EntityType.ZOMBIE, EntityType.HUSK)) {
      add(mobs, zombie, BABY);
      add(mobs, zombie, riding(EntityType.CHICKEN));
      add(mobs, zombie, RIGHT_HANDED);
      add(mobs, zombie, LEFT_HANDED);
      for (Material weapon : ZOMBIE_WEAPONS) add(mobs, zombie, holding(weapon));
      for (String armor : ARMOR) add(mobs, zombie, armor(armor));
    }
    add(mobs, EntityType.DROWNED, BABY);
    add(mobs, EntityType.DROWNED, RIGHT_HANDED);
    add(mobs, EntityType.DROWNED, LEFT_HANDED);
    add(mobs, EntityType.DROWNED, holding(Material.TRIDENT));
    add(mobs, EntityType.DROWNED, holding(Material.FISHING_ROD));
    add(mobs, EntityType.DROWNED, offHand(Material.NAUTILUS_SHELL));
    add(mobs, EntityType.ZOMBIFIED_PIGLIN, BABY);
    add(mobs, EntityType.ZOMBIFIED_PIGLIN, riding(EntityType.CHICKEN));
    add(mobs, EntityType.ZOMBIFIED_PIGLIN, riding(EntityType.STRIDER));
    add(mobs, EntityType.ZOMBIFIED_PIGLIN, holding(Material.GOLDEN_SWORD));
    add(mobs, EntityType.ZOMBIFIED_PIGLIN, holding(Material.GOLDEN_SPEAR));

    // Skeletons
    for (EntityType skeleton : List.of(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON, EntityType.BOGGED, EntityType.PARCHED)) {
      add(mobs, skeleton, RIGHT_HANDED);
      add(mobs, skeleton, LEFT_HANDED);
    }
    for (EntityType skeleton : List.of(EntityType.SKELETON, EntityType.STRAY)) {
      for (String armor : ARMOR) add(mobs, skeleton, armor(armor));
    }
    add(mobs, EntityType.SKELETON, riding(EntityType.SPIDER));
    add(mobs, EntityType.BOGGED, SHEARED, RIGHT_HANDED);
    add(mobs, EntityType.BOGGED, SHEARED, LEFT_HANDED);

    // Other monsters
    for (String effect : List.of("speed", "strength", "regeneration", "invisibility")) add(mobs, EntityType.SPIDER, effect(effect));
    add(mobs, EntityType.SPIDER, riddenBy(EntityType.SKELETON));
    add(mobs, EntityType.CREEPER, CHARGED);
    add(mobs, EntityType.EVOKER, RAID_CAPTAIN);
    add(mobs, EntityType.VINDICATOR, RAID_CAPTAIN);
    add(mobs, EntityType.PILLAGER, LEFT_HANDED);
    add(mobs, EntityType.PILLAGER, RAID_CAPTAIN);
    add(mobs, EntityType.PILLAGER, LEFT_HANDED, RAID_CAPTAIN);
    for (EntityType raider : List.of(EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.EVOKER)) {
      add(mobs, EntityType.RAVAGER, riddenBy(raider));
      add(mobs, EntityType.RAVAGER, riddenByCaptain(raider));
    }
    add(mobs, EntityType.PIGLIN, BABY);
    for (Material weapon : List.of(Material.CROSSBOW, Material.GOLDEN_SWORD, Material.GOLDEN_SPEAR)) {
      add(mobs, EntityType.PIGLIN, holding(weapon));
    }
    add(mobs, EntityType.PIGLIN, armor("GOLDEN"));
    for (EntityType cube : List.of(EntityType.SLIME, EntityType.MAGMA_CUBE)) {
      add(mobs, cube, size(1, "small"));
      add(mobs, cube, size(2, "medium"));
      add(mobs, cube, size(4, "large"));
    }
    add(mobs, EntityType.SULFUR_CUBE, size(1, "small"));
    add(mobs, EntityType.SULFUR_CUBE, size(2, "large"));
    for (EntityType baby : List.of(EntityType.HOGLIN, EntityType.ZOGLIN)) add(mobs, baby, BABY);

    // Farm animals
    for (DyeColor color : DyeColor.values()) {
      String key = color.name().toLowerCase(Locale.ROOT);
      MobTrait wool = property("color", key, () -> Component.translatable("color.minecraft." + key),
        entity -> entity instanceof Sheep sheep && sheep.getColor() == color);
      withBaby(mobs, EntityType.SHEEP, wool);
    }
    for (String climate : CLIMATES) {
      withBaby(mobs, EntityType.COW, property("climate", climate, entity -> entity.getType() == EntityType.COW && climate.equals(keyOf(cowVariant(entity)))));
      withBaby(mobs, EntityType.PIG, property("climate", Pig.class, Pig::getVariant, climate));
      withBaby(mobs, EntityType.CHICKEN, property("climate", Chicken.class, Chicken::getVariant, climate));
      add(mobs, EntityType.FROG, property("climate", Frog.class, Frog::getVariant, climate));
    }
    for (String variant : List.of("red", "brown")) {
      withBaby(mobs, EntityType.MOOSHROOM, property("mooshroom", MushroomCow.class, MushroomCow::getVariant, variant));
    }

    // Pets and horses
    for (String variant : List.of("ashen", "black", "chestnut", "pale", "rusty", "snowy", "spotted", "striped", "woods")) {
      withBaby(mobs, EntityType.WOLF, property("wolf", Wolf.class, Wolf::getVariant, variant));
    }
    for (String type : List.of("all_black", "black", "british_shorthair", "calico", "jellie", "persian", "ragdoll", "red",
      "siamese", "tabby", "white")) {
      withBaby(mobs, EntityType.CAT, property("cat", Cat.class, Cat::getCatType, type));
    }
    for (Horse.Color color : Horse.Color.values()) {
      for (Horse.Style style : Horse.Style.values()) {
        withBaby(mobs, EntityType.HORSE, property("horse-color", Horse.class, Horse::getColor, keyOf(color)),
          property("horse-marking", Horse.class, Horse::getStyle, keyOf(style)));
      }
    }
    for (Llama.Color color : Llama.Color.values()) {
      for (EntityType llama : List.of(EntityType.LLAMA, EntityType.TRADER_LLAMA)) {
        withBaby(mobs, llama, property("llama", Llama.class, Llama::getColor, keyOf(color)));
      }
    }
    for (Parrot.Variant variant : Parrot.Variant.values()) {
      add(mobs, EntityType.PARROT, property("parrot", Parrot.class, Parrot::getVariant, keyOf(variant)));
    }
    for (Rabbit.Type type : Rabbit.Type.values()) {
      if (type == Rabbit.Type.THE_KILLER_BUNNY) continue; // only from commands
      withBaby(mobs, EntityType.RABBIT, property("rabbit", Rabbit.class, Rabbit::getRabbitType, keyOf(type)));
    }

    // Wild animals
    for (Fox.Type type : Fox.Type.values()) {
      MobTrait coat = property("fox", Fox.class, Fox::getFoxType, keyOf(type));
      add(mobs, EntityType.FOX, ADULT, coat);
      add(mobs, EntityType.FOX, BABY, coat);
    }
    for (Material item : FOX_ITEMS) add(mobs, EntityType.FOX, holding(item));
    for (Panda.Gene gene : Panda.Gene.values()) {
      withBaby(mobs, EntityType.PANDA, property("panda", Panda.class, Panda::getCombinedGene, keyOf(gene)));
    }
    for (Axolotl.Variant variant : Axolotl.Variant.values()) {
      withBaby(mobs, EntityType.AXOLOTL, property("axolotl", Axolotl.class, Axolotl::getVariant, keyOf(variant)));
    }
    for (MobTrait[] state : List.of(new MobTrait[]{}, new MobTrait[]{NECTAR}, new MobTrait[]{ANGRY}, new MobTrait[]{ANGRY, NECTAR})) {
      if (state.length > 0) add(mobs, EntityType.BEE, concat(ADULT, state));
      add(mobs, EntityType.BEE, concat(BABY, state));
    }
    add(mobs, EntityType.GOAT, ADULT, LEFT_HORN_MISSING);
    add(mobs, EntityType.GOAT, ADULT, RIGHT_HORN_MISSING);
    add(mobs, EntityType.GOAT, ADULT, SCREAMING);
    add(mobs, EntityType.GOAT, ADULT, SCREAMING, LEFT_HORN_MISSING);
    add(mobs, EntityType.GOAT, ADULT, SCREAMING, RIGHT_HORN_MISSING);
    add(mobs, EntityType.GOAT, BABY);
    add(mobs, EntityType.GOAT, BABY, SCREAMING);
    for (EntityType baby : List.of(EntityType.DONKEY, EntityType.MULE, EntityType.OCELOT, EntityType.POLAR_BEAR, EntityType.TURTLE,
      EntityType.DOLPHIN, EntityType.SQUID, EntityType.GLOW_SQUID, EntityType.ARMADILLO, EntityType.CAMEL, EntityType.SNIFFER,
      EntityType.STRIDER, EntityType.NAUTILUS, EntityType.HAPPY_GHAST)) {
      add(mobs, baby, BABY);
    }
    add(mobs, EntityType.STRIDER, riding(EntityType.STRIDER));
    add(mobs, EntityType.CAMEL_HUSK, riddenBy(EntityType.HUSK));
    add(mobs, EntityType.CAMEL_HUSK, riddenBy(EntityType.PARCHED));
    for (String variant : List.of("temperate", "warm")) {
      MobTrait shell = property("zombie-nautilus", ZombieNautilus.class, ZombieNautilus::getVariant, variant);
      add(mobs, EntityType.ZOMBIE_NAUTILUS, shell);
    }
    add(mobs, EntityType.ZOMBIE_NAUTILUS, riddenBy(EntityType.DROWNED));

    // Fish
    for (Salmon.Variant size : Salmon.Variant.values()) {
      add(mobs, EntityType.SALMON, property("size", Salmon.class, Salmon::getVariant, keyOf(size)));
    }
    for (TropicalFish.Pattern pattern : TropicalFish.Pattern.values()) {
      add(mobs, EntityType.TROPICAL_FISH, property("pattern", keyOf(pattern),
        () -> Component.translatable("entity.minecraft.tropical_fish.type." + keyOf(pattern)),
        entity -> entity instanceof TropicalFish fish && fish.getPattern() == pattern));
    }
    for (int i = 0; i < NAMED_FISH.size(); i++) {
      FishLook look = NAMED_FISH.get(i);
      int index = i;
      add(mobs, EntityType.TROPICAL_FISH, property("named", String.valueOf(i),
        () -> Component.translatable("entity.minecraft.tropical_fish.predefined." + index), look::matches));
    }

    // Golems
    for (String state : List.of("unaffected", "exposed", "weathered", "oxidized")) {
      add(mobs, EntityType.COPPER_GOLEM, property("copper", CopperGolem.class, CopperGolem::getWeatheringState, state));
    }
    add(mobs, EntityType.SNOW_GOLEM, NO_PUMPKIN);

    mobs.replaceAll((_, list) -> List.copyOf(list));
    return mobs;
  }

  private static void add(@NotNull Map<EntityType, List<KillableMob>> mobs, @NotNull EntityType type, @NotNull MobTrait... traits) {
    mobs.computeIfAbsent(type, _ -> new ArrayList<>()).add(new KillableMob(type, List.of(traits)));
  }

  // The grown mob and its baby, each with the traits
  private static void withBaby(@NotNull Map<EntityType, List<KillableMob>> mobs, @NotNull EntityType type, @NotNull MobTrait... traits) {
    add(mobs, type, concat(ADULT, traits));
    add(mobs, type, concat(BABY, traits));
  }

  @NotNull
  private static MobTrait[] concat(@NotNull MobTrait first, @NotNull MobTrait... rest) {
    return Stream.concat(Stream.of(first), Stream.of(rest)).toArray(MobTrait[]::new);
  }

  @NotNull
  private static MobTrait biome(@NotNull String biome) {
    return property("biome", biome, entity -> biome.equals(keyOf(villagerType(entity))));
  }

  @NotNull
  private static MobTrait profession(@NotNull String profession) {
    Supplier<?> label = profession.equals("none")
      ? () -> MessageKey.of("challenge.generic.mob-trait.profession.none")
      : () -> Component.translatable("entity.minecraft.villager." + profession);
    return property("profession", profession, label, entity -> profession.equals(keyOf(villagerProfession(entity))));
  }

  // With the plugin's old api-version Paper rewrites Cow to AbstractCow, which has no variant, so it is looked up by name
  @Nullable
  private static Object cowVariant(@NotNull Entity entity) {
    try {
      return entity.getClass().getMethod("getVariant").invoke(entity);
    } catch (ReflectiveOperationException ex) {
      return null;
    }
  }

  @Nullable
  private static Object villagerType(@NotNull Entity entity) {
    if (entity instanceof Villager villager) return villager.getVillagerType();
    if (entity instanceof ZombieVillager zombie) return zombie.getVillagerType();
    return null;
  }

  @Nullable
  private static Object villagerProfession(@NotNull Entity entity) {
    if (entity instanceof Villager villager) return villager.getProfession();
    if (entity instanceof ZombieVillager zombie) return zombie.getVillagerProfession();
    return null;
  }

}
