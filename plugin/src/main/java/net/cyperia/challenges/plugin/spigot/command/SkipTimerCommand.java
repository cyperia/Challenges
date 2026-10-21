package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import net.cyperia.challenges.plugin.challenges.type.SkippableTimer;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class SkipTimerCommand implements ChallengeCommand {

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command("skiptimer", ChallengesPermissions.COMMAND_SKIPTIMER);
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    root
      .executes(nodes.run((sender, context) -> skip(sender, null)))
      .then(nodes.argument("challenge", StringArgumentType.word())
        .suggests(nodes.suggest(sender -> skippable().map(IChallenge::getUniqueName).toList()))
        .executes(nodes.run((sender, context) -> skip(sender, StringArgumentType.getString(context, "challenge")))));
  }

  private static void skip(@NotNull CommandSender sender, @Nullable String name) {
    List<SkippableTimer> challenges = skippable()
      .filter(challenge -> name == null || challenge.getUniqueName().equalsIgnoreCase(name))
      .filter(SkippableTimer::skipTimer)
      .toList();

    if (challenges.isEmpty()) {
      MessageKey.of("command.skip-timer.none").send(sender, Prefix.CHALLENGES);
    } else {
      LocalizableMessage[] names = challenges.stream().map(IChallenge::getName).toArray(LocalizableMessage[]::new);
      MessageKey.of("command.skip-timer.done").broadcast(Prefix.CHALLENGES, LocalizableMessage.joinArray(names));
    }
  }

  @NotNull
  private static Stream<SkippableTimer> skippable() {
    return Challenges.getInstance().getChallengeManager().getChallenges().stream()
      .filter(IChallenge::isEnabled)
      .filter(challenge -> challenge instanceof SkippableTimer)
      .map(challenge -> (SkippableTimer) challenge);
  }

}
