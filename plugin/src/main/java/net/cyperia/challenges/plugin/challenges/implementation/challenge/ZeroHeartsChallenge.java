package net.cyperia.challenges.plugin.challenges.implementation.challenge;

import org.bukkit.attribute.Attribute;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.implementation.setting.MaxHealthSetting;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class ZeroHeartsChallenge extends SettingModifier {

  public ZeroHeartsChallenge() {
    super(5, 30, 10);
    setValueUnit(ValueUnit.MINUTES);
  }

  @Override
  protected void onEnable() {
    bossbar.setContent((bossbar, player) -> {
      int currentTime = getCurrentTime();
      int maxTime = getValue() * 60;
      bossbar.setTitle(getChallengeMessageKey("bossbar"), ChallengeAPI.formatTime(maxTime - currentTime));
      bossbar.setColor(BossBar.Color.GREEN);
      bossbar.setProgress(1 - ((float) currentTime / maxTime));
    });
    bossbar.show();
    broadcastFiltered(this::removeHearts);
  }

  @Override
  protected void onDisable() {
    bossbar.hide();
    AbstractChallenge.getFirstInstance(MaxHealthSetting.class).onValueChange();
  }

  @Override
  protected void onValueChange() {
    bossbar.update();
  }

  @ScheduledTask(ticks = 20, async = false)
  public void onSecond() {
    broadcastFiltered(this::removeHearts);
    int absorptionTime = getCurrentTime();
    if (absorptionTime >= getValue() * 60) {
      bossbar.hide();
      if (absorptionTime == getValue() * 60) {
        removeAbsorptionEffect();
      }
      return;
    }
    getGameStateData().set("absorption-time", absorptionTime + 1);
    applyAbsorptionEffect();
    bossbar.update();
  }

  private void removeHearts(@NotNull Player player) {
    AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
    if (attribute == null) return;
    attribute.setBaseValue(0);
  }

  private int getCurrentTime() {
    return getGameStateData().getInt("absorption-time");
  }

  private void applyAbsorptionEffect() {
    broadcastFiltered(player -> player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, (getValue() - getCurrentTime()) * 20 * 60, 1, true, false, false)));
  }

  private void removeAbsorptionEffect() {
    broadcastFiltered(player -> player.removePotionEffect(PotionEffectType.ABSORPTION));
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onEntityPotionEffect(@NotNull EntityPotionEffectEvent event) {
    if (event.getAction() != Action.REMOVED) return;
    if (!(event.getEntity() instanceof Player player)) return;
    if (!shouldExecuteEffect(player)) return;
    ChallengeHelper.kill(player);
    getChallengeMessageKey("failed").broadcast(Prefix.CHALLENGES, player);
  }

}
