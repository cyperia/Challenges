package net.cyperia.challenges.plugin.utils.misc;

public class MemoryConverter {

  private static final long GIGABYTE = 1024L * 1024L * 1024L;

  public static int getGB(long bytes) {
    return (int) (bytes / GIGABYTE);
  }

}
