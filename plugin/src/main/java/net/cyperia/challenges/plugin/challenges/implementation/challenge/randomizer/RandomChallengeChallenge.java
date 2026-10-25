package net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.EntryAccess;
import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge;
import net.cyperia.challenges.plugin.challenges.implementation.LockedEntry;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.kyori.adventure.bossbar.BossBar;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RandomChallengeChallenge extends TimedChallenge {

  private AbstractChallenge lastUsed;

  public RandomChallengeChallenge() {
    super(3, 60, 6, false);
    setValueUnit(ValueUnit.INTERVAL_SECONDS.scaled(10));
  }

  @Override
  protected void onEnable() {
    bossbar.setContent((bossbar, player) -> {
      if (lastUsed == null) {
        bossbar.setTitle(getChallengeMessageKey("bossbar-waiting"));
        return;
      }
      bossbar.setColor(BossBar.Color.GREEN);
      bossbar.setProgress(getProgress());
      bossbar.setTitle(getChallengeMessageKey("bossbar-current"), lastUsed.getName());
    });
    bossbar.show();
  }

  @Override
  protected void onDisable() {
    if (lastUsed != null) {
      setEnabled(lastUsed, false);
      lastUsed = null;
    }
    bossbar.hide();
  }

  @Override
  protected void handleCountdown() {
    bossbar.update();
  }

  @Override
  protected void onTimeActivation() {
    restartTimer();

    if (lastUsed != null) {
      setEnabled(lastUsed, false);
      lastUsed = null;
    }

    List<IChallenge> challenges = new ArrayList<>(Challenges.getInstance().getChallengeManager().getChallenges());
    challenges.remove(this);
    challenges.removeIf(challenge -> challenge.getMenu() != MenuType.CHALLENGES);
    challenges.removeIf(challenge -> !(challenge instanceof AbstractChallenge));
    // a pick turns the challenge on for everyone, past the permission of one that names one
    challenges.removeIf(challenge -> challenge instanceof LockedEntry
      || ((AbstractChallenge) challenge).getCatalogEntry().excludeFromRandom()
      || EntryAccess.hasPermission(challenge));
    challenges.removeIf(IChallenge::isEnabled);
    if (challenges.isEmpty()) return;

    AbstractChallenge challenge = (AbstractChallenge) globalRandom.choose(challenges);
    getChallengeMessageKey("challenge-enabled").broadcast(Prefix.CHALLENGES, challenge.getName(), challenge.getDescription());

    setEnabled(challenge, true);
    lastUsed = challenge;
    bossbar.update();

  }

  private void setEnabled(@NotNull IChallenge challenge, boolean enabled) {
    if (challenge instanceof Setting setting) {
      setting.setEnabled(enabled);
    }
    if (challenge instanceof SettingModifier setting) {
      setting.setEnabled(enabled);
    }

    if (enabled && challenge instanceof TimedChallenge timedChallenge) {
      if (timedChallenge.isTimerRunning()) {
        int seconds = globalRandom.range(10, 20);
        if (seconds < timedChallenge.getSecondsLeftUntilNextActivation())
          timedChallenge.shortCountDownTo(seconds);
      }
    }
  }

}
