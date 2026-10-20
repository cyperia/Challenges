package net.cyperia.commons.common.config;

import net.cyperia.commons.common.config.exceptions.ConfigReadOnlyException;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * @see Document
 */
public interface Config extends Propertyable {

  /**
   * Sets the value at the given path.
   * <p>
   * Setting a value to {@code null} has the same effect as {@link #remove(String) removing} it.
   * {@code config.set(path, null)} is equivalent with {@code config.remove(path)}
   *
   * @param value The value to change to, {@code null} to remove
   * @return {@code this} for chaining
   * @throws ConfigReadOnlyException If this is {@link #isReadonly() readonly}
   */
  @NotNull
  Config set(@NotNull String path, @Nullable Object value);

  /**
   * @throws ConfigReadOnlyException If this is {@link #isReadonly() readonly}
   */
  @NotNull
  Config clear();

  /**
   * Removing a value has the same effect as {@link #set(String, Object) setting} it to {@code null}
   * {@code config.set(path, null)} is equivalent with {@code config.remove(path)}
   *
   * @return {@code this} for chaining
   * @throws ConfigReadOnlyException If this is {@link #isReadonly() readonly}
   */
  @NotNull
  Config remove(@NotNull String path);

  boolean isReadonly();

  /**
   * @return A new config which is readonly, or {@code this} if already {@link #isReadonly() readonly}
   */
  @NotNull
  @CheckReturnValue
  Config readonly();

  @NotNull
  @Override
  default <O extends Propertyable> Config apply(@NotNull Consumer<O> action) {
    return (Config) Propertyable.super.apply(action);
  }

  @NotNull
  @Override
  default <O extends Propertyable> Config applyIf(boolean expression, @NotNull Consumer<O> action) {
    return (Config) Propertyable.super.applyIf(expression, action);
  }

  @NotNull
  default Config increment(@NotNull String path, double amount) {
    return set(path, getDouble(path) + amount);
  }

  @NotNull
  default Config decrement(@NotNull String path, double amount) {
    return set(path, getDouble(path) - amount);
  }

  @NotNull
  default Config multiply(@NotNull String path, double factor) {
    return set(path, getDouble(path) * factor);
  }

  @NotNull
  default Config divide(@NotNull String path, double divisor) {
    return set(path, getDouble(path) / divisor);
  }

  @NotNull
  default Config setIfAbsent(@NotNull String path, @NotNull Object defaultValue) {
    if (!contains(path))
      set(path, defaultValue);
    return this;
  }

}
