package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Modifier;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.content.i18n.TranslationManager;
import net.cyperia.challenges.plugin.content.i18n.hook.LanguageProvider;
import net.cyperia.challenges.plugin.content.loader.LanguageLoader;
import net.cyperia.challenges.plugin.management.menu.info.ChallengeMenuClickInfo;
import net.cyperia.challenges.plugin.utils.bukkit.command.Completer;
import net.cyperia.challenges.plugin.utils.bukkit.command.SenderCommand;
import net.cyperia.challenges.plugin.utils.misc.Utils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.item.ItemBuilder;
import net.cyperia.commons.common.config.Document;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class LanguageSetting extends Modifier implements SenderCommand, Completer {

  public static final int ENGLISH = 1;
  public static final int GERMAN = 2;

  // TODO centralize registry using LanguageLoader
  private static final Map<String, Integer> TAG_TO_VALUE = Map.of("en", ENGLISH, "de", GERMAN);
  private static final Map<Integer, String> VALUE_TO_TAG = Map.of(ENGLISH, "en", GERMAN, "de");

  public static final String GERMAN_SKULL = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWU3ODk5YjQ4MDY4NTg2OTdlMjgzZjA4NGQ5MTczZmU0ODc4ODY0NTM3NzQ2MjZiMjRiZDhjZmVjYzc3YjNmIn19fQ",
    ENGLISH_SKULL = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODgzMWM3M2Y1NDY4ZTg4OGMzMDE5ZTI4NDdlNDQyZGZhYTg4ODk4ZDUwY2NmMDFmZDJmOTE0YWY1NDRkNTM2OCJ9fX0";

  public LanguageSetting() {
    super(1, TAG_TO_VALUE.size(), ENGLISH);
    setValueUnit(ValueUnit.named(this::getLanguageName));
    Challenges.getInstance().getLoaderRegistry().subscribe(LanguageLoader.class, this::onLanguageChange);
  }

  public void onLanguageChange() {
    loadSettings(null);
  }

  @Override
  public boolean isVisible() {
    TranslationManager translationManager = Challenges.getInstance().getTranslationManager();
    if (translationManager.isLanguageProviderUninitialized()) return true;
    // hidden when an external provider (e.g. the network bridge) controls languages
    return translationManager.getLanguageProvider().isDefaultBehaviour();
  }

  @NotNull
  @Override
  public ItemStack getSettingsItemPreset() {
    return switch (getValue()) {
      case GERMAN -> new ItemBuilder.SkullBuilder().setBase64Texture(GERMAN_SKULL).build();
      case ENGLISH -> new ItemBuilder.SkullBuilder().setBase64Texture(ENGLISH_SKULL).build();
      default -> new ItemStack(Material.BARRIER);
    };
  }

  @NotNull
  private LocalizableMessage getLanguageName(int value) {
    String languageTag = VALUE_TO_TAG.get(value);
    if (languageTag == null) return MessageKey.of("generic.unknown");
    return getLanguageName(languageTag);
  }

  @NotNull
  private MessageKey getLanguageName(@NotNull String languageTag) {
    return MessageKey.of("language", languageTag);
  }

  @Override
  public void handleClick(@NotNull ChallengeMenuClickInfo info) {
    LanguageProvider languageProvider = Challenges.getInstance().getTranslationManager().getLanguageProvider();
    if (!languageProvider.isDefaultBehaviour()) {
      getChallengeMessageKey("command-disabled").send(info.getPlayer(), Prefix.CHALLENGES);
      SoundSample.BASS_OFF.play(info.getPlayer());
      return;
    }

    super.handleClick(info);

    String languageTag = VALUE_TO_TAG.get(getValue());
    getChallengeMessageKey("command-changing").send(info.getPlayer(), Prefix.CHALLENGES, languageTag);
  }

  @Override
  protected void onValueChange() {
    LanguageLoader languageLoader = Challenges.getInstance().getLoaderRegistry().findLoaderByClassOrThrow(LanguageLoader.class);
    String languageTag = VALUE_TO_TAG.get(getValue());
    if (languageTag == null) return;

    Challenges.getInstance().runAsync(() -> {
      languageLoader.changeLanguage(languageTag);
      getChallengeMessageKey("command-changed").broadcast(Prefix.CHALLENGES, getLanguageName(languageTag), languageTag);
    });
  }

  @Override
  public void restoreDefaults() {
    // "/reset settings" should not reset the chosen language
  }

  @Override
  public void loadSettings(@Nullable Document document) {
    // will be saved in plugin.yml
    LanguageLoader languageLoader = Challenges.getInstance().getLoaderRegistry().findLoaderByClassOrThrow(LanguageLoader.class);
    Integer value = TAG_TO_VALUE.get(languageLoader.getConfigLanguageTag());
    if (value == null) return;
    overwriteValue(value); // don't trigger change language logic!
  }

  @Override
  public void writeSettings(@NotNull Document document) {
    // will be saved in plugin.yml (saved by LanguageLoader)
  }

  @Override
  public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) throws Exception {
    if (args.length < 1) {
      MessageKey.of("command.syntax").send(sender, Prefix.CHALLENGES, "setlang <language>");
      return;
    }

    LanguageProvider languageProvider = Challenges.getInstance().getTranslationManager().getLanguageProvider();
    if (!languageProvider.isDefaultBehaviour()) {
      getChallengeMessageKey("command-disabled").send(sender, Prefix.CHALLENGES);
      return;
    }

    String languageTag;
    switch (args[0].toLowerCase()) {
      case "german", "deutsch", "de" -> languageTag = "de";
      case "english", "englisch", "en" -> languageTag = "en";
      default -> {
        getChallengeMessageKey("command-unknown").send(sender, Prefix.CHALLENGES, Component.text(args[0]));
        return;
      }
    }

    getChallengeMessageKey("command-changing").send(sender, Prefix.CHALLENGES, languageTag);

    int value = TAG_TO_VALUE.get(languageTag);
    setValue(value); // trigger change language logic
    playValueChangeTitle();
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
    return Utils.filterRecommendations(args[0], "german", "english");
  }
}
