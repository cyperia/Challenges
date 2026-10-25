package net.cyperia.challenges.catalog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// id is the key the plugin saves the challenge's settings under, fallbackIds are ids it was saved under before;
// an entry that is not registered keeps a challenge the plugin does not offer (yet), for example one that is still broken;
// a player without the permission sees the entry locked, or not at all when it is hidden
public record Challenge(@NotNull String id, @NotNull List<String> fallbackIds, @NotNull String key, @NotNull String category,
                        @NotNull Icon icon, @NotNull Definition definition, @Nullable Pairing pairing, boolean excludeFromRandom,
                        boolean featured, boolean registered, @NotNull Release release, @Nullable String permission,
                        boolean hidden) {

  // The trigger and action a challenge with a class of its own plays at heart, its extra rules on top, so a custom
  // challenge builder outside the plugin can point a player who builds that pairing to it
  public record Pairing(@NotNull String trigger, @NotNull String action) {
  }

  @NotNull
  public String nameKey() {
    return "challenge." + key + ".name";
  }

  // The name where the challenge is picked at its default settings, for one whose name leaves out a value
  // the player can change. Optional: without it the plain name applies.
  @NotNull
  public String catalogNameKey() {
    return "challenge." + key + ".catalog-name";
  }

  @NotNull
  public String descriptionKey() {
    return "challenge." + key + ".desc";
  }

}
