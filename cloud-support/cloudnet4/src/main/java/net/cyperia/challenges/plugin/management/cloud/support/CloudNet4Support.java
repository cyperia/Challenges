package net.cyperia.challenges.plugin.management.cloud.support;

import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.modules.bridge.BridgeServiceHelper;
import eu.cloudnetservice.wrapper.holder.ServiceInfoHolder;
import net.cyperia.challenges.plugin.management.cloud.CloudSupport;

public final class CloudNet4Support implements CloudSupport {

  @Override
  public void startNewService() {
    InjectionLayer.ext().instance(BridgeServiceHelper.class).changeToIngame(true);
  }

  @Override
  public void setIngame() {
    InjectionLayer.ext().instance(BridgeServiceHelper.class).state().set("INGAME");
    publishServiceInfoUpdate();
  }

  @Override
  public void setLobby() {
    InjectionLayer.ext().instance(BridgeServiceHelper.class).state().set("LOBBY");
    publishServiceInfoUpdate();
  }

  private void publishServiceInfoUpdate() {
    InjectionLayer.boot().instance(ServiceInfoHolder.class).publishServiceInfoUpdate();
  }

}
