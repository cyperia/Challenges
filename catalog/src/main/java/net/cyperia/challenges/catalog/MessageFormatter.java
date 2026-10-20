package net.cyperia.challenges.catalog;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.System.Logger;
import java.util.*;
import java.util.regex.Pattern;

public final class MessageFormatter { // pre expansion for better server runtime performance

  public static final char ESCAPE_CHAR = '\\';
  public static final char START_ARG_CHAR = '{';
  public static final char END_ARG_CHAR = '}';
  public static final char REFERENCE_CHAR = '@';
  public static final char START_REFERENCE_ARGS_CHAR = '(';
  public static final char END_REFERENCE_ARGS_CHAR = ')';
  public static final char REFERENCE_ARG_CHAR = '\'';
  public static final char COLORIZE_CHAR = '*';
  public static final String COLORIZE_KEY = "*colorize*";
  public static final char SUB_DOCUMENT_DELIMITER = '.';
  public static final String SUB_DOCUMENT_PATH_PATTERN = "\\.";

  public static final Pattern POS_ARG_PATTERN = Pattern.compile("\\{(\\d+)\\}");
  public static final String[] EMPTY_STRINGS = new String[0];

  private static final Logger logger = System.getLogger(MessageFormatter.class.getName()); // logs illegal argument indices or references

  private MessageFormatter() {
  }

  public static boolean isMetaTag(@NotNull String keyName) {
    String[] subPathComponents = keyName.split(SUB_DOCUMENT_PATH_PATTERN);
    String relativeKeyName = subPathComponents[subPathComponents.length - 1];
    return relativeKeyName.equals(COLORIZE_KEY); // only meta tag currently implemented
  }

  @NotNull
  @CheckReturnValue
  public static String[] embedReferences(@NotNull String[] origin, @NotNull String originKey,
                                         @NotNull Map<String, String[]> messagesBundle) {
    HashSet<String> visited = new HashSet<>();
    visited.add(originKey);
    return embedReferences(origin, originKey, messagesBundle, visited, true);
  }

