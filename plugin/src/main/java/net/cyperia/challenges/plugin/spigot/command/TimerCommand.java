package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.utils.bukkit.command.Completer;
import net.cyperia.challenges.plugin.utils.bukkit.command.SenderCommand;
import net.cyperia.challenges.plugin.utils.misc.Utils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.common.misc.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TimerCommand implements SenderCommand, Completer {

  public static final String MANAGE_PERMISSION = "challenges.timer.manage";

  private static boolean isResume(@NotNull String[] args) {
    return args.length == 1 && ("start".equalsIgnoreCase(args[0]) || "resume".equalsIgnoreCase(args[0]));
  }

  @Override
  public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {

    if (!isResume(args) && !sender.hasPermission(MANAGE_PERMISSION)) {
      MessageKey.of("command.no-permission").send(sender, Prefix.TIMER);
      SoundSample.BASS_OFF.playIfPlayer(sender);
      return;
    }

    if (args.length == 0) {
      if (sender instanceof Player player) {
        Challenges.getInstance().getMenuManager().openMenu(player, MenuType.TIMER, 0);
        SoundSample.OPEN.play(player);
      }
      return;
    }

    switch (args[0].toLowerCase()) {
      case "resume", "start" -> {
        if (ChallengeAPI.isStarted()) {
          MessageKey.of("timer.messages.already-started").send(sender, Prefix.TIMER);
          SoundSample.BASS_OFF.playIfPlayer(sender);
          break;
        }
        if (ChallengeAPI.getCountdownSecondsLeft() > 0 && !ChallengeAPI.isCountdown()) {
          Challenges.getInstance().getChallengeTimer().resumeCountdown();
        } else {
          Challenges.getInstance().getChallengeTimer().resume();
        }
      }
      case "stop", "pause" -> {
        if (ChallengeAPI.isPaused()) {
          MessageKey.of("timer.messages.already-paused").send(sender, Prefix.TIMER);
          SoundSample.BASS_OFF.playIfPlayer(sender);
          break;
        }
        Challenges.getInstance().getChallengeTimer().pause(true);
      }
      case "reset" -> Challenges.getInstance().getChallengeTimer().reset();
      case "set" -> {
        if (args.length < 2) {
          if (sender instanceof Player player) {
            MenuType.TIMER.getMenuGenerator().openTimeMenu(player);
            SoundSample.OPEN.play(player);
            return;
          }
        }

        long seconds = StringUtils.parseSeconds(String.join(" ", args));
        if (seconds >= Integer.MAX_VALUE || seconds < 0) {
          seconds = Integer.MAX_VALUE;
        }
        Challenges.getInstance().getChallengeTimer().setSeconds(seconds);
        MessageKey.of("timer.messages.set").broadcast(Prefix.TIMER, Challenges.getInstance().getChallengeTimer().getFormattedTime());
      }
      case "show" -> Challenges.getInstance().getChallengeTimer().setHidden(false);
      case "hide" -> Challenges.getInstance().getChallengeTimer().setHidden(true);
      case "mode" -> {
        if (args.length != 2) {
          MessageKey.of("command.syntax").send(sender, Prefix.TIMER, "timer mode <up/down>");
          break;
        }
        switch (args[1].toLowerCase()) {
          case "up", "forward" -> Challenges.getInstance().getChallengeTimer().setCountingUp(true);
          case "down", "back", "backwards" -> Challenges.getInstance().getChallengeTimer().setCountingUp(false);
          default -> MessageKey.of("command.syntax").send(sender, Prefix.TIMER, "timer mode <up/down>");
        }
      }
      case "countdown" -> {
        if (args.length < 2 && !(sender instanceof Player)) {
          MessageKey.of("command.syntax").send(sender, Prefix.TIMER, "timer countdown <start/pause/set>");
          break;
        }
        if (args.length < 2) {
          MenuType.TIMER.getMenuGenerator().openCountdownMenu((Player) sender);
          SoundSample.OPEN.playIfPlayer(sender);
          return;
        }

        switch (args[1]) {
          case "resume", "start" -> {
            if (ChallengeAPI.isStarted()) {
              MessageKey.of("timer.messages.already-started").send(sender, Prefix.TIMER);
              SoundSample.BASS_OFF.playIfPlayer(sender);
              break;
            }
            if (ChallengeAPI.isCountdown()) {
              MessageKey.of("timer.messages.countdown.already-running").send(sender, Prefix.TIMER);
              SoundSample.BASS_OFF.playIfPlayer(sender);
              break;
            }
            Challenges.getInstance().getChallengeTimer().resumeCountdown();
          }
          case "stop", "pause" -> {
            if (ChallengeAPI.isStarted()) {
              MessageKey.of("timer.messages.already-started").send(sender, Prefix.TIMER);
              SoundSample.BASS_OFF.playIfPlayer(sender);
              break;
            }
            if (!ChallengeAPI.isCountdown()) {
              MessageKey.of("timer.messages.countdown.not-running").send(sender, Prefix.TIMER);
              SoundSample.BASS_OFF.playIfPlayer(sender);
              break;
            }
            Challenges.getInstance().getChallengeTimer().pauseCountdown();
          }
          case "toggle" -> {
            if (ChallengeAPI.isStarted()) {
              MessageKey.of("timer.messages.already-started").send(sender, Prefix.TIMER);
              SoundSample.BASS_OFF.playIfPlayer(sender);
              break;
            }
            Challenges.getInstance().getChallengeTimer().toggleCountdown();
          }
          case "set" -> {
            long seconds = StringUtils.parseSeconds(String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
            if (seconds >= Integer.MAX_VALUE || seconds < 0) {
              seconds = Integer.MAX_VALUE;
            }
            Challenges.getInstance().getChallengeTimer().setCountdown((int) seconds);
            MessageKey.of("timer.messages.countdown.set").broadcast(Prefix.TIMER, ChallengeAPI.formatTime(seconds));
          }
          default -> MessageKey.of("command.syntax").send(sender, Prefix.TIMER, "timer countdown <start/pause/set>");
        }
      }
      default -> MessageKey.of("command.syntax").send(sender, Prefix.TIMER, "timer <resume/pause/reset/set/mode>");
    }

  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length == 0) return new ArrayList<>();
    if (!sender.hasPermission(MANAGE_PERMISSION))
      return args.length == 1 ? Utils.filterRecommendations(args[0], "resume", "start") : new ArrayList<>();
    String last = args[args.length - 1];
    if (args.length == 1) {
      return Utils.filterRecommendations(args[0], "resume", "pause", "stop", "start", "reset", "show", "hide", "mode", "set", "countdown");
    } else if (args.length == 2 && "mode".equalsIgnoreCase(args[0])) {
      return Utils.filterRecommendations(args[1], "up", "down");
    } else if ("set".equalsIgnoreCase(args[0])) {
      return StringUtils.isNumber(last) ? Utils.filterRecommendations(last, last + "m", last + "h", last + "d", last + "w") : last.isEmpty() ? Arrays.asList("10", "30", "60", "120") : Collections.singletonList(last);
    } else if ("countdown".equals(args[0])) {
      if (args.length == 2) {
        return Utils.filterRecommendations(args[1], "resume", "pause", "stop", "start", "toggle", "set");
      } else if ("set".equals(args[1])) {
        return StringUtils.isNumber(last) ? Utils.filterRecommendations(last, last + "m", last + "h", last + "d", last + "w") : last.isEmpty() ? Arrays.asList("10", "30", "60", "120") : Collections.singletonList(last);
      }
    }
    return new ArrayList<>();
  }

}
