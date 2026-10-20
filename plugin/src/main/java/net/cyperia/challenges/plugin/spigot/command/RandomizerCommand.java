package net.cyperia.challenges.plugin.spigot.command;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.abstraction.RandomizerSetting;
import net.cyperia.challenges.plugin.challenges.type.abstraction.RandomizerSetting.Pairing;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.menu.generator.impl.randomizer.RandomizerMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.randomizer.RandomizerPairingsMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.randomizer.RandomizerPairingsMenuGenerator.Entry;
import net.cyperia.challenges.plugin.management.randomizer.GameObtainSources;
import net.cyperia.challenges.plugin.management.randomizer.ObtainChain;
import net.cyperia.challenges.plugin.management.randomizer.ObtainChain.*;
import net.cyperia.challenges.plugin.utils.misc.ExperimentalUtils;
import net.cyperia.commons.bukkit.utils.item.ItemUtils;
import net.cyperia.challenges.plugin.utils.bukkit.command.Completer;
import net.cyperia.challenges.plugin.utils.bukkit.command.PlayerCommand;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

public class RandomizerCommand implements PlayerCommand, Completer {

  @Override
  public void onCommand(@NotNull Player player, @NotNull String[] args) {
    Material item = args.length == 0 ? null : Material.matchMaterial(String.join("_", args));
    if (item != null && item.isItem()) {
      sendObtainChain(player, item);
      return;
    }

    List<RandomizerSetting> randomizers = getEnabledRandomizers();
    if (randomizers.isEmpty()) {
      MessageKey.of("command.randomizer.none").send(player, Prefix.CHALLENGES);
      return;
    }

    if (args.length == 0) {
      SoundSample.PLOP.play(player);
      if (randomizers.size() == 1) {
        RandomizerPairingsMenuGenerator.of(randomizers.get(0)).openMenu(player, 0);
      } else {
        new RandomizerMenuGenerator(randomizers.toArray(new RandomizerSetting[0])).openMenu(player, 0);
      }
      return;
    }

    Component query = Component.text(String.join(" ", args));
    String id = String.join("_", args).toLowerCase(Locale.ROOT);
    Entry[] matches = randomizers.stream()
      .flatMap(randomizer -> randomizer.getPairings().stream()
        .filter(pairing -> matches(pairing, id))
        .map(pairing -> new Entry(randomizer, pairing)))
      .toArray(Entry[]::new);

    if (matches.length == 0) {
      MessageKey.of("command.randomizer.no-match").send(player, Prefix.CHALLENGES, query);
      return;
    }

    SoundSample.PLOP.play(player);
    new RandomizerPairingsMenuGenerator(query, matches).openMenu(player, 0);
  }

  private static void sendObtainChain(@NotNull Player player, @NotNull Material item) {
    ObtainChain chain = new ObtainChain(GameObtainSources.collect());
    if (chain.getSource(item) == null) {
      MessageKey.of("command.randomizer.obtain.none").send(player, Prefix.CHALLENGES, item);
      return;
    }

    MessageKey.of("command.randomizer.obtain.header").send(player, Prefix.CHALLENGES, item);
    sendStep(player, chain, item, 1, "", "", new HashSet<>());
  }

  // one line per item, its ingredients below it; an item already shown further up is not expanded again
  private static void sendStep(@NotNull Player player, @NotNull ObtainChain chain, @NotNull Material item, int count,
                               @NotNull String branch, @NotNull String indent, @NotNull Set<Material> shown) {
    String amount = count > 1 ? count + "x " : "";
    Source source = chain.getSource(item);
    if (source == null) return;
    if (!shown.add(item)) {
      MessageKey.of("command.randomizer.obtain.shown").send(player, null, branch, amount, item);
      return;
    }

    switch (source) {
      case Craft craft when craft.recipeOf() != null ->
        MessageKey.of("command.randomizer.obtain.craft-recipe-of").send(player, null, branch, amount, item, craft.recipeOf());
      case Craft craft -> MessageKey.of("command.randomizer.obtain.craft").send(player, null, branch, amount, item);
      case Smelt smelt -> MessageKey.of("command.randomizer.obtain.smelt").send(player, null, branch, amount, item);
      case Break broken -> MessageKey.of("command.randomizer.obtain.break").send(player, null, branch, amount, item, broken.block());
      case Kill kill when kill.spawnsInsteadOf() != null ->
        MessageKey.of("command.randomizer.obtain.kill-instead").send(player, null, branch, amount, item, kill.mob(), kill.spawnsInsteadOf());
      case Kill kill -> MessageKey.of("command.randomizer.obtain.kill").send(player, null, branch, amount, item, kill.mob());
    }

    List<Ingredient> ingredients = source.ingredients();
    for (int i = 0; i < ingredients.size(); i++) {
      boolean last = i == ingredients.size() - 1;
      Ingredient ingredient = ingredients.get(i);
      sendStep(player, chain, chain.getCheapestOption(ingredient), ingredient.count(),
        indent + (last ? "└ " : "├ "), indent + (last ? "  " : "│ "), shown);
    }
  }

  // only the english ids are matched, so a translated item name finds nothing
  static boolean matches(@NotNull Pairing pairing, @NotNull String id) {
    return names(pairing).anyMatch(name -> name.contains(id));
  }

  @NotNull
  private static Stream<String> names(@NotNull Pairing pairing) {
    return Stream.concat(Stream.of(pairing.from()), pairing.to().stream()).map(value -> value.name().toLowerCase(Locale.ROOT));
  }

  @NotNull
  private static List<RandomizerSetting> getEnabledRandomizers() {
    return Challenges.getInstance().getChallengeManager().getChallenges().stream()
      .filter(challenge -> challenge instanceof RandomizerSetting randomizer && randomizer.isEnabled())
      .map(challenge -> (RandomizerSetting) challenge)
      .toList();
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
    if (args.length != 1) return List.of();
    Stream<String> items = Arrays.stream(ExperimentalUtils.getMaterials())
      .filter(material -> material.isItem() && ItemUtils.isObtainableInSurvival(material))
      .map(material -> material.name().toLowerCase(Locale.ROOT));
    Stream<String> pairings = getEnabledRandomizers().stream()
      .flatMap(randomizer -> randomizer.getPairings().stream())
      .flatMap(RandomizerCommand::names);
    return Stream.concat(items, pairings)
      .distinct()
      .sorted()
      .toList();
  }

}
