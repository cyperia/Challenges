package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.ChecklistGoal;
import net.cyperia.challenges.plugin.challenges.type.IGoal;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.menu.generator.impl.goal.GoalChecklistMenuGenerator;
import net.cyperia.challenges.plugin.utils.bukkit.command.PlayerCommand;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GoalCommand implements PlayerCommand {

  @Override
  public void onCommand(@NotNull Player player, @NotNull String[] args) {
    IGoal goal = Challenges.getInstance().getChallengeManager().getCurrentGoal();
    if (goal == null) {
      MessageKey.of("command.goal.none").send(player, Prefix.CHALLENGES);
      return;
    }
    if (!(goal instanceof ChecklistGoal checklist)) {
      MessageKey.of("command.goal.no-checklist").send(player, Prefix.CHALLENGES, goal.getName());
      return;
    }

    GoalChecklistMenuGenerator menu = new GoalChecklistMenuGenerator(checklist, player);
    // a finder's name may need a lookup to be shown properly, so the menu opens once those are done
    Challenges.getInstance().getPlayerManager().getNameProvider().load(menu.getFinders())
      .exceptionally(_ -> null)
      .thenRun(() -> Bukkit.getScheduler().runTask(Challenges.getInstance(), () -> {
        if (!player.isOnline()) return;
        SoundSample.PLOP.play(player);
        menu.openMenu(player, 0);
      }));
  }

}
