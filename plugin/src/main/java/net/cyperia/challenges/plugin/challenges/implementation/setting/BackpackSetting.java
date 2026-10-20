package net.cyperia.challenges.plugin.challenges.implementation.setting;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingModifier;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.content.i18n.TranslationManager;
import net.cyperia.challenges.plugin.content.loader.LanguageLoader;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.utils.bukkit.command.PlayerCommand;
import net.cyperia.challenges.plugin.utils.bukkit.container.BukkitSerialization;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.common.config.Document;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class BackpackSetting extends SettingModifier implements PlayerCommand {

  public static final int SHARED = 1, PLAYER = 2;

  private final int size;
  private final Map<UUID, Inventory> backpacks = new HashMap<>();
  private final Inventory sharedBackpack;

  public BackpackSetting() {
    super(1, 2, SHARED);
    setValueUnit(namedValues("settings.shared", "settings.player"));
    size = Math.clamp(Challenges.getInstance().getConfigDocument().getInt(ConfigManager.Keys.CHALLENGE_SETTINGS_BACKPACK_SIZE) * 9L, 9, 6 * 9);
    // The shared backpack is a single inventory used by every player, so its title cannot depend
    // on the viewer. It is resolved once with the server language via a locale-independent key.
    sharedBackpack = createInventory(getSharedBackpackTitle());
  }

  @NotNull
  @Override
  public ItemStack getSettingsItemPreset() {
    return new ItemStack(getValue() == SHARED ? Material.ENDER_CHEST : Material.PLAYER_HEAD);
  }

  @Override
  public void onCommand(@NotNull Player player, @NotNull String[] args) {
    if (ChallengeAPI.isPaused()) {
      MessageKey.of("timer.messages.not-started").send(player, Prefix.BACKPACK);
      SoundSample.BASS_OFF.play(player);
      return;
    }

    if (!isEnabled()) {
      getChallengeMessageKey("disabled").send(player, Prefix.BACKPACK);
      SoundSample.BASS_OFF.play(player);
      return;
    }

    if (getValue() == SHARED || getValue() == PLAYER) {
      getChallengeMessageKey("opened").send(player, Prefix.BACKPACK, getBackpackName());
      player.openInventory(getCurrentBackpack(player));
      SoundSample.OPEN.play(player);
    } else {
      getChallengeMessageKey("disabled").send(player, Prefix.BACKPACK);
      SoundSample.BASS_OFF.play(player);
    }
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    super.loadGameState(document);

    loadChecked(document, "shared", sharedBackpack);

    Document players = document.getDocument("players");
    for (String key : players.keys()) {
      loadChecked(players, key, backpacks.computeIfAbsent(UUID.fromString(key), playerId ->
        createInventory(getPlayerBackpackTitle(getPlayerLanguageOrServer(playerId)))));
    }

  }

  protected void loadChecked(@NotNull Document document, @NotNull String key, @NotNull Inventory inventory) {

    if (document.isDocument(key)) {
      loadLegacy(document.getDocument(key), inventory);
    } else {
      try {
        String value = document.getString(key);
        if (value == null) return;
        BukkitSerialization.fromBase64(inventory, value);
      } catch (IOException exception) {
        Challenges.getInstance().getILogger().error(exception);
      }
    }

  }

  protected void loadLegacy(@NotNull Document document, @NotNull Inventory inventory) {

    for (String key : document.keys()) {
      try {
        int index = Integer.parseInt(key);
        ItemStack item = document.getSerializable(key, ItemStack.class);
        inventory.setItem(index, item);
      } catch (Exception ignored) {
      }
    }
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    super.writeGameState(document);

    write(document, "shared", sharedBackpack);

    Document players = document.getDocument("players");
    backpacks.forEach((uuid, inventory) -> {
      write(players, uuid.toString(), inventory);
    });
  }

  protected void write(@NotNull Document document, @NotNull String key, @NotNull Inventory inventory) {
    document.set(key, BukkitSerialization.toBase64(inventory));
  }

  @NotNull
  protected Inventory createInventory(@NotNull Component title) {
    return Bukkit.createInventory(null, size, title);
  }

  @NotNull
  protected Inventory getCurrentBackpack(@NotNull Player player) {
    return (getValue() == SHARED) ? sharedBackpack : backpacks.computeIfAbsent(player.getUniqueId(), key -> createInventory(getPlayerBackpackTitle(player)));
  }

  @NotNull
  protected LocalizableMessage getBackpackName() {
    return getChallengeMessageKey(getValue() == SHARED ? "shared-name" : "player-name");
  }

  // TODO (hopefully) temp solution for translation port; brought to you by claude :D
  @NotNull
  protected Component getSharedBackpackTitle() {
    // lives in global.json
    return getChallengeMessageKey("inventory-shared").asComponent(getServerLanguage());
  }

  @NotNull
  protected Component getPlayerBackpackTitle(@NotNull Player player) {
    return getChallengeMessageKey("inventory-player").asComponent(player);
  }

  @NotNull
  protected Component getPlayerBackpackTitle(@NotNull Locale locale) {
    return getChallengeMessageKey("inventory-player").asComponent(locale);
  }

  @NotNull
  private Locale getServerLanguage() {
    return Challenges.getInstance().getLoaderRegistry().getFirstLoaderByClass(LanguageLoader.class)
      .map(LanguageLoader::getConfigLanguage)
      .orElse(TranslationManager.FALLBACK_LOCALE);
  }

  @NotNull
  private Locale getPlayerLanguageOrServer(@NotNull UUID playerId) {
    Player player = Bukkit.getPlayer(playerId);
    if (player == null) return getServerLanguage();
    return Challenges.getInstance().getTranslationManager().getLanguageProvider().getPlayerLanguage(player);
  }

}
