package net.cyperia.challenges.catalog;

import org.jetbrains.annotations.NotNull;

// menu is the name of the menu the category is listed in: challenges, goal, settings or custom
public record Category(@NotNull String id, @NotNull String menu, int priority, @NotNull Icon icon) {

  @NotNull
  public String key() {
    return "category." + id;
  }

  @NotNull
  public String nameKey() {
    return key() + ".name";
  }

  @NotNull
  public String descriptionKey() {
    return key() + ".desc";
  }

}
