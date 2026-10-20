package net.cyperia.commons.common.config.document;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility for editing raw YAML text in place.
 * <p>
 * Bukkit's {@link org.bukkit.configuration.file.YamlConfiguration} re-serializes the whole document when it is
 * saved and thereby strips every comment. To avoid that, this helper does not rewrite the file from the parsed
 * model: it locates the single line that defines a key and swaps out only its value, leaving every comment,
 * blank line, indentation and line ending exactly the way it was.
 *
 * @see YamlDocument
 */
public final class YamlHelper {

  private YamlHelper() {
  }

  /**
   * Returns a copy of {@code content} in which the value of the given dot-separated {@code key} has been
   * replaced by {@code value}, serialized the way YAML would write it (strings wrapped in double quotes to
   * match the usual config style). Everything else - comments, indentation, blank lines, the original line
   * endings and any trailing inline comment on the edited line - is preserved unchanged.
   * <p>
   * Only single-line values are rewritten. Returns {@code null} when the key does not exist or when it opens a
   * block-style structure that spans several lines (so the caller can react instead of corrupting the file).
   *
   * @param content the full YAML document
   * @param key     the dot-separated path of an existing value, e.g. {@code "timer.format.seconds"}
   * @param value   the new value, or {@code null} to write a literal {@code null}
   * @return the updated document, or {@code null} if {@code key} could not be replaced
   */
  @Nullable
  public static String replaceValue(@NotNull String content, @NotNull String key, @Nullable Object value) {
    String lineSeparator = content.contains("\r\n") ? "\r\n" : "\n";

    // split on '\n' (keeping trailing empties) and drop a possible '\r' so indentation can be measured cleanly
    String[] rawLines = content.split("\n", -1);
    List<String> lines = new ArrayList<>(rawLines.length);
    for (String rawLine : rawLines) {
      lines.add(rawLine.endsWith("\r") ? rawLine.substring(0, rawLine.length() - 1) : rawLine);
    }

    int index = findKeyLine(lines, key);
    if (index == -1) return null;

    String line = lines.get(index);
    int colon = indexOfKeyColon(line);
    String afterColon = line.substring(colon + 1);

    // recover a possible inline comment ('value # comment') so it survives the value swap
    String inlineComment = extractInlineComment(afterColon);
    String existingValue = afterColon.substring(0, afterColon.length() - inlineComment.length()).trim();

    // refuse block-style parents (nothing on this line, children indented underneath) - replacing the line
    // would orphan those children
    if (existingValue.isEmpty() && opensBlock(lines, index)) return null;

    // keep the indentation, the key and its colon exactly as they are
    String keyPart = line.substring(0, colon + 1);
    lines.set(index, keyPart + " " + serializeValue(value) + inlineComment);

    return String.join(lineSeparator, lines);
  }

  /**
   * Serializes a single value the way YAML would write it, forced onto one line (flow style) so it can be
   * dropped into an existing line. String scalars are wrapped in double quotes to match the surrounding config
   * style, while numbers, booleans and {@code null} stay bare so they remain type-correct on re-read. Quoting
   * of values that need it (e.g. the string {@code "2.4"}, which would otherwise be read as a number) is left
   * to SnakeYAML.
   */
  @NotNull
  public static String serializeValue(@Nullable Object value) {
    if (value instanceof Enum<?>) value = ((Enum<?>) value).name();

    DumperOptions options = new DumperOptions();
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.FLOW); // keep collections on a single line
    options.setWidth(Integer.MAX_VALUE);                       // never wrap the value across multiple lines
    if (isStringOnly(value)) {
      // prefer double quotes ("...") over SnakeYAML's default single quotes ('...'); only safe to force on
      // string-only values, otherwise numbers/booleans would be quoted and read back as strings
      options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
    }

