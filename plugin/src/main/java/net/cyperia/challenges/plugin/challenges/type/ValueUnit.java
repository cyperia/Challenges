package net.cyperia.challenges.plugin.challenges.type;

import net.cyperia.challenges.plugin.content.i18n.ArgumentFormat;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.commons.common.collection.IRandom;
import net.cyperia.commons.common.collection.pair.Tuple;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

/**
 * What the raw {@link IModifier#getValue() value} of a modifier stands for: the factor it scales by, how
 * the resulting quantity is rendered and, for time units, how long an interval it describes.
 * <p>
 * Declaring it in one place is what keeps the item name, the lore, the click tooltips and the timer of a
 * {@link net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge} from drifting apart.
 */
public final class ValueUnit {

  private static final String DESC_TIME = "challenge.settings-modifier-time";
  private static final String DESC_INTERVAL = "challenge.settings-modifier-interval";

  public static final ValueUnit
    VALUE = quantity(withKey("challenge.settings-modifier-value"), null),
    MULTIPLIER = quantity(withKey("challenge.settings-modifier-multiplier"), null),
    PERCENT = quantity(withKey("arg-format.percent"), null),
    STRENGTH = quantity(withKey("challenge.settings-modifier-value"), "challenge.settings-modifier-strength"),
    HEARTS = quantity(ArgumentFormat.HEARTS::apply, null),
    DAMAGE = health("challenge.settings-modifier-damage"),
    MAX_HEALTH = health("challenge.settings-modifier-max-health"),
    BLOCKS = quantity(amountOf("generic.block"), "challenge.settings-modifier-radius"),
    CHUNKS = quantity(amountOf("generic.chunk"), "challenge.settings-modifier-radius"),
    SECONDS = time(DESC_TIME),
    MINUTES = SECONDS.scaled(60),
    INTERVAL_SECONDS = time(DESC_INTERVAL),
    INTERVAL_MINUTES = INTERVAL_SECONDS.scaled(60);

  private final IntFunction<LocalizableMessage> format;
  private final IntFunction<LocalizableMessage> compactFormat;
  private final String descriptionKey;
  private final Object[] descriptionArgs;
  private final IntUnaryOperator quantity;
  private final int jitter;
  private final boolean time, named, linear;

  private ValueUnit(@NotNull IntFunction<LocalizableMessage> format,
                    @NotNull IntFunction<LocalizableMessage> compactFormat,
                    @Nullable String descriptionKey, @NotNull Object[] descriptionArgs,
                    @NotNull IntUnaryOperator quantity, int jitter, boolean time, boolean named, boolean linear) {
    this.format = format;
    this.compactFormat = compactFormat;
    this.descriptionKey = descriptionKey;
    this.descriptionArgs = descriptionArgs;
    this.quantity = quantity;
    this.jitter = jitter;
    this.time = time;
    this.named = named;
    this.linear = linear;
  }

  /**
   * A value that names a state rather than counting something: it becomes the item name, and clicks are
   * shown as the state they lead to ({@code Set to Hard}) instead of as a delta ({@code +1}).
   */
  @NotNull
  @Contract(pure = true)
  public static ValueUnit named(@NotNull IntFunction<LocalizableMessage> format) {
    return new ValueUnit(format, format, null, LocalizableMessage.EMPTY_ARGS, IntUnaryOperator.identity(), 0, false, true, true);
  }

  @NotNull
  @Contract(pure = true)
  public ValueUnit scaled(int factor) {
    return new ValueUnit(format, compactFormat, descriptionKey, descriptionArgs,
      value -> quantity.applyAsInt(value) * factor, jitter, time, named, linear);
  }

  /**
   * Values that pick from {@code quantities} in order instead of counting: value 1 stands for the first
   * entry, so the modifier's range has to be {@code 1..quantities.length}.
   */
  @NotNull
  @Contract(pure = true)
  public ValueUnit steps(int... quantities) {
    return new ValueUnit(format, compactFormat, descriptionKey, descriptionArgs,
      value -> quantity.applyAsInt(quantities[value - 1]), jitter, time, named, false);
  }

  /**
   * The same unit without its label, for items that show the amount as their name.
   */
  @NotNull
  @Contract(pure = true)
  public ValueUnit bare() {
    return new ValueUnit(format, compactFormat, null, LocalizableMessage.EMPTY_ARGS, quantity, jitter, time, named, linear);
  }

  /**
   * The same unit labelled by the given message instead of the shared one, for challenges that need wording
   * of their own. The message receives the amount as {@code {0}}, any {@code extraArgs} follow it.
   */
  @NotNull
  @Contract(pure = true)
  public ValueUnit describedBy(@NotNull MessageKey descriptionKey, @NotNull Object... extraArgs) {
    return new ValueUnit(format, compactFormat, descriptionKey.getKey(), extraArgs, quantity, jitter, time, named, linear);
  }

