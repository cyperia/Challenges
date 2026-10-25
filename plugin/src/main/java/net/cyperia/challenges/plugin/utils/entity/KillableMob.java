package net.cyperia.challenges.plugin.utils.entity;

import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.commons.bukkit.utils.item.ItemUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A mob to kill: a type, and with variants a type with every one of its {@link MobTrait traits}. A kill counts for
 * every killable mob it matches, so a baby zombie on a chicken is a zombie, a baby zombie and a chicken jockey at once.
 */
public record KillableMob(@NotNull EntityType type, @NotNull List<MobTrait> traits) {

  @NotNull
  public static KillableMob of(@NotNull EntityType type) {
    return new KillableMob(type, List.of());
  }

  /**
   * Every type first on its own, followed by its attribute or equipment variants that are asked for; on its own only
   * where the variants leave some of it out.
   */
  @NotNull
  public static List<KillableMob> pool(@NotNull Collection<EntityType> types, boolean attributes, boolean equipment) {
    List<KillableMob> mobs = new ArrayList<>();
    for (EntityType type : types) {
      // Only the attribute variants of a type add up to every mob of it, a mob may hold nothing
      if (!attributes || !MobVariants.coversEveryMob(type)) mobs.add(of(type));
      for (KillableMob variant : MobVariants.of(type)) {
        if (variant.isEquipment() ? equipment : attributes) mobs.add(variant);
      }
    }
    return List.copyOf(mobs);
  }

  boolean isEquipment() {
    return traits.stream().anyMatch(MobTrait::isEquipment);
  }

  @NotNull
  public static Map<EntityType, List<KillableMob>> byType(@NotNull List<KillableMob> mobs) {
    Map<EntityType, List<KillableMob>> byType = new EnumMap<>(EntityType.class);
    for (KillableMob mob : mobs) {
      byType.computeIfAbsent(mob.type(), _ -> new ArrayList<>()).add(mob);
    }
    return byType;
  }

  /**
   * What the death of {@code dying} counts for: every mob of the pool it matches, and the pairs it was part of, so
   * killing the pillager on a ravager counts the ravager ridden by a pillager as well.
   */
  @NotNull
  public static List<KillableMob> killed(@NotNull Entity dying, @NotNull Map<EntityType, List<KillableMob>> pool) {
    List<KillableMob> killed = new ArrayList<>();
    for (KillableMob mob : pool.getOrDefault(dying.getType(), List.of())) {
      if (mob.matches(dying)) killed.add(mob);
    }
    List<Entity> partners = new ArrayList<>(dying.getPassengers());
    if (dying.getVehicle() != null) partners.add(dying.getVehicle());
    for (Entity partner : partners) {
      for (KillableMob mob : pool.getOrDefault(partner.getType(), List.of())) {
        if (mob.isPair() && mob.matches(partner)) killed.add(mob);
      }
    }
    return killed;
  }

  private boolean isPair() {
    return traits.stream().anyMatch(MobTrait::pairsWithAnotherMob);
  }

  public boolean matches(@NotNull Entity entity) {
    if (entity.getType() != type) return false;
    for (MobTrait trait : traits) {
      if (!trait.test().test(entity)) return false;
    }
    return true;
  }

  /**
   * {@code minecraft:zombie} or {@code minecraft:zombie[baby,riding=chicken]}, the bare type the same as before
   * variants existed.
   */
  @NotNull
  public String id() {
    String id = type.getKey().toString();
    if (traits.isEmpty()) return id;
    return id + traits.stream().map(MobTrait::id).collect(Collectors.joining(",", "[", "]"));
  }

  // Gamestates written before variants kept a bare type by its enum name
  @NotNull
  public static String normalizeId(@NotNull String id) {
    return id.contains(":") ? id : "minecraft:" + id.toLowerCase(Locale.ROOT);
  }

  @NotNull
  public LocalizableMessage displayName() {
    List<Object> args = new ArrayList<>();
    args.add(type);
    for (MobTrait trait : traits) {
      if (trait.label() != null) args.add(trait.label().get());
    }
    if (args.size() == 1) return LocalizableMessage.wrap(type);
    return MessageKey.of("challenge.generic.mob-variant." + (args.size() - 1)).withArgs(args.toArray());
  }

  @NotNull
  public ItemStack icon() {
    return new ItemStack(ItemUtils.getIcon(type));
  }

}
