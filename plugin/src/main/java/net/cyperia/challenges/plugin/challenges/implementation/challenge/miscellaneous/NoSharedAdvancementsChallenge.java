package net.cyperia.challenges.plugin.challenges.implementation.challenge.miscellaneous;

import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.commons.common.config.Document;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class NoSharedAdvancementsChallenge extends Setting {

  // org.bukkit.advancement.Advancement seems to not override equals/hashCode making #contain unusable
  private final List<NamespacedKey> advancementsDone = new LinkedList<>();

  public NoSharedAdvancementsChallenge() {
    setUsesGlobalPunish(true);
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onAdvancementDone(PlayerAdvancementDoneEvent event) {
    if (!shouldExecuteEffect()) return;
    NamespacedKey advancementKey = event.getAdvancement().getKey();
    if (advancementKey.toString().contains(":recipes/")) return;
    if (advancementsDone.contains(advancementKey)) {
      getChallengeMessageKey("collected").broadcast(Prefix.CHALLENGES, event.getPlayer(), event.getAdvancement());
      ChallengeHelper.punish(event.getPlayer());
    } else {
      advancementsDone.add(advancementKey);
    }
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    advancementsDone.clear();
    List<String> advancementKeys = document.getStringList("advancements");
    for (String advancementKey : advancementKeys) {
      try {
        NamespacedKey advancement = NamespacedKey.fromString(advancementKey);
        if (advancement == null) continue;
        advancementsDone.add(advancement);
      } catch (Exception _) {
        // DON'T EXIST
      }
    }
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    List<String> foundItems = new LinkedList<>();
    for (NamespacedKey advancement : advancementsDone) {
      foundItems.add(advancement.toString());
    }
    document.set("advancements", foundItems);
  }

}
