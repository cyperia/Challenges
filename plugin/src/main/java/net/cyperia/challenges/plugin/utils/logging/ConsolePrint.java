package net.cyperia.challenges.plugin.utils.logging;

import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ConsolePrint {

  private ConsolePrint() {
  }

  public static void unknownLanguage(@Nullable String language) {
    log("");
    log("Found unknown language '" + language + "'!");
    log("Defaulting to en (English)");
    log("");
  }

  public static void unableToGetLanguages() {
    log("");
    log("No languages found to load");
    log("Is the server / plugin set up correctly?");
    log("");
  }

  public static void alreadyExecutingContentLoader() {
    log("");
    log("Cannot load contents; Already loading contents?");
    log("Are you reloading too fast?");
    log("");
  }

  public static void accessBlocked() {
    log(" ");
    log(" ");
    log("██████╗░██╗░░░░░░█████╗░░█████╗░██╗░░██╗███████╗██████╗░");
    log("██╔══██╗██║░░░░░██╔══██╗██╔══██╗██║░██╔╝██╔════╝██╔══██╗");
    log("██████╦╝██║░░░░░██║░░██║██║░░╚═╝█████═╝░█████╗░░██║░░██║");
    log("██╔══██╗██║░░░░░██║░░██║██║░░██╗██╔═██╗░██╔══╝░░██║░░██║");
    log("██████╦╝███████╗╚█████╔╝╚█████╔╝██║░╚██╗███████╗██████╔╝");
    log("╚═════╝░╚══════╝░╚════╝░░╚════╝░╚═╝░░╚═╝╚══════╝╚═════╝░");
    log(" ");
    log("Your server's access is blocked.");
    log("For more information and support visit our discord server: https://discord.cyperia.net");
    log(" ");
    log(" ");
  }

  public static void noMongoDependencies() {
    log("");
    log("=============================================================================================");
    log("");
    log("Cannot use MongoDB as database without the Challenges-MongoConnector dependency plugin.");
    log("Please add this plugin in order to be able to access a mongodb database.");
    log("");
    log("=============================================================================================");
    log("");
  }

  private static void log(@NotNull String message) {
    Logger.error(message);
  }

}
