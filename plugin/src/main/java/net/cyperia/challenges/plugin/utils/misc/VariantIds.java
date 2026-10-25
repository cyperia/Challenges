package net.cyperia.challenges.plugin.utils.misc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Ids of item and mob variants are their base id with the variant parts in brackets, like
 * {@code minecraft:zombie[baby,holding=iron_sword]}. A find of a variant counts for every less specific id of the
 * same base as well: the base itself and every variant made of some of its parts.
 */
public final class VariantIds {

  private VariantIds() {
  }

  // The base id, which is the whole id for one without variant parts
  @NotNull
  public static String baseOf(@NotNull String id) {
    int variantStart = id.indexOf('[');
    return variantStart < 0 ? id : id.substring(0, variantStart);
  }

  public static boolean covers(@NotNull String found, @NotNull String target) {
    if (found.equals(target) || !baseOf(found).equals(baseOf(target))) return false;
    return parts(found).containsAll(parts(target));
  }

  /**
   * Calls {@code action} with every id of {@code found} and each other id of {@code pool} it {@link #covers covers}.
   */
  public static void forEachCovered(@NotNull Collection<String> found, @NotNull Collection<String> pool,
                                    @NotNull BiConsumer<String, String> action) {
    Map<String, List<String>> poolByBase = new HashMap<>();
    for (String id : pool) poolByBase.computeIfAbsent(baseOf(id), _ -> new ArrayList<>()).add(id);
    for (String id : List.copyOf(found)) {
      for (String target : poolByBase.getOrDefault(baseOf(id), List.of())) {
        if (covers(id, target)) action.accept(id, target);
      }
    }
  }

  @NotNull
  private static Set<String> parts(@NotNull String id) {
    int variantStart = id.indexOf('[');
    if (variantStart < 0 || !id.endsWith("]")) return Set.of();
    return Set.of(id.substring(variantStart + 1, id.length() - 1).split(","));
  }

}
