package net.cyperia.challenges.plugin.challenges.type.helper;

import org.bukkit.attribute.Attribute;
import lombok.RequiredArgsConstructor;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface ChallengePenalty {

  @NotNull
  @Contract(pure = true)
  static ChallengePenalty kill() {
    return ChallengeHelper::kill;
  }

  @NotNull
  @Contract(pure = true)
  static ChallengePenalty damage(double damage) {
    return new DamagePenalty(damage);
  }

  @NotNull
  @Contract(pure = true)
  static ChallengePenalty reduceMaxHealth(int reduction) {
    return new MaxHealthPenalty(reduction);
  }

  void punish(@NotNull Player player);

  @RequiredArgsConstructor
  class DamagePenalty implements ChallengePenalty {
    protected final double damage;

    @Override
    public void punish(@NotNull Player player) {
      if (damage > player.getHealth()) {
        ChallengeHelper.kill(player);
        return;
      }

      SoundSample.DAMAGE.play(player);
      player.damage(damage);
    }
  }

  @RequiredArgsConstructor
  class MaxHealthPenalty implements ChallengePenalty {
    protected final int reduction;

    @Override
    public void punish(@NotNull Player player) {
      AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
      if (attribute == null) return;

      double reduced = attribute.getBaseValue() - reduction;
      if (reduced < reduction) {
        ChallengeHelper.kill(player);
        return;
      }
      attribute.setBaseValue(reduced);
      SoundSample.BREAK.play(player);
    }

  }

}
