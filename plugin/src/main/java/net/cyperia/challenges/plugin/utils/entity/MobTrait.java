package net.cyperia.challenges.plugin.utils.entity;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.AbstractCubeMob;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Raider;
import org.bukkit.entity.Shearable;
import org.bukkit.entity.Snowman;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * One property a mob has to have to count as a {@link KillableMob}: the id is part of the mob's id, the label part of
 * its name, {@code null} for a property the name leaves out. Labels are made on demand, as messages need the plugin.
 */
public record MobTrait(@NotNull String id, @Nullable Supplier<?> label, @NotNull Predicate<Entity> test) {

  private static final String KEY = "challenge.generic.mob-trait.";

  // Tells a grown mob apart from its baby without naming it
  public static final MobTrait ADULT = new MobTrait("adult", null, entity -> !(entity instanceof Ageable ageable) || ageable.isAdult());
  public static final MobTrait BABY = flag("baby", entity -> entity instanceof Ageable ageable && !ageable.isAdult());
  public static final MobTrait LEFT_HANDED = flag("left-handed", entity -> entity instanceof Mob mob && mob.isLeftHanded());
  public static final MobTrait RIGHT_HANDED = flag("right-handed", entity -> entity instanceof Mob mob && !mob.isLeftHanded());
  public static final MobTrait CHARGED = flag("charged", entity -> entity instanceof Creeper creeper && creeper.isPowered());
  public static final MobTrait RAID_CAPTAIN = flag("raid-captain", entity -> entity instanceof Raider raider && raider.isPatrolLeader());
  public static final MobTrait SHEARED = flag("sheared", entity -> entity instanceof Shearable shearable && shearable.isSheared());
  public static final MobTrait NO_PUMPKIN = flag("no-pumpkin", entity -> entity instanceof Snowman snowman && snowman.isDerp());
  public static final MobTrait SCREAMING = flag("screaming", entity -> entity instanceof Goat goat && goat.isScreaming());
  public static final MobTrait LEFT_HORN_MISSING = flag("left-horn-missing", entity -> entity instanceof Goat goat && !goat.hasLeftHorn());
  public static final MobTrait RIGHT_HORN_MISSING = flag("right-horn-missing", entity -> entity instanceof Goat goat && !goat.hasRightHorn());
  public static final MobTrait NECTAR = flag("nectar", entity -> entity instanceof Bee bee && bee.hasNectar());
  public static final MobTrait ANGRY = flag("angry", entity -> entity instanceof Bee bee && bee.getAnger() > 0);

  // A rider or mount: the pair counts when either of the two dies
  public boolean pairsWithAnotherMob() {
    return id.startsWith("riding=") || id.startsWith("ridden-by=");
  }

  // What the mob holds, wears or rides with, as opposed to an attribute of the mob itself
  public boolean isEquipment() {
    return id.startsWith("holding=") || id.startsWith("off-hand=") || id.startsWith("armor=") || pairsWithAnotherMob();
  }

  @NotNull
  private static MobTrait flag(@NotNull String id, @NotNull Predicate<Entity> test) {
    return new MobTrait(id, () -> MessageKey.of(KEY + id), test);
  }

  /**
   * One value of a property every mob of the type has, like a wolf's coat: {@code challenge.generic.mob-trait.<group>.<value>}
   * names it.
   */
  @NotNull
  public static MobTrait property(@NotNull String group, @NotNull String value, @NotNull Predicate<Entity> test) {
    return property(group, value, () -> MessageKey.of(KEY + group + "." + value), test);
  }

  @NotNull
  public static MobTrait property(@NotNull String group, @NotNull String value, @NotNull Supplier<?> label, @NotNull Predicate<Entity> test) {
    return new MobTrait(group + "=" + value, label, test);
  }

  // The value is compared lower case, so registry keys and enum names both work
  @NotNull
  public static <E extends Entity> MobTrait property(@NotNull String group, @NotNull Class<E> type, @NotNull Function<E, Object> getter,
                                                     @NotNull String value) {
    return property(group, value, entity -> type.isInstance(entity) && value.equals(keyOf(getter.apply(type.cast(entity)))));
  }

