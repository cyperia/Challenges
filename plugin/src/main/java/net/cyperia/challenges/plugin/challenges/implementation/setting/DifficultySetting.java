package net.cyperia.challenges.plugin.challenges.implementation.setting;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Modifier;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandNodes;
import net.cyperia.challenges.plugin.management.command.CommandProvider;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import net.cyperia.commons.common.config.Document;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class DifficultySetting extends Modifier implements CommandProvider {

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
    Regions.global(Challenges.getInstance(), () -> {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:difficulty " + difficulty.name().toLowerCase());
      for (World world : Bukkit.getWorlds()) {
        world.setDifficulty(difficulty);
      }
    });
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

  @NotNull
  @Override
  public List<ChallengeCommand> getCommands() {
    return List.of(new DifficultyCommand());
  }

  private void setDifficultyByCommand(int difficulty) {
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

  private class DifficultyCommand implements ChallengeCommand {

    @NotNull
    @Override
    public CommandInfo info() {
      return CommandInfo.command("difficulty", ChallengesPermissions.COMMAND_DIFFICULTY);
    }

    @Override
    public <S> void define(@NotNull LiteralArgumentBuilder<S> root, @NotNull CommandNodes<S> nodes) {
      root.executes(nodes.run((sender, context) ->
        getChallengeMessageKey("command-current").send(sender, Prefix.CHALLENGES, getDifficultyComponent(getValue()))));
      for (Difficulty difficulty : Difficulty.values()) {
        for (String name : List.of(difficulty.name().toLowerCase(Locale.ROOT), String.valueOf(difficulty.ordinal()))) {
          root.then(nodes.literal(name).executes(nodes.run((sender, context) -> setDifficultyByCommand(difficulty.ordinal()))));
        }
      }
    }

  }

}
