package net.cyperia.commons.common.misc;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.cyperia.commons.common.collection.WrappedException;
import net.cyperia.commons.common.function.ExceptionallyConsumer;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class FileUtils {

  public static final InputStream EMPTY_STREAM = new ByteArrayInputStream(new byte[0]);
  private static final Map<String, String> ZIP_FILE_SYSTEM_PROPERTIES = ImmutableMap
    .of("create", "false", "encoding", "UTF-8");
  private static Path tempDirectory;

  private FileUtils() {
  }

  @Nullable
  public static Path getTempDirectory() {
    return tempDirectory;
  }

  public static void setTempDirectory(@Nullable Path tempDirectory) {
    FileUtils.tempDirectory = tempDirectory;
  }

  @NotNull
  public static BufferedWriter newBufferedWriter(@NotNull File file) throws IOException {
    return newBufferedWriter(file.toPath());
  }

  @NotNull
  public static BufferedReader newBufferedReader(@NotNull File file) throws IOException {
    return newBufferedReader(file.toPath());
  }

  @NotNull
  public static BufferedWriter newBufferedWriter(@NotNull Path file) throws IOException {
    return Files.newBufferedWriter(file, StandardCharsets.UTF_8);
  }

  @NotNull
  public static BufferedReader newBufferedReader(@NotNull Path file) throws IOException {
    return Files.newBufferedReader(file, StandardCharsets.UTF_8);
  }

  @NotNull
  public static String getFileExtension(@NotNull File file) {
    return getFileExtension(file.getName());
  }

  @NotNull
  public static String getFileExtension(@NotNull Path file) {
    return getFileExtension(file.toString());
  }

  @NotNull
  public static String getFileExtension(@NotNull String filename) {
    return StringUtils.getAfterLastIndex(filename, ".").toLowerCase();
  }

  @NotNull
  public static String getFileName(@NotNull File file) {
    return getFileName(file.getName());
  }

  @NotNull
  public static String getFileName(@NotNull Path file) {
    return getFileName(file.toString());
  }

  @NotNull
  public static String getFileName(@NotNull String filename) {
    filename = stripFolders(filename);
    int index = filename.lastIndexOf('.');
    if (index == -1) return filename;
    return filename.substring(0, index);
  }

  @NotNull
  public static String getRealFileName(@NotNull File file) {
    return getRealFileName(file.getName());
  }

  @NotNull
  public static String getRealFileName(@NotNull Path file) {
    return getRealFileName(file.toString());
  }

  @NotNull
  public static String getRealFileName(@NotNull String filename) {
    return stripFolders(filename);
  }

  @NotNull
  @CheckReturnValue
  private static String stripFolders(@NotNull String filename) {
    int index = filename.lastIndexOf(File.pathSeparator);
    if (index == -1) return filename;
    return filename.substring(index + 1);
  }

  public static void createFilesIfNecessary(@NotNull File file) throws IOException {
    if (file.exists()) return;

    if (file.isDirectory()) {
      file.mkdirs();
    } else if (file.getParentFile() != null) {
      file.getParentFile().mkdirs();
    }

    file.createNewFile();
  }

  /**
   * Deletes a world folder completely, directories included.
   *
   * <p>Emptying the folder but keeping its directories is not enough: since the per-dimension world
   * layout, a world folder that still exists is a world the server expects to load, and it aborts
   * startup with {@code Unable to read or access the world gen settings file} because the file the
   * folder promises was just deleted. A world that is gone from disk is simply generated again.
   */
  public static void deleteWorldFolder(@NotNull File path) {
    if (!path.exists()) return;

    File[] files = path.listFiles();
    if (files != null) {
      for (File currentFile : files) {
        if (currentFile.isDirectory()) {
          deleteWorldFolder(currentFile);
        } else {
          currentFile.delete();
        }
      }
    }

    path.delete();
  }

  public static byte[] toByteArray(@Nullable InputStream inputStream) {
    return toByteArray(inputStream, new byte[8192]);
  }

  public static byte[] toByteArray(@Nullable InputStream inputStream, byte[] buffer) {
    if (inputStream == null) {
      return null;
    }

    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
      copy(inputStream, byteArrayOutputStream, buffer);
      return byteArrayOutputStream.toByteArray();
    } catch (IOException exception) {
      exception.printStackTrace();
    }

    return null;
  }

  public static void openZipFileSystem(@NotNull Path path, @NotNull ExceptionallyConsumer<? super FileSystem> consumer) {
    try (FileSystem fileSystem = FileSystems
      .newFileSystem(URI.create("jar:" + path.toUri()), ZIP_FILE_SYSTEM_PROPERTIES)) {
      consumer.accept(fileSystem);
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
  }

  public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
    copy(inputStream, outputStream, new byte[8192]);
  }

  public static void copy(InputStream inputStream, OutputStream outputStream, byte[] buffer) throws IOException {
    copy(inputStream, outputStream, buffer, null);
  }

  public static void copy(InputStream inputStream, OutputStream outputStream, byte[] buffer,
                          Consumer<Integer> lengthInputListener) throws IOException {
    int len;
    while ((len = inputStream.read(buffer, 0, buffer.length)) != -1) {
      if (lengthInputListener != null) {
        lengthInputListener.accept(len);
      }

      outputStream.write(buffer, 0, len);
      outputStream.flush();
    }
  }

  public static void copy(Path from, Path to) throws IOException {
    copy(from, to, new byte[8192]);
  }

  public static void copy(Path from, Path to, byte[] buffer) throws IOException {
    if (from == null || to == null || !Files.exists(from)) {
      return;
    }

    if (Files.notExists(to)) {
      createDirectory(to.getParent());
    }

    try (InputStream stream = Files.newInputStream(from); OutputStream target = Files.newOutputStream(to)) {
      copy(stream, target, buffer);
    }
  }

  public static void copyFilesToDirectory(Path from, Path to) {
    walkFileTree(from, (root, current) -> {
      if (!Files.isDirectory(current)) {
        try {
          FileUtils.copy(current, to.resolve(from.relativize(current)));
        } catch (IOException exception) {
          exception.printStackTrace();
        }
      }
    });
  }

  public static void copyFilesToDirectory(Path from, Path to, DirectoryStream.Filter<Path> filter) {
    if (filter == null) {
      copyFilesToDirectory(from, to);
    } else {
      walkFileTree(from, (root, current) -> {
        if (!Files.isDirectory(current)) {
          try {
            FileUtils.copy(current, to.resolve(from.relativize(current)));
          } catch (IOException exception) {
            exception.printStackTrace();
          }
        }
      }, true, filter);
    }
  }

  public static void delete(Path file) {
    if (file == null || Files.notExists(file)) {
      return;
    }

    if (Files.isDirectory(file)) {
      walkFileTree(file, (root, current) -> FileUtils.deleteFile(current));
    }

    FileUtils.deleteFile(file);
  }

  public static long size(@NotNull Path path) {
    try {
      return Files.size(path);
    } catch (IOException ex) {
      throw new WrappedException(ex);
    }
  }

  /**
   * Converts a bunch of directories to a byte array
   *
   * @param directories The directories which should get converted
   * @return A byte array of a zip file created from the provided directories
   * @deprecated May cause a heap space (over)load
   */
  @Deprecated
  public static byte[] convert(Path... directories) {
    if (directories == null) {
      return emptyZipByteArray();
    }

    try (ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream()) {
      try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteBuffer, StandardCharsets.UTF_8)) {
        for (Path directory : directories) {
          zipDir(zipOutputStream, directory, null);
        }
      }

      return byteBuffer.toByteArray();

    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return emptyZipByteArray();
  }

  @NotNull
  public static Path createTempFile() {
    if (tempDirectory != null)
      return createTempFile(UUID.randomUUID());

    try {
      File file = File.createTempFile(UUID.randomUUID().toString(), null);
      file.deleteOnExit();
      return file.toPath();
    } catch (IOException ex) {
      throw new WrappedException(ex);
    }
  }

  @NotNull
  public static Path createTempFile(@NotNull UUID uuid) {
    Preconditions.checkNotNull(tempDirectory, "The temp directory cannot be null");
    Path file = tempDirectory.resolve(uuid.toString());
    createFile(file);
    return file;
  }

  public static void setAttribute(@NotNull Path path, @NotNull String attribute, @Nullable Object value, @NotNull LinkOption... options) {
    try {
      Files.setAttribute(path, attribute, value, options);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public static void setHiddenAttribute(@NotNull Path path, boolean hidden) {
    setAttribute(path, "dos:hidden", hidden, LinkOption.NOFOLLOW_LINKS);
  }

  @NotNull
  public static InputStream zipToStream(@NotNull Path directory) throws IOException {
    return zipToStream(directory, null);
  }

  @NotNull
  public static InputStream zipToStream(@NotNull Path directory, @Nullable Predicate<Path> fileFilter) throws IOException {
    Path target = createTempFile();
    zipToFile(directory, target, path -> !target.equals(path) && (fileFilter == null || fileFilter.test(path)));
    return Files.newInputStream(target, StandardOpenOption.DELETE_ON_CLOSE, LinkOption.NOFOLLOW_LINKS);
  }

  @Nullable
  public static Path zipToFile(Path directory, Path target) {
    return zipToFile(directory, target, null);
  }

  @Nullable
  public static Path zipToFile(Path directory, Path target, Predicate<Path> fileFilter) {
    if (directory == null || !Files.exists(directory)) {
      return null;
    }

    delete(target);
    try (OutputStream outputStream = Files.newOutputStream(target, StandardOpenOption.CREATE)) {
      zipStream(directory, outputStream, fileFilter);
      return target;
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return null;
  }

  private static void zipStream(Path source, OutputStream buffer, Predicate<Path> fileFilter) throws IOException {
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(buffer, StandardCharsets.UTF_8)) {
      if (Files.exists(source)) {
        zipDir(zipOutputStream, source, fileFilter);
      }
    }
  }

  private static void zipDir(ZipOutputStream zipOutputStream, Path directory, Predicate<Path> fileFilter) throws IOException {
    Files.walkFileTree(
      directory,
      new SimpleFileVisitor<Path>() {
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          if (fileFilter != null && !fileFilter.test(file)) {
            return FileVisitResult.CONTINUE;
          }

          try {
            zipOutputStream.putNextEntry(new ZipEntry(directory.relativize(file).toString().replace("\\", "/")));
            Files.copy(file, zipOutputStream);
            zipOutputStream.closeEntry();
          } catch (IOException ex) {
            zipOutputStream.closeEntry();
            throw ex;
          }
          return FileVisitResult.CONTINUE;
        }
      }
    );
  }

  public static Path extract(Path zipPath, Path targetDirectory) throws IOException {
    if (zipPath == null || targetDirectory == null || !Files.exists(zipPath)) {
      return targetDirectory;
    }

    try (InputStream input = Files.newInputStream(zipPath)) {
      return extract(input, targetDirectory);
    }
  }

  public static Path extract(InputStream input, Path targetDirectory) throws IOException {
    if (input == null || targetDirectory == null) {
      return targetDirectory;
    }

    extract0(new ZipInputStream(input, StandardCharsets.UTF_8), targetDirectory);

    return targetDirectory;
  }

  public static Path extract(byte[] zipData, Path targetDirectory) throws IOException {
    if (zipData == null || zipData.length == 0 || targetDirectory == null) {
      return targetDirectory;
    }

    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipData)) {
      extract0(new ZipInputStream(byteArrayInputStream, StandardCharsets.UTF_8), targetDirectory);
    }

    return targetDirectory;
  }

  public static void extract0(ZipInputStream zipInputStream, Path targetDirectory) throws IOException {
    ZipEntry zipEntry;
    while ((zipEntry = zipInputStream.getNextEntry()) != null) {
      extractEntry(zipInputStream, zipEntry, targetDirectory);
      zipInputStream.closeEntry();
    }
  }

  public static byte[] emptyZipByteArray() {
    byte[] bytes = null;

    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
      ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream, StandardCharsets.UTF_8);
      zipOutputStream.close();

      bytes = byteArrayOutputStream.toByteArray();
    } catch (IOException exception) {
      exception.printStackTrace();
    }

    return bytes;
  }

  private static void extractEntry(ZipInputStream zipInputStream, ZipEntry zipEntry, Path targetDirectory)
    throws IOException {
    Path file = targetDirectory.resolve(zipEntry.getName());
    ensureChild(targetDirectory, file);

    if (zipEntry.isDirectory()) {
      if (!Files.exists(file)) {
        Files.createDirectories(file);
      }
    } else {
      Path parent = file.getParent();
      if (!Files.exists(parent)) {
        Files.createDirectories(parent);
      }

      if (Files.exists(file)) {
        Files.delete(file);
      }

      Files.createFile(file);
      try (OutputStream outputStream = Files.newOutputStream(file)) {
        copy(zipInputStream, outputStream);
      }
    }
  }

  public static void walkFileTree(Path rootDirectoryPath, BiConsumer<Path, Path> consumer) {
    walkFileTree(rootDirectoryPath, consumer, true);
  }

  public static void walkFileTree(Path rootDirectoryPath, BiConsumer<Path, Path> consumer, boolean visitDirectories) {
    walkFileTree(rootDirectoryPath, consumer, visitDirectories, "*");
  }

  public static void walkFileTree(Path rootDirectoryPath, BiConsumer<Path, Path> consumer, boolean visitDirectories,
                                  String glob) {
    if (Files.notExists(rootDirectoryPath)) {
      return;
    }
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootDirectoryPath, glob)) {
      for (Path path : stream) {
        if (Files.isDirectory(path) && visitDirectories) {
          walkFileTree(path, consumer, true, glob);
        }
        consumer.accept(rootDirectoryPath, path);
      }
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  public static void walkFileTree(Path rootDirectoryPath, BiConsumer<Path, Path> consumer, boolean visitDirectories,
                                  DirectoryStream.Filter<Path> filter) {
    if (Files.notExists(rootDirectoryPath)) {
      return;
    }
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(rootDirectoryPath, filter)) {
      for (Path path : stream) {
        if (Files.isDirectory(path) && visitDirectories) {
          walkFileTree(path, consumer, true, filter);
        }
        consumer.accept(rootDirectoryPath, path);
      }
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  public static void createDirectory(@Nullable Path directoryPath) {
    if (directoryPath != null && Files.notExists(directoryPath)) {
      try {
        Files.createDirectories(directoryPath);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  public static void createFile(@Nullable Path filePath) {
    if (filePath != null && Files.notExists(filePath)) {
      try {
        if (filePath.getParent() != null)
          Files.createDirectories(filePath.getParent());
        Files.createFile(filePath);
      } catch (IOException ex) {
      }
    }
  }

  public static void deleteFile(Path file) {
    try {
      Files.deleteIfExists(file);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private static void ensureChild(Path root, Path child) {
    Path rootNormal = root.normalize().toAbsolutePath();
    Path childNormal = child.normalize().toAbsolutePath();

    if (childNormal.getNameCount() <= rootNormal.getNameCount() || !childNormal.startsWith(rootNormal)) {
      throw new IllegalStateException("Child " + childNormal + " is not in root path " + rootNormal);
    }
  }

  @NotNull
  public static Stream<Path> list(@NotNull Path directory) {
    try {
      return Files.list(directory);
    } catch (IOException ex) {
      throw new WrappedException(ex);
    }
  }

}
