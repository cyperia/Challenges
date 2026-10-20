package net.cyperia.commons.bukkit.utils.menu;

import lombok.Getter;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * @implSpec The declaration order is the order clicks are listed in and preferred by
 * ({@link net.cyperia.challenges.plugin.management.menu.format.InteractionTooltips}), so a plain click
 * comes before the shift variant that does the same thing.
 */
public enum MenuClickType {

  LEFT(false, false),
  RIGHT(true, false),
  LEFT_SHIFT(false, true),
  RIGHT_SHIFT(true, true);

  private final String messageKey;
  @Getter
  private final boolean rightClick;
  @Getter
  private final boolean shiftClick;

  MenuClickType(boolean rightClick, boolean shiftClick) {
    this.messageKey = "menu.generic.tooltip." + this.name().toLowerCase(Locale.ROOT);
    this.rightClick = rightClick;
    this.shiftClick = shiftClick;
  }

  @NotNull
  public MessageKey getTooltipKey() {
    return MessageKey.of(messageKey);
  }

  @NotNull
  public static MenuClickType of(boolean right, boolean shift) {
    if (right) {
      return shift ? RIGHT_SHIFT : RIGHT;
    } else {
      return shift ? LEFT_SHIFT : LEFT;
    }
  }

}
