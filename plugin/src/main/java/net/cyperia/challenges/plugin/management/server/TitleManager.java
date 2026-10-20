package net.cyperia.challenges.plugin.management.server;

import lombok.Getter;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import net.cyperia.challenges.plugin.challenges.type.IGoal;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.commons.common.config.Document;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public final class TitleManager {

  public static final int FADEIN = 5, DURATION = 20, FADEOUT = 10;

  private final boolean timerStatusEnabled;
  private final boolean challengeStatusEnabled;

  public TitleManager() {
    Document config = Challenges.getInstance().getConfigDocument();
    timerStatusEnabled = config.getBoolean(ConfigManager.Keys.TITLES_TIMER_STATUS);
    challengeStatusEnabled = config.getBoolean(ConfigManager.Keys.TITLES_CHALLENGE_STATUS);
  }

  public void sendTimerStatusTitle(@NotNull MessageKey message) {
    if (!timerStatusEnabled) return;
    message.broadcastTitle();
  }

  public void sendChallengeStatusTitle(@NotNull MessageKey message, @NotNull Object... args) {
    if (!challengeStatusEnabled) return;
    message.broadcastTitle(args);
  }

  public void sendChallengeToggleTitle(@NotNull IChallenge challenge, boolean enabled) {
    MessageKey titleMessage = enabled ? MessageKey.of("title.challenge-enabled") : MessageKey.of("title.challenge-disabled");
    sendChallengeStatusTitle(titleMessage, challenge.getName());
  }

  public void sendGoalToggleTitle(@NotNull IGoal goal, boolean enabled) {
    MessageKey titleMessage = enabled ? MessageKey.of("title.goal-enabled") : MessageKey.of("title.goal-disabled");
    sendChallengeStatusTitle(titleMessage, goal.getName());
  }

  public void sendSubSettingToggleTitle(@NotNull IChallenge challenge, @NotNull Object subSettingName, boolean enabled) {
    MessageKey titleMessage = enabled ? MessageKey.of("title.challenge-sub-enabled") : MessageKey.of("title.challenge-sub-disabled");
    sendChallengeStatusTitle(titleMessage, challenge.getName(), subSettingName);
  }

  public void sendChallengeValueTitle(@NotNull IChallenge challenge, @NotNull Object arg) {
    MessageKey titleMessage = MessageKey.of("title.challenge-value-changed");
    sendChallengeStatusTitle(titleMessage, challenge.getName(), arg);
  }

  public void sendSubSettingValueTitle(@NotNull IChallenge challenge, @NotNull Object subSettingName, @NotNull Object arg) {
    MessageKey titleMessage = MessageKey.of("title.challenge-sub-value-changed");
    sendChallengeStatusTitle(titleMessage, challenge.getName(), subSettingName, arg);
  }

  @Deprecated
  public void sendTitle(@NotNull Player player, @NotNull String title, @NotNull String subtitle) {
    player.sendTitle(title, subtitle, FADEIN, DURATION, FADEOUT);
  }

  @Deprecated
  public void sendTitleInstant(@NotNull Player player, @NotNull String title, @NotNull String subtitle) {
    player.sendTitle(title, subtitle, 0, DURATION, FADEOUT);
  }

}
