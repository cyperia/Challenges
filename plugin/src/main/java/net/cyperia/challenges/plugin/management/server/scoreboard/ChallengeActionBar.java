package net.cyperia.challenges.plugin.management.server.scoreboard;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class ChallengeActionBar {

  private Function<Player, LocalizableMessage> content = player -> null;

  public void setContent(@NotNull Function<Player, LocalizableMessage> content) {
    this.content = content;
  }

  public void send() {
    Challenges.getInstance().getPlayerManager().forEachAudience(this::send);
  }

  public void send(@NotNull Player player) {
    if (!isShown()) {
      Logger.warn("Tried to update actionbar which is not shown");
      return;
    }

    try {
      LocalizableMessage actionbar = content.apply(player);
      // TODO add LocalizableMessage helper methods (same as in MessageKey without args!)
      player.sendActionBar(actionbar.asComponent(player));
    } catch (Exception ex) {
      Logger.error("Unable to update actionbar for player '{}'", player.getName(), ex);
    }
  }

  public boolean isShown() {
    return Challenges.getInstance().getScoreboardManager().isShown(this);
  }

  /**
   * Displaying a challenge actionbar overwrites the timer actionbar!
   */
  public void show() {
    Challenges.getInstance().getScoreboardManager().setCurrentActionBar(this);
  }

  public void hide() {
    if (Challenges.getInstance().getScoreboardManager().getCurrentActionBar() != this) return;
    Challenges.getInstance().getScoreboardManager().setCurrentActionBar(null);
  }

}
