package net.cyperia.commons.common.version;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class VersionComparator implements Comparator<Version> {

  public static final VersionComparator INSTANCE = new VersionComparator();

  @Override
  public int compare(@NotNull Version v1, @NotNull Version v2) {
    return v1.compareTo(v2);
  }

}
