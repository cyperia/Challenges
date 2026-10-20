package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle;

import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets.AdvancementTarget;
import net.cyperia.challenges.plugin.challenges.type.abstraction.ForceBattleGoal;
import net.cyperia.commons.common.config.Document;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ForceAdvancementBattleGoal extends ForceBattleGoal<AdvancementTarget> {

  @Override
  protected AdvancementTarget[] getTargetsPossibleToFind() {
    List<Advancement> advancements = AdvancementTarget.getPossibleAdvancements();
    return advancements.stream().map(AdvancementTarget::new).toArray(AdvancementTarget[]::new);
  }

  @Override
  public AdvancementTarget getTargetFromDocument(Document document, String path) {
    return savedTarget(document.getString(path));
  }

  @Override
  public List<AdvancementTarget> getListFromDocument(Document document, String path) {
    return document.getStringList(path).stream()
      .map(ForceAdvancementBattleGoal::savedTarget)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  // A save can name an advancement this server no longer has, which Bukkit.getAdvancement returns as null
  @Nullable
  private static AdvancementTarget savedTarget(@Nullable String key) {
    NamespacedKey namespacedKey = key == null ? null : NamespacedKey.fromString(key);
    Advancement advancement = namespacedKey == null ? null : Bukkit.getAdvancement(namespacedKey);
    return advancement == null ? null : new AdvancementTarget(advancement);
  }

  @Override
  public void setRandomTarget(Player player) {
    super.setRandomTarget(player);
    AdvancementTarget target = currentTarget.get(player.getUniqueId());
    if (target != null) {
      target.resetIfDone(player);
    }
  }

  // A target completed without being counted, e.g. while the player was ignored, never fires
  // PlayerAdvancementDoneEvent again
  @Override
  public void setRandomTargetIfCurrentlyNone(Player player) {
    AdvancementTarget target = currentTarget.get(player.getUniqueId());
    if (target != null) {
      target.resetIfDone(player);
    }
    super.setRandomTargetIfCurrentlyNone(player);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onAdvancement(PlayerAdvancementDoneEvent event) {
    Player player = event.getPlayer();
    if (!shouldExecuteEffect(player)) return;
    AdvancementTarget target = currentTarget.get(player.getUniqueId());
    if (target != null && target.matches(event.getAdvancement())) {
      handleTargetFound(player);
    }
  }
}
