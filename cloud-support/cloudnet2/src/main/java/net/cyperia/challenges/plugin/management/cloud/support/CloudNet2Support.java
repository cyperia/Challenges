package net.cyperia.challenges.plugin.management.cloud.support;

import de.dytanic.cloudnet.bridge.CloudServer;
import de.dytanic.cloudnet.lib.server.ServerState;
import net.cyperia.challenges.plugin.management.cloud.CloudSupport;

public final class CloudNet2Support implements CloudSupport {

  @Override
  public void startNewService() {
    CloudServer.getInstance().changeToIngame();
  }

  @Override
  public void setIngame() {
    CloudServer.getInstance().setServerState(ServerState.INGAME);
  }

  @Override
  public void setLobby() {
    CloudServer.getInstance().setServerStateAndUpdate(ServerState.LOBBY);
  }

}
