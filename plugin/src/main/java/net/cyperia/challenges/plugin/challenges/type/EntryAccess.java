package net.cyperia.challenges.plugin.challenges.type;

import net.cyperia.challenges.catalog.Challenge;
import net.cyperia.challenges.plugin.challenges.custom.CustomChallenge;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// A catalog entry may name a permission: without it a player sees the entry locked, or not at all when it is hidden
public final class EntryAccess {

  private EntryAccess() {
  }

  public static boolean isLocked(@NotNull IChallenge challenge, @NotNull Permissible viewer) {
    Challenge entry = entryOf(challenge);
    return entry != null && isLocked(entry, viewer);
  }

  public static boolean isListed(@NotNull IChallenge challenge, @NotNull Permissible viewer) {
    Challenge entry = entryOf(challenge);
    return entry == null || isListed(entry, viewer);
  }

  public static boolean isHidden(@NotNull IChallenge challenge) {
    Challenge entry = entryOf(challenge);
    return entry != null && entry.hidden();
  }

  public static boolean hasPermission(@NotNull IChallenge challenge) {
    Challenge entry = entryOf(challenge);
    return entry != null && entry.permission() != null;
  }

  static boolean isLocked(@NotNull Challenge entry, @NotNull Permissible viewer) {
    return entry.permission() != null && !viewer.hasPermission(entry.permission());
  }

  static boolean isListed(@NotNull Challenge entry, @NotNull Permissible viewer) {
    return !entry.hidden() || !isLocked(entry, viewer);
  }

  @Nullable
  private static Challenge entryOf(@NotNull IChallenge challenge) {
    if (challenge instanceof CustomChallenge || !(challenge instanceof AbstractChallenge abstractChallenge)) return null;
    return abstractChallenge.getCatalogEntry();
  }

}
