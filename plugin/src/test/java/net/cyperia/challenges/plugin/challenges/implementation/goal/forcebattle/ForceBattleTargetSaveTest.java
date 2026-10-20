package net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle;

import io.papermc.paper.InternalAPIBridge;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets.BiomeTarget;
import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets.ForceTarget;
import net.cyperia.challenges.plugin.challenges.implementation.goal.forcebattle.targets.PositionTarget;
import net.cyperia.commons.common.collection.pair.Tuple;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.document.GsonDocument;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.UnsafeValues;
import org.bukkit.block.Biome;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ForceBattleTargetSaveTest {

  private static final NamespacedKey MEADOW_KEY = NamespacedKey.minecraft("meadow");

  // Registry and Biome fill their constants from RegistryAccess once, while they initialise, and keep them for the
  // whole test JVM. The registries are therefore created lazily from inside that initialisation and shared.
  private static Biome meadow;
  private static Registry<?> biomes, others;

  private MockedStatic<Bukkit> bukkit;
  private MockedStatic<RegistryAccess> registryAccess;
  private MockedStatic<InternalAPIBridge> apiBridge;
  private ExtremeForceBattleGoal extreme;
  private ForceBiomeBattleGoal biomeBattle;

  @BeforeEach
  void serverWithMeadow() {
    RegistryAccess access = mock(RegistryAccess.class);
    when(access.getRegistry(any(RegistryKey.class))).thenAnswer(invocation -> RegistryKey.BIOME.equals(invocation.getArgument(0)) ? biomes() : others());
    when(access.getRegistry(any(Class.class))).thenAnswer(invocation -> Biome.class.equals(invocation.getArgument(0)) ? biomes() : others());
    registryAccess = mockStatic(RegistryAccess.class);
    registryAccess.when(RegistryAccess::registryAccess).thenReturn(access);
    apiBridge = mockStatic(InternalAPIBridge.class);
    apiBridge.when(InternalAPIBridge::get).thenReturn(mock(InternalAPIBridge.class));

    if (meadow == null) {
      meadow = mock(Biome.class);
      when(meadow.getKey()).thenReturn(MEADOW_KEY);
    }

    UnsafeValues unsafe = mock(UnsafeValues.class);
    when(unsafe.get(eq(RegistryKey.BIOME), any(NamespacedKey.class)))
      .thenAnswer(invocation -> MEADOW_KEY.equals(invocation.getArgument(1)) ? meadow : null);
    bukkit = mockStatic(Bukkit.class);
    bukkit.when(Bukkit::getUnsafe).thenReturn(unsafe);

    extreme = mock(ExtremeForceBattleGoal.class, CALLS_REAL_METHODS);
    biomeBattle = mock(ForceBiomeBattleGoal.class, CALLS_REAL_METHODS);
  }

  private static Registry<?> biomes() {
    if (biomes == null) biomes = registry(key -> MEADOW_KEY.equals(key) ? meadow : null);
    return biomes;
  }

  private static Registry<?> others() {
    if (others == null) others = registry(key -> null);
    return others;
  }

  private static Registry<?> registry(Function<NamespacedKey, Keyed> lookup) {
    return (Registry<?>) Proxy.newProxyInstance(Registry.class.getClassLoader(), new Class<?>[]{Registry.class}, (proxy, method, args) -> {
      if ((method.getName().equals("get") || method.getName().equals("getOrThrow")) && args[0] instanceof Key key) {
        return lookup.apply(new NamespacedKey(key.namespace(), key.value()));
      }
      return switch (method.getName()) {
        case "hashCode" -> System.identityHashCode(proxy);
        case "equals" -> proxy == args[0];
        case "toString" -> "test registry";
        default -> null;
      };
    });
  }

  @AfterEach
  void close() {
    bukkit.close();
    registryAccess.close();
    apiBridge.close();
  }

  private static Document reloaded(Document saved) {
    return new GsonDocument(((GsonDocument) saved).toJson());
  }

  @Test
  void extremeBattleCurrentBiomeSurvivesASaveAndLoad() {
    Document player = new GsonDocument();
    extreme.setTargetInDocument(player, "currentTarget", new BiomeTarget(meadow));

    assertSame(meadow, extreme.getTargetFromDocument(reloaded(player), "currentTarget").getTarget());
  }

  @Test
  void extremeBattleFoundBiomesSurviveASaveAndLoad() {
    Document player = new GsonDocument();
    extreme.setFoundListInDocument(player, "foundTargets", List.of(new BiomeTarget(meadow)));

    assertSame(meadow, extreme.getListFromDocument(reloaded(player), "foundTargets").getFirst().getTarget());
  }

  @Test
  void extremeBattlePositionTargetsSurviveASaveAndLoad() {
    Document player = new GsonDocument();
    extreme.setTargetInDocument(player, "currentTarget", new PositionTarget(12, -34));
    extreme.setFoundListInDocument(player, "foundTargets", List.of(new PositionTarget(56, -78)));

    Document loaded = reloaded(player);

    assertEquals(Tuple.of(12d, -34d), extreme.getTargetFromDocument(loaded, "currentTarget").getTarget());
    assertEquals(Tuple.of(56d, -78d), extreme.getListFromDocument(loaded, "foundTargets").getFirst().getTarget());
  }

  @Test
  void biomeBattleTargetsSurviveASaveAndLoad() {
    Document player = new GsonDocument();
    biomeBattle.setTargetInDocument(player, "currentTarget", new BiomeTarget(meadow));
    biomeBattle.setFoundListInDocument(player, "foundTargets", List.of(new BiomeTarget(meadow)));

    Document loaded = reloaded(player);

    assertSame(meadow, biomeBattle.getTargetFromDocument(loaded, "currentTarget").getTarget());
    assertSame(meadow, biomeBattle.getListFromDocument(loaded, "foundTargets").getFirst().getTarget());
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "\"minecraft:meadow\"",
    "\"MEADOW\"",
    "{\"namespace\":\"minecraft\",\"key\":\"meadow\"}",
    "{\"NAMESPACE\":\"MINECRAFT\",\"KEY\":\"MEADOW\"}",
  })
  void olderSavedBiomesStillLoad(String savedValue) {
    Document extremePlayer = new GsonDocument("{\"currentTarget\":{\"type\":\"BIOME\",\"value\":" + savedValue + "},"
      + "\"foundTargets\":[{\"type\":\"BIOME\",\"value\":" + savedValue + "}]}");
    Document biomePlayer = new GsonDocument("{\"currentTarget\":" + savedValue + ",\"foundTargets\":[" + savedValue + "]}");

    List<ForceTarget<?>> loaded = List.of(
      extreme.getTargetFromDocument(extremePlayer, "currentTarget"),
      extreme.getListFromDocument(extremePlayer, "foundTargets").getFirst(),
      biomeBattle.getTargetFromDocument(biomePlayer, "currentTarget"),
      biomeBattle.getListFromDocument(biomePlayer, "foundTargets").getFirst()
    );
    for (ForceTarget<?> target : loaded) {
      assertSame(meadow, target.getTarget());
    }
  }

}
