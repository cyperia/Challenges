package net.cyperia.challenges.plugin.challenges.custom.settings.trigger.impl;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SelectableKey;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SubSettingsBuilder;
import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.management.scheduler.policy.PlayerCountPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class IntervallTrigger extends ChallengeTrigger {

  /**
   * The longest interval that can fire, in seconds. Anything above it never divides the timer.
   */
  private static final int MAX_INTERVAL = 600;

  public IntervallTrigger(@NotNull String name) {
    super(name, SubSettingsBuilder.createChooseItem("time").fill(builder -> {
      builder.addSetting(SelectableKey.ofName("1", Material.MUSIC_DISC_13, "interval.second", "1"));
      builder.addSetting(SelectableKey.ofName("2", Material.MUSIC_DISC_CAT, "interval.seconds", "2"));
      builder.addSetting(SelectableKey.ofName("5", Material.MUSIC_DISC_BLOCKS, "interval.seconds", "5"));
      builder.addSetting(SelectableKey.ofName("10", Material.MUSIC_DISC_CHIRP, "interval.seconds", "10"));
      builder.addSetting(SelectableKey.ofName("20", Material.MUSIC_DISC_FAR, "interval.seconds", "20"));
      builder.addSetting(SelectableKey.ofName("30", Material.MUSIC_DISC_MALL, "interval.seconds", "30"));
      builder.addSetting(SelectableKey.ofName("60", Material.MUSIC_DISC_MELLOHI, "interval.seconds", "60"));
      builder.addSetting(SelectableKey.ofName("120", Material.MUSIC_DISC_STAL, "interval.minutes", "2"));
      builder.addSetting(SelectableKey.ofName("180", Material.MUSIC_DISC_STRAD, "interval.minutes", "3"));
      builder.addSetting(SelectableKey.ofName("240", Material.MUSIC_DISC_WARD, "interval.minutes", "4"));
      builder.addSetting(SelectableKey.ofName("300", Material.MUSIC_DISC_11, "interval.minutes", "5"));
    }));
    Challenges.getInstance().getScheduler().register(this);
  }

  @ScheduledTask(ticks = 20, playerPolicy = PlayerCountPolicy.ALWAYS)
  public void onSecond() {
    long currentTime = Challenges.getInstance().getChallengeTimer().getTime();

    // every divisor, not just the ones the menu offers: a preset challenge sets its interval by value
    List<String> list = new LinkedList<>();
    for (int interval = 1; interval <= MAX_INTERVAL; interval++) {
      if (currentTime % interval == 0) {
        list.add(String.valueOf(interval));
      }
    }

    createData()
      .data("time", list)
      .execute();
  }

}
