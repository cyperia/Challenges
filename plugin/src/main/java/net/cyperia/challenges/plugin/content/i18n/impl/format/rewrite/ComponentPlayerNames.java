package net.cyperia.challenges.plugin.content.i18n.impl.format.rewrite;

import net.cyperia.challenges.plugin.content.i18n.impl.format.ComponentArguments;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Replaces the plain player names inside a <b>server generated</b> message (death messages,
 * join/quit messages, ...) with the {@link Player#displayName() display name} of the matching
 * online player, so forwarded vanilla messages show the same (colored) names as every message
 * of ours does via {@link ComponentArguments#convertToComponent(Object)}.
 * <p>
 * Vanilla passes the involved entities' names as {@link TranslatableComponent} <i>arguments</i>
 * (not children) of those messages, e.g. {@code death.attack.player -> [victim, killer]}, which is
 * what {@link ComponentRewriter} walks. Each name carries a
 * {@link HoverEvent.ShowEntity show_entity} hover holding the entity's uuid, and that uuid — not
 * the rendered text — is what identifies a player here: a mob or a custom named item that happens
 * to read like a player name is never touched, and neither is an offline player, who has no
 * display name to insert.
 */
public final class ComponentPlayerNames {

  /**
   * Swaps the name of every online player a message mentions for their display name.
   */
  public static final ComponentRewriter.Rewrite REWRITE = ComponentPlayerNames::replaceName;

  private ComponentPlayerNames() {
  }

  @Nullable
  private static Component replaceName(@NotNull Component component) {
    HoverEvent<?> hover = component.hoverEvent();
    if (hover == null || !(hover.value() instanceof HoverEvent.ShowEntity entity)) return null;

    Player player = Bukkit.getPlayer(entity.id());
    // the whole name component gets replaced, including the parts a scoreboard team added to it
    return player != null ? displayNameOf(player, component) : null;
  }

  /**
   * Vanilla name components are interactive: hovering shows the entity, clicking suggests
   * {@code /tell <name>} and shift clicking inserts the name into the chat box. A display name
   * usually carries none of that, so those are taken over from the name we replace unless the
   * display name defines its own.
   */
  @NotNull
  private static Component displayNameOf(@NotNull Player player, @NotNull Component vanillaName) {
    // fallback to yellow color to fit generic Challenges message style
    Component displayName = player.displayName().colorIfAbsent(ComponentRewriter.REWRITE_COLOR);
    return displayName.style(displayName.style().merge(vanillaName.style(),
      Style.Merge.Strategy.IF_ABSENT_ON_TARGET, Style.Merge.EVENTS, Style.Merge.INSERTION));
  }

}