  @NotNull
  private static String[] embedReferences(@NotNull String[] origin, @NotNull String originKey,
                                          @NotNull Map<String, String[]> messagesBundle,
                                          @NotNull Set<String> visited, boolean applyColorize) {
    if (origin.length == 0) return origin;
    if (messagesBundle.isEmpty()) return origin;
    // colorization via meta tag will be applied after references are resolved and embedded!

    String[] originSubDocumentPath = extractSubDocumentPath(originKey); // for relative references resolution

    // if a referenced message is more than one line, the result will grow
    ArrayList<String> result = new ArrayList<>(origin.length);
    // will be cleared and reused
    StringBuilder builder = new StringBuilder(origin[0].length());
    StringBuilder reference = new StringBuilder(32); // usually short (e.g., @message.key)
    StringBuilder referenceArgument = new StringBuilder(32);
    ArrayList<String> referenceArguments = new ArrayList<>(4); // usually few arguments
    boolean inReference = false;
    boolean inReferenceArguments = false;
    boolean inReferenceArgument = false;

    for (String line : origin) {
      int len = line.length();
      for (int i = 0; i < len; i++) {
        char c = line.charAt(i);

        if (c == ESCAPE_CHAR && i + 1 < len) {
          // append escaped chars normally, don't trigger reference logic
          c = line.charAt(++i);
          if (inReference) {
            reference.append(c);
          } else if (inReferenceArgument) {
            referenceArgument.append(c);
          } else {
            builder.append(c);
          }
          continue;
        }

        if (c == END_ARG_CHAR && inReference && !inReferenceArguments) {
          inReference = false;
          resolveReferenceInto(originKey, messagesBundle, visited, line, reference, originSubDocumentPath, referenceArguments, builder, result, applyColorize);
          continue; // skip end char
        }

        if (c == START_ARG_CHAR && !inReference) {
          if (i + 1 < len && line.charAt(i + 1) == REFERENCE_CHAR) {
            i++; // also skip ref char
            inReference = true;
            continue; // skip start char
          }
          // append normally
        }

        if (c == START_REFERENCE_ARGS_CHAR && inReference && !inReferenceArguments) {
          inReferenceArguments = true;
          continue; // skip start char
        }
        if (c == REFERENCE_ARG_CHAR && inReferenceArguments) {
          if (inReferenceArgument) {
            String builtReferenceArgument = referenceArgument.toString();

            // resolve nested references in argument
            String[] embeddedReferenceArgument = embedReferences(new String[]{builtReferenceArgument}, originKey, messagesBundle, visited, applyColorize);

            referenceArguments.add(String.join("", embeddedReferenceArgument)); // TODO newline?
            referenceArgument.setLength(0); // clear and reuse
          }
          inReferenceArgument = !inReferenceArgument;
          continue; // skip arg char
        }
        if (c == END_REFERENCE_ARGS_CHAR && inReferenceArguments && !inReferenceArgument) {
          inReferenceArguments = false;
          continue; // skip end char
        }
        if (inReferenceArgument) {
          referenceArgument.append(c);
          continue;
        }
        if (inReferenceArguments) {
          continue; // skip ", "
        }

        if (inReference) {
          reference.append(c);
          continue;
        }

        builder.append(c);
      } // end of processing this line

      if (!reference.isEmpty()) { // arg was not closed
        warn("Unclosed reference in '" + line + "' of " + originKey + ": '" + reference + "'");
        builder.append(START_ARG_CHAR).append(reference);
      }
      if (!referenceArgument.isEmpty()) { // arg was not closed
        warn("Unclosed reference argument in '" + line + "' of " + originKey + ": '" + referenceArgument + "'");
        builder.append(REFERENCE_ARG_CHAR).append(referenceArgument);
      }

      if (!builder.isEmpty()) { // don't append if line ends with reference
        result.add(builder.toString());
      }

      builder.setLength(0); // reset line for next iteration
    }

    String[] embedded = result.toArray(String[]::new);
    if (applyColorize) {
      colorize(embedded, originSubDocumentPath, messagesBundle); // apply colorization if defined for this sub-document
    }
    return embedded;
  }

  private static void resolveReferenceInto(@NotNull String originKey, @NotNull Map<String, String[]> messagesBundle,
                                           @NotNull Set<String> visited, @NotNull String line, StringBuilder reference,
                                           @NotNull String[] originSubDocumentPath, @NotNull ArrayList<String> referenceArguments,
                                           @NotNull StringBuilder builder, @NotNull ArrayList<String> result, boolean applyColorize) {
    String relativeReferenceName = reference.toString();
    reference.setLength(0); // clear and reuse

    String qualifiedReferenceName = qualifyRelativeKey(relativeReferenceName, originSubDocumentPath, messagesBundle);

    if (visited.contains(qualifiedReferenceName)) {
      warn("Cyclic message reference '" + relativeReferenceName + "' (qualified: " + qualifiedReferenceName
        + ") in '" + originKey + "' [visited " + visited + "]");
      return;
    }

    String[] rawReferenceValue = messagesBundle.get(qualifiedReferenceName);

    if (rawReferenceValue != null && rawReferenceValue.length != 0) {
      // resolve nested references
      visited.add(qualifiedReferenceName);
      String[] referenced = embedReferences(rawReferenceValue, qualifiedReferenceName, messagesBundle, visited, applyColorize);
      visited.remove(qualifiedReferenceName); // allow the same references multiple times

      // embed reference args
      String[] embeddedReferenced = referenceArguments.isEmpty() ? referenced : embedPositionalArgs(referenced, referenceArguments.toArray());
      referenceArguments.clear(); // clear and reuses

      // append reference message
      if (referenced.length == 1) {
        builder.append(embeddedReferenced[0]);
      } else {
        // if the reference is multiple lines
        // e.g. ["Unser Text {@ref} mit Spaß", "..."] + @ref=["Zeile1", "Zeile2"]
        // =    ["Unser Text Zeile1", "Zeile2", " mit Spaß" "..."]
        result.add(builder + embeddedReferenced[0]);
        builder.setLength(0); // reset line, already added
        for (int r = 1; r < embeddedReferenced.length; r++) {
          result.add(embeddedReferenced[r]);
        }
      }
    } else {
      // fallback if reference is not found or empty
      warn("Invalid message reference '" + relativeReferenceName + "' (qualified: " + qualifiedReferenceName
        + ") in " + originKey + ": '" + line + "'");
      builder.append(START_ARG_CHAR).append(REFERENCE_CHAR).append(qualifiedReferenceName).append(END_ARG_CHAR);
    }
  }

