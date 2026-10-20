package net.cyperia.commons.bukkit.core;

import net.cyperia.commons.bukkit.utils.misc.MinecraftVersion;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.version.Version;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class RequirementsChecker {

  private static final String PAPER_DOWNLOAD = "https://papermc.io/downloads";
  private static final String DISCORD = "https://discord.cyperia.net";

  private final BukkitModule module;

  public RequirementsChecker(@NotNull BukkitModule module) {
    this.module = module;
  }

  public void checkExceptionally(@NotNull Document requirements) throws IllegalStateException {
    if (requirements.getBoolean("paper")) requirePaper();
  }

  public boolean check(@NotNull Document requirements) {
    try {
      checkExceptionally(requirements);
      return true;
    } catch (IllegalStateException ex) {
      return false;
    }
  }

  private void requirePaper() {
    try {
      Class.forName("com.destroystokyo.paper.VersionHistoryManager");
    } catch (Throwable ex) {
      log("");
      log("============================== {} ==============================", module.getName());
      log("");
      log("This server runs {}, but the plugin needs a Paper based server to run (Your server: {})", module.getName(), Bukkit.getVersion());
      log("How to fix it:");
      log(" 1. Download Paper, or a fork of it like Purpur: {}", PAPER_DOWNLOAD);
      log(" 2. Replace the server jar with it and restart the server");
      log("Need help? Join our Discord: {}", DISCORD);
      log("");
      log("============================== {} ==============================", module.getName());
      log("");
      throw new IllegalStateException();
    }
  }

  // The same guide for players, without Adventure: a server that is not Paper based does not have it
  public void sendPaperGuide(@NotNull Player player) {
    String prefix = "§8§l┃§r §6" + module.getName() + " §8┃ ";
    player.sendMessage(prefix + "§cThis server runs §e" + module.getName() + "§c, but the plugin needs a §ePaper§c based server to run");
    player.sendMessage(prefix + "§7How to fix it:");
    sendLink(player, prefix + "§7 1. Download §ePaper§7: §e", PAPER_DOWNLOAD);
    player.sendMessage(prefix + "§7 2. Replace the server jar with it and restart the server");
    sendLink(player, prefix + "§7Need help? Join our Discord: §e", DISCORD);
  }

  private static void sendLink(@NotNull Player player, @NotNull String text, @NotNull String url) {
    String shown = text + url.substring("https://".length());
    BaseComponent[] parts = TextComponent.fromLegacyText(shown);
    ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
    for (BaseComponent part : parts) part.setClickEvent(click);
    try {
      player.spigot().sendMessage(parts);
    } catch (UnsupportedOperationException ex) {
      // CraftBukkit has no Spigot chat API: the link is not clickable there, but still readable
      player.sendMessage(shown);
    }
  }

  // Logs a warning when the server runs another Minecraft version than the one required; the plugin starts anyway
  public boolean checkVersion(@NotNull Document requirements) {
    if (!requirements.contains("version")) return true;
    Version required = requirements.getVersion("version");
    Version current = MinecraftVersion.current();
    if (supports(required, current)) return true;

    log("");
    log("============================== {} ==============================", module.getName());
    log("");
    log("This plugin is made for Minecraft {} (You have: {})", required.format(), current.format());
    log("It still starts, but parts of it may not work and errors are to be expected!");
    log("");
    log("Paper Download: https://papermc.io/downloads");
    log("");
    log("============================== {} ==============================", module.getName());
    log("");
    return false;
  }

  // Only the required major.minor line is supported: older and newer releases both get the warning
  static boolean supports(@NotNull Version required, @NotNull Version current) {
    return current.getMajor() == required.getMajor() && current.getMinor() == required.getMinor();
  }

  private void log(@NotNull String line, @NotNull Object... args) {
    module.getILogger().error(line, args);
  }

}
