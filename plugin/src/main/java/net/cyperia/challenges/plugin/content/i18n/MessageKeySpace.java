package net.cyperia.challenges.plugin.content.i18n;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.cyperia.challenges.catalog.MessageFormatter;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a parent of {@link MessageKey} or deeper nested {@link MessageKeySpace}.
 * It may not be a {@link MessageKey} itself and may hold no translation value
 * (key may not be contained in the localization files).
 */
public interface MessageKeySpace {

  char DELIMITER = MessageFormatter.SUB_DOCUMENT_DELIMITER;

  @NotNull
  @CheckReturnValue
  @Contract(pure = true)
  static MessageKeySpace of(@NotNull String keySpace) {
    return new Impl(keySpace);
  }

  @NotNull
  @CheckReturnValue
  @Contract(pure = true)
  static MessageKeySpace of(@NotNull String parentSpace, @NotNull String childSpace) {
    return new Impl(parentSpace + DELIMITER + childSpace);
  }

  @NotNull
  @CheckReturnValue
  @Contract(pure = true)
  static MessageKeySpace of(@NotNull String parentSpace, @NotNull String childSpace, @NotNull String grandChildSpace) {
    return new Impl(parentSpace + DELIMITER + childSpace + DELIMITER + grandChildSpace);
  }

  @NotNull
  String getKey();

  @NotNull
  default MessageKey getChildKey(@NotNull String child) {
    return MessageKey.of(getKey() + DELIMITER + child);
  }

  @NotNull
  @CheckReturnValue
  default MessageKeySpace getChildSpace(@NotNull String child) {
    return MessageKeySpace.of(getKey() + DELIMITER + child);
  }

  @Getter
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  final class Impl implements MessageKeySpace {
    private final String key;
  }

}
