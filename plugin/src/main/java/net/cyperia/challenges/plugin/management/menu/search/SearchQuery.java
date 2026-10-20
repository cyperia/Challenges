package net.cyperia.challenges.plugin.management.menu.search;

import org.jetbrains.annotations.NotNull;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;

/**
 * A search query, prepared once and then matched against many texts.
 * <p>
 * A text matches when it contains the query, either exactly or with a few typos. Approximate matching
 * uses the bit-parallel Wu&nbsp;&amp;&nbsp;Manber extension of the bitap / shift-and automaton, which
 * finds the smallest Levenshtein distance between the query and <i>any</i> substring of a text in
 * {@code O(errors * textLength)} word operations, without the {@code O(n*m)} matrix a plain edit
 * distance would need. Texts that cannot match are rejected by a character mask beforehand, so the
 * automaton only ever runs on plausible candidates.
 * <p>
 * {@link #score(String, long)} rates a match between 1 and {@link #MAX_SCORE}: exact before prefix
 * before contained, fewer typos before more, early before late, starting a word before starting inside
 * one, and covering most of the text before being buried in it.
 * <p>
 * Instances are immutable and may be reused across threads.
 */
public final class SearchQuery {

  // How many typos are forgiven, by query length. Same shape as Elasticsearch's AUTO fuzziness, one
  // step stricter than its 3/6 defaults: those are calibrated for matching whole terms, while matching
  // any substring of a text is far more permissive. At two errors "nether" already matches the "nster"
  // in "Monsters".
  private static final int ONE_ERROR_LENGTH = 4;
  private static final int TWO_ERROR_LENGTH = 8;
  private static final int MAX_ERRORS = 2;

  /**
   * Texts of this length forgive one typo less. Long texts offer so many substrings that the full
   * budget matches almost any query somewhere; this sits above the longest name and below all but the
   * shortest descriptions.
   */
  private static final int LONG_TEXT_LENGTH = 32;

  /** The automaton lives in one machine word, so a pattern covers at most 64 characters. */
  private static final int MAX_PATTERN_LENGTH = 64;

  private static final int MAX_TOKENS = 4;
  private static final int MIN_TOKEN_LENGTH = 2;
  // words matched one by one rank below the same query matched as one piece
  private static final int TOKEN_PENALTY_NUMERATOR = 3;
  private static final int TOKEN_PENALTY_DENOMINATOR = 4;

  private static final int SCORE_EXACT = 1000;    // the whole text is the query
  private static final int SCORE_PREFIX = 880;    // the text starts with the query
  private static final int SCORE_CONTAINS = 700;  // the query sits somewhere inside
  private static final int ERROR_PENALTY = 160;   // per typo, keeps a clean hit ahead of a fuzzy one
  private static final int POSITION_PENALTY_LIMIT = 30;
  private static final int BOUNDARY_BONUS = 45;   // the match starts a word
  private static final int DENSITY_BONUS = 120;   // the match covers most of the text

  /** Upper bound of {@link #score(String, long)}, for callers that build score bands around it. */
  public static final int MAX_SCORE = SCORE_EXACT + BOUNDARY_BONUS + DENSITY_BONUS;

  private static final int ASCII_RANGE = 128;
  private static final Needle[] NO_TOKENS = new Needle[0];

  private final Needle whole;
  /** The individual words, so {@code "health split"} still finds <i>Split Health</i>. Empty for one word. */
  private final Needle[] tokens;

  public SearchQuery(@NotNull String query) {
    String normalized = normalize(query.trim());
    this.whole = new Needle(normalized);
    this.tokens = split(normalized);
  }

  public boolean isEmpty() {
    return whole.length == 0;
  }

  /**
   * @param text     {@link #normalize(String) normalized} text to match against
   * @param textMask its {@link #charMask(String) character mask}
   * @return {@code 0} when the query does not match, otherwise how well it does, up to {@link #MAX_SCORE}
   */
  public int score(@NotNull String text, long textMask) {
    if (text.isEmpty() || whole.length == 0) return 0;
    int errorRelief = text.length() >= LONG_TEXT_LENGTH ? 1 : 0;

    int best = score(whole, text, textMask, errorRelief);
    if (tokens.length == 0) return best;

    int sum = 0;
    for (Needle token : tokens) {
      int score = score(token, text, textMask, errorRelief);
      if (score == 0) return best; // every word has to appear somewhere
      sum += score;
    }
    return Math.max(best, sum / tokens.length * TOKEN_PENALTY_NUMERATOR / TOKEN_PENALTY_DENOMINATOR);
  }

