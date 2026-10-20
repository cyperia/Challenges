package net.cyperia.commons.common.collection;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.function.Consumer;

public interface NumberFormatter {

  @NotNull
  String format(double value);

  @NotNull
  default String format(float value) {
    return format(Float.valueOf(value));
  }

  @NotNull
  default String format(long value) {
    return format(Long.valueOf(value));
  }

  @NotNull
  default String format(int value) {
    return format(Integer.valueOf(value));
  }

  @NotNull
  default String format(short value) {
    return format(Short.valueOf(value));
  }

  @NotNull
  default String format(byte value) {
    return format(Byte.valueOf(value));
  }

  @NotNull
  default String format(@NotNull Number number) {
    return format(number.doubleValue());
  }

  public static final NumberFormatter
    DEFAULT = fromPattern("0.##", null, false),
    INTEGER = value -> (int) value + "",
    SPACE_SPLIT = fromPattern("###,##0.###############", null, false,
      init -> updateSymbols(init, symbols -> symbols.setGroupingSeparator(' '))),
    FLOATING_POINT = fromPattern("0.0", null, false),
    DOUBLE_FLOATING_POINT = fromPattern("0.00", null, false),
    BIG_FLOATING_POINT = fromPattern("###,##0.00000", null, false),
    PERCENTAGE = fromPattern("0.##", "%", true),
    FLOATING_PERCENTAGE = fromPattern("0.00", "%", true),
    MIDDLE_NUMBER = fromPattern("###,###,##0.#", null, false),

  /**
   * days, hours, minutes, seconds
   */
  TIME = value -> {

    int seconds = (int) value;
    int minutes = seconds / 60;
    int hours = minutes / 60;
    int days = hours / 24;
    int years = days / 365;

    seconds %= 60;
    minutes %= 60;
    hours %= 24;
    days %= 365;

    return ((years > 0 ? years + "y " : "")
      + (days > 0 ? days + "d " : "")
      + (hours > 0 ? hours + "h " : "")
      + (minutes > 0 ? minutes + "m " : "")
      + (seconds > 0 || (years == 0 && days == 0 && hours == 0 && minutes == 0) ? seconds + "s" : "")).trim();

  },

  /**
   * days, hours, minutes
   */
  BIG_TIME = value -> {

    int seconds = (int) value;
    int minutes = seconds / 60;
    int hours = minutes / 60;
    int days = hours / 24;
    int years = days / 365;

    minutes %= 60;
    hours %= 24;
    days %= 365;

    return ((years > 0 ? years + "y " : "")
      + (days > 0 ? days + "d " : "")
      + (hours > 0 ? hours + "h " : "")
      + (minutes > 0 || (years == 0 && days == 0 && hours == 0) ? minutes + "m " : "")).trim();

  },

  /**
   * input: millis
   * 1 Tag, H:M:S
   */
  GERMAN_TIME = value -> {

    DecimalFormat format = new DecimalFormat("00");

    long millis = (long) value;
    long seconds = millis / 1000;
    long minutes = seconds / 60;
    long hours = minutes / 60;
    long days = hours / 24;
    seconds %= 60;
    minutes %= 60;
    hours %= 24;

    return (days > 0 ? (days == 1 ? "1 Tag " : days + " Tage ") : "")
      + (hours > 0 ? format.format(hours) + ":" : "")
      + format.format(minutes) + ":"
      + format.format(seconds);

  },
  /**
   * input: seconds
   * 1 Tag, H:M:S
   */
  FULL_GERMAN_TIME_HOURS = value -> {

    long seconds = (long) (value);
    long minutes = seconds / 60;
    long hours = minutes / 60;
    seconds %= 60;
    minutes %= 60;

    return hours > 0 ? (hours == 1 ? "1 Stunde" : hours + " Stunden") : (minutes == 1 ? "1 Minute" : minutes + " Minuten");
  },

  FULL_GERMAN_TIME = value -> {

    long seconds = (long) value;
    long minutes = seconds / 60;
    long hours = minutes / 60;
    long days = hours / 24;
    long years = days / 365;

    seconds %= 60;
    minutes %= 60;
    hours %= 24;
    days %= 265;

    return ((years > 0 ? (years == 1 ? "1 Jahr " : years + " Jahre ") : "")
      + (days > 0 ? (days == 1 ? "1 Tag " : days + " Tage ") : "")
      + (hours > 0 ? (hours == 1 ? "1 Stunde " : hours + " Stunden ") : "")
      + (minutes > 0 ? (minutes == 1 ? "1 Minute " : minutes + " Minuten ") : "")
      + (seconds > 0 || years == 0 && hours == 0 && minutes == 0 ? (seconds == 1 ? "1 Sekunde" : seconds + " Sekunden") : "")).trim();

  },

