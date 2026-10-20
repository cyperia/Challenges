package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Modifier;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.utils.bukkit.command.SenderCommand;
import net.cyperia.commons.common.config.Document;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DifficultySetting extends Modifier implements SenderCommand, TabCompleter {

  public DifficultySetting() {
    super(0, 3, 2);
    setValueUnit(ValueUnit.named(value -> LocalizableMessage.wrap(getDifficultyComponent(value))));
  }

  @NotNull
  @Override
  public ItemStack getSettingsItemPreset() {
    return switch (getDifficultyByValue(getValue())) {
      case PEACEFUL -> new ItemStack(Material.GREEN_DYE);
      case EASY -> new ItemStack(Material.LIME_DYE);
      case NORMAL -> new ItemStack(Material.ORANGE_DYE);
      case HARD -> new ItemStack(Material.RED_DYE);
    };
  }

  @Override
  protected void onValueChange() {
    setDifficulty(getDifficultyByValue(getValue()));
  }

  private void setDifficulty(Difficulty difficulty) {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:difficulty " + difficulty.name().toLowerCase());
    for (World world : Bukkit.getWorlds()) {
      world.setDifficulty(difficulty);
    }
  }

  @NotNull
  private Difficulty getCurrentDifficulty() {
    return Bukkit.getWorlds().isEmpty() ? Difficulty.NORMAL : ChallengeAPI.getGameWorld(Environment.NORMAL)
      .getDifficulty();
  }

  @NotNull
  private Difficulty getDifficultyByValue(int value) {
    Difficulty difficulty = Difficulty.values()[value];
    return difficulty == null ? Difficulty.NORMAL : difficulty;
  }

  @Override
  public void loadSettings(@NotNull Document document) {
    if (!document.contains("value"))
      setValue(getCurrentDifficulty().ordinal());

    super.loadSettings(document);
  }

  @Override
  public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) throws Exception {

    if (args.length == 0) {
      getChallengeMessageKey("command-current").send(sender, Prefix.CHALLENGES, getDifficultyComponent(getValue()));
      return;
    }

    int difficulty = getDifficultyValue(args[0]);
    if (difficulty == -1) {
      MessageKey.of("command.syntax").send(sender, Prefix.CHALLENGES, "difficulty <difficulty>");
      return;
    }

    setValue(difficulty);
    getChallengeMessageKey("command-set").broadcast(Prefix.CHALLENGES, getDifficultyComponent(difficulty));
    playValueChangeTitle();
  }

  @NotNull
  public Component getDifficultyComponent(int value) {
    Difficulty difficulty = getDifficultyByValue(value);
    return Component.translatable(difficulty).color(getDifficultyColor(difficulty));
  }

  @NotNull
  private TextColor getDifficultyColor(@NotNull Difficulty difficulty) {
    return switch (difficulty) {
      case PEACEFUL -> NamedTextColor.DARK_GREEN;
      case EASY -> NamedTextColor.GREEN;
      case NORMAL -> NamedTextColor.GOLD;
      case HARD -> NamedTextColor.RED;
    };
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
    if (args.length > 1) return new ArrayList<>();
    return Arrays.asList("peaceful", "easy", "normal", "hard");
  }

  private int getDifficultyValue(@NotNull String input) {

    switch (input.toLowerCase()) {
      case "peaceful":
        return 0;
      case "easy":
        return 1;
      case "normal":
        return 2;
      case "hard":
        return 3;
    }

    try {
      int value = Integer.parseInt(input);
      if (value < 0 || value > 3) return -1;
      return value;
    } catch (Exception ex) {
      return -1;
    }

  }

}
