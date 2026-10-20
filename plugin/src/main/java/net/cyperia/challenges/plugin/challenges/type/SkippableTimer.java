package net.cyperia.challenges.plugin.challenges.type;

public interface SkippableTimer extends IChallenge {

  // Brings the next activation to now, false when there is nothing to skip
  boolean skipTimer();

}
