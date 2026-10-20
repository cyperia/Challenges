package net.cyperia.challenges.plugin.management.cloud.support;

import cloud.spawnery.agent.api.Spawnery;
import java.util.Map;
import net.cyperia.challenges.plugin.management.cloud.CloudSupport;

public final class SpawnerySupport implements CloudSupport {

  private static final String STATE_INGAME = "ingame";
  private static final String STATE_LOBBY = "lobby";

  @Override
  public void setIngame() {
    Spawnery.api().announce(STATE_INGAME, Map.of());
    Spawnery.api().acceptJoins(false);
  }

  @Override
  public void setLobby() {
    Spawnery.api().announce(STATE_LOBBY, Map.of());
    Spawnery.api().acceptJoins(true);
  }

  @Override
  public void setRoundOver() {
    Spawnery.api().endRound();
  }

  @Override
  public void startNewService() {
    // Nothing to ask for: acceptJoins(false) already drops this server out of the group's free
    // capacity, and the group builds the replacement from that.
  }

}
