package net.cyperia.challenges.plugin.content.i18n.impl.format.rewrite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Restyles the advancement name vanilla renders as a green {@code [Advancement Name]} into a plain
 * {@link #NAME_COLOR yellow} one, fitting the generic Challenges message style.
 * <p>
 * Vanilla wraps the name into a {@code chat.square_brackets} ({@code [%s]}) translatable holding
 * the title as its only argument, styled with the color of the frame of the advancement, so the
 * brackets are dropped by replacing that translatable with its very own argument. Only that green
 * is recolored: the dark purple of a challenge, a color an advancement gives its title itself and
 * the hover holding the description are left as they are.
 */
public final class ComponentAdvancementNames {

  /**
   * Unwraps and recolors every advancement name a message mentions.
   */
  public static final ComponentRewriter.Rewrite REWRITE = ComponentAdvancementNames::formatName;

  // by value: NamedTextColor brings no equals of its own, and a color does not have to be the
  // named instance to render as the very same green
  private static final int VANILLA_NAME_COLOR = NamedTextColor.GREEN.value();

  private static final String SQUARE_BRACKETS_KEY = "chat.square_brackets"; // "[%s]"

  private ComponentAdvancementNames() {
  }

  /**
   * Reformats a standalone advancement name, e.g. the one
   * {@link org.bukkit.advancement.Advancement#displayName()} hands out.
   */
  @NotNull
  public static Component format(@NotNull Component displayName) {
    Component name = ComponentRewriter.rewrite(displayName, REWRITE);
    return name != displayName ? name : recolor(displayName);
  }

  @Nullable
  private static Component formatName(@NotNull Component component) {
    if (!(component instanceof TranslatableComponent wrapper) || !SQUARE_BRACKETS_KEY.equals(wrapper.key()))
      return null;

    List<TranslationArgument> arguments = wrapper.arguments();
    if (arguments.size() != 1 || !(arguments.getFirst().value() instanceof Component title)) return null;

    // what the wrapper would have passed down to the name while rendering is merged into it
    Component name = recolor(title.style(title.style().merge(wrapper.style(), Style.Merge.Strategy.IF_ABSENT_ON_TARGET)));
    for (Component sibling : wrapper.children()) { // vanilla appends none, another plugin might
      name = name.append(sibling);
    }
    return name;
  }

  @NotNull
  private static Component recolor(@NotNull Component name) {
    TextColor color = name.color();
    return color != null && color.value() == VANILLA_NAME_COLOR ? name.color(ComponentRewriter.REWRITE_COLOR) : name;
  }

}
