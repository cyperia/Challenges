package net.cyperia.challenges.plugin.management.server.player;

import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ServerPlayerNameProvider implements PlayerNameProvider {

  @NotNull
  @Override
  public CompletableFuture<Void> load(@NotNull Collection<UUID> players) {
    return CompletableFuture.completedFuture(null);
  }

  @NotNull
  @Override
  public Component name(@NotNull OfflinePlayer player, @Nullable Player viewer) {
    Player online = player.getPlayer();
    return online != null ? online.displayName() : Component.text(Objects.requireNonNullElse(player.getName(), ""));
  }

}
