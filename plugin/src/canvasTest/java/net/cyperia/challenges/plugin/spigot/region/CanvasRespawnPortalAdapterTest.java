package net.cyperia.challenges.plugin.spigot.region;

import io.canvasmc.canvas.event.EntityPortalAsyncEvent;
import io.canvasmc.canvas.event.EntityPostPortalAsyncEvent;
import io.canvasmc.canvas.event.PlayerPostRespawnAsyncEvent;
import io.canvasmc.canvas.event.PlayerRespawnAsyncEvent;
import net.cyperia.challenges.plugin.spigot.events.PlayerRespawnLocationEvent;
import net.cyperia.challenges.plugin.spigot.events.PlayerRespawnedEvent;
import net.cyperia.challenges.plugin.spigot.events.PortalArrivedEvent;
import net.cyperia.challenges.plugin.spigot.events.PortalTravelEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerRespawnEvent.RespawnReason;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class CanvasRespawnPortalAdapterTest {

  private final CanvasRespawnPortalAdapter adapter = new CanvasRespawnPortalAdapter();
  private final PluginManager pluginManager = mock(PluginManager.class);
  private final List<Event> fired = new ArrayList<>();
  private final World overworld = mock(World.class);
  private final World nether = mock(World.class);
  private final World replacement = mock(World.class);
  private final Player player = mock(Player.class);
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
    }).when(pluginManager).callEvent(any(Event.class));
  }

  @AfterEach
  void closeServer() {
    bukkit.close();
  }

  @Test
  void respawn_appliesTheLocationTheOwnEventSettledOn() {
    Location moved = new Location(replacement, 5, 70, 5);
    listener = event -> {
      if (event instanceof PlayerRespawnLocationEvent respawn) respawn.setRespawnLocation(moved);
    };
    PlayerRespawnAsyncEvent event = new PlayerRespawnAsyncEvent(player, new Location(overworld, 1, 64, 1), false, false, false, RespawnReason.DEATH);

    adapter.onRespawn(event);

    assertEquals(moved, event.getRespawnLocation());
  }

  @Test
  void postRespawn_firesTheOwnEvent() {
    adapter.onPostRespawn(new PlayerPostRespawnAsyncEvent(player, new Location(overworld, 1, 64, 1), false, false, false, RespawnReason.DEATH));

    assertSame(player, assertInstanceOf(PlayerRespawnedEvent.class, fired.getFirst()).getPlayer());
  }

  @Test
  void portal_hasNoDestinationAndTakesTheChosenWorldAndCancel() {
    Entity entity = mock(Entity.class);
    listener = event -> {
      if (event instanceof PortalTravelEvent travel) {
        travel.setTargetWorld(replacement);
        travel.setCancelled(true);
      }
    };
    EntityPortalAsyncEvent event = new EntityPortalAsyncEvent(entity, overworld, nether, PortalType.NETHER);

    adapter.onPortal(event);

    PortalTravelEvent travel = assertInstanceOf(PortalTravelEvent.class, fired.getFirst());
    assertEquals(null, travel.getDestination());
    assertEquals(PortalType.NETHER, travel.getPortalType());
    assertSame(replacement, event.getTo());
    assertTrue(event.isCancelled());
  }

  @Test
  void adapterIsPickedByTheCanvasEventsPresence() {
    assertTrue(RespawnPortalAdapters.isClassPresent(RespawnPortalAdapters.CANVAS_EVENT));
    assertInstanceOf(CanvasRespawnPortalAdapter.class, RespawnPortalAdapters.create());
  }

  @Test
  void postPortal_firesTheArrivalForTheEntity() {
    adapter.onPostPortal(new EntityPostPortalAsyncEvent(player, overworld, nether, PortalType.NETHER));

    PortalArrivedEvent arrived = assertInstanceOf(PortalArrivedEvent.class, fired.getFirst());
    assertSame(player, arrived.getEntity());
    assertSame(overworld, arrived.getFrom());
    assertEquals(PortalType.NETHER, arrived.getPortalType());
  }

}
