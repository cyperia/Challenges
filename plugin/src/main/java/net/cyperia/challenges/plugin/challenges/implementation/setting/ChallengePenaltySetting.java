package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.menu.MenuModifier;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengePenalty;
import net.cyperia.challenges.plugin.content.i18n.MessageKeySpace;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ChallengePenaltySetting extends MenuModifier {

  public ChallengePenaltySetting() {
    OneEnabledGroup group = new OneEnabledGroup().requireOneEnabled();
    registerSetting("insta_kill", group, new InstaKillSubSetting(getSubSettingSpace("insta_kill")));
    registerSetting("damage", group, new DamageSubSetting(getSubSettingSpace("damage")));
    registerSetting("max_health", group, new MaxHealthSubSetting(getSubSettingSpace("max_health")));
  }

  public class InstaKillSubSetting extends BooleanSubSetting {

    public InstaKillSubSetting(@NotNull MessageKeySpace key) {
      super(new ItemStack(Material.TNT), key, true);
    }

    @Override
    protected void onEnable() {
      ChallengeHelper.setPenalty(ChallengePenalty.kill());
    }
  }

  public class DamageSubSetting extends NumberAndBooleanSubSetting {

    public DamageSubSetting(@NotNull MessageKeySpace key) {
      super(new ItemStack(Material.GOLDEN_SWORD), key, 1, 200, 10, false);
      unit(ValueUnit.DAMAGE);
    }

    @Override
    protected void onValueChange() {
      ChallengeHelper.setPenalty(ChallengePenalty.damage(getValue()));
    }

    @Override
    protected void onEnable() {
      onValueChange();
    }
  }

  public class MaxHealthSubSetting extends NumberAndBooleanSubSetting {

    public MaxHealthSubSetting(@NotNull MessageKeySpace key) {
      super(new ItemStack(Material.APPLE), key, 1, 20, 2, false);
      unit(ValueUnit.HEARTS);
    }

    @Override
    protected void onValueChange() {
      ChallengeHelper.setPenalty(ChallengePenalty.reduceMaxHealth(getValue()));
    }

    @Override
    protected void onEnable() {
      onValueChange();
    }
  }

}
