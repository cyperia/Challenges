package net.cyperia.challenges.plugin.management.menu.format;

import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The lore block telling the player what each way of clicking an item does.
 *
 * @implSpec Only add a click type that actually does something distinct: every entry becomes its own lore
 * line, so listing the same effect twice reads as if the clicks differed.
 */
public class InteractionTooltips {

  private final Map<MenuClickType, LocalizableMessage> content = new EnumMap<>(MenuClickType.class);

  @NotNull
  public InteractionTooltips on(@NotNull MenuClickType type, @NotNull LocalizableMessage description) {
    content.put(type, description);
    return this;
  }

  @NotNull
  public InteractionTooltips onIf(boolean condition, @NotNull MenuClickType type, @NotNull LocalizableMessage description) {
    if (condition) on(type, description);
    return this;
  }

  public boolean has(@NotNull MenuClickType type) {
    return content.containsKey(type);
  }

  public boolean isEmpty() {
    return content.isEmpty();
  }

  /**
   * @return one line per registered {@link MenuClickType}, in enum order
   */
  @NotNull
  public List<Component> render(@NotNull Locale locale) {
    List<Component> lines = new ArrayList<>(content.size());
    for (Map.Entry<MenuClickType, LocalizableMessage> entry : content.entrySet()) {
      lines.add(entry.getKey().getTooltipKey().asComponent(locale, entry.getValue()));
    }
    return lines;
  }

}
