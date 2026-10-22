package net.cyperia.challenges.plugin.management.challenges.entities;

import net.cyperia.commons.common.config.Document;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface GamestateSaveable {

  String getUniqueGamestateName();

  // Names its settings and game state were saved under before a rename, read when the current one is missing
  @NotNull
  default List<String> getFallbackNames() {
    return List.of();
  }

  void writeGameState(@NotNull Document document);

  void loadGameState(@NotNull Document document);

}
