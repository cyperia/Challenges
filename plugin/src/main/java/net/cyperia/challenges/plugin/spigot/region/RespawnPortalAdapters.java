package net.cyperia.challenges.plugin.spigot.region;

import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public final class RespawnPortalAdapters {

  static final String CANVAS_EVENT = "io.canvasmc.canvas.event.PlayerRespawnAsyncEvent";
  static final String CANVAS_ADAPTER = "net.cyperia.challenges.plugin.spigot.region.CanvasRespawnPortalAdapter";

  private RespawnPortalAdapters() {
  }

  // Canvas never fires the Bukkit respawn and portal events, Paper and Purpur never fire the Canvas ones
  @NotNull
  public static Listener create() {
    if (!isClassPresent(CANVAS_EVENT)) return new BukkitRespawnPortalAdapter();

    try {
      return (Listener) Class.forName(CANVAS_ADAPTER).getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException ex) {
      throw new IllegalStateException("Canvas was detected but its adapter could not be created", ex);
    }
  }

  static boolean isClassPresent(@NotNull String name) {
    try {
      Class.forName(name, false, RespawnPortalAdapters.class.getClassLoader());
      return true;
    } catch (ClassNotFoundException ex) {
      return false;
    }
  }

}
