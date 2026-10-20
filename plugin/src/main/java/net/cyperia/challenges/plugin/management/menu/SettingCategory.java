package net.cyperia.challenges.plugin.management.menu;

import net.cyperia.challenges.catalog.Category;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.challenges.CatalogIcons;
import net.cyperia.challenges.plugin.management.menu.generator.IMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.challenge.categorised.CategorisedMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.CustomHomeMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.types.IChallengesMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.search.SearchCategory;
import net.cyperia.challenges.plugin.management.menu.search.SearchFindable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SettingCategory implements SearchFindable, SearchCategory {

  private static final Map<String, SettingCategory> byId = new ConcurrentHashMap<>();

  private final Category entry;
  private final MenuType<? extends IChallengesMenuGenerator> menu;

  @SuppressWarnings("unchecked")
  private SettingCategory(@NotNull Category entry) {
    this.entry = entry;
    this.menu = (MenuType<? extends IChallengesMenuGenerator>) MenuType.byKey(entry.menu());
    SearchFindable.register(this);
  }

  @NotNull
  public static SettingCategory of(@NotNull String id) {
    return byId.computeIfAbsent(id, key -> new SettingCategory(Challenges.getInstance().getCatalog().category(key)
      .orElseThrow(() -> new IllegalArgumentException("No category " + key + " in the challenge catalog"))));
  }

  // Lowest priority will be displayed first
  public int getPriority() {
    return entry.priority();
  }

  @NotNull
  public String getMessageKey() {
    return entry.key();
  }

  @NotNull
  public MenuType<? extends IChallengesMenuGenerator> getMenu() {
    return menu;
  }

  @NotNull
  public ItemStack getDisplayItemPreset() {
    return CatalogIcons.item(entry.icon());
  }

  @NotNull
  @Override
  public MessageKey getName() {
    return MessageKey.of(getMessageKey() + ".name");
  }

  @NotNull
  @Override
  public MessageKey getDescription() {
    return MessageKey.of(getMessageKey() + ".desc");
  }

  @NotNull
  public MessageKey getMenuName() {
    return MessageKey.of(getMessageKey() + ".menu");
  }

  @NotNull
  public MessageKey getDescriptionInfo() {
    return MessageKey.of(getMessageKey() + ".info");
  }

  @NotNull
  @Override
  public SearchCategory getCategory() {
    return SearchCategory.specify(SearchCategory.SETTING_CATEGORY, menu);
  }

  @NotNull
  @Override
  public LocalizableMessage getSearchCategoryName() {
    return SearchCategory.specifiedName(menu, getName());
  }

  @Override
  public void showFindable(@NotNull Player player) {
    IMenuGenerator generator = menu.getMenuGenerator();
    if (generator instanceof CategorisedMenuGenerator categorisedGenerator) {
      categorisedGenerator.openMenu(player, this);
      return;
    }
    if (generator instanceof CustomHomeMenuGenerator customGenerator) {
      customGenerator.getListGenerator().openMenu(player);
      return;
    }
    generator.openMenu(player);
  }
}
