package net.cyperia.challenges.plugin.challenges.implementation.goal;

import io.papermc.paper.advancement.AdvancementDisplay;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ChecklistGoal;
import net.cyperia.challenges.plugin.challenges.type.abstraction.PointsGoal;
import net.cyperia.challenges.plugin.challenges.type.helper.GoalLeaderboard;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AllAdvancementGoal extends PointsGoal implements ChecklistGoal {

  private final List<Advancement> allAdvancements;
  private final int advancementCount;

  public AllAdvancementGoal() {
    allAdvancements = loadAdvancements();
    advancementCount = allAdvancements.size();
  }

  @NotNull
  static List<Advancement> loadAdvancements() {
    List<Advancement> advancements = new ArrayList<>();
    Bukkit.getServer().advancementIterator().forEachRemaining(advancement -> {
      if (!isRecipe(advancement)) {
        advancements.add(advancement);
      }
    });
    return advancements;
  }

  static boolean isRecipe(@NotNull Advancement advancement) {
    return advancement.getKey().toString().contains(":recipes/");
  }

  @Override
  protected void onEnable() {
    updateAdvancements();
    scoreboard.setContent(GoalLeaderboard.scoreboardContent(this::getLeaderboard,
      _ -> Collections.singletonList(getChallengeMessageKey("bossbar").withArgs(advancementCount))));
    scoreboard.show();
  }

  @Override
  protected void onDisable() {
    scoreboard.hide();
  }

  @Override
  public void getWinnersOnEnd(@NotNull List<Player> winners) {
    // From the points, read on this thread: the winners are used as soon as this returns
    for (Player player : plugin.getPlayerManager().getChallengePlayers()) {
      if (hasWon(player)) winners.add(player);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onAdvancement(PlayerAdvancementDoneEvent event) {
    if (isRecipe(event.getAdvancement())) return;
    if (!shouldExecuteEffect(event.getPlayer())) return;
    updateAdvancements(event.getPlayer());
    scoreboard.update();
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onJoin(PlayerJoinEvent event) {
    if (!shouldExecuteEffect(event.getPlayer())) return;
    updateAdvancements(event.getPlayer());
    scoreboard.update();
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onGamemodeChange(PlayerGameModeChangeEvent event) {
    if (!shouldExecuteEffect()) return;
    Regions.nextTick(plugin, (Runnable) scoreboard::update);
  }

  protected void updateAdvancements() {
    broadcastFiltered(this::updateAdvancements);
  }

  protected void updateAdvancements(@NotNull Player player) {
    int done = 0;
    for (Advancement advancement : allAdvancements) {
      AdvancementProgress progress = player.getAdvancementProgress(advancement);
      if (progress.isDone()) {
        done++;
      }
    }
    setPoints(player.getUniqueId(), done);
    checkForWinning(player);
  }

  protected void checkForWinning(Player player) {
    if (hasWon(player)) {
      ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED);
    }
  }

  protected boolean hasWon(@NotNull Player player) {
    return getPoints(player.getUniqueId()) >= advancementCount;
  }

  // Progress is read from the players themselves, so offline players are not listed as finders
  @NotNull
  @Override
  public List<ChecklistEntry> getChecklist(@NotNull Player viewer) {
    List<ChecklistEntry> entries = new ArrayList<>(advancementCount);
    for (Advancement advancement : allAdvancements) {
      AdvancementDisplay display = advancement.getDisplay();
      if (display == null) continue;
      List<UUID> finders = ChallengeAPI.getIngamePlayers().stream()
        .filter(player -> player.getAdvancementProgress(advancement).isDone())
        .map(Player::getUniqueId)
        .toList();
      entries.add(new ChecklistEntry(display.title(), display.icon(), viewer.getAdvancementProgress(advancement).isDone(), finders));
    }
    return entries;
  }

  @NotNull
  @Override
  public MessageKey getFinderMessage() {
    return MessageKey.of("menu.goal.earned-by");
  }

}
