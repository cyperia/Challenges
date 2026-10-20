package net.cyperia.commons.bukkit.core;

import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.FileDocument;
import net.cyperia.commons.common.config.document.GsonDocument;
import net.cyperia.commons.common.config.document.PropertiesDocument;
import net.cyperia.commons.common.config.document.YamlDocument;
import net.cyperia.commons.common.misc.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SimpleConfigManager {

  protected final Map<String, FileDocument> configs = new HashMap<>();
  protected final BukkitModule module;

  public SimpleConfigManager(@NotNull BukkitModule module) {
    this.module = module;
  }

  @NotNull
  public FileDocument getDocument(@NotNull String filename) {
    return getDocument(getFile(filename));
  }

  @NotNull
  public File getFile(@NotNull String filename) {
    if (!filename.contains(".")) filename += ".json";
    return module.getDataFile(filename);
  }

  public void saveDefault(@NotNull String filename) {
    module.saveResource(filename, false);
  }

  public synchronized FileDocument getDocument(@NotNull File file) {
    String extension = FileUtils.getFileExtension(file);
    return configs.computeIfAbsent(file.getName(), _ -> FileDocument.readFile(resolveType(extension), file));
  }

  @NotNull
  public static Class<? extends Document> resolveType(@NotNull String extension) {
    return switch (extension.toLowerCase()) {
      case "json" -> GsonDocument.class;
      case "yml", "yaml" -> YamlDocument.class;
      case "properties" -> PropertiesDocument.class;
      default -> throw new IllegalArgumentException("Unknown document file extension '" + extension + "'");
    };
  }

}
