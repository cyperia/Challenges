package net.cyperia.challenges.plugin.management.cloud.support;

import de.dytanic.cloudnet.ext.bridge.BridgeHelper;
import de.dytanic.cloudnet.ext.bridge.bukkit.BukkitCloudNetHelper;
import net.cyperia.challenges.plugin.management.cloud.CloudSupport;

public final class CloudNet3Support implements CloudSupport {

  @Override
  public void startNewService() {
    BukkitCloudNetHelper.changeToIngame();
  }

  @Override
  public void setIngame() {
    BukkitCloudNetHelper.setState("INGAME");
    BridgeHelper.updateServiceInfo();
  }

  @Override
  public void setLobby() {
    BukkitCloudNetHelper.setState("LOBBY");
    BridgeHelper.updateServiceInfo();
  }

}
