package net.cyperia.challenges.plugin.challenges.type.helper;

import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.server.scoreboard.ChallengeScoreboard.ScoreboardInstance;
import net.cyperia.commons.common.collection.NumberFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * Ranking and rendering of goal leaderboards, shared by the scoreboard sidebar and the chat leaderboard.
 */
public final class GoalLeaderboard {

  public static final int SCOREBOARD_ENTRIES = 9;

  private static final TextColor GOLD = TextColor.color(0xFFD700);
  private static final TextColor SILVER = TextColor.color(0xC0C0C0);
  private static final TextColor BRONZE = TextColor.color(0xCD7F32);
  private static final TextColor DIM_GRAY = TextColor.color(0x7A7A7A);

  // the sidebar is rebuilt on every update, so every place it can reach is precomputed
  private static final Component[] PLACES = new Component[16];

  static {
    for (int i = 0; i < PLACES.length; i++) {
      PLACES[i] = buildPlace(i + 1);
    }
  }

  private GoalLeaderboard() {
  }

  // TODO extract styling via localization
  @NotNull
  public static TextColor placeColor(int place) {
    return switch (place) {
      case 1 -> GOLD;
      case 2 -> SILVER;
      case 3 -> BRONZE;
      default -> DIM_GRAY;
    };
  }

  @NotNull
  public static Component placeComponent(int place) {
    return place >= 1 && place <= PLACES.length ? PLACES[place - 1] : buildPlace(place);
  }

  @NotNull
  private static Component buildPlace(int place) {
    return Component.text("#" + place, placeColor(place));
  }

  /**
   * @param viewer player the name is rendered for, whose own name is underlined; {@code null} to never underline
   */
  @NotNull
  public static Component nameComponent(@NotNull OfflinePlayer player, @Nullable Player viewer) {
    Component name = Challenges.getInstance().getPlayerManager().getNameProvider().name(player, viewer);
    return viewer != null && viewer.getUniqueId().equals(player.getUniqueId())
      ? name.decorate(TextDecoration.UNDERLINED) : name;
  }

  @NotNull
  public static BiConsumer<ScoreboardInstance, Player> scoreboardContent(@NotNull Supplier<Map<Player, Integer>> points) {
    return scoreboardContent(points, null);
  }

  @NotNull
  public static BiConsumer<ScoreboardInstance, Player> scoreboardContent(@NotNull Supplier<Map<Player, Integer>> points,
                                                                         @Nullable Function<Player, List<LocalizableMessage>> additionalLines) {
    return content(points, null, additionalLines);
  }

  /**
   * The same leaderboard, with one more column behind every entry describing what that player is
   * currently up to (force battles put the target they are hunting there).
   */
  @NotNull
  public static BiConsumer<ScoreboardInstance, Player> scoreboardContentWithDetail(@NotNull Supplier<Map<Player, Integer>> points,
                                                                                   @NotNull Function<Player, LocalizableMessage> entryDetail) {
    return content(points, entryDetail, null);
  }

  @NotNull
  private static BiConsumer<ScoreboardInstance, Player> content(@NotNull Supplier<Map<Player, Integer>> points,
                                                                @Nullable Function<Player, LocalizableMessage> entryDetail,
                                                                @Nullable Function<Player, List<LocalizableMessage>> additionalLines) {
    return (board, viewer) -> {
      Ranking<Player> ranking = rank(points.get(), Integer::intValue);

      board.addEmptyLine();
      board.addLine(MessageKey.of("scoreboard.leaderboard.placement"), placeComponent(ranking.placeOf(viewer)));
      board.addEmptyLine();

      MessageKey entryKey = MessageKey.of(entryDetail == null
        ? "scoreboard.leaderboard.entry" : "scoreboard.leaderboard.entry-detail");
      int entries = Math.min(ranking.size(), SCOREBOARD_ENTRIES);
      for (int i = 0; i < entries; i++) {
        Player ranked = ranking.keyAt(i);
        Component place = placeComponent(ranking.placeAt(i));
        Component name = nameComponent(ranked, viewer);
        String score = NumberFormatter.MIDDLE_NUMBER.format(ranking.scoreAt(i));

        if (entryDetail == null) {
          board.addLine(entryKey, place, name, score);
        } else {
          board.addLine(entryKey, place, name, score, entryDetail.apply(ranked));
        }
      }
      board.addEmptyLine();

      if (additionalLines == null) return;
      List<LocalizableMessage> lines = additionalLines.apply(viewer);
      int remaining = board.getRemainingLines() - 1;
      for (int i = 0; i < lines.size() && remaining > 0; i++, remaining--) {
        board.addLine(lines.get(i));
      }
    };
  }

