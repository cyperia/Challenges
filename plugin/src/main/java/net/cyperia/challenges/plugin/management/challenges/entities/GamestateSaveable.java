package net.cyperia.challenges.plugin.management.challenges.entities;

import net.cyperia.commons.common.config.Document;
import org.jetbrains.annotations.NotNull;

public interface GamestateSaveable {

  String getUniqueGamestateName();

  void writeGameState(@NotNull Document document);

  void loadGameState(@NotNull Document document);

}
