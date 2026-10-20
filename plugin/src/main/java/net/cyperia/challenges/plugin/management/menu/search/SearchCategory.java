package net.cyperia.challenges.plugin.management.menu.search;

import net.cyperia.challenges.plugin.content.i18n.ArgumentFormat;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface SearchCategory {

  SearchCategory MENU_TYPE = create("search.category.menu");
  SearchCategory SETTING_CATEGORY = create("search.category.category");
  SearchCategory SUB_SETTING = create("search.category.sub_setting");

  @NotNull
  @Contract(pure = true)
  static SearchCategory create(@NotNull String nameMessageKey) {
    return new Entry(nameMessageKey);
  }

  @NotNull
  @Contract(pure = true)
  static SearchCategory specify(@NotNull SearchCategory mainCategory, @NotNull SearchCategory subCategory) {
    return new Specified(mainCategory, subCategory);
  }

  @NotNull
  @Contract(pure = true)
  static LocalizableMessage specifiedName(@NotNull SearchCategory mainCategory, @NotNull LocalizableMessage subCategory) {
    return specifiedName(mainCategory.getSearchCategoryName(), subCategory);
  }

  @NotNull
  @Contract(pure = true)
  static LocalizableMessage specifiedName(@NotNull LocalizableMessage mainCategory, @NotNull LocalizableMessage subCategory) {
    return ArgumentFormat.BRACKETS.apply(mainCategory, subCategory);
  }

  @NotNull
  LocalizableMessage getSearchCategoryName();

  record Entry(@NotNull String nameMessageKey) implements SearchCategory {

    @NotNull
    @Override
    public LocalizableMessage getSearchCategoryName() {
      return MessageKey.of(nameMessageKey);
    }
  }

  record Specified(@NotNull SearchCategory mainCategory,
                   @NotNull SearchCategory subCategory) implements SearchCategory {

    @NotNull
    @Override
    public LocalizableMessage getSearchCategoryName() {
      return specifiedName(mainCategory.getSearchCategoryName(), subCategory.getSearchCategoryName());
    }
  }

}
