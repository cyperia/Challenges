package net.cyperia.commons.bukkit.utils.misc;

import net.cyperia.commons.common.version.Version;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public final class MinecraftVersion {

  private static Version current;

  private MinecraftVersion() {
  }

  @NotNull
  public static Version current() {
    if (current == null) current = Version.parse(Bukkit.getMinecraftVersion());
    return current;
  }

}