  NORMAL_FULL_GERMAN_TIME = value -> {

    long seconds = (long) value;
    long minutes = seconds / 60;
    long hours = minutes / 60;
    long days = hours / 24;
    long years = days / 365;

    minutes %= 60;
    hours %= 24;
    days %= 265;

    return ((years > 0 ? (years == 1 ? "1 Jahr " : years + " Jahre ") : "")
      + (days > 0 ? (days == 1 ? "1 Tag " : days + " Tage ") : "")
      + (hours > 0 ? (hours == 1 ? "1 Stunde " : hours + " Stunden ") : "")
      + (minutes > 0 || value == 0 ? (minutes == 1 ? "1 Minute " : minutes + " Minuten ") : "")).trim();

  },

  BIG_FULL_GERMAN_TIME = value -> {

    long seconds = (long) value;
    long minutes = seconds / 60;
    long hours = minutes / 60;
    long days = hours / 24;
    long years = days / 365;

    hours %= 24;
    days %= 265;

    return ((years > 0 ? (years == 1 ? "1 Jahr " : years + " Jahre ") : "")
      + (days > 0 ? (days == 1 ? "1 Tag " : days + " Tage ") : "")
      + (hours > 0 || years == 0 && days == 0 ? (hours == 1 ? "1 Stunde" : hours + " Stunden") : "")).trim();

  },

  /**
   * billion, million, thousand, number
   */
  BIG_NUMBER = value -> {

    DecimalFormat format = new DecimalFormat("0.##");
    double divide;
    String ending = "";

    // Normal number
    if (value < 1000) {
      divide = 1;
      format = new DecimalFormat("0.#");
      // Thousand
    } else if (value < 1000000) {
      divide = 1000;
      ending = "k";
      // Million
    } else if (value < 1000000000) {
      divide = 1000000;
      ending = "m";
      // Billion (Milliarde)
    } else if (value < 1000000000000D) {
      divide = 1000000000;
      ending = "b";
      // Trillion (Billion)
    } else {
      divide = 1000000000000D;
      ending = "t";
    }

    value /= divide;
    return format.format(value) + ending;

  },

  /**
   * input in bytes
   * kilobyte, megabyte, gigabyte, terrabyte
   */
  DATA_SIZE = value -> {

    if (value < 0) value = 0;

    DecimalFormat format = new DecimalFormat("0.##");
    double divide;
    String ending;

    // KiloByte
    if (value < 1000000L) {
      divide = 1000;
      format = new DecimalFormat("0.#");
      ending = "KB";
    } else if (value < 1000000000L) {
      // MegaByte
      divide = 1000000L;
      ending = "MB";
      // GigaByte
    } else if (value < 1000000000000L) {
      divide = 1000000000L;
      ending = "GB";
      // TerraByte
    } else {
      divide = 1000000000000L;
      ending = "TB";
    }

    value /= divide;
    return format.format(value) + ending;

  },

  /**
   * input in bytes
   * gigabyte, terrabyte, petabyte
   */
  BIG_DATA_SIZE = value -> {

    if (value < 0) value = 0;

    DecimalFormat format = new DecimalFormat("0.##");
    double divide;
    String ending;

    // GigaByte
    if (value < 1000000000000L) {
      divide = 1000000000L;
      ending = "GB";
      // TerraByte
    } else if (value < 1000000000000000L) {
      divide = 1000000000000L;
      ending = "TB";
      // PetaByte
    } else {
      divide = 1000000000000000L;
      ending = "PB";
    }

    value /= divide;
    return format.format(value) + ending;

  },
    ORDINAL = value -> {

      String string = String.valueOf(((long) value));
      int number = Integer.parseInt(string.substring(string.length() - 1));
      String ending = "th";

      if (value != 11 && value != 12 && value != 13) {
        switch (number) {
          case 1:
            ending = "st";
            break;
          case 2:
            ending = "nd";
            break;
          case 3:
            ending = "rd";
            break;
        }
      }

      return string + ending;

    },
    GERMAN_ORDINAL = fromPattern("0", ".", false);

  @NotNull
  public static NumberFormatter fromPattern(@NotNull String pattern, String ending, boolean positive) {
    return fromPattern(pattern, ending, positive, null);
  }

  @NotNull
  public static NumberFormatter fromPattern(@NotNull String pattern, String ending, boolean positive, Consumer<? super DecimalFormat> init) {
    DecimalFormat format = new DecimalFormat(pattern);
    if (init != null) init.accept(format);
    return value -> Double.isNaN(value) ? "NaN" : format.format(positive ? (value > 0 ? value : 0) : value) + (ending != null ? ending : "");
  }

  @NotNull
  public static DecimalFormatSymbols updateSymbols(@NotNull DecimalFormatSymbols symbols, @NotNull Consumer<? super DecimalFormatSymbols> action) {
    action.accept(symbols);
    return symbols;
  }

  public static void updateSymbols(@NotNull DecimalFormat format, @NotNull Consumer<? super DecimalFormatSymbols> action) {
    format.setDecimalFormatSymbols(updateSymbols(format.getDecimalFormatSymbols(), action));
  }

}
