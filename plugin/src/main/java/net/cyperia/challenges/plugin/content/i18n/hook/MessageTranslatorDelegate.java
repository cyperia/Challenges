package net.cyperia.challenges.plugin.content.i18n.hook;

import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Set;

public interface MessageTranslatorDelegate {

  /**
   * Returns the {@link MessageKey} instance with which the given {@code key} should be
   * overridden, or {@code null} if no override should take place.
   * <p>
   * Overriding just the message key only replaces its direct value. Any key that depends on it using a reference
   * will NOT be affected, as references are pre-resolved. When targeting a reference value overwrite you should use
   * {@link #getMessageBundleOverride(String, Locale)} or overwrite all keys using the reference.
   */
  @Nullable
  MessageKey getMessageKeyOverride(@NotNull String key, @NotNull MessageKey original);

  /**
   * Returns the raw translation value for the given {@code key} if it should be
   * overridden, or {@code null} if no override should take place.
   */
  @Nullable
  String[] getMessageBundleOverride(@NotNull String key, @NotNull Locale locale);

  @NotNull
  Set<String> getMessageBundleKeyAdditions();

}
