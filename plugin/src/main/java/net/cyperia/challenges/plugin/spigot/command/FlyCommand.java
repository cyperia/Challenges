package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlyCommand extends PlayerTargetCommand {

  public FlyCommand() {
    super(CommandInfo.command("fly", ChallengesPermissions.COMMAND_FLY).asGeneric(),
      ChallengesPermissions.COMMAND_FLY_OTHER, "command.fly.done-others");
  }

  @Override
  protected void applyTo(@NotNull Player target) {
    if (target.getAllowFlight()) {
      MessageKey.of("command.fly.disabled").send(target, Prefix.CHALLENGES);
    } else {
      MessageKey.of("command.fly.enabled").send(target, Prefix.CHALLENGES);
    }
    target.setAllowFlight(!target.getAllowFlight());
  }

}
