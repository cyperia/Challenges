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

import java.util.*;

@RequiredArgsConstructor
public abstract class SingleAnimatedMenuGenerator implements IMenuGenerator {

  protected final boolean animated;

  private final Map<Locale, AnimatedInventory> inventoryCache = new HashMap<>();
  private volatile boolean stale;

  @NotNull
  public abstract MenuPosition createMenuPosition();

  // TODO impl dynamic formatting as in Multi-/SinglePageMenuGenerator
  @NotNull
  public abstract AnimatedInventory createAnimatedInventory(@NotNull Locale locale);

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
  public AnimatedInventory getOrCreateInventory(@NotNull Locale locale) {
    if (stale) {
      stale = false;
      inventoryCache.clear();
    }
    return inventoryCache.computeIfAbsent(locale, this::createAnimatedInventory);
  }

  @Override
  public void openMenu(@NotNull Player player, int page) {
    if (!animated) {
      openMenuInstantly(player, true);
      return;
    }

    Locale locale = findLanguageProvider().getPlayerLanguage(player);
    AnimatedInventory inventory = getOrCreateInventory(locale);
    MenuPosition.set(player, createMenuPosition());
    inventory.open(player);
  }

  public void openMenuInstantly(@NotNull Player player, boolean playSound) {
    Locale locale = findLanguageProvider().getPlayerLanguage(player);
    AnimatedInventory inventory = getOrCreateInventory(locale);
    MenuPosition.set(player, createMenuPosition());
    inventory.openNotAnimated(player, playSound);
  }

  @Override
  public void updateOrGeneratePages(@NotNull Locale locale) {
    inventoryCache.put(locale, createAnimatedInventory(locale));
  }

  @Override
  public void updatePages() {
    // updating every frame of the animation is tedious, just recreate it
    stale = false;
    List<Locale> locales = new ArrayList<>(inventoryCache.keySet());
    inventoryCache.clear();
    locales.forEach(locale -> inventoryCache.put(locale, createAnimatedInventory(locale)));
  }

  @Override
  public void updatePage(int page) {
    updatePages(); // only one page
  }

  @Override
  public final int getPageCount() {
    return 1;
  }

  public abstract int getInventorySize();

  @NotNull
  protected LanguageProvider findLanguageProvider() {
    return Challenges.getInstance().getTranslationManager().getLanguageProvider();
  }
}