  /**
   * Writes the same ranking into chat, for goals that show a result once instead of a live sidebar.
   * Offline players stay listed, so leaving right before the end does not erase a score.
   *
   * @param title    headline above the entries, usually the goal's own {@code leaderboard} key
   * @param receiver player to send it to, or {@code null} to broadcast it to everyone
   */
  public static void sendChatLeaderboard(@NotNull MessageKey title, @NotNull Ranking<UUID> ranking, @Nullable Player receiver) {
    sendOrBroadcast(title, receiver);

    MessageKey entryKey = MessageKey.of("challenge.generic.leaderboard-entry");
    for (int i = 0; i < ranking.size(); i++) {
      sendOrBroadcast(entryKey, receiver, placeComponent(ranking.placeAt(i)),
        nameComponent(Bukkit.getOfflinePlayer(ranking.keyAt(i)), receiver), ranking.scoreAt(i));
    }
  }

  private static void sendOrBroadcast(@NotNull MessageKey message, @Nullable Player receiver, @NotNull Object... args) {
    if (receiver == null) {
      message.broadcast(Prefix.CHALLENGES, args);
    } else {
      message.send(receiver, Prefix.CHALLENGES, args);
    }
  }

  /**
   * Ranks {@code values} by score, highest first, with dense places: equal scores share one ({@code 1, 2, 2, 3}).
   */
  @NotNull
  public static <K, V> Ranking<K> rank(@NotNull Map<K, V> values, @NotNull ToIntFunction<V> score) {
    List<Entry<K, V>> sorted = new ArrayList<>(values.entrySet());
    sorted.sort((left, right) -> Integer.compare(score.applyAsInt(right.getValue()), score.applyAsInt(left.getValue())));

    int size = sorted.size();
    Object[] keys = new Object[size];
    int[] scores = new int[size];
    int[] places = new int[size];
    int place = 0;
    for (int i = 0; i < size; i++) {
      Entry<K, V> entry = sorted.get(i);
      int value = score.applyAsInt(entry.getValue());
      if (i == 0 || value != scores[i - 1]) place++;
      keys[i] = entry.getKey();
      scores[i] = value;
      places[i] = place;
    }
    return new Ranking<>(keys, scores, places);
  }

  public static final class Ranking<K> {

    private final Object[] keys;
    private final int[] scores;
    private final int[] places;

    private Ranking(@NotNull Object[] keys, @NotNull int[] scores, @NotNull int[] places) {
      this.keys = keys;
      this.scores = scores;
      this.places = places;
    }

    public int size() {
      return keys.length;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public K keyAt(int index) {
      return (K) keys[index];
    }

    public int scoreAt(int index) {
      return scores[index];
    }

    public int placeAt(int index) {
      return places[index];
    }

    /**
     * @return the place of {@code key}, or the place right behind the last ranked one if it is not ranked
     */
    public int placeOf(@NotNull K key) {
      for (int i = 0; i < keys.length; i++) {
        if (key.equals(keys[i])) return places[i];
      }
      return keys.length == 0 ? 1 : places[keys.length - 1] + 1;
    }
  }

}
