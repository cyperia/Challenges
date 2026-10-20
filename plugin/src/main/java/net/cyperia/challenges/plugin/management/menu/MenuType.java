package net.cyperia.challenges.plugin.management.menu;

import lombok.Getter;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.generator.AbstractMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.TeamsMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.types.IChallengesMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.IMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.types.ISearchMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.types.ITimerMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.timer.TimerMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.challenge.categorised.CategorisedMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.CustomHomeMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.search.SearchDialogMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.search.SearchCategory;
import net.cyperia.challenges.plugin.management.menu.search.SearchFindable;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class MenuType<T extends IMenuGenerator> implements SearchFindable, SearchCategory {

  public static final MenuType<ITimerMenuGenerator> TIMER =
    new MenuType<>("timer", new TimerMenuGenerator(), Material.BELL /* 1.13 */);

  public static final MenuType<ISearchMenuGenerator> SEARCH =
    new MenuType<>("search", new SearchDialogMenuGenerator(), Material.OAK_HANGING_SIGN);

  public static final MenuType<IChallengesMenuGenerator> CHALLENGES =
    new MenuType<>("challenges", new CategorisedMenuGenerator(), Material.OMINOUS_TRIAL_KEY);

  public static final MenuType<IChallengesMenuGenerator> GOAL =
    new MenuType<>("goal", new CategorisedMenuGenerator(), Material.SPYGLASS /* 1.17 */);

  public static final MenuType<IChallengesMenuGenerator> SETTINGS =
    new MenuType<>("settings", new CategorisedMenuGenerator(), Material.COMPARATOR);

  public static final MenuType<CustomHomeMenuGenerator> CUSTOM =
    new MenuType<>("custom", new CustomHomeMenuGenerator(), Material.WRITABLE_BOOK);

  public static final MenuType<TeamsMenuGenerator> TEAMS =
    new MenuType<>("teams", new TeamsMenuGenerator(), Material.BLUE_HARNESS);

  @Getter
  private final String key;
  @Getter
  private final Material displayItemMaterial;
  @Getter
  private final T menuGenerator;

  private MenuType(@NotNull String key, @NotNull T menuGenerator, @NotNull Material displayItemMaterial) {
    this.key = key;
    this.displayItemMaterial = displayItemMaterial;
    this.menuGenerator = menuGenerator;

    if (menuGenerator instanceof AbstractMenuGenerator generator) {
      generator.setMenuType(this);
    }

    SearchFindable.register(this);
    Registry.VALUES.add(this);
  }

  /**
   * Colored, translation key used in item names.
   */
  @NotNull
  @Override
  public MessageKey getName() {
    return MessageKey.of("menu." + key + ".name");
  }

  /**
   * Uncolored, translation key used in inventory titles.
   */
  @NotNull
  public MessageKey getMenuName() {
    return MessageKey.of("menu." + key + ".menu");
  }

  /**
   * Multiline, translation key used in item lore.
   */
  @NotNull
  @Override
  public MessageKey getDescription() {
    return MessageKey.of("menu." + key + ".desc");
  }

  @NotNull
  @Override
  public ItemStack getDisplayItemPreset() {
    return new ItemStack(displayItemMaterial);
  }

  @Override
  public void showFindable(@NotNull Player player) {
    menuGenerator.openMenu(player);
  }

  @NotNull
  public String getMessageKey() {
    return "menu." + key;
  }

  @NotNull
  @Override
  public SearchCategory getCategory() {
    return SearchCategory.MENU_TYPE;
  }

  @NotNull
  @Override
  public LocalizableMessage getSearchCategoryName() {
    return getName();
  }

  @Override
  public boolean isVisible() {
    return this != SEARCH;
  }

  @NotNull
  public static List<MenuType<?>> values() {
    return Registry.VIEW;
  }

  @NotNull
  public static MenuType<?> byKey(@NotNull String key) {
    return values().stream().filter(type -> type.key.equals(key)).findFirst()
      .orElseThrow(() -> new IllegalArgumentException("No menu " + key));
  }

  /**
   * @return the menu type registered under this {@link #getKey() key}, or {@code null} if there is none
   */
  @Nullable
  public static MenuType<?> find(@NotNull String key) {
    for (MenuType<?> type : Registry.VALUES) {
      if (type.key.equalsIgnoreCase(key)) return type;
    }
    return null;
  }

  private static final class Registry {
    private static final List<MenuType<?>> VALUES = new ArrayList<>();
    private static final List<MenuType<?>> VIEW = Collections.unmodifiableList(VALUES);
  }

}
