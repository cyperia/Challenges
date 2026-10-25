package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.ChecklistGoal;
import net.cyperia.challenges.plugin.challenges.type.IGoal;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SharedCollectionGoal;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.management.menu.generator.impl.goal.GoalChecklistMenuGenerator;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GoalCommand implements ChallengeCommand {

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command("goal", ChallengesPermissions.COMMAND_GOAL);
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    root.executes(nodes.runAsPlayer((player, context) -> openChecklist(player)));
    root.then(nodes.literal("skip").requires(nodes.permission(ChallengesPermissions.COMMAND_GOAL_SKIP))
      .executes(nodes.run((sender, context) -> skip(sender))));
  }

  private static void openChecklist(@NotNull Player player) {
    IGoal goal = currentGoal(player);
    if (goal == null) return;
    if (!(goal instanceof ChecklistGoal checklist)) {
      MessageKey.of("command.goal.no-checklist").send(player, Prefix.CHALLENGES, goal.getName());
      return;
    }

    GoalChecklistMenuGenerator menu = new GoalChecklistMenuGenerator(checklist, player);
    // a finder's name may need a lookup to be shown properly, so the menu opens once those are done
    Challenges.getInstance().getPlayerManager().getNameProvider().load(menu.getFinders())
      .exceptionally(_ -> null)
      .thenRun(() -> Regions.nextTick(Challenges.getInstance(), player, () -> {
        if (!player.isOnline()) return;
        SoundSample.PLOP.play(player);
        menu.openMenu(player, 0);
      }));
  }

  private static void skip(@NotNull CommandSender sender) {
    IGoal goal = currentGoal(sender);
    if (goal == null) return;
    if (!(goal instanceof SharedCollectionGoal shared)) {
      MessageKey.of("command.goal.no-skip").send(sender, Prefix.CHALLENGES, goal.getName());
      return;
    }
    shared.skip(sender);
  }

  @Nullable
  private static IGoal currentGoal(@NotNull CommandSender sender) {
    IGoal goal = Challenges.getInstance().getChallengeManager().getCurrentGoal();
    if (goal == null) {
      MessageKey.of("command.goal.none").send(sender, Prefix.CHALLENGES);
    }
    return goal;
  }

}
