package net.cyperia.challenges.plugin.management.server.player;

import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerNameProvider {

  // completes once name() can render these players without waiting, so a menu listing them opens after it
  @NotNull
  CompletableFuture<Void> load(@NotNull Collection<UUID> players);

  @NotNull
  Component name(@NotNull OfflinePlayer player, @Nullable Player viewer);

}
