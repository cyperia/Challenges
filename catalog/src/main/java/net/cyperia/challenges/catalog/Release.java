package net.cyperia.challenges.catalog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

// since and updated are plugin versions; a beta entry's class is missing from the public release
public record Release(@Nullable String since, @Nullable String updated, boolean beta) {

  /** The Challenges release this catalog was built for. */
  public static final String CURRENT = readCurrent();

  // A beta entry shows as beta instead of new or updated
  public boolean isNew() {
    return !beta && reached(since, CURRENT);
  }

  public boolean isUpdated() {
    return !beta && reached(updated, CURRENT);
  }

  static boolean reached(@Nullable String version, @NotNull String release) {
    if (version == null) return false;
    String[] own = version.split("\\.");
    String[] other = release.split("\\.");
    for (int i = 0; i < Math.max(own.length, other.length); i++) {
      int compared = Integer.compare(number(own, i), number(other, i));
      if (compared != 0) return compared > 0;
    }
    return true;
  }

  // A missing part counts as 0 and a qualifier such as -SNAPSHOT is ignored, so 3.0 equals 3.0.0-SNAPSHOT
  private static int number(@NotNull String[] parts, int index) {
    if (index >= parts.length) return 0;
    String digits = parts[index].replaceFirst("\\D.*", "");
    return digits.isEmpty() ? 0 : Integer.parseInt(digits);
  }

  @NotNull
  private static String readCurrent() {
    Properties properties = new Properties();
    try (InputStream in = Release.class.getClassLoader().getResourceAsStream(ChallengeCatalog.DIRECTORY + "release.properties")) {
      if (in == null) throw new IllegalStateException("The catalog is missing its release.properties");
      properties.load(in);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
    return properties.getProperty("version");
  }

}
