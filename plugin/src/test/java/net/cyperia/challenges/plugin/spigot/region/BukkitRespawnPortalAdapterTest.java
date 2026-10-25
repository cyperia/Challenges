package net.cyperia.challenges.plugin.spigot.region;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.cyperia.challenges.plugin.spigot.events.PlayerRespawnLocationEvent;
import net.cyperia.challenges.plugin.spigot.events.PlayerRespawnedEvent;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.cyperia.challenges.plugin.spigot.events.PortalArrivedEvent;
import net.cyperia.challenges.plugin.spigot.events.PortalTravelEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BukkitRespawnPortalAdapterTest {

  private final BukkitRespawnPortalAdapter adapter = new BukkitRespawnPortalAdapter();
  private final PluginManager pluginManager = mock(PluginManager.class);
  private final List<Event> fired = new ArrayList<>();
  private final World overworld = mock(World.class);
  private final World nether = mock(World.class);
  private final World replacement = mock(World.class);
  private final Player player = mock(Player.class);
  private final List<Runnable> nextTicks = new ArrayList<>();
  private MockedStatic<Bukkit> bukkit;
  private Consumer<Event> listener = _ -> {
  };

  @BeforeEach
  void mockServer() {
    bukkit = org.mockito.Mockito.mockStatic(Bukkit.class);
    bukkit.when(Bukkit::getPluginManager).thenReturn(pluginManager);
    bukkit.when(Bukkit::isPrimaryThread).thenReturn(true);
    doAnswer(invocation -> {
      Event event = invocation.getArgument(0);
      fired.add(event);
      listener.accept(event);
      return null;
    }).when(pluginManager).callEvent(org.mockito.ArgumentMatchers.any(Event.class));
    EntityScheduler playerScheduler = scheduler();
    when(player.getScheduler()).thenReturn(playerScheduler);
  }

  // Runs what an entity schedules for later by hand
  private EntityScheduler scheduler() {
    EntityScheduler scheduler = mock(EntityScheduler.class);
    when(scheduler.runDelayed(any(), any(), any(), org.mockito.ArgumentMatchers.anyLong())).thenAnswer(call -> {
      Consumer<ScheduledTask> task = call.getArgument(1);
      nextTicks.add(() -> task.accept(null));
      return null;
    });
    return scheduler;
  }

  @AfterEach
  void closeServer() {
    bukkit.close();
  }

  @Test
  void respawn_firesTheOwnEventAndAppliesTheChangedLocation() {
    Location original = new Location(overworld, 1, 64, 1);
    Location moved = new Location(replacement, 5, 70, 5);
    listener = event -> {
      if (event instanceof PlayerRespawnLocationEvent respawn) respawn.setRespawnLocation(moved);
    };
    PlayerRespawnEvent event = new PlayerRespawnEvent(player, original, false);

    adapter.onRespawn(event);

    assertEquals(1, fired.size());
    assertInstanceOf(PlayerRespawnLocationEvent.class, fired.getFirst());
    assertEquals(moved, event.getRespawnLocation());
  }

  @Test
  void respawn_leavesTheLocationAloneWhenNobodyChangesIt() {
    Location original = new Location(overworld, 1, 64, 1);
    PlayerRespawnEvent event = spy(new PlayerRespawnEvent(player, original, false));

    adapter.onRespawn(event);

    verify(event, never()).setRespawnLocation(any());
  }

  @Test
  void postRespawn_firesTheOwnEventForThePlayer() {
    adapter.onPostRespawn(new PlayerPostRespawnEvent(player, new Location(overworld, 0, 64, 0), false, false, false, PlayerRespawnEvent.RespawnReason.DEATH));

    PlayerRespawnedEvent respawned = assertInstanceOf(PlayerRespawnedEvent.class, fired.getFirst());
    assertSame(player, respawned.getPlayer());
  }

  @Test
  void playerPortal_carriesTheDestinationAndTheTypeOfThePortal() {
    Location from = new Location(overworld, 10, 64, 10);
    Location to = new Location(nether, 1, 64, 1);

    adapter.onPlayerPortal(new PlayerPortalEvent(player, from, to, TeleportCause.NETHER_PORTAL));

    PortalTravelEvent travel = assertInstanceOf(PortalTravelEvent.class, fired.getFirst());
    assertSame(overworld, travel.getFrom());
    assertSame(nether, travel.getTargetWorld());
    assertSame(to, travel.getDestination());
    assertEquals(PortalType.NETHER, travel.getPortalType());
  }

  @Test
  void playerPortal_endPortalIsTheEnderType() {
    adapter.onPlayerPortal(new PlayerPortalEvent(player, new Location(overworld, 0, 64, 0), new Location(nether, 0, 64, 0), TeleportCause.END_PORTAL));

    assertEquals(PortalType.ENDER, assertInstanceOf(PortalTravelEvent.class, fired.getFirst()).getPortalType());
  }

  @Test
  void playerPortal_appliesARedirectedDestinationAndCancel() {
    Location redirected = new Location(replacement, 3, 64, 3);
    listener = event -> {
      if (event instanceof PortalTravelEvent travel) {
        travel.setDestination(redirected);
        travel.setCancelled(true);
      }
    };
    PlayerPortalEvent event = new PlayerPortalEvent(player, new Location(overworld, 10, 64, 10), new Location(nether, 1, 64, 1), TeleportCause.NETHER_PORTAL);

    adapter.onPlayerPortal(event);

    assertEquals(redirected, event.getTo());
    assertTrue(event.isCancelled());
  }

  @Test
  void entityPortal_usesThePortalTypeOfTheEvent() {
    Entity entity = mock(Entity.class);
    EntityScheduler entityScheduler = scheduler();
    when(entity.getScheduler()).thenReturn(entityScheduler);
    EntityPortalEvent event = new EntityPortalEvent(entity, new Location(overworld, 10, 64, 10), new Location(nether, 1, 64, 1), 128, true, 16, PortalType.NETHER);

    adapter.onEntityPortal(event);

    PortalTravelEvent travel = assertInstanceOf(PortalTravelEvent.class, fired.getFirst());
    assertSame(entity, travel.getEntity());
    assertEquals(PortalType.NETHER, travel.getPortalType());
    assertFalse(event.isCancelled());
  }

  @Test
  void portal_firesTheArrivalOnTheEntitysNextTick() {
    adapter.onPlayerPortal(new PlayerPortalEvent(player, new Location(overworld, 10, 64, 10), new Location(nether, 1, 64, 1), TeleportCause.NETHER_PORTAL));
    assertEquals(1, fired.size());

    nextTicks.getFirst().run();

    PortalArrivedEvent arrived = assertInstanceOf(PortalArrivedEvent.class, fired.get(1));
    assertSame(player, arrived.getEntity());
    assertSame(overworld, arrived.getFrom());
    assertEquals(PortalType.NETHER, arrived.getPortalType());
  }

  @Test
  void cancelledPortal_firesNoArrival() {
    listener = event -> {
      if (event instanceof PortalTravelEvent travel) travel.setCancelled(true);
    };

    adapter.onPlayerPortal(new PlayerPortalEvent(player, new Location(overworld, 10, 64, 10), new Location(nether, 1, 64, 1), TeleportCause.NETHER_PORTAL));

    assertTrue(nextTicks.isEmpty());
  }

  @Test
  void respawnLocationIsSettledAtHighest() throws NoSuchMethodException {
    EventHandler handler = BukkitRespawnPortalAdapter.class.getMethod("onRespawn", PlayerRespawnEvent.class).getAnnotation(EventHandler.class);

    assertEquals(EventPriority.HIGHEST, handler.priority());
  }

  @Test
  void portalWithoutADestinationFiresNothing() {
    adapter.onPlayerPortal(new PlayerPortalEvent(player, new Location(overworld, 0, 64, 0), null, TeleportCause.NETHER_PORTAL));

    assertTrue(fired.isEmpty());
  }

  @Test
  void portalType_mapsEveryCause() {
    assertEquals(PortalType.NETHER, BukkitRespawnPortalAdapter.portalType(TeleportCause.NETHER_PORTAL));
    assertEquals(PortalType.ENDER, BukkitRespawnPortalAdapter.portalType(TeleportCause.END_PORTAL));
    assertEquals(PortalType.END_GATEWAY, BukkitRespawnPortalAdapter.portalType(TeleportCause.END_GATEWAY));
    assertEquals(PortalType.CUSTOM, BukkitRespawnPortalAdapter.portalType(TeleportCause.PLUGIN));
  }

  @Test
  void retargetingAWorldRescalesTheDestination() {
    when(nether.getCoordinateScale()).thenReturn(8.0);
    when(overworld.getCoordinateScale()).thenReturn(1.0);
    PortalTravelEvent travel = new PortalTravelEvent(player, overworld, overworld, PortalType.NETHER, new Location(overworld, 80, 64, 160));

    travel.setTargetWorld(nether);

    assertSame(nether, travel.getDestination().getWorld());
    assertEquals(10, travel.getDestination().getX());
    assertEquals(20, travel.getDestination().getZ());
  }

}
