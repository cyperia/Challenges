package net.cyperia.commons.common.misc;

public final class MathHelper {

  private MathHelper() {
  }

  public static double percentage(double total, double proportion) {
    if (proportion == 0) return 0;
    return (proportion * 100) / total;
  }

}
