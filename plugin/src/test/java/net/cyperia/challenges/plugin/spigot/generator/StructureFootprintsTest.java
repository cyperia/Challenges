package net.cyperia.challenges.plugin.spigot.generator;

import org.bukkit.util.BoundingBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructureFootprintsTest {

  @TempDir
  Path directory;

  @Test
  void coversTheChunksAndColumnsOfTheBox() {
    StructureFootprints footprints = new StructureFootprints(directory.resolve("footprints.bin"));
    footprints.add(new BoundingBox(-20, 0, 5, 17, 80, 40), false);

    assertTrue(footprints.covers(-2, 0));
    assertTrue(footprints.covers(1, 2));
    assertFalse(footprints.covers(2, 2));
    assertTrue(footprints.contains(-20, 40));
    assertFalse(footprints.contains(-21, 40));
    assertFalse(footprints.contains(0, 41));
  }

  @Test
  void survivesARestart() {
    Path file = directory.resolve("footprints.bin");
    new StructureFootprints(file).add(new BoundingBox(100, 0, -100, 110, 10, -90), true);
    new StructureFootprints(file).add(new BoundingBox(-300, 0, 300, -290, 10, 310), false);

    StructureFootprints reloaded = new StructureFootprints(file);
    assertTrue(reloaded.contains(105, -95));
    assertTrue(reloaded.contains(-295, 305));
    assertFalse(reloaded.covers(0, 0));
    assertTrue(reloaded.getCarved(6, -7).getFirst().carved());
    assertTrue(reloaded.getCarved(-19, 18).isEmpty());
  }

}