    // Yaml#dump appends a trailing line break (and, for flow scalars, no document markers)
    return new Yaml(options).dump(value).trim();
  }

  /**
   * Finds the index of the line that defines the given dot-separated {@code key} by tracking the mapping nesting
   * through indentation, or {@code -1} if no such line exists.
   */
  private static int findKeyLine(@NotNull List<String> lines, @NotNull String key) {
    String[] segments = key.split("\\.");
    List<String> pathKeys = new ArrayList<>();
    List<Integer> pathIndents = new ArrayList<>();

    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      String trimmed = line.trim();
      // skip blank lines, full-line comments and (block) sequence items - none of them open a mapping key
      if (trimmed.isEmpty() || trimmed.charAt(0) == '#' || trimmed.charAt(0) == '-') continue;

      int colon = indexOfKeyColon(line);
      if (colon == -1) continue;

      int indent = countIndent(line);
      String keyName = unquote(line.substring(indent, colon).trim());

      // walk back up to the parent of the current indentation level
      while (!pathIndents.isEmpty() && pathIndents.get(pathIndents.size() - 1) >= indent) {
        pathIndents.remove(pathIndents.size() - 1);
        pathKeys.remove(pathKeys.size() - 1);
      }
      pathIndents.add(indent);
      pathKeys.add(keyName);

      if (pathMatches(pathKeys, segments)) return i;
    }

    return -1;
  }

  private static boolean pathMatches(@NotNull List<String> pathKeys, @NotNull String[] segments) {
    if (pathKeys.size() != segments.length) return false;
    for (int i = 0; i < segments.length; i++) {
      if (!pathKeys.get(i).equals(segments[i])) return false;
    }
    return true;
  }

  /**
   * Returns {@code true} if the key on the given line has no value of its own but is followed by a deeper
   * indented (block-style) child line.
   */
  private static boolean opensBlock(@NotNull List<String> lines, int index) {
    int keyIndent = countIndent(lines.get(index));
    for (int i = index + 1; i < lines.size(); i++) {
      String trimmed = lines.get(i).trim();
      if (trimmed.isEmpty() || trimmed.charAt(0) == '#') continue;
      return countIndent(lines.get(i)) > keyIndent;
    }
    return false;
  }

  /**
   * Returns the index of the colon that separates the key from its value, ignoring colons that appear inside a
   * quoted scalar, or {@code -1} if the line carries no mapping key.
   */
  private static int indexOfKeyColon(@NotNull String line) {
    boolean inSingle = false, inDouble = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (c == '\'' && !inDouble) inSingle = !inSingle;
      else if (c == '"' && !inSingle) inDouble = !inDouble;
      else if (c == ':' && !inSingle && !inDouble) {
        // a key/value colon is followed by whitespace or ends the line
        if (i + 1 >= line.length() || line.charAt(i + 1) == ' ' || line.charAt(i + 1) == '\t') return i;
      }
    }
    return -1;
  }

  /**
   * Extracts the trailing inline comment from the part of a line that follows the key's colon, including the
   * whitespace in front of the {@code #} so it can be re-appended unchanged. Returns an empty string when there
   * is no inline comment. The {@code #} must be preceded by whitespace and must not sit inside a quoted scalar.
   */
  @NotNull
  private static String extractInlineComment(@NotNull String valuePart) {
    boolean inSingle = false, inDouble = false;
    for (int i = 0; i < valuePart.length(); i++) {
      char c = valuePart.charAt(i);
      if (c == '\'' && !inDouble) inSingle = !inSingle;
      else if (c == '"' && !inSingle) inDouble = !inDouble;
      else if (c == '#' && !inSingle && !inDouble && (i == 0 || isWhitespace(valuePart.charAt(i - 1)))) {
        int start = i;
        while (start > 0 && isWhitespace(valuePart.charAt(start - 1))) start--;
        return valuePart.substring(start);
      }
    }
    return "";
  }

  private static int countIndent(@NotNull String line) {
    int i = 0;
    while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t')) i++;
    return i;
  }

  @NotNull
  private static String unquote(@NotNull String text) {
    if (text.length() >= 2) {
      char first = text.charAt(0), last = text.charAt(text.length() - 1);
      if ((first == '"' && last == '"') || (first == '\'' && last == '\''))
        return text.substring(1, text.length() - 1);
    }
    return text;
  }

  private static boolean isStringOnly(@Nullable Object value) {
    if (value instanceof String) return true;
    if (value instanceof Iterable<?>) {
      boolean any = false;
      for (Object element : (Iterable<?>) value) {
        any = true;
        if (!(element instanceof String)) return false;
      }
      return any;
    }
    if (value instanceof Object[]) {
      Object[] array = (Object[]) value;
      if (array.length == 0) return false;
      for (Object element : array) {
        if (!(element instanceof String)) return false;
      }
      return true;
    }
    return false;
  }

  private static boolean isWhitespace(char c) {
    return c == ' ' || c == '\t';
  }
}
