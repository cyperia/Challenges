package net.cyperia.challenges.plugin.management.files;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// Only a beta build handed to one content creator carries this resource
public record CreatorBuild(@NotNull String creator, @NotNull String build) {

  public static final String RESOURCE = "creator-build.properties";

  @Nullable
  public static CreatorBuild read(@NotNull ClassLoader loader) {
    try (InputStream in = loader.getResourceAsStream(RESOURCE)) {
      return in == null ? null : parse(in);
    } catch (IOException ex) {
      return null;
    }
  }

  @Nullable
  static CreatorBuild parse(@NotNull InputStream in) throws IOException {
    Properties properties = new Properties();
    properties.load(in);
    String creator = properties.getProperty("creator");
    String build = properties.getProperty("build");
    return creator == null || build == null ? null : new CreatorBuild(creator, build);
  }

}