  private static int score(@NotNull Needle needle, @NotNull String text, long textMask, int errorRelief) {
    int maxErrors = Math.max(maxErrorsFor(needle.length) - errorRelief, 0);
    long located = needle.locate(text, textMask, maxErrors);
    if (located < 0) return 0;

    int start = (int) located;
    int errors = (int) (located >>> Integer.SIZE);
    int textLength = text.length();

    int score;
    if (errors == 0 && start == 0 && needle.length == textLength) {
      score = SCORE_EXACT;
    } else if (start == 0) {
      score = SCORE_PREFIX;
    } else {
      score = SCORE_CONTAINS;
    }

    score -= errors * ERROR_PENALTY;
    score -= Math.min(start, POSITION_PENALTY_LIMIT);
    // a fuzzy start is off by at most one position per error, so look that far in both directions
    if (startsWord(text, start, errors)) score += BOUNDARY_BONUS;
    // a query covering most of the text is a better hit than the same query buried in a long one
    score += DENSITY_BONUS * needle.length / Math.max(textLength, needle.length);

    return Math.max(score, 1);
  }

  private static boolean startsWord(@NotNull String text, int start, int tolerance) {
    int last = Math.min(text.length() - 1, start + tolerance);
    for (int i = Math.max(0, start - tolerance); i <= last; i++) {
      if (i == 0 || !Character.isLetterOrDigit(text.charAt(i - 1))) return true;
    }
    return false;
  }

  private static int maxErrorsFor(int queryLength) {
    if (queryLength < ONE_ERROR_LENGTH) return 0;
    return queryLength < TWO_ERROR_LENGTH ? 1 : MAX_ERRORS;
  }

  @NotNull
  private static Needle[] split(@NotNull String query) {
    int length = query.length();
    int spaces = 0;
    for (int i = 0; i < length; i++) {
      if (query.charAt(i) == ' ') spaces++;
    }
    if (spaces == 0) return NO_TOKENS;

    Needle[] tokens = new Needle[Math.min(spaces + 1, MAX_TOKENS)];
    int count = 0;
    int start = 0;
    while (start < length && count < tokens.length) {
      int end = query.indexOf(' ', start);
      if (end < 0) end = length;
      // single characters are dropped, they match everywhere and would only add noise
      if (end - start >= MIN_TOKEN_LENGTH) tokens[count++] = new Needle(query.substring(start, end));
      start = end + 1;
    }

    if (count < 2) return NO_TOKENS;
    return count == tokens.length ? tokens : Arrays.copyOf(tokens, count);
  }

  /**
   * Folds text into the form everything is matched in: lower case and without diacritics, so
   * {@code "Blöcke"} is found by {@code "blocke"} and {@code "Größe"} by {@code "grosse"}.
   */
  @NotNull
  public static String normalize(@NotNull String text) {
    String lowerCase = text.toLowerCase(Locale.ROOT);

    for (int i = 0, length = lowerCase.length(); i < length; i++) {
      if (lowerCase.charAt(i) >= ASCII_RANGE) return fold(lowerCase);
    }
    return lowerCase; // the overwhelmingly common case, nothing to fold
  }

  @NotNull
  private static String fold(@NotNull String lowerCase) {
    String decomposed = Normalizer.normalize(lowerCase, Normalizer.Form.NFD);
    StringBuilder builder = new StringBuilder(decomposed.length());
    for (int i = 0, length = decomposed.length(); i < length; i++) {
      char character = decomposed.charAt(i);
      if (Character.getType(character) == Character.NON_SPACING_MARK) continue; // the stripped accent
      if (character == 'ß') {
        builder.append("ss");
        continue;
      }
      builder.append(character);
    }
    return builder.toString();
  }

  /**
   * @return a bloom-filter-ish mask of the characters in {@code text}. Different characters may share a
   * bit, which can only ever make the filter miss a rejection, never reject a real match.
   */
  public static long charMask(@NotNull String text) {
    long mask = 0L;
    for (int i = 0, length = text.length(); i < length; i++) {
      mask |= 1L << (text.charAt(i) & 63);
    }
    return mask;
  }

