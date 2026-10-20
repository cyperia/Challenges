package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import net.cyperia.challenges.plugin.challenges.type.SkippableTimer;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.bukkit.command.Completer;
import net.cyperia.challenges.plugin.utils.bukkit.command.SenderCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class SkipTimerCommand implements SenderCommand, Completer {

  @Override
  public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) throws Exception {
    List<SkippableTimer> challenges = skippable()
      .filter(challenge -> args.length == 0 || challenge.getUniqueName().equalsIgnoreCase(args[0]))
      .filter(SkippableTimer::skipTimer)
      .toList();

    if (challenges.isEmpty()) {
      MessageKey.of("command.skip-timer.none").send(sender, Prefix.CHALLENGES);
    } else {
      LocalizableMessage[] names = challenges.stream().map(IChallenge::getName).toArray(LocalizableMessage[]::new);
      MessageKey.of("command.skip-timer.done").broadcast(Prefix.CHALLENGES, LocalizableMessage.joinArray(names));
    }
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length != 1) return Collections.emptyList();
    return skippable().map(IChallenge::getUniqueName)
      .filter(name -> name.startsWith(args[0].toLowerCase()))
      .toList();
  }

  @NotNull
  private static Stream<SkippableTimer> skippable() {
    return Challenges.getInstance().getChallengeManager().getChallenges().stream()
      .filter(IChallenge::isEnabled)
      .filter(challenge -> challenge instanceof SkippableTimer)
      .map(challenge -> (SkippableTimer) challenge);
  }

}
