package net.cyperia.challenges.plugin.management.menu.generator;

import lombok.RequiredArgsConstructor;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.hook.LanguageProvider;
import net.cyperia.commons.bukkit.utils.animation.AnimatedInventory;
import net.cyperia.commons.bukkit.utils.menu.MenuPosition;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@RequiredArgsConstructor
public abstract class SingleAnimatedMenuGenerator implements IMenuGenerator {

  protected final boolean animated;

  private final Map<CacheKey, AnimatedInventory> inventoryCache = new HashMap<>();
  private volatile boolean stale;

  @NotNull
  public abstract MenuPosition createMenuPosition();

  // TODO impl dynamic formatting as in Multi-/SinglePageMenuGenerator
  @NotNull
  public abstract AnimatedInventory createAnimatedInventory(@NotNull Locale locale, @NotNull Player viewer);

  // Viewers of one language share an inventory unless this differs between them
  @Nullable
  protected Object variantOf(@NotNull Player viewer) {
    return null;
  }

  @NotNull
  protected AnimatedInventory createEmptyInventory(@NotNull Locale locale) {
    return new AnimatedInventory(MessageKey.of("menu.title-format").asComponent(locale, getMenuName()), getInventorySize(), MenuPosition.HOLDER);
  }

  @NotNull
  public abstract LocalizableMessage getMenuName();

  // Callable from any thread: the cache is only touched on the next open, which runs on the main thread.
  public void markStale() {
    stale = true;
  }

  @NotNull
  private AnimatedInventory getOrCreateInventory(@NotNull Player viewer) {
    if (stale) {
      stale = false;
      inventoryCache.clear();
    }
    Locale locale = findLanguageProvider().getPlayerLanguage(viewer);
    return inventoryCache.computeIfAbsent(new CacheKey(locale, variantOf(viewer)), _ -> createAnimatedInventory(locale, viewer));
  }

  @Override
  public void openMenu(@NotNull Player player, int page) {
    if (!animated) {
      openMenuInstantly(player, true);
      return;
    }

    AnimatedInventory inventory = getOrCreateInventory(player);
    MenuPosition.set(player, createMenuPosition());
    inventory.open(player);
  }

  public void openMenuInstantly(@NotNull Player player, boolean playSound) {
    AnimatedInventory inventory = getOrCreateInventory(player);
    MenuPosition.set(player, createMenuPosition());
    inventory.openNotAnimated(player, playSound);
  }

  // Updating every frame of the animation is tedious, the next open builds it again
  @Override
  public void updatePages() {
    stale = false;
    inventoryCache.clear();
  }

  @Override
  public void updatePage(int page) {
    updatePages(); // only one page
  }

  @Override
  public final int getPageCount(@NotNull Player viewer) {
    return 1;
  }

  public abstract int getInventorySize();

  @NotNull
  protected LanguageProvider findLanguageProvider() {
    return Challenges.getInstance().getTranslationManager().getLanguageProvider();
  }

  private record CacheKey(@NotNull Locale locale, @Nullable Object variant) {
  }
}
