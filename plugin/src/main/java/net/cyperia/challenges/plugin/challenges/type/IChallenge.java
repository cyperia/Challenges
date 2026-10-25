package net.cyperia.challenges.plugin.challenges.type;

import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.management.challenges.ChallengeManager;
import net.cyperia.challenges.plugin.management.challenges.entities.GamestateSaveable;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.management.menu.SettingCategory;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.menu.info.ChallengeMenuClickInfo;
import net.cyperia.challenges.plugin.management.menu.search.SearchFindable;
import net.cyperia.challenges.plugin.management.menu.generator.types.IChallengesMenuGenerator;
import net.cyperia.commons.common.config.Document;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface IChallenge extends GamestateSaveable, SearchFindable {

  /**
   * Returns if this challenge is enabled.
   * This value is used in checks and policies like {@link net.cyperia.challenges.plugin.management.scheduler.policy.ChallengeStatusPolicy} which are used for tasks.
   * If this challenge does not have clear boolean value, you may always return {@code true}
   *
   * @return if this challenge is enabled
   */
  boolean isEnabled();

  /**
   * Restores the default settings of this challenge.
   * This will be executed when the /config reset command is executed.
   *
   * @see ChallengeManager#restoreDefaults()
   */
  void restoreDefaults();

  /**
   * Whether this challenge is currently using its default settings.
   *
   * @see #restoreDefaults()
   */
  boolean isDefaultSettings();

  /**
   * This method will be executed when the challenges plugin is being disabled.
   * This will occur when the server is shutdown or reloaded.
   * This method is intended for removing temporarily content or calling disable logic.
   *
   * @see ChallengeManager#shutdownChallenges()
   */
  void handleShutdown();

  /**
   * Returns the internal name of this challenges.
   * This name will be used for saving and assigning the right settings or gamestate.
   * If multiple instances of class are registered, they must not return the same value.
   * Multiple challenges should not have the same name.
   *
   * @return the internal name of this challenge
   */
  @NotNull
  String getUniqueName();

  /**
   * This challenge will be displayed in the menu of its {@link #getCategory() category}.
   * This has to always return the same value.
   *
   * @return the target menu for the challenge
   */
  @NotNull
  default MenuType<? extends IChallengesMenuGenerator> getMenu() {
    return getCategory().getMenu();
  }

  /**
   * Whether this challenge is currently shown in its menu and the search menu.
   * Unlike {@link #getMenu()} this may change at runtime; menus are filtered on (re)generation.
   * The challenge stays registered either way — settings and gamestate are unaffected.
   *
   * @return whether this challenge is displayed in the menu/search
   */
  default boolean isVisible() {
    return true;
  }

  /**
   * @return whether this challenge is {@link #isVisible() visible} and not hidden from this player by its permission
   * @see EntryAccess
   */
  @Override
  default boolean isVisibleTo(@NotNull Player viewer) {
    return isVisible() && EntryAccess.isListed(this, viewer);
  }

  /**
   * The category this challenge is grouped under, which also decides the {@link #getMenu() menu} it lives in.
   *
   * @return the category of this challenge
   */
  @NotNull
  SettingCategory getCategory();

  @NotNull
  ItemFormatter getDisplayItem();

  @NotNull
  ItemFormatter getSettingsItem();

  @Override
  @NotNull
  LocalizableMessage getName();

  @NotNull
  @Override
  LocalizableMessage getDescription();

  void handleClick(@NotNull ChallengeMenuClickInfo info);

  void writeSettings(@NotNull Document document);

  void loadSettings(@NotNull Document document);

}
