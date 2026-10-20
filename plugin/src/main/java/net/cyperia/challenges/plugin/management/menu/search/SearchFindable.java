package net.cyperia.challenges.plugin.management.menu.search;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface SearchFindable {

  static void register(@NotNull SearchFindable findable) {
    Challenges.getInstance().getSearchRegistry().register(findable);
  }

  static void register(@NotNull SearchFindable... findable) {
    Challenges.getInstance().getSearchRegistry().registerAll(findable);
  }

  @NotNull
  @Contract(pure = true)
  static SearchFindable create(@NotNull String messageKeySpace, @NotNull Material displayMaterial, @NotNull SearchCategory category,
                               @NotNull Consumer<Player> showAction) {
    return create(messageKeySpace, displayMaterial, Interaction.SEARCH_SELECT, category, showAction);
  }

  @NotNull
  @Contract(pure = true)
  static SearchFindable create(@NotNull String messageKeySpace, @NotNull Material displayMaterial,
                               @NotNull Interaction interaction, @NotNull SearchCategory category, @NotNull Consumer<Player> showAction) {
    return new Entry(messageKeySpace + ".name", messageKeySpace + ".desc",
      new ItemStack(displayMaterial), category, interaction, showAction);
  }

  void showFindable(@NotNull Player player);

  @NotNull
  LocalizableMessage getName();

  @NotNull
  LocalizableMessage getDescription();

  @Nullable
  ItemStack getDisplayItemPreset();

  @NotNull
  SearchCategory getCategory();

  /**
   * @return whether this findable is offered as a search result
   * @implSpec Called for every registered findable on every search, off the main thread; keep it cheap
   * and safe to call from there; may change at runtime, so it is asked per search and never cached
   */
  default boolean isVisible() {
    return true;
  }

  @NotNull
  default LocalizableMessage getFindableInteraction() {
    return Interaction.SEARCH_SELECT;
  }

  record Entry(@NotNull String nameMessageKey, @NotNull String descMessageKey, @Nullable ItemStack displayPreset,
               @NotNull SearchCategory category, @NotNull Interaction interaction, @NotNull Consumer<Player> showAction)
    implements SearchFindable {

    @Override
    public void showFindable(@NotNull Player player) {
      showAction.accept(player);
    }

    @NotNull
    @Override
    public LocalizableMessage getName() {
      return MessageKey.of(nameMessageKey);
    }

    @NotNull
    @Override
    public LocalizableMessage getDescription() {
      return MessageKey.of(descMessageKey);
    }

    @Nullable
    @Override
    public ItemStack getDisplayItemPreset() {
      return displayPreset;
    }

    @NotNull
    @Override
    public SearchCategory getCategory() {
      return category;
    }
  }

}
