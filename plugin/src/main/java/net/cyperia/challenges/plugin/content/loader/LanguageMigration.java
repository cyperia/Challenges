package net.cyperia.challenges.plugin.content.loader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Brings an extracted language file up to the bundled one. A text this version changed replaces the extracted one,
 * customized or not, and the extracted texts it replaces are kept apart for a backup.
 */
final class LanguageMigration {

  record Result(@NotNull JsonObject replaced, int replacedCount) {
  }

  private LanguageMigration() {
  }

  /**
   * Updates {@code extracted} in place. {@code previous} is the bundled file it was last migrated to, or null for a
   * file from before that was recorded: then every text that differs from the bundled one is replaced.
   */
  @NotNull
  static Result migrate(@NotNull JsonObject extracted, @NotNull JsonObject bundled, @Nullable JsonObject previous) {
    JsonObject replaced = new JsonObject();
    int count = migrate(extracted, bundled, previous, replaced);
    return new Result(replaced, count);
  }

  // Keys are compared one level at a time, as they are written: a key like "up.*colorize*" is a single key
  private static int migrate(@NotNull JsonObject extracted, @NotNull JsonObject bundled, @Nullable JsonObject previous,
                             @NotNull JsonObject replaced) {
    int count = 0;
    for (Map.Entry<String, JsonElement> entry : bundled.entrySet()) {
      String key = entry.getKey();
      JsonElement ours = entry.getValue();
      JsonElement theirs = extracted.get(key);
      JsonElement before = previous == null ? null : previous.get(key);

      if (ours.isJsonObject() && theirs != null && theirs.isJsonObject()) {
        JsonObject previousSection = previous == null ? null : before != null && before.isJsonObject() ? before.getAsJsonObject() : new JsonObject();
        JsonObject replacedSection = new JsonObject();
        count += migrate(theirs.getAsJsonObject(), ours.getAsJsonObject(), previousSection, replacedSection);
        if (!replacedSection.isEmpty()) replaced.add(key, replacedSection);
        continue;
      }

      if (theirs == null) {
        extracted.add(key, ours.deepCopy());
        continue;
      }
      if (theirs.equals(ours)) continue;
      // our text is unchanged, so theirs is a customization to keep
      if (previous != null && ours.equals(before)) continue;

      if (previous == null || !theirs.equals(before)) {
        replaced.add(key, theirs);
        count++;
      }
      extracted.add(key, ours.deepCopy());
    }
    return count;
  }

}
