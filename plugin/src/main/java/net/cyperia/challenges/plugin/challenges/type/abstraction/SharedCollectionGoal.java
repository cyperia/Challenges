package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.challenges.plugin.challenges.type.abstraction.menu.MenuGoal;
import net.cyperia.challenges.plugin.challenges.type.helper.GoalHelper;
import net.cyperia.challenges.plugin.challenges.type.helper.GoalLeaderboard;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.MessageKeySpace;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.server.scoreboard.ChallengeBossBar.BossBarInstance;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * A goal everyone completes together, in any order or one target after another in a random order, with an
 * optional sidebar ranking players by how much of it they found first.
 */
public abstract class SharedCollectionGoal extends MenuGoal {

  private final BooleanSubSetting rankingSetting;
  private final BooleanSubSetting randomOrderSetting;

  public SharedCollectionGoal(boolean randomOrderByDefault) {
    rankingSetting = registerSetting("ranking", "3.1", new BooleanSubSetting(
      new ItemStack(Material.BOOK), MessageKeySpace.of("challenge.generic.sub.ranking")
    ) {
      @Override
      protected void onEnable() {
        updateRankingVisibility();
      }

      @Override
      protected void onDisable() {
        updateRankingVisibility();
      }
    });
    randomOrderSetting = registerSetting("randomOrder", "3.1", new BooleanSubSetting(
      new ItemStack(Material.COMPASS), MessageKeySpace.of("challenge.generic.sub.random-order"), randomOrderByDefault
    ) {
      @Override
      protected void onEnable() {
        onOrderChange();
      }

      @Override
      protected void onDisable() {
        onOrderChange();
      }
    });
  }

  protected enum FindType {
    ITEM, MOB, ADVANCEMENT
  }

  // A mob is the only find without a finder: a boss dying to an explosion nobody caused
  protected final void announceFind(@NotNull FindType type, @NotNull Object target, @Nullable Player finder) {
    String key = "challenge.generic.found." + type.name().toLowerCase(Locale.ROOT);
    if (finder == null) {
      MessageKey.of(key + "-without-finder").broadcast(Prefix.CHALLENGES, target);
    } else {
      MessageKey.of(key).broadcast(Prefix.CHALLENGES, target, finder);
    }
  }

  // Marks the current target of the random order as done without a finder, null when everything is done
  @Nullable
  protected abstract Object skipCurrentTarget();

  public final void skip(@NotNull CommandSender sender) {
    if (!isRandomOrder()) {
      MessageKey.of("command.goal.skip-any-order").send(sender, Prefix.CHALLENGES);
      SoundSample.BASS_OFF.playIfPlayer(sender);
      return;
    }
    Object skipped = skipCurrentTarget();
    if (skipped == null) {
      MessageKey.of("command.goal.skip-finished").send(sender, Prefix.CHALLENGES);
      SoundSample.BASS_OFF.playIfPlayer(sender);
      return;
    }
    MessageKey.of("challenge.generic.skipped").broadcast(Prefix.CHALLENGES, skipped);
    SoundSample.PLING.broadcast();
    bossbar.update();
  }

  // One entry per find, finds without a player left out
  @NotNull
  protected abstract Collection<UUID> getFinders();

  // A line above the ranking saying what it counts, null for none
  @Nullable
  protected LocalizableMessage getRankingHeading() {
    return null;
  }

  protected abstract void enableGoal();

  protected abstract void disableGoal();

  @Override
  protected final void onEnable() {
    enableGoal();
    scoreboard.setContent(GoalLeaderboard.sharedScoreboardContent(this::getRanking, this::getRankingHeading));
    updateRankingVisibility();
  }

  @Override
  protected final void onDisable() {
    scoreboard.hide();
    disableGoal();
  }

  protected final boolean isRandomOrder() {
    return randomOrderSetting.getAsBoolean();
  }

  // Runs while the settings load too, before the goal itself may be enabled
  protected void onOrderChange() {
    if (isEnabled()) bossbar.update();
  }

  // The Cyperia texture pack draws only the green bar: green for a progress count, white while a next target is shown
  protected static void styleBossbar(@NotNull BossBarInstance bar, boolean showsNextTarget, float progress) {
    bar.setColor(showsNextTarget ? BossBar.Color.WHITE : BossBar.Color.GREEN);
    bar.setProgress(progress);
  }

  protected void updateRanking() {
    if (!scoreboard.isShown()) return;
    scoreboard.update();
  }

  private void updateRankingVisibility() {
    if (isEnabled() && rankingSetting.getAsBoolean()) {
      scoreboard.show();
    } else {
      scoreboard.hide();
    }
  }

  @NotNull
  private Map<OfflinePlayer, Integer> getRanking() {
    Map<UUID, Integer> finds = new HashMap<>();
    for (UUID finder : getFinders()) {
      finds.merge(finder, 1, Integer::sum);
    }
    return GoalHelper.createLeaderboardFromValues(finds, (_, count) -> count);
  }

  // The same seed gives the same order as long as the targets do not change, so a restart keeps it
  @NotNull
  protected static <T> List<T> seededOrder(@NotNull Collection<T> targets, long seed) {
    List<T> order = new ArrayList<>(targets);
    Collections.shuffle(order, new Random(seed));
    return order;
  }

}