  /**
   * Spreads the activation randomly by up to {@code jitter} seconds around the exact amount.
   */
  @NotNull
  @Contract(pure = true)
  public ValueUnit jitter(int jitter) {
    if (!time) throw new UnsupportedOperationException("jitter is only supported by time units");
    return new ValueUnit(format, compactFormat, descriptionKey, descriptionArgs, quantity, jitter, true, named, linear);
  }

  /**
   * The exact quantity {@code value} stands for, without any jitter or label, e.g. {@code 5 minutes} or
   * {@code 10 HP (5 ❤)}. This is the full form, used wherever the value is stated for the first time.
   */
  @NotNull
  public LocalizableMessage amount(int value) {
    return format.apply(quantityOf(value));
  }

  /**
   * The same quantity as {@link #amount(int)}, reduced to what is worth repeating — {@code 10 HP} rather
   * than {@code 10 HP (5 ❤)}. Click tooltips use this because they sit right under an item whose name
   * already spells the value out in full, where the second unit only adds noise. Units without a verbose
   * form return the same message as {@link #amount(int)}.
   */
  @NotNull
  public LocalizableMessage compact(int value) {
    return compactFormat.apply(quantityOf(value));
  }

  /**
   * The labelled form, e.g. {@code Interval: 5 minutes ± 30 seconds}, or {@code null} for units without a
   * label of their own.
   */
  @Nullable
  public LocalizableMessage describe(int value) {
    if (descriptionKey == null) return null;
    LocalizableMessage amount = jitter == 0
      ? amount(value)
      : ArgumentFormat.SECONDS_RANGE.apply(Tuple.of(quantityOf(value), jitter));

    if (descriptionArgs.length == 0) return MessageKey.of(descriptionKey).withArgs(amount);
    Object[] args = new Object[descriptionArgs.length + 1];
    args[0] = amount;
    System.arraycopy(descriptionArgs, 0, args, 1, descriptionArgs.length);
    return MessageKey.of(descriptionKey).withArgs(args);
  }

  /**
   * How the value reads as an item name: the labelled form where the unit has one, the bare amount
   * otherwise. Items that carry the value in their name use this rather than {@link #amount(int)}, which
   * has to stay usable as a click delta.
   */
  @NotNull
  public LocalizableMessage name(int value) {
    LocalizableMessage described = describe(value);
    return described != null ? described : amount(value);
  }

  /**
   * The quantity {@code value} stands for as a plain number: {@code 5} on a seconds unit, {@code 300} on a
   * minutes one. This is what {@link #amount(int)} renders, for the places that need the number itself.
   */
  public int quantityOf(int value) {
    return quantity.applyAsInt(value);
  }

  /**
   * The seconds {@code value} stands for, jitter applied.
   */
  public int roll(int value, @NotNull IRandom random) {
    int seconds = quantityOf(value);
    return jitter == 0 ? seconds : random.around(seconds, jitter);
  }

  public boolean isTime() {
    return time;
  }

  /**
   * Whether the value names a state instead of counting something.
   */
  public boolean isNamed() {
    return named;
  }

  /**
   * Whether the quantity grows by the same amount per value step, so a click can be shown as a delta.
   */
  public boolean isLinear() {
    return linear;
  }

  @NotNull
  @Contract(pure = true)
  private static ValueUnit quantity(@NotNull IntFunction<LocalizableMessage> format, @Nullable String descriptionKey) {
    return new ValueUnit(format, format, descriptionKey, LocalizableMessage.EMPTY_ARGS, IntUnaryOperator.identity(), 0, false, false, true);
  }

  /**
   * Health in HP, spelled out with its hearts equivalent and repeated as plain HP.
   */
  @NotNull
  @Contract(pure = true)
  private static ValueUnit health(@NotNull String descriptionKey) {
    return new ValueUnit(ArgumentFormat.HP::apply, ArgumentFormat.HP_SHORT::apply,
      descriptionKey, LocalizableMessage.EMPTY_ARGS, IntUnaryOperator.identity(), 0, false, false, true);
  }

  @NotNull
  @Contract(pure = true)
  private static ValueUnit time(@NotNull String descriptionKey) {
    return new ValueUnit(ArgumentFormat.TIME::apply, ArgumentFormat.TIME::apply,
      descriptionKey, LocalizableMessage.EMPTY_ARGS, IntUnaryOperator.identity(), 0, true, false, true);
  }

  @NotNull
  @Contract(pure = true)
  private static IntFunction<LocalizableMessage> withKey(@NotNull String messageKey) {
    return amount -> MessageKey.of(messageKey).withArgs(amount);
  }

  /**
   * A count followed by its pluralized unit word, e.g. {@code 5 Blocks}.
   */
  @NotNull
  @Contract(pure = true)
  private static IntFunction<LocalizableMessage> amountOf(@NotNull String unitKey) {
    return amount -> MessageKey.of("arg-format.radius").withArgs(amount, MessageKey.pluralize(unitKey, amount));
  }

}
