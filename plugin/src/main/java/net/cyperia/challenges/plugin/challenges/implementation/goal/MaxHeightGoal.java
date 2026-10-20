package net.cyperia.challenges.plugin.challenges.implementation.goal;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.abstraction.FirstPlayerAtHeightGoal;
import org.bukkit.World.Environment;

public class MaxHeightGoal extends FirstPlayerAtHeightGoal {

  public MaxHeightGoal() {
    setHeightToGetTo(ChallengeAPI.getGameWorld(Environment.NORMAL).getMaxHeight());
  }

}
