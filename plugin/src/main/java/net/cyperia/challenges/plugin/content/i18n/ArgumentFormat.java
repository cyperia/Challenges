package net.cyperia.challenges.plugin.content.i18n;

import lombok.RequiredArgsConstructor;
import net.cyperia.commons.common.collection.NumberFormatter;
import net.cyperia.commons.common.collection.pair.Tuple;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface ArgumentFormat<T> extends Function<T, LocalizableMessage> {

  @NotNull
  LocalizableMessage apply(@NotNull T arg);

  boolean isApplicable(@NotNull Object arg);

  BiArgumentFormat<Object, Object> BRACKETS = register(Object.class, Object.class, "brackets", (arg1, arg2) ->
    MessageKey.of("arg-format.brackets_arg").withArgs(arg1, arg2));

  ArgumentFormat<Number> HEARTS = register(Number.class, "hearts", arg -> {
    double hearts = arg.doubleValue() / 2;
    return MessageKey.of("arg-format.hearts").withArgs(NumberFormatter.FLOATING_POINT.format(hearts));
  });
  ArgumentFormat<Number> HEARTS_LIMITED = register(Number.class, "hearts_limited", arg -> {
    if (arg.doubleValue() >= 1000) return MessageKey.of("symbol.infinity");
    return HEARTS.apply(arg);
  });

  ArgumentFormat<Number> HP = register(Number.class, "hp", arg -> {
    int hp = arg.intValue();
    double hearts = hp / 2d;
    return MessageKey.of("arg-format.hp").withArgs(hp, NumberFormatter.FLOATING_POINT.format(hearts));
  });

  ArgumentFormat<Number> HP_SHORT = register(Number.class, "hp_short",
    arg -> MessageKey.of("arg-format.hp-short").withArgs(arg.intValue()));

  @SuppressWarnings({"unchecked", "rawtypes"})
  ArgumentFormat<Tuple> SECONDS_RANGE = register(Tuple.class, "time_range", arg -> {
    Tuple<Integer, Integer> range = (Tuple<Integer, Integer>) arg;
    int seconds = range.getFirst();
    int secondsRange = range.getSecond();

    if (seconds % 15 == 0) {
      float minutes = seconds / 60f;
      return MessageKey.of("arg-format.time-range").withArgs(
        NumberFormatter.DEFAULT.format(minutes), MessageKey.pluralize("generic.minute", minutes),
        NumberFormatter.DEFAULT.format(secondsRange), MessageKey.pluralize("generic.second", secondsRange));
    }

    return MessageKey.of("arg-format.time-range").withArgs(
      NumberFormatter.DEFAULT.format(seconds), MessageKey.pluralize("generic.second", seconds),
      NumberFormatter.DEFAULT.format(secondsRange), MessageKey.pluralize("generic.second", secondsRange));
  });

  ArgumentFormat<Number> TIME = register(Number.class, "time", arg -> {
    long time = arg.longValue();
    long s = time % 60;
    long m = (time / 60) % 60;
    long h = (time / 3_600) % 24;
    long d = time / 86_400;

    List<LocalizableMessage> components = new LinkedList<>();
    if (s > 0 || time == 0) { // show 0s
      components.add(MessageKey.of("arg-format.time").withArgs(s, MessageKey.pluralize("generic.second", s)));
    }
    if (m > 0) {
      components.addFirst(MessageKey.of("arg-format.time").withArgs(m, MessageKey.pluralize("generic.minute", m)));
    }
    if (h > 0) {
      components.addFirst(MessageKey.of("arg-format.time").withArgs(h, MessageKey.pluralize("generic.hour", h)));
    }
    if (d > 0) {
      components.addFirst(MessageKey.of("arg-format.time").withArgs(d, MessageKey.pluralize("generic.day", d)));
    }

    return LocalizableMessage.joinList(components);
  });

  ArgumentFormat<EntityDamageEvent> DAMAGE_CAUSE = register(EntityDamageEvent.class, "damage_cause", event -> {
    if (event.getCause() == EntityDamageEvent.DamageCause.CUSTOM) return MessageKey.of("generic.undefined");
    EntityDamageEvent.DamageCause cause = event.getCause();

    if (event instanceof EntityDamageByBlockEvent damageEvent) {
      if (damageEvent.getDamager() != null) {
        return MessageKey.of("arg-format.damage-cause-source").withArgs(cause, damageEvent.getDamager().getType());
      }
    }

    if (!(event instanceof EntityDamageByEntityEvent damageEvent)) {
      return MessageKey.of("arg-format.damage-cause").withArgs(cause);
    }

    if (damageEvent.getDamager() instanceof Player damagerPlayer) {
      return MessageKey.of("arg-format.damage-cause-source").withArgs(cause, damagerPlayer);
    } else if (damageEvent.getDamager() instanceof Projectile projectile) {
      ProjectileSource shooter = projectile.getShooter();
      if (shooter instanceof Player shooterPlayer) {
        return MessageKey.of("arg-format.damage-cause-source").withArgs(projectile.getType(), shooterPlayer);
      } else if (shooter instanceof Entity shooterEntity) {
        return MessageKey.of("arg-format.damage-cause-source").withArgs(projectile.getType(), shooterEntity.getType());
      } else {
        return MessageKey.of("arg-format.damage-cause").withArgs(projectile.getType());
      }
    } else if (damageEvent.getDamager() instanceof TNTPrimed tntPrimed) {
      Entity source = tntPrimed.getSource();
      if (source instanceof Player sourcePlayer) {
        return MessageKey.of("arg-format.damage-cause-source").withArgs(tntPrimed.getType(), sourcePlayer);
      } else if (source instanceof Entity sourceEntity) {
        return MessageKey.of("arg-format.damage-cause-source").withArgs(tntPrimed.getType(), sourceEntity.getType());
      } else {
        return MessageKey.of("arg-format.damage-cause").withArgs(cause, tntPrimed.getType());
      }
    } else {
      return MessageKey.of("arg-format.damage-cause-source").withArgs(cause, damageEvent.getDamager().getType());
    }
  });

  @NotNull
  static <T> ArgumentFormat<T> register(@NotNull Class<T> argClass, @NotNull String key, @NotNull Function<T, LocalizableMessage> formatter) {
    ArgumentFormatImpl<T> format = new ArgumentFormatImpl<>(argClass, formatter);
    ArgumentFormatImpl.REGISTRY.put(key, format);
    return format;
  }

  static <T, U> BiArgumentFormat<T, U> register(@NotNull Class<T> arg1Class, @NotNull Class<U> arg2Class,
                                                @NotNull String key, @NotNull BiFunction<T, U, LocalizableMessage> formatter) {
    BiArgumentFormatImpl<T, U> format = new BiArgumentFormatImpl<>(arg1Class, arg2Class, formatter);
    ArgumentFormatImpl.REGISTRY.put(key, format);
    return format;
  }

  @Nullable
  static ArgumentFormat<?> getByName(@NotNull String name) {
    return ArgumentFormatImpl.REGISTRY.get(name);
  }

  interface BiArgumentFormat<T, U> extends ArgumentFormat<Tuple<T, U>> {

    @NotNull
    LocalizableMessage apply(@NotNull T arg1, @NotNull U arg2);

    @NotNull
    @Override
    default LocalizableMessage apply(@NotNull Tuple<T, U> args) {
      return apply(args.getFirst(), args.getSecond());
    }

  }

  @RequiredArgsConstructor
  class ArgumentFormatImpl<T> implements ArgumentFormat<T> {

    private static final Map<String, ArgumentFormat<?>> REGISTRY = new HashMap<>();

    private final Class<T> argClass;
    private final Function<T, LocalizableMessage> formatter;

    @NotNull
    @Override
    public LocalizableMessage apply(@NotNull T arg) {
      return formatter.apply(arg);
    }

    @Override
    public boolean isApplicable(@NotNull Object arg) {
      return argClass.isAssignableFrom(arg.getClass());
    }
  }

  @RequiredArgsConstructor
  class BiArgumentFormatImpl<T, U> implements BiArgumentFormat<T, U> {

    private final Class<T> arg1Class;
    private final Class<U> arg2Class;
    private final BiFunction<T, U, LocalizableMessage> formatter;

    @NotNull
    @Override
    public LocalizableMessage apply(@NonNull T arg1, @NonNull U arg2) {
      return formatter.apply(arg1, arg2);
    }

    @Override
    public boolean isApplicable(@NotNull Object arg) {
      return Tuple.class.isAssignableFrom(arg.getClass())
        && ((Tuple<?, ?>) arg).getFirst().getClass().isAssignableFrom(arg1Class)
        && ((Tuple<?, ?>) arg).getSecond().getClass().isAssignableFrom(arg2Class);
    }
  }

}
