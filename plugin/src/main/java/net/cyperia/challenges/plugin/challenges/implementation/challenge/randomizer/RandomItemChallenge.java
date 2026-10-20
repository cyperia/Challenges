package net.cyperia.challenges.plugin.challenges.implementation.challenge.randomizer;

import net.cyperia.challenges.plugin.challenges.custom.settings.action.impl.RandomItemAction;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge;

public class RandomItemChallenge extends TimedChallenge {

  public RandomItemChallenge() {
    super(1, 60, 30, false);
    setValueUnit(ValueUnit.INTERVAL_SECONDS);
  }

  @Override
  protected void onTimeActivation() {
    restartTimer();
    broadcastFiltered(player -> RandomItemAction.giveRandomItemToPlayer(player, true));
  }

}
