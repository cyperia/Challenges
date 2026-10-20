package net.cyperia.challenges.plugin.content.i18n.impl.format.rewrite;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Splits an Adventure {@link Component} into multiple components by newline
 * characters ({@code \n}), while preserving all styling (colors, decorations,
 * click/hover events, fonts, etc.).
 * <p>
 * Designed for frequent calls: no regex, no component builders, no redundant
 * allocations, and a zero-copy fast path for components without newlines.
 */
public final class ComponentSplitter {

  private ComponentSplitter() {
  }

  /**
   * Splits the given component at every {@code \n} into separate components.
   * Styles are resolved against their parents before splitting, so each
   * resulting line renders exactly like the corresponding part of the input.
   * <p>
   * A trailing newline produces a trailing empty line; an input without any
   * newline is returned unchanged as a single-element list.
   *
   * @param component the component to split
   * @return the line components (never empty); the list is freshly allocated
   * and owned by the caller, except for the newline-free fast path
   * which returns an immutable singleton
   */
  @NotNull
  @Contract(pure = true)
  public static List<Component> split(@NotNull Component component) {
    // Fast path: nothing to split -> return the original instance untouched.
    if (!containsNewline(component)) {
      return Collections.singletonList(component);
    }

    final SplitState state = new SplitState();
    visit(component, Style.empty(), state);
    state.finishLine(); // flush the last line
    return state.lines;
  }

  private static boolean containsNewline(final Component component) {
    if (component instanceof TextComponent
      && ((TextComponent) component).content().indexOf('\n') >= 0) {
      return true;
    }
    for (final Component child : component.children()) {
      if (containsNewline(child)) {
        return true;
      }
    }
    return false;
  }

  private static void visit(final Component component, final Style parentStyle, final SplitState state) {
    // Resolve the effective style like vanilla rendering does, but skip the
    // merge entirely when one side cannot contribute anything.
    final Style own = component.style();
    final Style style;
    if (parentStyle.isEmpty()) {
      style = own;
    } else if (own.isEmpty()) {
      style = parentStyle;
    } else {
      style = own.merge(parentStyle, Style.Merge.Strategy.IF_ABSENT_ON_TARGET);
    }

    if (component instanceof TextComponent) {
      final String content = ((TextComponent) component).content();
      final int firstNewline = content.indexOf('\n');

      if (firstNewline < 0) {
        if (!content.isEmpty()) {
          // Reuse the original component when it already carries the
          // resolved style and has no children; otherwise re-wrap.
          if (style == own && component.children().isEmpty()) {
            state.append(component);
          } else {
            state.append(Component.text(content, style));
          }
        }
      } else {
        // Manual scan instead of String#split: no regex, no String[].
        int start = 0;
        int newline = firstNewline;
        do {
          if (newline > start) {
            state.append(Component.text(content.substring(start, newline), style));
          }
          state.finishLine();
          start = newline + 1;
        } while ((newline = content.indexOf('\n', start)) >= 0);

        if (start < content.length()) {
          state.append(Component.text(content.substring(start), style));
        }
      }
    } else {
      // Non-text components (translatable, keybind, score, selector, ...)
      // cannot be split internally. Append them with the resolved style
      // and without children - those are visited separately below so
      // newlines inside them are still handled.
      if (style == own && component.children().isEmpty()) {
        state.append(component);
      } else {
        state.append(component.children(Collections.<Component>emptyList()).style(style));
      }
    }

    for (final Component child : component.children()) {
      visit(child, style, state);
    }
  }

  /**
   * Mutable state used while walking the component tree. Lines are assembled
   * without component builders: a single-part line is used as-is, and
   * multi-part lines are attached as children of an empty root via
   * {@link Component#children(List)}.
   */
  private static final class SplitState {
    private final List<Component> lines = new ArrayList<>();

    /**
     * The only part of the current line, while it has exactly one.
     */
    private Component single;
    /**
     * All parts of the current line, once it has two or more.
     */
    private List<Component> parts;

    void append(final Component part) {
      if (this.parts != null) {
        this.parts.add(part);
        return;
      }
      if (this.single == null) {
        this.single = part;
        return;
      }
      // Second part arrived: promote to a list.
      this.parts = new ArrayList<>(8);
      this.parts.add(this.single);
      this.parts.add(part);
      this.single = null;
    }

    void finishLine() {
      if (this.parts != null) {
        this.lines.add(Component.empty().children(this.parts));
        this.parts = null;
      } else if (this.single != null) {
        this.lines.add(this.single);
        this.single = null;
      } else {
        this.lines.add(Component.empty());
      }
    }
  }
}
