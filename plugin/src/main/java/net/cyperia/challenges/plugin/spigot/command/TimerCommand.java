package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.management.scheduler.timer.ChallengeTimer;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.common.misc.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TimerCommand implements ChallengeCommand {

  private static final List<String> DURATION_EXAMPLES = List.of("10", "30", "60", "120");
  private static final List<String> DURATION_UNITS = List.of("m", "h", "d", "w");

  @NotNull
  @Override
  public CommandInfo info() {
    return CommandInfo.command("timer", ChallengesPermissions.COMMAND_TIMER).withAliases("t");
  }

  @Override
  public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
    root.executes(nodes.run((sender, context) -> {
      if (sender instanceof Player player) {
        Challenges.getInstance().getMenuManager().openMenu(player, MenuType.TIMER, 0);
        SoundSample.OPEN.play(player);
      }
    }));

    for (String name : List.of("start", "resume")) {
      root.then(nodes.literal(name).requires(nodes.permission(ChallengesPermissions.COMMAND_TIMER_START))
        .executes(nodes.run((sender, context) -> start(sender))));
    }
    for (String name : List.of("pause", "stop")) {
      root.then(nodes.literal(name).requires(nodes.permission(ChallengesPermissions.COMMAND_TIMER_PAUSE))
        .executes(nodes.run((sender, context) -> pause(sender))));
    }
    root.then(nodes.literal("reset").requires(nodes.permission(ChallengesPermissions.COMMAND_TIMER_RESET))
      .executes(nodes.run((sender, context) -> timer().reset())));
    root.then(nodes.literal("show").requires(nodes.permission(ChallengesPermissions.COMMAND_TIMER_SHOW))
      .executes(nodes.run((sender, context) -> timer().setHidden(false))));
    root.then(nodes.literal("hide").requires(nodes.permission(ChallengesPermissions.COMMAND_TIMER_HIDE))
      .executes(nodes.run((sender, context) -> timer().setHidden(true))));

    root.then(nodes.literal("set").requires(nodes.permission(ChallengesPermissions.COMMAND_TIMER_SET))
      .executes(nodes.runAsPlayer((player, context) -> {
        MenuType.TIMER.getMenuGenerator().openTimeMenu(player);
        SoundSample.OPEN.play(player);
      }))
      .then(nodes.argument("time", StringArgumentType.greedyString())
        .suggests(durations())
        .executes(nodes.run((sender, context) -> {
          timer().setSeconds(seconds(StringArgumentType.getString(context, "time")));
          MessageKey.of("timer.messages.set").broadcast(Prefix.TIMER, timer().getFormattedTime());
        }))));

    LiteralArgumentBuilder<S> mode = nodes.literal("mode").requires(nodes.permission(ChallengesPermissions.COMMAND_TIMER_MODE));
    for (String name : List.of("up", "forward")) {
      mode.then(nodes.literal(name).executes(nodes.run((sender, context) -> timer().setCountingUp(true))));
    }
    for (String name : List.of("down", "back", "backwards")) {
      mode.then(nodes.literal(name).executes(nodes.run((sender, context) -> timer().setCountingUp(false))));
    }
    root.then(mode);

    LiteralArgumentBuilder<S> countdown = nodes.literal("countdown").requires(nodes.permission(ChallengesPermissions.COMMAND_TIMER_COUNTDOWN))
      .executes(nodes.runAsPlayer((player, context) -> {
        MenuType.TIMER.getMenuGenerator().openCountdownMenu(player);
        SoundSample.OPEN.play(player);
      }));
    for (String name : List.of("start", "resume")) {
      countdown.then(nodes.literal(name).executes(nodes.run((sender, context) -> startCountdown(sender))));
    }
    for (String name : List.of("pause", "stop")) {
      countdown.then(nodes.literal(name).executes(nodes.run((sender, context) -> pauseCountdown(sender))));
    }
    countdown.then(nodes.literal("toggle").executes(nodes.run((sender, context) -> {
      if (rejectStarted(sender)) return;
      timer().toggleCountdown();
    })));
    countdown.then(nodes.literal("set").then(nodes.argument("time", StringArgumentType.greedyString())
      .suggests(durations())
      .executes(nodes.run((sender, context) -> {
        long seconds = seconds(StringArgumentType.getString(context, "time"));
        timer().setCountdown((int) seconds);
        MessageKey.of("timer.messages.countdown.set").broadcast(Prefix.TIMER, ChallengeAPI.formatTime(seconds));
      }))));
    root.then(countdown);
  }

  public static void start(@NotNull CommandSender sender) {
    if (rejectStarted(sender)) return;
    if (ChallengeAPI.getCountdownSecondsLeft() > 0 && !ChallengeAPI.isCountdown()) {
      timer().resumeCountdown();
    } else {
      timer().resume();
    }
  }

  public static void pause(@NotNull CommandSender sender) {
    if (ChallengeAPI.isPaused()) {
      MessageKey.of("timer.messages.already-paused").send(sender, Prefix.TIMER);
      SoundSample.BASS_OFF.playIfPlayer(sender);
      return;
    }
    timer().pause(true);
  }

  private static void startCountdown(@NotNull CommandSender sender) {
    if (rejectStarted(sender)) return;
    if (ChallengeAPI.isCountdown()) {
      MessageKey.of("timer.messages.countdown.already-running").send(sender, Prefix.TIMER);
      SoundSample.BASS_OFF.playIfPlayer(sender);
      return;
    }
    timer().resumeCountdown();
  }

  private static void pauseCountdown(@NotNull CommandSender sender) {
    if (rejectStarted(sender)) return;
    if (!ChallengeAPI.isCountdown()) {
      MessageKey.of("timer.messages.countdown.not-running").send(sender, Prefix.TIMER);
      SoundSample.BASS_OFF.playIfPlayer(sender);
      return;
    }
    timer().pauseCountdown();
  }

  private static boolean rejectStarted(@NotNull CommandSender sender) {
    if (!ChallengeAPI.isStarted()) return false;
    MessageKey.of("timer.messages.already-started").send(sender, Prefix.TIMER);
    SoundSample.BASS_OFF.playIfPlayer(sender);
    return true;
  }

  static long seconds(@NotNull String input) {
    long seconds = StringUtils.parseSeconds(input);
    return seconds >= Integer.MAX_VALUE || seconds < 0 ? Integer.MAX_VALUE : seconds;
  }

  // Completes the word being typed: example amounts, or the units after a number
  @NotNull
  static <S> SuggestionProvider<S> durations() {
    return (context, builder) -> {
      String remaining = builder.getRemaining();
      int wordStart = remaining.lastIndexOf(' ') + 1;
      String word = remaining.substring(wordStart);
      SuggestionsBuilder wordBuilder = builder.createOffset(builder.getStart() + wordStart);
      if (word.isEmpty()) {
        DURATION_EXAMPLES.forEach(wordBuilder::suggest);
      } else if (StringUtils.isNumber(word)) {
        DURATION_UNITS.forEach(unit -> wordBuilder.suggest(word + unit));
      }
      return wordBuilder.buildFuture();
    };
  }

  @NotNull
  private static ChallengeTimer timer() {
    return Challenges.getInstance().getChallengeTimer();
  }

}
