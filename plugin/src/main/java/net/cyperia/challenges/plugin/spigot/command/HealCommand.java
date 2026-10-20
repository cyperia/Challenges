package net.cyperia.challenges.plugin.spigot.command;

import org.bukkit.attribute.Attribute;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.bukkit.command.Completer;
import net.cyperia.challenges.plugin.utils.bukkit.command.SenderCommand;
import net.cyperia.challenges.plugin.utils.misc.CommandHelper;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HealCommand implements SenderCommand, Completer {

  @Override
  public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {

    List<Player> targets = new ArrayList<>();

    if (args.length > 0) {
      targets.addAll(CommandHelper.getPlayers(sender, args[0]));

    } else if (sender instanceof Player) {
      targets.add((Player) sender);
    }

    if (targets.isEmpty()) {
      MessageKey.of("command.no-target").send(sender, Prefix.CHALLENGES);
      return;
    }

    boolean otherPlayers = false;
    for (Player player : targets) {
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

      if (player != sender)
        otherPlayers = true;
    }

    if (otherPlayers)
      MessageKey.of("command.heal.done-others").send(sender, Prefix.CHALLENGES, targets.size());
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
    return CommandHelper.getCompletions(sender);
  }

}
