package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.challenges.type.IGoal;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.challenges.type.helper.GoalHelper;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SettingModifierGoal extends SettingModifier implements IGoal {

  public SettingModifierGoal() {
  }

  public SettingModifierGoal(int max) {
    super(max);
  }

  public SettingModifierGoal(int min, int max) {
    super(min, max);
  }

  public SettingModifierGoal(int min, int max, int defaultValue) {
    super(min, max, defaultValue);
  }

  @Override
  public final void setEnabled(boolean enabled) {
    if (isEnabled() == enabled) return;
    GoalHelper.handleSetEnabled(this, enabled);
    super.setEnabled(enabled);
  }

  @NotNull
  @Override
  public SoundSample getStartSound() {
    return SoundSample.DRAGON_BREATH;
  }

  @Nullable
  @Override
  public SoundSample getWinSound() {
    return SoundSample.WIN;
  }

  @Override
  public void playStatusUpdateTitle() {
    ChallengeHelper.playGoalToggleTitle(this);
  }
}
