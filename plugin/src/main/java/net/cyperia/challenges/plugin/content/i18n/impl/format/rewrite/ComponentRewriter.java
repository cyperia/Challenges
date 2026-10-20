package net.cyperia.challenges.plugin.content.i18n.impl.format.rewrite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Walks a <b>server generated</b> message (death messages, advancement messages, join/quit
 * messages, ...) and swaps out the single components a caller is interested in.
 * <p>
 * Vanilla holds the interesting parts of those messages in the <i>arguments</i> of a
 * {@link TranslatableComponent} rather than in its children, e.g.
 * {@code death.attack.player -> [victim, killer]} or
 * {@code chat.type.advancement.task -> [player, advancement]}, so both have to be walked. Subtrees
 * with nothing to replace in them are handed back as they came in, so a message holding nothing of
 * interest allocates nothing.
 */
public final class ComponentRewriter {

  public static final TextColor REWRITE_COLOR = NamedTextColor.YELLOW;

  private ComponentRewriter() {
  }

  /**
   * A single replacement rule, offered every component of a message.
   */
  @FunctionalInterface
  public interface Rewrite {

    /**
     * @return the replacement for {@code component}, or {@code null} to leave that one (and only
     * that one) alone
     */
    @Nullable
    Component apply(@NotNull Component component);

  }

  /**
   * Applies all {@code rewrites} in a single walk; the first one offering a replacement for a
   * component wins. Replacements are taken over as a whole, they are not walked into any further.
   *
   * @return the rewritten message, or the very same instance if there was nothing to replace
   */
  @NotNull
  @Contract(pure = true)
  public static Component rewrite(@NotNull Component component, @NotNull Rewrite... rewrites) {
    return rewriteComponent(component, rewrites);
  }

  /**
   * @return the rewritten subtree, or the very same instance if nothing inside it had to be
   * replaced
   */
  @NotNull
  private static Component rewriteComponent(@NotNull Component component, @NotNull Rewrite[] rewrites) {
    for (Rewrite rewrite : rewrites) {
      Component replacement = rewrite.apply(component);
      if (replacement != null) return replacement;
    }

    Component result = component instanceof TranslatableComponent translatable
      ? rewriteArguments(translatable, rewrites)
      : component;
    return rewriteChildren(result, rewrites);
  }

  @NotNull
  private static Component rewriteArguments(@NotNull TranslatableComponent component, @NotNull Rewrite[] rewrites) {
    List<TranslationArgument> arguments = component.arguments();
    List<TranslationArgument> rewritten = null;

    for (int i = 0, size = arguments.size(); i < size; i++) {
      // numeric and boolean arguments hold no component to walk
      if (!(arguments.get(i).value() instanceof Component argument)) continue;

      Component replacement = rewriteComponent(argument, rewrites);
      if (replacement == argument) continue;

      if (rewritten == null) rewritten = new ArrayList<>(arguments);
      rewritten.set(i, TranslationArgument.component(replacement));
    }

    return rewritten == null ? component : component.arguments(rewritten);
  }

  @NotNull
  private static Component rewriteChildren(@NotNull Component component, @NotNull Rewrite[] rewrites) {
    List<Component> children = component.children();
    List<Component> rewritten = null;

    for (int i = 0, size = children.size(); i < size; i++) {
      Component child = children.get(i);
      Component replacement = rewriteComponent(child, rewrites);
      if (replacement == child) continue;

      if (rewritten == null) rewritten = new ArrayList<>(children);
      rewritten.set(i, replacement);
    }

    return rewritten == null ? component : component.children(rewritten);
  }

}
