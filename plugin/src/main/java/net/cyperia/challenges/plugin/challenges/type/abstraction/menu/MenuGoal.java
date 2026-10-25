package net.cyperia.challenges.plugin.challenges.type.abstraction.menu;

import net.cyperia.challenges.plugin.challenges.type.IGoal;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.challenges.type.helper.GoalHelper;
import net.cyperia.challenges.plugin.content.i18n.MessageKeySpace;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.format.InteractionTooltips;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MenuGoal extends MenuSetting implements IGoal {

  // Whether the item pool has every enchanted book level and potion on its own, see CollectableItem.
  // onChange also runs while the settings load, before the goal may be enabled
  @NotNull
  protected final BooleanSubSetting registerItemVariantsSetting(boolean enabledByDefault, @NotNull Runnable onChange) {
    return registerPoolToggle("itemVariants", Material.ENCHANTED_BOOK, "item-variants", enabledByDefault, onChange);
  }

  // Whether the mob pool has the variants of a mob by its attributes, like coat or age, see KillableMob
  @NotNull
  protected final BooleanSubSetting registerMobAttributesSetting(boolean enabledByDefault, @NotNull Runnable onChange) {
    return registerPoolToggle("mobAttributes", Material.ZOMBIE_HEAD, "mob-attributes", enabledByDefault, onChange);
  }

  // Whether the mob pool has the variants of a mob by what it holds, wears and rides with
  @NotNull
  protected final BooleanSubSetting registerMobEquipmentSetting(boolean enabledByDefault, @NotNull Runnable onChange) {
    return registerPoolToggle("mobEquipment", Material.IRON_SWORD, "mob-equipment", enabledByDefault, onChange);
  }

  @NotNull
  private BooleanSubSetting registerPoolToggle(@NotNull String key, @NotNull Material icon, @NotNull String name,
                                               boolean enabledByDefault, @NotNull Runnable onChange) {
    return registerSetting(key, "3.1", new BooleanSubSetting(
      new ItemStack(icon), MessageKeySpace.of("challenge.generic.sub." + name), enabledByDefault
    ) {
      @Override
      protected void onEnable() {
        onChange.run();
      }

      @Override
      protected void onDisable() {
        onChange.run();
      }
    });
  }

  // Once enabled the settings item opens the sub menu, see MenuSetting#handleClick
  @NotNull
  @Override
  protected InteractionTooltips getSettingsTooltips() {
    return new InteractionTooltips()
      .on(MenuClickType.LEFT, isEnabled() ? Interaction.OPEN : Interaction.ENABLE_GOAL);
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
