package net.cyperia.challenges.plugin.management.menu;

import lombok.Getter;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.EntryAccess;
import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import net.cyperia.challenges.plugin.challenges.type.abstraction.menu.MenuSetting;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.content.loader.LanguageLoader;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.management.menu.generator.AbstractMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.MainMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.types.IChallengesMenuGenerator;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuPosition;
import net.cyperia.commons.common.config.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;


public final class MenuManager {

  public static final int[] GUI_SLOTS = {30, 32, 19, 25, 11, 15, 4};
  @Getter
  private final boolean newSuffix;
  @Getter
  private final boolean updatedSuffix;
  @Getter
  private final boolean displayNewInFront;
  @Getter
  private final boolean displayUpdatedInFront;

  private final boolean permissionToManageGUI;
  @Getter
  private boolean generated = false;
  private final MainMenuGenerator mainMenu;

  public MenuManager() {
    Document config = Challenges.getInstance().getConfigDocument();
    newSuffix = config.getBoolean(ConfigManager.Keys.NEW_SUFFIX);
    updatedSuffix = config.getBoolean(ConfigManager.Keys.UPDATED_SUFFIX);
    displayNewInFront = config.getBoolean(ConfigManager.Keys.NEW_IN_FRONT);
    displayUpdatedInFront = config.getBoolean(ConfigManager.Keys.UPDATED_IN_FRONT);
    permissionToManageGUI = config.getBoolean(ConfigManager.Keys.MANAGE_SETTINGS_PERMISSION);

    mainMenu = new MainMenuGenerator();

    ChallengeAPI.subscribeLoader(LanguageLoader.class, this::generateMenus);
    ChallengeAPI.subscribeLoader(LanguageLoader.class, this::reopenCurrentMenus);
    ChallengeAPI.subscribeLoader(LanguageLoader.class, mainMenu::updatePages);
  }

  public void generateMenus() {
    for (MenuType<?> value : MenuType.values()) {
      if (value.getMenuGenerator() instanceof IChallengesMenuGenerator generator) {
        generator.resetCache();
      }
    }

    for (IChallenge challenge : Challenges.getInstance().getChallengeManager().getChallenges()) {
      if (!challenge.isVisible()) continue;
      challenge.getMenu().getMenuGenerator().addToCache(challenge);
      if (challenge instanceof MenuSetting menuSetting) {
        menuSetting.getMenuGenerator().updatePages(); // trigger regeneration (e.g. to apply message override)
      }
    }

    mainMenu.updatePages();
    for (MenuType<?> value : MenuType.values()) {
      value.getMenuGenerator().updatePages();
    }

    generated = true;
  }

  public void invalidateMainMenu() {
    mainMenu.markStale();
  }

  public void openMainMenu(@NotNull Player player) {
    SoundSample.PLOP.play(player);
    mainMenu.openMenu(player);
  }

  public void openMainMenuInstantly(@NotNull Player player) {
    mainMenu.openMenuInstantly(player, true);
  }

  /**
   * @return If the specified menu page be opened.
   * The menu may not be opened, when there are no challenges registered to that menu or the languages are not loaded
   */
  public boolean openMenu(@NotNull Player player, @NotNull MenuType<?> type, int page) {
    if (!generated) {
      SoundSample.BASS_OFF.play(player);
      player.sendMessage(Prefix.FALLBACK + "§cCould not open gui, languages are not loaded");
      player.sendMessage(Prefix.FALLBACK + "§cIs the plugin set up correctly?");
      return false;
    }

    type.getMenuGenerator().openMenu(player, page);

    return true;
  }

  public void reopenCurrentMenus() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      reopenCurrentMenu(player);
    }
  }

  public void reopenCurrentMenu(@NotNull Player player) {
    // applies language change
    if (MenuPosition.get(player) instanceof AbstractMenuGenerator.GeneratorMenuPosition position) {
      position.reopen(player);
    }
  }

  public void playNoPermissionsEffect(@NotNull Player player) {
    SoundSample.BASS_OFF.play(player);
    MessageKey.of("command.no-permission").send(player, Prefix.CHALLENGES);
  }

  // true means the rejection effect has already been played and the click must not be handled
  public boolean denySettingsChange(@NotNull Player player) {
    if (maySettingsChange(permissionToManageGUI, player)) return false;
    playNoPermissionsEffect(player);
    return true;
  }

  // true means the rejection effect has already been played and the click must not be handled
  public boolean denyLockedChange(@NotNull Player player, @NotNull IChallenge challenge) {
    if (!EntryAccess.isLocked(challenge, player)) return false;
    SoundSample.BASS_OFF.play(player);
    MessageKey.of("menu.restricted.click").send(player, Prefix.CHALLENGES);
    return true;
  }

  static boolean maySettingsChange(boolean permissionRequired, @NotNull Permissible player) {
    return !permissionRequired || player.hasPermission(ChallengesPermissions.MANAGE);
  }

}
