package net.cyperia.challenges.plugin.utils.misc;

import io.papermc.paper.world.flag.FeatureDependant;
import org.bukkit.Keyed;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExperimentalUtilsTest {

  // Paper resolves the registry key of whatever it is asked about, which throws for EntityType.UNKNOWN.
  private static World serverLikeWorld() {
    World world = mock(World.class);
    when(world.isEnabled(any(FeatureDependant.class))).thenAnswer(invocation -> {
      invocation.getArgument(0, Keyed.class).getKey();
      return true;
    });
    return world;
  }

  @Test
  void entityTypesSkipUnknown() {
    List<EntityType> types = List.of(ExperimentalUtils.enabledEntityTypes(serverLikeWorld()));

    assertFalse(types.contains(EntityType.UNKNOWN));
    assertTrue(types.contains(EntityType.ZOMBIE));
  }

}
