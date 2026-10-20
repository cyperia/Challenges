package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets;

import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdvancementTargetTest {

  private static Advancement advancement(String key) {
    Advancement advancement = mock(Advancement.class);
    when(advancement.getKey()).thenReturn(NamespacedKey.minecraft(key));
    return advancement;
  }

  @Test
  void matchesAnotherWrapperOfTheSameAdvancement() {
    AdvancementTarget target = new AdvancementTarget(advancement("story/smelt_iron"));
    assertTrue(target.matches(advancement("story/smelt_iron")));
    assertFalse(target.matches(advancement("story/mine_stone")));
  }

  @Test
  void targetsOfTheSameAdvancementAreEqual() {
    AdvancementTarget target = new AdvancementTarget(advancement("story/smelt_iron"));
    AdvancementTarget restored = new AdvancementTarget(advancement("story/smelt_iron"));
    assertEquals(target, restored);
    assertEquals(target.hashCode(), restored.hashCode());
    assertNotEquals(target, new AdvancementTarget(advancement("story/mine_stone")));
  }

  private static AdvancementProgress progress(Player player, Advancement advancement, boolean done) {
    AdvancementProgress progress = mock(AdvancementProgress.class);
    when(progress.isDone()).thenReturn(done);
    when(progress.getAwardedCriteria()).thenReturn(List.of("minecraft:plains", "minecraft:desert"));
    when(player.getAdvancementProgress(advancement)).thenReturn(progress);
    return progress;
  }

  @Test
  void resettingADoneAdvancementRemovesEveryAwardedCriterion() {
    Advancement advancement = advancement("adventure/adventuring_time");
    Player player = mock(Player.class);
    AdvancementProgress progress = progress(player, advancement, true);

    new AdvancementTarget(advancement).resetIfDone(player);

    verify(progress).revokeCriteria("minecraft:plains");
    verify(progress).revokeCriteria("minecraft:desert");
  }

  @Test
  void resettingKeepsPartialProgress() {
    Advancement advancement = advancement("adventure/adventuring_time");
    Player player = mock(Player.class);
    AdvancementProgress progress = progress(player, advancement, false);

    new AdvancementTarget(advancement).resetIfDone(player);

    verify(progress, never()).revokeCriteria(anyString());
  }

}
