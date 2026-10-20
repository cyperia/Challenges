package net.cyperia.challenges.plugin.management.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public enum ChallengeEndCause {

  TIMER_HIT_ZERO("challenge-end.timer-hit-zero", "challenge-end.timer-hit-zero-winner"),
  GOAL_REACHED("challenge-end.goal-reached", "challenge-end.goal-reached-winner"),
  GOAL_FAILED("challenge-end.goal-failed", null);

  private final String noWinnerMessage, winnerMessage;

  @NotNull
  public MessageKey getMessage(boolean withWinner) {
    return MessageKey.of(withWinner && winnerMessage != null ? winnerMessage : noWinnerMessage);
  }

  public boolean isWinnable() {
    return winnerMessage != null;
  }

}
