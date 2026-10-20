package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets;

import lombok.Getter;
import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.ExtremeForceBattleGoal;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.impl.format.CustomTranslatable;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

@Getter
public abstract class ForceTarget<T> {
  protected final T target;

  protected ForceTarget(T target) {
    this.target = target;
  }

  public abstract boolean check(Player player);

  public abstract ExtremeForceBattleGoal.TargetType getType();

  /**
   * The name of this target, resolved in the language of whoever it is shown to and, where minecraft
   * knows the value itself, translated by their own client (items, blocks, mobs, biomes, advancements).
   *
   * @implSpec Handing over the value keeps that client side translation intact, so only targets whose
   * value carries no name of its own - a damage amount, a pair of coordinates - need to override this
   * with a {@link MessageKey}. Never render a name into a {@link String} here: that would freeze it
   * into one language for every viewer.
   */
  @NotNull
  public LocalizableMessage getDisplayName() {
    return CustomTranslatable.of(target);
  }

  /**
   * The name preceded by the kind of target it is ({@code Item: Diamond}), for the places that mix
   * target types and would read ambiguously without it.
   */
  @NotNull
  public LocalizableMessage getTypedDisplayName() {
    return getTargetMessageKey("display").withArgs(getDisplayName());
  }

  /**
   * All target wording lives under {@code challenge.force-battles.target.<type>}, so the same
   * message is used no matter which goal handed out the target.
   */
  @NotNull
  public MessageKey getTargetMessageKey(@NotNull String keySuffix) {
    return MessageKey.of("challenge.force-battles.target."
      + getType().name().toLowerCase(Locale.ROOT) + "." + keySuffix);
  }

  @NotNull
  public MessageKey getNewTargetMessage() {
    return getTargetMessageKey("new");
  }

  @NotNull
  public MessageKey getCompletedMessage() {
    return getTargetMessageKey("found");
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null || getClass() != other.getClass()) return false;
    return Objects.equals(target, ((ForceTarget<?>) other).target);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClass(), target);
  }

  @Override
  public String toString() {
    return target.toString();
  }

  public void updateDisplayStand(@NotNull ArmorStand armorStand) {
    if (target instanceof Material && ((Material) target).isItem()) {
      Objects.requireNonNull(armorStand.getEquipment()).setHelmet(new ItemStack((Material) target));
    } else {
      Objects.requireNonNull(armorStand.getEquipment()).setHelmet(null);
    }
  }

  public Object getTargetSaveObject() {
    return target.toString();
  }

}
