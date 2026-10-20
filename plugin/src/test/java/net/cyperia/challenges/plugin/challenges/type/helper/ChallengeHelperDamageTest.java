package net.cyperia.challenges.plugin.challenges.type.helper;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChallengeHelperDamageTest {

  @Test
  void kill_countsOnlyTheHealthLeft() {
    assertEquals(2, ChallengeHelper.getFinalDamage(event(player(2), Float.MAX_VALUE)));
  }

  @Test
  void normalHit_countsInFull() {
    assertEquals(3, ChallengeHelper.getFinalDamage(event(player(20), 3)));
  }

  @Test
  void nonLivingEntity_isNotCapped() {
    assertEquals(50, ChallengeHelper.getFinalDamage(event(mock(Entity.class), 50)));
  }

  private static Player player(double health) {
    Player player = mock(Player.class);
    when(player.getHealth()).thenReturn(health);
    return player;
  }

  private static EntityDamageEvent event(Entity entity, double damage) {
    EntityDamageEvent event = mock(EntityDamageEvent.class);
    when(event.getEntity()).thenReturn(entity);
    when(event.getFinalDamage()).thenReturn(damage);
    when(event.getDamage(DamageModifier.ABSORPTION)).thenReturn(0d);
    return event;
  }

}
