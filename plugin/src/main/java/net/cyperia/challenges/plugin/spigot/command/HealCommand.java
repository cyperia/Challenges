package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HealCommand extends PlayerTargetCommand {

  public HealCommand() {
    super(CommandInfo.command("heal", ChallengesPermissions.COMMAND_HEAL).asGeneric(),
      ChallengesPermissions.COMMAND_HEAL_OTHER, "command.heal.done-others");
  }

  @Override
  protected void applyTo(@NotNull Player player) {
    MessageKey.of("command.heal.done").send(player, Prefix.CHALLENGES);
    AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
    if (attribute == null) {
      player.setHealth(20);
    } else {
      player.setHealth(attribute.getValue());
    }
    player.setFoodLevel(20);
    player.setSaturation(20);
    player.setFireTicks(0);
    player.setFallDistance(0);
    player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
  }

}
