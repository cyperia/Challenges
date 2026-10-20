package net.cyperia.commons.common.version;

import lombok.Getter;
import net.cyperia.commons.common.logging.ILogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VersionInfo implements Version, Comparable<Version> {

  protected static final ILogger logger = ILogger.forThisClass();

  @Getter
  private final int major, minor, revision;

  public VersionInfo(int major, int minor, int revision) {
    this.major = major;
    this.minor = minor;
    this.revision = revision;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (!(other instanceof Version)) return false;
    return this.equals((Version) other);
  }

  @Override
  public int compareTo(@NotNull Version other) {
    return Version.super.compareTo(other);
  }

  @Override
  public int hashCode() {
    return Objects.hash(major, minor, revision);
  }

  @Override
  public String toString() {
    return this.format();
  }

  /**
   * @throws IllegalArgumentException If the version could not be parsed
   */
  public static Version parseExceptionally(@Nullable String input) {
    if (input == null) throw new IllegalArgumentException("Version cannot be null");
    String[] array = input.split("\\.");
    if (array.length == 0) throw new IllegalArgumentException("Version cannot be empty");
    try {
      int major = Integer.parseInt(array[0]);
      int minor = array.length >= 2 ? Integer.parseInt(array[1]) : 0;
      int revision = array.length >= 3 ? Integer.parseInt(array[2]) : 0;
      return new VersionInfo(major, minor, revision);
    } catch (Exception ex) {
      throw new IllegalArgumentException("Cannot parse Version: " + input + " (" + ex.getMessage() + ")");
    }
  }

  public static Version parse(@Nullable String input, Version def) {
    try {
      return parseExceptionally(input);
    } catch (Exception ex) {
      logger.error("Could not parse version for input {}", ex.getMessage());
      return def;
    }
  }
}