  @NotNull
  static String keyOf(@Nullable Object value) {
    if (value instanceof org.bukkit.Keyed keyed) return keyed.getKey().getKey();
    if (value instanceof Enum<?> constant) return constant.name().toLowerCase(Locale.ROOT);
    return String.valueOf(value);
  }

  @NotNull
  public static MobTrait holding(@NotNull Material item) {
    return new MobTrait("holding=" + item.getKey().getKey(), () -> Component.translatable(item.getItemTranslationKey()),
      entity -> equipment(entity) != null && equipment(entity).getItemInMainHand().getType() == item);
  }

  @NotNull
  public static MobTrait offHand(@NotNull Material item) {
    return new MobTrait("off-hand=" + item.getKey().getKey(), () -> Component.translatable(item.getItemTranslationKey()),
      entity -> equipment(entity) != null && equipment(entity).getItemInOffHand().getType() == item);
  }

  /**
   * Any piece of armor of one material, as mobs rarely spawn with all four.
   *
   * @param material the prefix of the item names, like {@code GOLDEN}
   */
  @NotNull
  public static MobTrait armor(@NotNull String material) {
    String name = material.toLowerCase(Locale.ROOT);
    return new MobTrait("armor=" + name, () -> MessageKey.of(KEY + "armor." + name), entity -> {
      EntityEquipment equipment = equipment(entity);
      if (equipment == null) return false;
      return isPiece(equipment.getHelmet(), material, "HELMET") || isPiece(equipment.getChestplate(), material, "CHESTPLATE")
        || isPiece(equipment.getLeggings(), material, "LEGGINGS") || isPiece(equipment.getBoots(), material, "BOOTS");
    });
  }

  private static boolean isPiece(@Nullable ItemStack item, @NotNull String material, @NotNull String piece) {
    return item != null && item.getType().name().equals(material + "_" + piece);
  }

  @NotNull
  public static MobTrait riding(@NotNull EntityType vehicle) {
    return new MobTrait("riding=" + vehicle.getKey().getKey(), () -> MessageKey.of(KEY + "riding").withArgs(name(vehicle)),
      entity -> entity.getVehicle() != null && entity.getVehicle().getType() == vehicle);
  }

  @NotNull
  public static MobTrait riddenBy(@NotNull EntityType rider) {
    return new MobTrait("ridden-by=" + rider.getKey().getKey(), () -> MessageKey.of(KEY + "ridden-by").withArgs(name(rider)),
      entity -> entity.getPassengers().stream().anyMatch(passenger -> passenger.getType() == rider));
  }

  @NotNull
  public static MobTrait riddenByCaptain(@NotNull EntityType rider) {
    return new MobTrait("ridden-by=" + rider.getKey().getKey() + "+captain", () -> MessageKey.of(KEY + "ridden-by-captain").withArgs(name(rider)),
      entity -> entity.getPassengers().stream().anyMatch(passenger -> passenger.getType() == rider && RAID_CAPTAIN.test().test(passenger)));
  }

  // By key: the effect constants need a running server, the variant list does not
  @NotNull
  public static MobTrait effect(@NotNull String effect) {
    return new MobTrait("effect=" + effect, () -> Component.translatable("effect.minecraft." + effect),
      entity -> entity instanceof LivingEntity living
        && living.getActivePotionEffects().stream().anyMatch(active -> active.getType().getKey().getKey().equals(effect)));
  }

  @NotNull
  public static MobTrait size(int size, @NotNull String name) {
    return new MobTrait("size=" + size, () -> MessageKey.of(KEY + "size." + name),
      entity -> entity instanceof AbstractCubeMob cube && cube.getSize() == size);
  }

  // Without the mob icon an EntityType argument brings along, which looks out of place inside the brackets
  @NotNull
  private static Component name(@NotNull EntityType type) {
    return Component.translatable(type.translationKey());
  }

  @Nullable
  private static EntityEquipment equipment(@NotNull Entity entity) {
    return entity instanceof LivingEntity living ? living.getEquipment() : null;
  }

}
