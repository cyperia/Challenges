package net.cyperia.challenges.plugin.content.i18n.impl.format.rewrite;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Joins multiple Adventure {@link Component}s into a single component, separated by newline
 * characters ({@code \n}). This is the inverse of {@link ComponentSplitter#split(Component)}:
 * splitting and re-joining renders the very same message again.
 * <p>
 * Every part keeps exactly the styling (colors, decorations, click/hover events, fonts, ...) it
 * came in with. The parts are attached as <i>siblings</i> of a style-less
 * {@link Component#empty() empty} root, never chained via {@link Component#append(Component)}:
 * appending would turn every following part into a <i>child</i> of the first one, so the first
 * line's color, decorations and events would bleed into the rest of the message. The separator is
 * style-less for the same reason - an underlined or struck-through line must not drag its
 * decoration across the line break.
 * <p>
 * Designed for frequent calls: parts and separators are collected flat and attached in a single
 * {@link Component#children(List)} call, because {@link Component#append(Component)} copies the
 * whole child list every time and is quadratic for messages with many lines. Empty inputs and
 * single-element inputs are handled without allocating anything.
 *
 * <h2>Version compatibility</h2>
 * Only API that is unchanged since Adventure 4.0.0 is used, so the compiled call sites keep working
 * against the older Adventure versions bundled with older server versions:
 * <ul>
 *   <li>{@code Component#join(...)} is avoided: the {@code (ComponentLike, Iterable)} overload was
 *   removed in Adventure 5, and the {@link net.kyori.adventure.text.JoinConfiguration} overloads
 *   only exist since 4.9.0.</li>
 *   <li>Component builders are avoided: {@code ComponentBuilder}'s bound changed in Adventure 5
 *   ({@code C extends BuildableComponent} to {@code C extends Component}, dropping
 *   {@code Buildable.Builder}), which breaks {@code build()} call sites across that boundary.</li>
 *   <li>{@code Component#appendNewline()} is avoided: only available since 4.12.0.</li>
 * </ul>
 *
 * @see ComponentSplitter
 */
public final class ComponentJoiner {

  private ComponentJoiner() {
  }

  /**
   * Joins the given components into one component, putting a newline between every two of them.
   * <p>
   * No trailing newline is added, so an input of {@code n} parts renders as {@code n} lines. Parts
   * that are {@link Component#empty()} contribute a blank line rather than disappearing, because
   * the line break is carried by the separators and not by the parts themselves.
   *
   * @param lines the components to join, in order; each one becomes its own line
   * @return the joined component, or {@link Component#empty()} for an empty input; a single
   * element is handed back untouched
   */
  @NotNull
  @Contract(pure = true)
  public static Component join(@NotNull Collection<? extends Component> lines) {
    return join(lines, Component.newline());
  }

  /**
   * Joins the given components into one component, putting a newline between every two of them.
   *
   * @param lines the components to join, in order; each one becomes its own line
   * @return the joined component, or {@link Component#empty()} for an empty input; a single
   * element is handed back untouched
   * @see #join(Collection)
   */
  @NotNull
  @Contract(pure = true)
  public static Component join(@NotNull Component... lines) {
    return join(Arrays.asList(lines), Component.newline());
  }

  /**
   * Joins the given components into one component, putting {@code separator} between every two of
   * them.
   * <p>
   * The separator should carry no style of its own: it is inserted as a sibling and would
   * otherwise render its formatting in between the parts.
   *
   * @param parts     the components to join, in order
   * @param separator the component inserted between two consecutive parts; the very same instance
   *                  is reused for every gap, which is safe because components are immutable
   * @return the joined component, or {@link Component#empty()} for an empty input; a single
   * element is handed back untouched
   */
  @NotNull
  @Contract(pure = true)
  public static Component join(@NotNull Collection<? extends Component> parts, @NotNull Component separator) {
    final int size = parts.size();
    if (size == 0) return Component.empty();
    // Fast path: nothing to join -> return the original instance untouched.
    if (size == 1) return onlyElement(parts);

    final List<Component> children = new ArrayList<>(size * 2 - 1); // parts + separators
    boolean first = true;
    for (final Component part : parts) {
      if (!first) children.add(separator);
      first = false;
      children.add(part);
    }

    // The root is style-less, so every part renders exactly like it did on its own.
    // children(...) discards Component.empty() parts, but the separators already carry the breaks.
    return Component.empty().children(children);
  }

  /**
   * Reads the only element of a single-element collection, without allocating an iterator for the
   * lists this is called with virtually all of the time.
   */
  private static Component onlyElement(final Collection<? extends Component> parts) {
    if (parts instanceof List<? extends Component> list) return list.getFirst();
    return parts.iterator().next();
  }
}
