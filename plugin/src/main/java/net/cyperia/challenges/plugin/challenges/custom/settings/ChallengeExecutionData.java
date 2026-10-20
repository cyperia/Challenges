package net.cyperia.challenges.plugin.challenges.custom.settings;

import lombok.Getter;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.impl.CancelEventAction;
import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.IChallengeTrigger;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.challenges.type.helper.SubSettingsHelper;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.util.*;

public class ChallengeExecutionData {

  @Getter
  private final IChallengeTrigger trigger;
  @Getter
  private final Map<String, List<String>> triggerData;
  @Getter
  private Entity entity;
  private Runnable cancelAction;

  public ChallengeExecutionData(IChallengeTrigger trigger) {
    this.trigger = trigger;
    this.triggerData = new HashMap<>();
  }

  public ChallengeExecutionData data(String key, String data) {
    triggerData.put(key, Collections.singletonList(data));
    return this;
  }

  public ChallengeExecutionData data(String key, List<String> data) {
    triggerData.put(key, data);
    return this;
  }

  public ChallengeExecutionData data(String key, String... data) {
    triggerData.put(key, Arrays.asList(data));
    return this;
  }

  public ChallengeExecutionData block(Material material) {
    return data(SubSettingsHelper.BLOCK, SubSettingsHelper.ANY, material.name());
  }

  public ChallengeExecutionData entityType(EntityType type) {
    return data(SubSettingsHelper.ENTITY_TYPE, SubSettingsHelper.ANY, type.name());
  }

  public ChallengeExecutionData entity(Entity entity) {
    this.entity = entity;
    return this;
  }

  public ChallengeExecutionData cancelAction(Runnable cancelAction) {
    this.cancelAction = cancelAction;
    return this;
  }

  public ChallengeExecutionData event(Cancellable event) {
    this.cancelAction = () -> event.setCancelled(true);
    return this;
  }

  public void execute() {
    if (ChallengeAPI.isStarted() && !ChallengeAPI.isWorldInUse()) {
      if (entity instanceof Player && AbstractChallenge.ignorePlayer(((Player) entity))) {
        return;
      }
      if (cancelAction != null) {
        CancelEventAction.onPreTrigger();
      }
      Challenges.getInstance().getCustomChallengesLoader().executeTrigger(this);
      Challenges.getInstance().getPresetChallengeLoader().executeTrigger(this);
      if (cancelAction != null && CancelEventAction.shouldCancel()) {
        cancelAction.run();
      }
    }
  }

}
