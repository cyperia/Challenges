package net.cyperia.challenges.plugin.management.scheduler.timer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TimerStatus {

  RUNNING(true),
  PAUSED(false),
  COUNTDOWN(false);

  private final boolean ingame;

}
