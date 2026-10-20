package net.cyperia.commons.common.collection;

import org.jetbrains.annotations.NotNull;

import java.util.TreeMap;

public final class RomanNumerals {

  public static final class IllegalRomanNumeralException extends IllegalArgumentException {

    private IllegalRomanNumeralException(int number) {
      super("Number " + number + " out of bounds for 0 to 3999");
    }

  }

  private static final TreeMap<Integer, String> values = new TreeMap<>();

  static {
    values.put(1000, "M");
    values.put(900, "CM");
    values.put(500, "D");
    values.put(400, "CD");
    values.put(100, "C");
    values.put(90, "XC");
    values.put(50, "L");
    values.put(40, "XL");
    values.put(10, "X");
    values.put(9, "IX");
    values.put(5, "V");
    values.put(4, "IV");
    values.put(1, "I");
  }

  private RomanNumerals() {
  }

  @NotNull
  public static String forNumber(int number) {
    if (number < 0 || number > 3999) throw new IllegalRomanNumeralException(number);
    if (number == 0) return "";
    int i = values.floorKey(number);
    if (number == i) {
      return values.get(number);
    }
    return values.get(i) + forNumber(number - i);
  }

}
