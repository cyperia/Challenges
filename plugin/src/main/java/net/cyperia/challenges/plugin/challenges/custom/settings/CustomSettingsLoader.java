package net.cyperia.challenges.plugin.challenges.custom.settings;

import lombok.Getter;
import net.cyperia.challenges.catalog.CustomSetting;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.settings.action.ChallengeAction;
import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.challenges.implementation.LockedAction;
import net.cyperia.challenges.plugin.challenges.implementation.LockedTrigger;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class CustomSettingsLoader {

  @Getter
  private final Map<String, ChallengeTrigger> triggers = new LinkedHashMap<>();
  private final Map<String, ChallengeTrigger> fallbackTriggers = new LinkedHashMap<>();
  @Getter
  private final Map<String, ChallengeAction> actions = new LinkedHashMap<>();
  private final Map<String, ChallengeAction> fallbackActions = new LinkedHashMap<>();

  public void enable() {
    for (CustomSetting entry : Challenges.getInstance().getCatalog().triggers()) {
      ChallengeTrigger trigger = create(entry, ChallengeTrigger.class, LockedTrigger::new);
      if (trigger == null) continue;
      register(entry, trigger, triggers, fallbackTriggers);
      Bukkit.getPluginManager().registerEvents(trigger, Challenges.getInstance());
    }
    for (CustomSetting entry : Challenges.getInstance().getCatalog().actions()) {
      ChallengeAction action = create(entry, ChallengeAction.class, LockedAction::new);
      if (action != null) register(entry, action, actions, fallbackActions);
    }
  }

  @Nullable
  public ChallengeAction getActionByName(String name) {
    return actions.getOrDefault(name, fallbackActions.get(name));
  }

  @Nullable
  public ChallengeTrigger getTriggerByName(String name) {
    return triggers.getOrDefault(name, fallbackTriggers.get(name));
  }

  private static <T> void register(@NotNull CustomSetting entry, @NotNull T setting,
                                   @NotNull Map<String, T> byId, @NotNull Map<String, T> byFallbackId) {
    byId.put(entry.id(), setting);
    entry.fallbackIds().forEach(id -> byFallbackId.put(id, setting));
  }

  @Nullable
  private static <T extends ChallengeSetting> T create(@NotNull CustomSetting entry, @NotNull Class<T> type,
                                                       @NotNull Function<String, T> locked) {
    try {
      return type.cast(Class.forName(entry.className()).getConstructor(String.class).newInstance(entry.id()));
    } catch (ClassNotFoundException ex) {
      // The public release ships without beta classes and shows their entries locked
      if (entry.release().beta()) return locked.apply(entry.id());
      Logger.error("The class {} of {} does not exist", entry.className(), entry.id());
    } catch (ReflectiveOperationException | ClassCastException ex) {
      Logger.error("Could not create {}", entry.id(), ex);
    }
    return null;
  }

}