  @NotNull
  @CheckReturnValue
  private static String qualifyRelativeKey(@NotNull String relativeReferenceName, @NotNull String[] originSubDocumentPath,
                                           @NotNull Map<String, String[]> messagesBundle) {
    // pre-build the full path prefix once; prefix lengths per depth let us "truncate" cheaply
    StringBuilder fullPrefix = new StringBuilder(originSubDocumentPath.length * 12);
    int[] prefixLengthAtDepth = new int[originSubDocumentPath.length];
    for (int depth = 0; depth < originSubDocumentPath.length; depth++) {
      fullPrefix.append(originSubDocumentPath[depth]).append(SUB_DOCUMENT_DELIMITER);
      prefixLengthAtDepth[depth] = fullPrefix.length();
    }

    String mostSpecificKey = fullPrefix + relativeReferenceName;
    if (messagesBundle.containsKey(mostSpecificKey)) {
      return mostSpecificKey;
    }

    // walk from the second-deepest scope toward global
    for (int depth = originSubDocumentPath.length - 2; depth >= 0; depth--) {
      String candidate = fullPrefix.substring(0, prefixLengthAtDepth[depth]) + relativeReferenceName;
      if (messagesBundle.containsKey(candidate)) {
        return candidate;
      }
    }

    // finally the global (unqualified) scope
    if (messagesBundle.containsKey(relativeReferenceName)) {
      return relativeReferenceName;
    }

    // nothing found anywhere -> return the fully qualified (most specific) key anyway
    return mostSpecificKey;
  }

  private static String[] extractSubDocumentPath(@NotNull String key) {
    String[] split = key.split(SUB_DOCUMENT_PATH_PATTERN);
    return split; // TODO TEST: don't drop last part (key itself) for flat path colorization
//    if (split.length <= 1) return EMPTY_STRINGS;
//    return Arrays.copyOfRange(split, 0, split.length - 1); // drop last part (key itself)
  }

  @NotNull
  @CheckReturnValue
  public static String[] embedPositionalArgs(@NotNull String[] origin, @Nullable Object[] args) {
    String[] result = new String[origin.length];
    for (int i = 0; i < origin.length; i++) {
      result[i] = embedPositionalArgs(origin[i], args);
    }
    return result;
  }

