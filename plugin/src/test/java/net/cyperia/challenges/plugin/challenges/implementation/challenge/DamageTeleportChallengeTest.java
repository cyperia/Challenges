package net.cyperia.challenges.plugin.challenges.implementation.challenge;

import net.cyperia.challenges.plugin.Challenges;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DamageTeleportChallengeTest {

  @Test
  void aSecondHitWhileATeleportIsPendingStartsNothing() {
    try (MockedStatic<Challenges> challenges = mockStatic(Challenges.class, RETURNS_DEEP_STUBS)) {
      AtomicInteger started = new AtomicInteger();
      CompletableFuture<Void> pending = new CompletableFuture<>();
      challenges.when(() -> Challenges.getInstance().getWorldManager().loadChunkAfterPrefetch(any(), any())).thenAnswer(i -> {
        started.incrementAndGet();
        return pending;
      });
      challenges.when(() -> Challenges.getInstance().getBorderManager().getSize(any())).thenReturn(100d);
      challenges.when(() -> Challenges.getInstance().getBorderManager().getCenter(any())).thenAnswer(i -> new Location(null, 0, 0, 0));
      DamageTeleportChallenge challenge = mock(DamageTeleportChallenge.class, withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS));
      Player player = mock(Player.class);
      when(player.getUniqueId()).thenReturn(UUID.randomUUID());
      when(player.getWorld()).thenReturn(mock(World.class));

      challenge.handleDamage(player);
      challenge.handleDamage(player);

      assertEquals(1, started.get());
      pending.complete(null);
      challenge.handleDamage(player);
      assertEquals(2, started.get());
    }
  }

  @Test
  void aWorldWithoutABorderStillTeleportsNearby() {
    try (MockedStatic<Challenges> challenges = mockStatic(Challenges.class, RETURNS_DEEP_STUBS)) {
      challenges.when(() -> Challenges.getInstance().getBorderManager().getSize(any())).thenReturn(59_999_968d);
      challenges.when(() -> Challenges.getInstance().getBorderManager().getCenter(any())).thenAnswer(i -> new Location(null, 0, 0, 0));
      DamageTeleportChallenge challenge = mock(DamageTeleportChallenge.class, withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS));

      for (int i = 0; i < 100; i++) {
        Location target = challenge.getRandomLocation(mock(World.class));
        assertTrue(Math.abs(target.getX()) <= 2000 && Math.abs(target.getZ()) <= 2000, target.toString());
      }
    }
  }

  @Test
  void aSmallBorderStaysTheLimit() {
    try (MockedStatic<Challenges> challenges = mockStatic(Challenges.class, RETURNS_DEEP_STUBS)) {
      challenges.when(() -> Challenges.getInstance().getBorderManager().getSize(any())).thenReturn(100d);
      challenges.when(() -> Challenges.getInstance().getBorderManager().getCenter(any())).thenAnswer(i -> new Location(null, 0, 0, 0));
      DamageTeleportChallenge challenge = mock(DamageTeleportChallenge.class, withSettings().useConstructor().defaultAnswer(CALLS_REAL_METHODS));

      for (int i = 0; i < 100; i++) {
        Location target = challenge.getRandomLocation(mock(World.class));
        assertTrue(Math.abs(target.getX()) < 50 && Math.abs(target.getZ()) < 50, target.toString());
      }
    }
  }

}
