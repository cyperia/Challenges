package net.cyperia.challenges.plugin.spigot.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestion;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.cyperia.challenges.plugin.management.command.TestSenders.senderWith;
import static org.junit.jupiter.api.Assertions.*;

class CommandTreesTest {

  private final CommandDispatcher<CommandSender> dispatcher = new CommandDispatcher<>();

  private CommandTreesTest register(ChallengeCommand... commands) {
    CommandNodes<CommandSender> nodes = new CommandNodes<>(sender -> sender);
    for (ChallengeCommand command : commands) {
      LiteralArgumentBuilder<CommandSender> root = nodes.literal(command.info().getLiteral());
      nodes.define(command, root);
      dispatcher.register(root);
    }
    return this;
  }


  private boolean runs(String input, CommandSender sender) {
    ParseResults<CommandSender> parse = dispatcher.parse(input, sender);
    return !parse.getReader().canRead() && parse.getExceptions().isEmpty() && parse.getContext().getCommand() != null;
  }

  private List<String> suggest(String input, CommandSender sender) {
    return dispatcher.getCompletionSuggestions(dispatcher.parse(input, sender)).join()
      .getList().stream().map(Suggestion::getText).toList();
  }

  @Test
  void timerSubcommandsNeedTheirOwnNode() {
    register(new TimerCommand());
    CommandSender starter = senderWith(ChallengesPermissions.COMMAND_TIMER, ChallengesPermissions.COMMAND_TIMER_START);

    assertTrue(runs("timer", starter));
    assertTrue(runs("timer start", starter));
    assertTrue(runs("timer resume", starter));
    assertFalse(runs("timer pause", starter));
    assertFalse(runs("timer set 10m", starter));
    assertFalse(runs("timer start", senderWith(ChallengesPermissions.COMMAND_TIMER_START)));
  }

  @Test
  void timerDurationsSuggestExamplesThenUnits() {
    register(new TimerCommand());
    CommandSender sender = senderWith(ChallengesPermissions.COMMAND_TIMER, ChallengesPermissions.COMMAND_TIMER_SET);

    assertEquals(List.of("10", "120", "30", "60"), suggest("timer set ", sender));
    assertEquals(List.of("1h 5d", "1h 5h", "1h 5m", "1h 5w"), suggest("timer set 1h 5", sender).stream().map(unit -> "1h " + unit).toList());
  }

  @Test
  void eachGameModeNeedsItsNodeAndOthersNeedTheOtherNode() {
    register(new GamemodeCommand(), new GamemodeShortcutCommand("gmc", GameMode.CREATIVE));
    CommandSender creative = senderWith(ChallengesPermissions.COMMAND_GAMEMODE, ChallengesPermissions.commandGamemode(GameMode.CREATIVE));

    assertTrue(runs("gamemode creative", creative));
    assertTrue(runs("gamemode 1", creative));
    assertTrue(runs("gamemode c", creative));
    assertFalse(runs("gamemode survival", creative));
    assertFalse(runs("gamemode c Steve", creative));
    assertTrue(runs("gmc", creative));
    assertFalse(runs("gmc", senderWith(ChallengesPermissions.commandGamemode(GameMode.CREATIVE))));
    assertTrue(runs("gmc @a", senderWith(ChallengesPermissions.COMMAND_GAMEMODE, ChallengesPermissions.commandGamemode(GameMode.CREATIVE), ChallengesPermissions.COMMAND_GAMEMODE_OTHER)));
  }

  @Test
  void healReachesOthersOnlyWithTheOtherNode() {
    register(new HealCommand());

    assertTrue(runs("heal", senderWith(ChallengesPermissions.COMMAND_HEAL)));
    assertFalse(runs("heal Steve", senderWith(ChallengesPermissions.COMMAND_HEAL)));
    assertTrue(runs("heal @a", senderWith(ChallengesPermissions.COMMAND_HEAL, ChallengesPermissions.COMMAND_HEAL_OTHER)));
  }

  @Test
  void shortcutsUseTheNodeOfWhatTheyAreShortFor() {
    register(TimeCommand.shortcut("night"), WeatherCommand.shortcut("rain", WeatherCommand.Weather.RAIN));

    assertTrue(runs("night", senderWith(ChallengesPermissions.COMMAND_TIME)));
    assertTrue(runs("rain", senderWith(ChallengesPermissions.COMMAND_WEATHER)));
    assertFalse(runs("rain", senderWith(ChallengesPermissions.COMMAND_TIME)));
  }

  @Test
  void timeTakesNamedTimesAndTicks() {
    register(new TimeCommand());
    CommandSender sender = senderWith(ChallengesPermissions.COMMAND_TIME);

    assertTrue(runs("time noon", sender));
    assertTrue(runs("time set midnight", sender));
    assertTrue(runs("time set 1200", sender));
    assertTrue(runs("time add 100", sender));
    assertFalse(runs("time set -5", sender));
    assertFalse(runs("time set dusk", sender));
  }

  @Test
  void genericCommandsAreTheOnesANetworkToolboxProvides() {
    assertTrue(new HealCommand().info().isGeneric());
    assertTrue(new GamemodeCommand().info().isGeneric());
    assertTrue(new TimeCommand().info().isGeneric());
    assertTrue(HelpCommand.INFO.isGeneric());
    assertFalse(new TimerCommand().info().isGeneric());
    assertFalse(new BackCommand().info().isGeneric());
  }

  @Test
  void goalSkipNeedsItsOwnPermission() {
    register(new GoalCommand());

    assertTrue(runs("goal", senderWith(ChallengesPermissions.COMMAND_GOAL)));
    assertFalse(runs("goal skip", senderWith(ChallengesPermissions.COMMAND_GOAL)));
    assertTrue(runs("goal skip", senderWith(ChallengesPermissions.COMMAND_GOAL, ChallengesPermissions.COMMAND_GOAL_SKIP)));
  }

  @Test
  void resetConfirmWorksWithAndWithoutTheConfirmStep() {
    CommandSender sender = senderWith(ChallengesPermissions.COMMAND_RESET);

    register(new ResetCommand(false, true));
    assertTrue(runs("reset", sender));
    assertTrue(runs("reset confirm", sender));
    assertTrue(runs("reset glacier", sender));
  }

  @Test
  void resetWithConfirmStepHintsForASeed() {
    CommandSender sender = senderWith(ChallengesPermissions.COMMAND_RESET);

    register(new ResetCommand(true, true));
    assertTrue(runs("reset", sender));
    assertTrue(runs("reset confirm 123", sender));
    assertTrue(runs("reset glacier", sender));
  }

  @Test
  void aTextSeedIsHashedLikeTheVanillaSeedField() {
    assertEquals(123L, ResetCommand.parseSeed("123"));
    assertEquals(-42L, ResetCommand.parseSeed(" -42 "));
    assertEquals("glacier".hashCode(), ResetCommand.parseSeed("glacier"));
  }

}
