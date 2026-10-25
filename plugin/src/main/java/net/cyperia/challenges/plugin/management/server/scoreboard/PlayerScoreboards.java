package net.cyperia.challenges.plugin.management.server.scoreboard;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * The one private scoreboard of each player, shared by everything that shows objectives to them. Creating a
 * scoreboard and registering, displaying or unregistering objectives has to happen on the global region of a
 * regionised server, so the map is owned by it. Only {@link Player#setScoreboard} runs on the player's thread.
 */
public final class PlayerScoreboards {

  // Owned by the global region
  private static final Map<UUID, Scoreboard> boards = new HashMap<>();

  private PlayerScoreboards() {
  }

  /**
   * Runs the task on the global region with the player's board, then puts the board on the player
   * unless they are on a scoreboard another plugin set.
   */
  public static void use(@NotNull Player player, @NotNull Consumer<Scoreboard> task) {
    Challenges plugin = Challenges.getInstance();
    Regions.global(plugin, () -> {
      if (!player.isOnline()) return;
      Scoreboard board = boards.computeIfAbsent(player.getUniqueId(), _ -> Bukkit.getScoreboardManager().getNewScoreboard());
      task.accept(board);
      Regions.entity(plugin, player, () -> install(player, board));
    });
  }

  // Skips players who never got a board, there is nothing of ours to remove from theirs
  public static void useExisting(@NotNull Player player, @NotNull Consumer<Scoreboard> task) {
    Regions.global(Challenges.getInstance(), () -> {
      Scoreboard board = boards.get(player.getUniqueId());
      if (board != null) task.accept(board);
    });
  }

  public static void forget(@NotNull Player player) {
    UUID id = player.getUniqueId();
    Regions.global(Challenges.getInstance(), () -> boards.remove(id));
  }

  private static void install(@NotNull Player player, @NotNull Scoreboard board) {
    if (player.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard()) {
      player.setScoreboard(board);
    }
  }

}