  @NotNull
  @CheckReturnValue
  public static String embedPositionalArgs(@NotNull String sequence, @Nullable Object[] args) {
    if (args.length == 0) return sequence;

    // faster (maybe more error-prone) impl than regex matching
    boolean inArgument = false;
    StringBuilder builder = new StringBuilder(sequence.length() + 16); // maybe avoid resizing
    StringBuilder argument = new StringBuilder(2); // usually short (e.g., {0}, {11})

    int len = sequence.length();
    for (int i = 0; i < len; i++) {
      char c = sequence.charAt(i);

      if (c == END_ARG_CHAR && inArgument) {
        inArgument = false;

        if (!argument.isEmpty() && argument.charAt(0) == REFERENCE_CHAR) {
          argument.setLength(0); // clear and reuse
          continue;
        }

        int argIndex = parsePositiveInt(argument);
        if (argIndex >= 0 && argIndex < args.length) {
          if (args[argIndex] != null) {
            builder.append(args[argIndex]); // String.valueOf
          } else {
            builder.append(START_ARG_CHAR).append(argument).append(END_ARG_CHAR); // leave null values as is
          }
        } else {
          // fallback if index is invalid or out of bounds
          warn("Invalid argument index '" + argument + "' in '" + sequence + "'");
          builder.append(START_ARG_CHAR).append(argument).append(END_ARG_CHAR);
        }
        argument.setLength(0); // clear and reuse
        continue; // skip end char
      }

      if (c == START_ARG_CHAR && !inArgument) {
        inArgument = true;
        continue; // skip start char
      }

      if (inArgument) {
        argument.append(c);
        continue;
      }

      builder.append(c);
    }

    if (!argument.isEmpty()) { // arg was not closed
      builder.append(START_ARG_CHAR).append(argument);
    }

    return builder.toString();
  }

  private static void colorize(@NotNull String[] value, @NotNull String[] originSubDocumentPath, @NotNull Map<String, String[]> messageBundle) {
    if (value.length == 0) return;

    String qualifiedColorizeKey = qualifyRelativeKey(COLORIZE_KEY, originSubDocumentPath, messageBundle);
    String[] colorizeRawValue = messageBundle.get(qualifiedColorizeKey);
    if (colorizeRawValue == null || colorizeRawValue.length == 0) return; // no colorizer defined
    // resolve references in colorizer, skip colorization
    String[] colorizeValue = embedReferences(colorizeRawValue, qualifiedColorizeKey, messageBundle, new HashSet<>(List.of(qualifiedColorizeKey)), false);

    StringBuilder builder = new StringBuilder(value[0].length() + colorizeValue[0].length());
    StringBuilder argument = new StringBuilder(32);
    boolean colorizing = false;
    for (int line = 0; line < value.length; line++) {
      String text = value[line];
      int len = text.length();
      for (int i = 0; i < len; i++) {
        char c = text.charAt(i);

        if (c == ESCAPE_CHAR) {
          if (i < len - 1) {
            c = text.charAt(++i);
          }
          if (colorizing) argument.append(c);
          else builder.append(c);
          continue;
        }

        if (c == COLORIZE_CHAR) {
          if (colorizing) {
            colorizing = false;
            // hotfix: null value in arra to allow {1} in *colorize* for gradient phase; more robust solution required
            String[] embeddedColorized = embedPositionalArgs(colorizeValue, new Object[]{argument.toString(), null});
            for (String s : embeddedColorized) {
              builder.append(s);
            }
            argument.setLength(0); // clear and reuse
          } else {
            colorizing = true;
          }
          continue; // skip colorize char
        }

        if (colorizing) {
          argument.append(c);
          continue;
        }
        builder.append(c);
      } // end of line

      if (!argument.isEmpty()) {
        builder.append(COLORIZE_CHAR).append(argument);
      }
      value[line] = builder.toString();
      builder.setLength(0); // reset line for next iteration
    }
  }

  private static void warn(@NotNull String message) {
    logger.log(Logger.Level.WARNING, message);
  }

  // helper to parse int without throwing expensive exceptions
  public static int parsePositiveInt(@NotNull StringBuilder sb) {
    int len = sb.length();
    if (len == 0) return -1;
    int num = 0;
    for (int i = 0; i < len; i++) {
      char c = sb.charAt(i);
      if (c < '0' || c > '9') return -1; // not a valid digit
      num = num * 10 + (c - '0');
    }
    return num;
  }

  public static boolean isPrimitiveArg(@Nullable Object arg) {
    return arg instanceof String || arg instanceof Number || arg instanceof Character;
  }

}
