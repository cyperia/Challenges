package net.cyperia.challenges.plugin.management.challenges;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.CustomChallenge;
import net.cyperia.challenges.plugin.challenges.custom.TriggerActionChallenge;
import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import net.cyperia.commons.bukkit.core.BukkitModule;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ModuleChallengeLoader {

  protected final BukkitModule plugin;

  public ModuleChallengeLoader(@NotNull BukkitModule plugin) {
    this.plugin = plugin;
  }

  public final void registerWithCommand(@NotNull IChallenge challenge, @NotNull String... commandNames) {
    try {

      Challenges.getInstance().getSearchRegistry().register(challenge);
      Challenges.getInstance().getChallengeManager().register(challenge);
      Challenges.getInstance().getScheduler().register(challenge);

      // A custom challenge dispatches through CustomChallengesLoader instead; everything else built from
      // a trigger and/or an action dispatches through the preset loader, preset or not.
      if (challenge instanceof TriggerActionChallenge triggerActionChallenge && !(challenge instanceof CustomChallenge)) {
        Challenges.getInstance().getPresetChallengeLoader().listen(triggerActionChallenge);
      }

      if (challenge instanceof CommandExecutor) {
        plugin.registerCommand((CommandExecutor) challenge, commandNames);
      }
      if (challenge instanceof Listener) {
        plugin.registerListener((Listener) challenge);
      }

    } catch (Throwable ex) {
      Logger.error("Could not register challenge {}", challenge.getClass().getSimpleName(), ex);
    }
  }

  public final void register(@NotNull IChallenge challenge) {
    registerWithCommand(challenge);
  }

  /**
   * Unregisters an existing challenge and deletes its settings.
   * It does not unregister commands!
   */
  public final void unregister(@NotNull IChallenge challenge) {
    Challenges.getInstance().getChallengeManager().unregister(challenge);
    Challenges.getInstance().getScheduler().unregister(challenge);
    Challenges.getInstance().getConfigManager().getSettingsConfig().remove(challenge.getUniqueName());
    Challenges.getInstance().getConfigManager().getGamestateConfig().remove(challenge.getUniqueGamestateName());

    if (challenge instanceof TriggerActionChallenge triggerActionChallenge && !(challenge instanceof CustomChallenge)) {
      Challenges.getInstance().getPresetChallengeLoader().unlisten(triggerActionChallenge);
    }

    if (challenge instanceof Listener) {
      HandlerList.unregisterAll((Listener) challenge);
    }
  }

}
