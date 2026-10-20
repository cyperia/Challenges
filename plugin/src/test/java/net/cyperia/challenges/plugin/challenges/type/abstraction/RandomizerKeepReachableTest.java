package net.cyperia.challenges.plugin.challenges.type.abstraction;

import net.cyperia.commons.common.collection.SeededRandomWrapper;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomizerKeepReachableTest {

  private static final List<String> KEYS = List.of("zombie", "cow", "blaze", "warden", "allay", "snow_golem", "pig");
  private static final List<String> REACHABLE = List.of("zombie", "cow", "blaze", "pig");

  private static Map<String, String> shuffled(long seed) {
    List<String> values = new ArrayList<>(KEYS);
    Collections.shuffle(values, new SeededRandomWrapper(seed));
    Map<String, String> randomization = new HashMap<>();
    for (int i = 0; i < KEYS.size(); i++) randomization.put(KEYS.get(i), values.get(i));
    return randomization;
  }

  @Test
  void blazeAlwaysEndsOnAReachableKeyAndStaysAPermutation() {
    for (long seed = 0; seed < 1000; seed++) {
      Map<String, String> randomization = shuffled(seed);
      Map<String, String> before = new HashMap<>(randomization);
      RandomizerSetting.keepReachable(new SeededRandomWrapper(seed), randomization, REACHABLE, List.of("blaze"));

      String holder = randomization.entrySet().stream().filter(e -> e.getValue().equals("blaze")).findFirst().orElseThrow().getKey();
      assertTrue(REACHABLE.contains(holder), "seed " + seed);

      List<String> valuesBefore = new ArrayList<>(before.values());
      List<String> valuesAfter = new ArrayList<>(randomization.values());
      Collections.sort(valuesBefore);
      Collections.sort(valuesAfter);
      assertEquals(valuesBefore, valuesAfter);

      String original = before.entrySet().stream().filter(e -> e.getValue().equals("blaze")).findFirst().orElseThrow().getKey();
      if (REACHABLE.contains(original)) assertEquals(before, randomization);
    }
  }

}
