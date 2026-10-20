package net.cyperia.challenges.plugin.management.menu.format;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.KeyedLocalizableMessage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * The generic actions a menu click can perform, each backed by {@code menu.generic.interact.<name>}.
 * Menu specific actions (e.g. the timer controls) keep using their own keys.
 */
public enum Interaction implements KeyedLocalizableMessage {

  OPEN,
  SEARCH,
  SEARCH_SELECT,
  ENABLE,
  ENABLE_GOAL,
  DISABLE,
  ADD,
  SUBTRACT,
  SET,
  BACK,
  BACK_SHIFT,
  BACK_FIRST,
  BACK_MENU,
  BACK_MAIN_MENU,
  CLOSE_MENU,
  NEXT,
  NEXT_SHIFT,
  NEXT_LAST,
  PUNISHMENT,
  TRANSFER,
  ;

  private final String key = "menu.generic.interact." + name().toLowerCase(Locale.ROOT);

  @NotNull
  @Contract(pure = true)
  public static Interaction toggle(boolean enabled) {
    return enabled ? DISABLE : ENABLE;
  }

  @NotNull
  @Override
  public MessageKey getLocalizableKey() {
    return MessageKey.of(key);
  }

}
