package net.cyperia.challenges.plugin.spigot.listener;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.content.i18n.TranslationManager;
import net.cyperia.challenges.plugin.content.i18n.impl.format.ComponentFormatter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HelpListener implements Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  public void onCommand(@NotNull PlayerCommandPreprocessEvent event) {

    String message = event.getMessage().toLowerCase();
    if (message.isEmpty()) return;

    String[] args = message.substring(1).trim().split(" ");
    String commandName = args[0];
    if (commandName.contains(":")) {
      commandName = commandName.substring(commandName.indexOf(':') + 1);
    }

    PluginCommand command = Challenges.getInstance().getCommand("help");
    if (command == null) return;
    List<String> names = new ArrayList<>(command.getAliases());
    names.add("help");

    if (!names.contains(commandName)) return;

    event.getPlayer().sendMessage(" ");
    sendPluginInfo(event.getPlayer());

  }

  // Kept in the jar instead of the language files, so a server cannot change the credits
  public static void sendPluginInfo(@NotNull CommandSender sender) {
    sendPluginSummary(sender);
    boolean german = isGerman(sender);
    send(sender, (german ? "Quellcode: " : "Source: ") + link(german, "github.com/cyperia/Challenges"));
    send(sender, "Download: " + link(german, "spigotmc.org/resources/80548", "SpigotMC")
      + " <dark_gray>•</dark_gray> " + link(german, "modrinth.com/plugin/challengesplugin", "Modrinth"));
    sendDiscord(sender, german);
  }

  public static void sendPluginSummary(@NotNull CommandSender sender) {
    boolean german = isGerman(sender);
    send(sender, (german ? "Auf diesem Server läuft " : "This server is running ")
      + "<yellow><b>Challenges</b> v" + Challenges.getInstance().getVersion() + "</yellow>");
    send(sender, (german ? "Entwickelt von " : "Made by ") + link(german, "cyperia.net")
      + " <dark_gray>(</dark_gray>" + link(german, "domiirl.svrt.dev", "DomiIRL")
      + " & " + link(german, "github.com/anweisen", "anweisen") + "<dark_gray>)</dark_gray>");
  }

  public static void sendFirstInstallWelcome(@NotNull CommandSender sender) {
    boolean german = isGerman(sender);
    send(sender, german ? "Danke, dass du <yellow><b>Challenges</b></yellow> heruntergeladen hast!"
      : "Thanks for downloading <yellow><b>Challenges</b></yellow>!");
    send(sender, (german ? "Die Sprache änderst du mit " : "You can change the language with ") + "<yellow>/setlang</yellow>");
    sendDiscord(sender, german);
  }

  private static void sendDiscord(@NotNull CommandSender sender, boolean german) {
    send(sender, (german ? "Mehr auf unserem Discord: " : "For more join our discord: ") + link(german, "discord.cyperia.net"));
  }

  static String link(boolean german, @NotNull String address) {
    return link(german, address, address);
  }

  static String link(boolean german, @NotNull String address, @NotNull String text) {
    return "<click:open_url:'https://" + address + "'><hover:show_text:'<gray>" + (german ? "Links-Klick" : "Left-Click")
      + "</gray> <dark_gray>┃</dark_gray> <gray>" + (german ? "Öffne" : "Open") + " <yellow>" + address + "</yellow></gray>'>"
      + "<yellow>" + text + "</yellow></hover></click>";
  }

  private static boolean isGerman(@NotNull CommandSender sender) {
    Locale locale = sender instanceof Player player ? TranslationManager.getLocaleOf(player) : TranslationManager.getServerLocale();
    return locale.getLanguage().equals("de");
  }

  private static void send(@NotNull CommandSender sender, @NotNull String line) {
    sender.sendMessage(Prefix.FALLBACK_COMPONENT.append(ComponentFormatter.MINI_MESSAGE.deserialize("<gray>" + line + "</gray>")));
  }

}
