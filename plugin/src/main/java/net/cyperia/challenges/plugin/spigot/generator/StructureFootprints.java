package net.cyperia.challenges.plugin.spigot.generator;

import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// the columns the kept structures of a world stand on, saved next to it because a structure is only announced once, when it is planned;
// only the columns, as several structures move up or down after they are planned
public final class StructureFootprints {

  private final Path file;
  private final Map<Long, List<Footprint>> byChunk = new ConcurrentHashMap<>();

  public StructureFootprints(@NotNull Path file) {
    this.file = file;
    if (!Files.exists(file)) return;
    try (DataInputStream in = new DataInputStream(Files.newInputStream(file))) {
      while (true) index(new Footprint(in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readBoolean()));
    } catch (EOFException end) {
      // the file is a plain list of footprints, so its end is the end of the list
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }

  public boolean covers(int chunkX, int chunkZ) {
    return byChunk.containsKey(key(chunkX, chunkZ));
  }

  // the structures whose pieces only replace blocks that are already there, like a stronghold carving itself into stone
  @NotNull
  public List<Footprint> getCarved(int chunkX, int chunkZ) {
    return byChunk.getOrDefault(key(chunkX, chunkZ), List.of()).stream().filter(Footprint::carved).toList();
  }

  public boolean contains(int x, int z) {
    for (Footprint footprint : byChunk.getOrDefault(key(x >> 4, z >> 4), List.of())) {
      if (footprint.contains(x, z)) return true;
    }
    return false;
  }

  public synchronized void add(@NotNull BoundingBox box, boolean carved) {
    Footprint footprint = new Footprint((int) Math.floor(box.getMinX()), (int) Math.floor(box.getMinY()), (int) Math.floor(box.getMinZ()),
      (int) Math.floor(box.getMaxX()), (int) Math.floor(box.getMaxY()), (int) Math.floor(box.getMaxZ()), carved);
    try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
      Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND)))) {
      out.writeInt(footprint.minX());
      out.writeInt(footprint.minY());
      out.writeInt(footprint.minZ());
      out.writeInt(footprint.maxX());
      out.writeInt(footprint.maxY());
      out.writeInt(footprint.maxZ());
      out.writeBoolean(footprint.carved());
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
    index(footprint);
  }

  private void index(@NotNull Footprint footprint) {
    for (int chunkX = footprint.minX() >> 4; chunkX <= footprint.maxX() >> 4; chunkX++) {
      for (int chunkZ = footprint.minZ() >> 4; chunkZ <= footprint.maxZ() >> 4; chunkZ++) {
        byChunk.computeIfAbsent(key(chunkX, chunkZ), key -> new CopyOnWriteArrayList<>()).add(footprint);
      }
    }
  }

  private static long key(int chunkX, int chunkZ) {
    return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
  }

  // the height only holds for a carved structure, the others move up or down after they are planned
  public record Footprint(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean carved) {

    boolean contains(int x, int z) {
      return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

  }

}
