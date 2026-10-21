package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FeedCommand extends PlayerTargetCommand {

  public FeedCommand() {
    super(CommandInfo.command("feed", ChallengesPermissions.COMMAND_FEED).asGeneric(),
      ChallengesPermissions.COMMAND_FEED_OTHER, "command.feed.done-others");
  }

  @Override
  protected void applyTo(@NotNull Player target) {
    target.setFoodLevel(20);
    MessageKey.of("command.feed.done").send(target, Prefix.CHALLENGES);
  }

}