  /** One pattern, with the character masks the automaton needs precomputed. */
  private static final class Needle {

    private final String text;
    private final int length;
    /** {@link #length}, capped to what fits into the automaton word. */
    private final int bits;
    private final long charMask;

    /** Bit {@code i} is set for the character {@link #text} has at position {@code i}. */
    private final long[] ascii = new long[ASCII_RANGE];
    private char[] wideChars;
    private long[] wideMasks;
    private int wideCount;

    private Needle(@NotNull String text) {
      this.text = text;
      this.length = text.length();
      this.bits = Math.min(length, MAX_PATTERN_LENGTH);
      this.charMask = charMask(text);
      for (int i = 0; i < bits; i++) {
        put(text.charAt(i), 1L << i);
      }
    }

    private void put(char character, long bit) {
      if (character < ASCII_RANGE) {
        ascii[character] |= bit;
        return;
      }
      for (int i = 0; i < wideCount; i++) {
        if (wideChars[i] == character) {
          wideMasks[i] |= bit;
          return;
        }
      }
      if (wideChars == null) { // only patterns that are not plain ascii pay for this
        wideChars = new char[bits];
        wideMasks = new long[bits];
      }
      wideChars[wideCount] = character;
      wideMasks[wideCount++] = bit;
    }

    private long equality(char character) {
      if (character < ASCII_RANGE) return ascii[character];
      for (int i = 0; i < wideCount; i++) {
        if (wideChars[i] == character) return wideMasks[i];
      }
      return 0L;
    }

    /**
     * @return {@code -1} when there is no match, otherwise the number of errors in the upper and the
     * start of the match in the lower {@value Integer#SIZE} bits. For a fuzzy match the start is
     * derived from the end of the match and can be off by up to {@code maxErrors} characters.
     */
    private long locate(@NotNull String text, long textMask, int maxErrors) {
      // every distinct query character the text does not have at all costs at least one error
      if (Long.bitCount(charMask & ~textMask) > maxErrors) return -1;

      int exact = text.indexOf(this.text);
      if (exact >= 0) return exact;

      // a pattern no longer than the error budget could be edited into anything
      if (maxErrors <= 0 || bits <= maxErrors) return -1;
      return fuzzyLocate(text, maxErrors);
    }

    /**
     * Wu &amp; Manber's bit-parallel extension of the bitap automaton. Level {@code d} holds, for every
     * pattern position, whether that prefix matches a suffix of the text read so far using at most
     * {@code d} errors; its initial {@code (1 << d) - 1} lets the first {@code d} pattern characters be
     * deleted for free. Per text character every level is advanced by one shift-and (the character
     * matches) plus the three edit operations: substitution and deletion from the level below, both
     * shifted, and insertion, which stays at the same pattern position. The levels are kept in locals
     * rather than an array, which both keeps them in registers and leaves this class immutable.
     */
    private long fuzzyLocate(@NotNull String text, int maxErrors) {
      long matchBit = 1L << (bits - 1);
      long level0 = 0L;
      long level1 = 1L;
      long level2 = 3L;

      int bestErrors = -1;
      int bestEnd = -1;
      for (int i = 0, length = text.length(); i < length; i++) {
        long equality = equality(text.charAt(i));

        // level 0 never reports a match here, an error free hit would have been found by indexOf
        long previous0 = level0;
        level0 = ((previous0 << 1) | 1L) & equality;

        long previous1 = level1;
        level1 = (((previous1 << 1) | 1L) & equality)
          | (((previous0 | level0) << 1) | 1L)
          | previous0;
        if ((level1 & matchBit) != 0) {
          // one error is the best still achievable, so this is the leftmost best match
          return (1L << Integer.SIZE) | Math.max(i - bits + 1, 0);
        }

        if (maxErrors > 1) {
          long previous2 = level2;
          level2 = (((previous2 << 1) | 1L) & equality)
            | (((previous1 | level1) << 1) | 1L)
            | previous1;
          if ((level2 & matchBit) != 0 && bestErrors < 0) {
            bestErrors = 2;
            bestEnd = i;
          }
        }
      }

      if (bestErrors < 0) return -1;
      return ((long) bestErrors << Integer.SIZE) | Math.max(bestEnd - bits + 1, 0);
    }
  }

}
