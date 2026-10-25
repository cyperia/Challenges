package net.cyperia.challenges.plugin.permission;

import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Every permission node the plugin checks. {@code challenges.command.<command>} opens a command,
 * {@code .<subcommand>} a part of it that does something else, {@code .other} using it on somebody
 * else. A shortcut like {@code /start} checks the node of what it is short for. The defaults are in
 * {@code plugin.yml}.
 */
public final class ChallengesPermissions {

  private static final String COMMAND = "challenges.command.";

  /** Change settings in the menu, when {@code manage-settings-permission} is on. */
  public static final String MANAGE = "challenges.manage";

  public static final String COMMAND_HELP = COMMAND + "help";
  /** Also the hotbar item and the join hints for whoever runs the server. */
  public static final String COMMAND_CHALLENGES = COMMAND + "challenges";
  public static final String COMMAND_SEARCH = COMMAND + "search";

  public static final String COMMAND_TIMER = COMMAND + "timer";
  public static final String COMMAND_TIMER_START = COMMAND_TIMER + ".start";
  public static final String COMMAND_TIMER_PAUSE = COMMAND_TIMER + ".pause";
  public static final String COMMAND_TIMER_RESET = COMMAND_TIMER + ".reset";
  public static final String COMMAND_TIMER_SET = COMMAND_TIMER + ".set";
  public static final String COMMAND_TIMER_SHOW = COMMAND_TIMER + ".show";
  public static final String COMMAND_TIMER_HIDE = COMMAND_TIMER + ".hide";
  public static final String COMMAND_TIMER_MODE = COMMAND_TIMER + ".mode";
  public static final String COMMAND_TIMER_COUNTDOWN = COMMAND_TIMER + ".countdown";

  public static final String COMMAND_RESET = COMMAND + "reset";
  public static final String COMMAND_RESET_SETTINGS = COMMAND_RESET + ".settings";
  public static final String COMMAND_RESET_CUSTOMS = COMMAND_RESET + ".customs";

  public static final String COMMAND_DATABASE = COMMAND + "database";
  public static final String COMMAND_DATABASE_SAVE = COMMAND_DATABASE + ".save";
  public static final String COMMAND_DATABASE_LOAD = COMMAND_DATABASE + ".load";
  public static final String COMMAND_DATABASE_RESET = COMMAND_DATABASE + ".reset";

  public static final String COMMAND_GAMESTATE = COMMAND + "gamestate";
  public static final String COMMAND_GAMESTATE_RESET = COMMAND_GAMESTATE + ".reset";
  public static final String COMMAND_GAMESTATE_RELOAD = COMMAND_GAMESTATE + ".reload";

  public static final String COMMAND_HEAL = COMMAND + "heal";
  public static final String COMMAND_HEAL_OTHER = COMMAND_HEAL + ".other";
  public static final String COMMAND_FEED = COMMAND + "feed";
  public static final String COMMAND_FEED_OTHER = COMMAND_FEED + ".other";
  public static final String COMMAND_FLY = COMMAND + "fly";
  public static final String COMMAND_FLY_OTHER = COMMAND_FLY + ".other";
  public static final String COMMAND_GODMODE = COMMAND + "godmode";
  public static final String COMMAND_GODMODE_OTHER = COMMAND_GODMODE + ".other";
  public static final String COMMAND_GAMEMODE = COMMAND + "gamemode";
  public static final String COMMAND_GAMEMODE_OTHER = COMMAND_GAMEMODE + ".other";

  public static final String COMMAND_TIME = COMMAND + "time";
  public static final String COMMAND_WEATHER = COMMAND + "weather";
  public static final String COMMAND_DIFFICULTY = COMMAND + "difficulty";
  public static final String COMMAND_INVSEE = COMMAND + "invsee";
  public static final String COMMAND_VILLAGE = COMMAND + "village";
  public static final String COMMAND_WORLD = COMMAND + "world";
  public static final String COMMAND_BACK = COMMAND + "back";
  public static final String COMMAND_RESULT = COMMAND + "result";
  public static final String COMMAND_SKIPTIMER = COMMAND + "skiptimer";
  public static final String COMMAND_RANDOMIZER = COMMAND + "randomizer";
  public static final String COMMAND_SETLANGUAGE = COMMAND + "setlanguage";
  public static final String COMMAND_SETPOSITION = COMMAND + "setposition";
  public static final String COMMAND_DELPOSITION = COMMAND + "delposition";
  public static final String COMMAND_GOAL_SKIP = COMMAND + "goal.skip";

  // Player commands, granted to everybody by default
  public static final String COMMAND_GOAL = COMMAND + "goal";
  public static final String COMMAND_TEAMS = COMMAND + "teams";
  public static final String COMMAND_CANCELINPUT = COMMAND + "cancelinput";
  public static final String COMMAND_POSITION = COMMAND + "position";
  public static final String COMMAND_BACKPACK = COMMAND + "backpack";
  public static final String COMMAND_TOP = COMMAND + "top";
  public static final String COMMAND_ENDERCHEST = COMMAND + "enderchest";
  public static final String COMMAND_OPENMISSINGITEMS = COMMAND + "openmissingitems";
  public static final String COMMAND_GUESS = COMMAND + "guess";

  private ChallengesPermissions() {
  }

  @NotNull
  public static String commandGamemode(@NotNull GameMode gamemode) {
    return COMMAND_GAMEMODE + "." + gamemode.name().toLowerCase(Locale.ROOT);
  }

}
