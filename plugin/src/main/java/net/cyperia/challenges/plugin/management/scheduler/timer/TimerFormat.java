package net.cyperia.challenges.plugin.management.scheduler.timer;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.commons.common.config.Document;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TimerFormat {

  private interface Token {
    void append(@NotNull StringBuilder sb, long d, long h, long m, long s);
  }

  private static final Token TOKEN_D = (sb, d, h, m, s) -> sb.append(d);
  private static final Token TOKEN_DD = (sb, d, h, m, s) -> {
    if (d < 10) sb.append('0');
    sb.append(d);
  };
  private static final Token TOKEN_H = (sb, d, h, m, s) -> sb.append(h);
  private static final Token TOKEN_HH = (sb, d, h, m, s) -> {
    if (h < 10) sb.append('0');
    sb.append(h);
  };
  private static final Token TOKEN_M = (sb, d, h, m, s) -> sb.append(m);
  private static final Token TOKEN_MM = (sb, d, h, m, s) -> {
    if (m < 10) sb.append('0');
    sb.append(m);
  };
  private static final Token TOKEN_S = (sb, d, h, m, s) -> sb.append(s);
  private static final Token TOKEN_SS = (sb, d, h, m, s) -> {
    if (s < 10) sb.append('0');
    sb.append(s);
  };

  public static final TimerFormat SIMPLE_FORMAT = new TimerFormat();

  // TODO impl primitive args token?

  private record LiteralToken(String text) implements Token {
    @Override
    public void append(@NotNull StringBuilder sb, long d, long h, long m, long s) {
      sb.append(text);
    }
  }

  private record CompiledFormat(Token[] tokens, int baseLength) {
  }

  // pre compiled formatting
  private final CompiledFormat secondsFormat, minutesFormat, hoursFormat, dayFormat, daysFormat;

  public TimerFormat(@NotNull Document document) {
    secondsFormat = compile(document.getString("seconds", ""));
    minutesFormat = compile(document.getString("minutes", ""));
    hoursFormat = compile(document.getString("hours", ""));
    dayFormat = compile(document.getString("day", ""));
    daysFormat = compile(document.getString("days", ""));
  }

  public TimerFormat(@NotNull MessageKey rootKey, @NotNull Locale locale) {
    secondsFormat = compile(rootKey.getChildKey("seconds").localizeRawValueAsSingleLine(locale));
    minutesFormat = compile(rootKey.getChildKey("minutes").localizeRawValueAsSingleLine(locale));
    hoursFormat = compile(rootKey.getChildKey("hours").localizeRawValueAsSingleLine(locale));
    dayFormat = compile(rootKey.getChildKey("day").localizeRawValueAsSingleLine(locale));
    daysFormat = compile(rootKey.getChildKey("days").localizeRawValueAsSingleLine(locale));
  }

  private TimerFormat() {
    secondsFormat = compile("{mm}:{ss}");
    minutesFormat = compile("{mm}:{ss}");
    hoursFormat = compile("{hh}:{mm}:{ss}");
    dayFormat = compile("{d}:{hh}:{mm}:{ss}");
    daysFormat = compile("{d}:{hh}:{mm}:{ss}");
  }

  @NotNull
  public String format(long time) {
    long s = time % 60;
    long m = (time / 60) % 60;
    long h = (time / 3_600) % 24;
    long d = time / 86_400;

    CompiledFormat compiled = getCompiledFormat(m, h, d);

    // + 8 padding safely fits any numerical values, preventing internal array resizing
    StringBuilder sb = new StringBuilder(compiled.baseLength + 8);

    for (Token token : compiled.tokens) {
      token.append(sb, d, h, m, s);
    }

    return sb.toString();
  }

  @NotNull
  private CompiledFormat getCompiledFormat(long minutes, long hours, long days) {
    if (days > 1) return this.daysFormat;
    if (days == 1) return this.dayFormat;
    if (hours >= 1) return this.hoursFormat;
    if (minutes >= 1) return this.minutesFormat;
    return this.secondsFormat;
  }

  @NotNull
  private static CompiledFormat compile(@NotNull String template) {
    List<Token> tokens = new ArrayList<>();
    int baseLength = 0;
    int cursor = 0;
    int len = template.length();

    while (cursor < len) {
      int start = template.indexOf('{', cursor);
      if (start == -1) {
        // no args remaining -> literal
        String remainder = template.substring(cursor);
        tokens.add(new LiteralToken(remainder));
        baseLength += remainder.length();
        break;
      }

      if (start > cursor) {
        // text in front of brackets as literal
        String literal = template.substring(cursor, start);
        tokens.add(new LiteralToken(literal));
        baseLength += literal.length();
      }

      int end = template.indexOf('}', start);
      if (end == -1) {
        // brackets was never closed
        String remainder = template.substring(start);
        tokens.add(new LiteralToken(remainder));
        baseLength += remainder.length();
        break;
      }

      String tag = template.substring(start, end + 1);
      Token token = getTokenByTag(tag);

      if (token != null) {
        tokens.add(token);
      } else {
        // unknown tag
        tokens.add(new LiteralToken(tag));
        baseLength += tag.length();
      }

      // set cursor BEHIND closing bracket
      cursor = end + 1;
    }

    return new CompiledFormat(tokens.toArray(new Token[0]), baseLength);
  }

  @Nullable
  private static Token getTokenByTag(@NotNull String tag) {
    return switch (tag) {
      case "{d}" -> TOKEN_D;
      case "{dd}" -> TOKEN_DD;
      case "{h}" -> TOKEN_H;
      case "{hh}" -> TOKEN_HH;
      case "{m}" -> TOKEN_M;
      case "{mm}" -> TOKEN_MM;
      case "{s}" -> TOKEN_S;
      case "{ss}" -> TOKEN_SS;
      default -> null;
    };
  }

}
