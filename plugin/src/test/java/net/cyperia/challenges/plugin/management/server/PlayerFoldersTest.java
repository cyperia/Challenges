package net.cyperia.challenges.plugin.management.server;

import net.cyperia.commons.common.misc.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerFoldersTest {

  @Test
  void playerFoldersSurviveTheWorldDeletionEmpty(@TempDir File container) throws IOException {
    File level = new File(container, "world");
    File data = new File(level, "players/data");
    File stats = new File(level, "players/stats");
    assertTrue(data.mkdirs() && stats.mkdirs());
    Files.writeString(new File(data, "player.dat").toPath(), "inventory");

    List<File> folders = WorldManager.listPlayerFolders(level);
    FileUtils.deleteWorldFolder(level);
    folders.forEach(File::mkdirs);

    assertTrue(data.isDirectory());
    assertTrue(stats.isDirectory());
    assertArrayEquals(new String[0], data.list());
  }

  @Test
  void aLevelWithoutPlayerFoldersHasNoneToKeep(@TempDir File container) {
    assertEquals(List.of(), WorldManager.listPlayerFolders(new File(container, "world")));
  }

}
