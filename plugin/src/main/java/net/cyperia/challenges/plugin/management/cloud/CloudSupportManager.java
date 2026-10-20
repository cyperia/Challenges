package net.cyperia.challenges.plugin.management.cloud;

import lombok.Getter;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.management.cloud.support.CloudNet2Support;
import net.cyperia.challenges.plugin.management.cloud.support.CloudNet3Support;
import net.cyperia.challenges.plugin.management.cloud.support.CloudNet4Support;
import net.cyperia.challenges.plugin.management.cloud.support.SpawnerySupport;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.cyperia.commons.common.config.Document;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CloudSupportManager implements Listener {

  private final boolean resetToLobby;
  private final boolean setIngame;
  @Getter
  private final String type;
  private final boolean startNewService;
  private boolean startedNewService = false;
  private CloudSupport support;

  public CloudSupportManager() {
    Document config = Challenges.getInstance().getConfigDocument();

    startNewService = config.getBoolean(ConfigManager.Keys.CLOUD_SUPPORT_START_NEW_SERVICE);
    resetToLobby = config.getBoolean(ConfigManager.Keys.CLOUD_SUPPORT_RESET_TO_LOBBY);
    setIngame = config.getBoolean(ConfigManager.Keys.CLOUD_SUPPORT_SET_INGAME);
    type = config.getString(ConfigManager.Keys.CLOUD_SUPPORT_TYPE, "none");
    Logger.debug("Detected cloud support type '{}'", type);

    if (type.equals("none")) return;

    support = loadSupport(type);
    ChallengeAPI.registerScheduler(this);
    Challenges.getInstance().registerListener(this);
  }

  @Nullable
  private CloudSupport loadSupport(@NotNull String name) {
    return switch (name) {
      case "spawnery" -> new SpawnerySupport();
      case "cloudnet4" -> new CloudNet4Support();
      case "cloudnet3" -> new CloudNet3Support();
      case "cloudnet2" -> new CloudNet2Support();
      case "cloudnet" -> detectLatestSupport();
      case "custom" -> null; // provided by another plugin via setSupport
      default -> null;
    };
  }

  private CloudSupport detectLatestSupport() {
    try {
      Class.forName("eu.cloudnetservice.driver.inject.InjectionLayer");
      return new CloudNet4Support();
    } catch (ClassNotFoundException ex) {
      return new CloudNet3Support();
    }
  }

  public void setSupport(@NotNull CloudSupport support) {
    this.support = support;
  }

  @TimerTask(status = TimerStatus.RUNNING, async = false)
  public void setIngameAndStartService() {
    if (!setIngame) return;
    if (support == null) return;

    try {
      support.setIngame();

      if (startNewService && !startedNewService)
        support.startNewService();
      startedNewService = true;
    } catch (NoClassDefFoundError ex) {
      Logger.error("Unable to set to ingame with cloud support '{}', missing dependencies", type);
    }
  }

  @TimerTask(status = TimerStatus.PAUSED, async = false)
  public void setLobby() {
    if (!resetToLobby || !startNewService) return;
    if (support == null) return;

    try {
      support.setLobby();
    } catch (NoClassDefFoundError ex) {
      Logger.error("Unable to set to lobby with cloud support '{}', missing dependencies", type);
    }
  }

  public void roundOver() {
    roundOver(support);
  }

  static void roundOver(@Nullable CloudSupport support) {
    if (support == null) return;

    try {
      support.setRoundOver();
    } catch (NoClassDefFoundError | RuntimeException ex) {
      Logger.error("Unable to tell the cloud the round is over", ex);
    }
  }

  public boolean handOverReset(@Nullable CommandSender requestedBy) {
    return handOverReset(support, requestedBy);
  }

  static boolean handOverReset(@Nullable CloudSupport support, @Nullable CommandSender requestedBy) {
    if (support == null) return false;

    try {
      return support.handOverReset(requestedBy);
    } catch (NoClassDefFoundError | RuntimeException ex) {
      Logger.error("Unable to hand the reset over to the cloud, resetting here", ex);
      return false;
    }
  }

  private boolean isEnabled() {
    return support != null;
  }

  public boolean isResetToLobby() {
    return isEnabled() && resetToLobby;
  }

  public boolean isStartNewService() {
    return isEnabled() && startNewService;
  }

}
