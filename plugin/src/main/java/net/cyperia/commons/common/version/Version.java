package net.cyperia.commons.common.version;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface Version {

  Version FALLBACK = new VersionInfo(1, 0, 0);

  int getMajor();

  int getMinor();

  int getRevision();

  default int compareTo(@NotNull Version other) {
    // eliminates Comparator chaining overhead
    int cmp = Integer.compare(this.getMajor(), other.getMajor());
    if (cmp != 0) return cmp;

    cmp = Integer.compare(this.getMinor(), other.getMinor());
    if (cmp != 0) return cmp;

    return Integer.compare(this.getRevision(), other.getRevision());
  }

  default boolean isNewerThan(@NotNull Version other) {
    return this.compareTo(other) > 0;
  }

  default boolean isNewerOrEqualThan(@NotNull Version other) {
    return this.compareTo(other) >= 0;
  }

  default boolean isOlderThan(@NotNull Version other) {
    return this.compareTo(other) < 0;
  }

  default boolean isOlderOrEqualThan(@NotNull Version other) {
    return this.compareTo(other) <= 0;
  }

  default boolean equals(@NotNull Version other) {
    return this.compareTo(other) == 0;
  }

  @NotNull
  default String format() {
    int revision = getRevision();
    return revision > 0 ? String.format("%s.%s.%s", getMajor(), getMinor(), revision)
      : String.format("%s.%s", getMajor(), getMinor());
  }

  @NotNull
  static Version parse(@Nullable String input) {
    return parse(input, FALLBACK);
  }

  static Version parse(@Nullable String input, Version def) {
    return VersionInfo.parse(input, def);
  }

  @NotNull
  static Version parseExceptionally(@Nullable String input) {
    return VersionInfo.parseExceptionally(input);
  }

  @NotNull
  static <V extends Version> V findNearest(@NotNull Version target, @NotNull V[] sortedVersionsArray) {
    List<V> versions = new ArrayList<>(Arrays.asList(sortedVersionsArray));
    Collections.reverse(versions);
    for (V version : versions) {
      if (version.isNewerThan(target)) continue;
      return version;
    }
    throw new IllegalArgumentException("No version found for '" + target + "'");
  }

  @NotNull
  static Comparator<Version> comparator() {
    return VersionComparator.INSTANCE;
  }

}
