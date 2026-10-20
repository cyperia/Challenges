package net.cyperia.challenges.catalog;

import org.jetbrains.annotations.NotNull;

// id is the key the plugin saves the challenge's settings under, so it must never change; an entry that is
// not registered keeps a challenge the plugin does not offer (yet), for example one that is still broken
public record Challenge(@NotNull String id, @NotNull String key, @NotNull String category, @NotNull Icon icon,
                        @NotNull Definition definition, boolean excludeFromRandom, boolean registered,
                        @NotNull Release release) {

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
