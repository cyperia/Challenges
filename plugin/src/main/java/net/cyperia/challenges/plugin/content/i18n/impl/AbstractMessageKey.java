package net.cyperia.challenges.plugin.content.i18n.impl;

import net.cyperia.challenges.catalog.MessageFormatter;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.i18n.*;
import net.cyperia.challenges.plugin.content.i18n.impl.format.ComponentFormatter;
import net.cyperia.challenges.plugin.content.i18n.impl.format.CustomTranslatable;
import net.cyperia.challenges.plugin.management.server.TitleManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.time.Duration;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.cyperia.challenges.plugin.content.i18n.TranslationManager.getLocaleOf;

public abstract class AbstractMessageKey implements MessageKey {

  @NotNull
  @Override
  public String[] localizeWithPrimitiveArgs(@NotNull Locale locale, @NotNull Object... args) {
    String[] rawValue = localizeRawValue(locale);
    if (args.length == 0) return rawValue;

    boolean anyPrimitiveArgs = false;
    Object[] primitiveArgsCopy = new Object[args.length];
    for (int i = 0; i < primitiveArgsCopy.length; i++) {
      if (MessageFormatter.isPrimitiveArg(args[i])) {
        primitiveArgsCopy[i] = args[i];
        anyPrimitiveArgs = true;
      }
      // else leave null
    }
    if (!anyPrimitiveArgs) return rawValue;

    // embedPositionalArgs will skip null arguments
    return MessageFormatter.embedPositionalArgs(rawValue, primitiveArgsCopy);
  }

  @NotNull
  @Override
  public String localizeWithPrimitiveArgsAsSingleLine(@NotNull Locale locale, @NotNull Object... args) {
    return asSingleLine(localizeWithPrimitiveArgs(locale, args));
  }

  @NotNull
  @Override
  public String localizePlainText(@NotNull Locale locale, @NotNull Object... args) {
    return ComponentFormatter.serializePlainText(asComponent(locale, args));
  }

  @NotNull
  protected String asSingleLine(@NotNull String[] value) {
    if (value.length == 1) return value[0];
    return String.join("\n", value);
  }

  @NotNull
  @Override
  public Component asComponent(@NotNull Locale locale) {
    return asComponent(locale, EMPTY_ARGS);
  }

  @NotNull
  @Override
  public List<Component> asComponents(@NotNull Locale locale) {
    return asComponents(locale, EMPTY_ARGS);
  }

  @NotNull
  @Override
  public KeyedLocalizableMessage withArgs(@NotNull Object... args) {
    return new LocalizableMessageImpl(this, args);
  }

  @NotNull
  @Override
  public MessageKey getLocalizableKey() {
    return this;
  }

  @Nullable
  protected String localizePrefix(@NotNull Locale locale, @Nullable Prefix prefix) {
    if (prefix == null) return null;
    return prefix.getLocalizableKey().localizeRawValueAsSingleLine(locale);
  }

  public static void localizeArgs(@NotNull Locale locale, @NotNull Object[] args) {
    for (int i = 0; i < args.length; i++) {
      args[i] = localizeArg(locale, args[i]);
    }
  }

  public static Object localizeArg(@NotNull Locale locale, @Nullable Object arg) {
    if (arg == null) return null;
    if (arg instanceof Collection<?> collection) {
      List<Object> localizedList = new ArrayList<>(collection.size());
      for (Object element : collection) {
        localizedList.add(localizeArg(locale, element));
      }
      return localizedList;
    }
    if (arg.getClass().isArray()) {
      int length = Array.getLength(arg);
      Object[] localizedArray = new Object[length];
      for (int i = 0; i < length; i++) {
        Object element = Array.get(arg, i);
        localizedArray[i] = localizeArg(locale, element);
      }
      return localizedArray;
    }
    if (arg instanceof Supplier<?> supplier) {
      return localizeArg(locale, supplier.get());
    }

    if (arg instanceof LocalizableMessage message) {
      return message.asComponent(locale);
    }
    MessageKey customTranslation = CustomTranslatable.find(arg);
    if (customTranslation != null) {
      return customTranslation.asComponent(locale);
    }
    return arg;
  }

  @NotNull
  @CheckReturnValue
  public static Object[] localizeArgsAsCopy(@NotNull Locale locale, @NotNull Object[] args) {
    if (args.length == 0) return args;
    // avoid mutating original array & ensure args array is really of type Object, so MessageHolder can be stored
    Object[] localizedArgs = new Object[args.length];
    System.arraycopy(args, 0, localizedArgs, 0, args.length);
    localizeArgs(locale, localizedArgs);
    return localizedArgs;
  }

  @NotNull
  public static String[] arrayOf(@NotNull String single) {
    return new String[]{single};
  }

  // Action Implementations

  @Override
  public void send(@NotNull CommandSender target, @Nullable Prefix prefix, @NotNull Object... args) {
    if (target instanceof Player) {
      send((Player) target, prefix, args);
      return;
    }
    Locale locale = TranslationManager.getServerLocale();
    if (isEmptyTranslation(locale)) return;
    asChatLines(locale, prefix, args).forEach(target::sendMessage);
  }

  @Override
  public void send(@NotNull Player target, @Nullable Prefix prefix, @NotNull Object... args) {
    Locale locale = getLocaleOf(target);
    if (isEmptyTranslation(locale)) return;
    asChatLines(locale, prefix, args).forEach(target::sendMessage);
  }

  // A message translated to "" is not sent at all, so a translation can drop a line the code always sends
  boolean isEmptyTranslation(@NotNull Locale locale) {
    for (String line : localizeRawValue(locale)) {
      if (!line.isEmpty()) return false;
    }
    return true;
  }

  @Override
  public void broadcast(@Nullable Prefix prefix, @NotNull Object... args) {
    doBroadcast0(Challenges.getInstance().getPlayerManager().getAudience(),
      (player, lines) -> lines.forEach(player::sendMessage), locale -> asChatLines(locale, prefix, args));
  }

  @Override
  public void sendTitle(@NotNull Player target, @NotNull Object... args) {
    target.showTitle(createTitleFromComponentList(asComponents(target, args),
      Title.Times.times(Ticks.duration(TitleManager.FADEIN), Ticks.duration(TitleManager.DURATION), Ticks.duration(TitleManager.FADEOUT))));
  }

  @Override
  public void sendTitleInstantly(@NotNull Player target, @NotNull Object... args) {
    target.showTitle(createTitleFromComponentList(asComponents(target, args),
      Title.Times.times(Duration.ZERO, Ticks.duration(TitleManager.DURATION), Ticks.duration(TitleManager.FADEOUT))));
  }

  @Override
  public void broadcastTitle(@NotNull Object... args) {
    doBroadcastAsList(args, (player, components) ->
      player.showTitle(createTitleFromComponentList(components,
        Title.Times.times(Ticks.duration(TitleManager.FADEIN), Ticks.duration(TitleManager.DURATION), Ticks.duration(TitleManager.FADEOUT))))
    );
  }

  @Override
  public void broadcastTitleInstantly(@NotNull Object... args) {
    doBroadcastAsList(args, (player, components) ->
      player.showTitle(createTitleFromComponentList(components,
        Title.Times.times(Duration.ZERO, Ticks.duration(TitleManager.DURATION), Ticks.duration(TitleManager.FADEOUT))))
    );
  }

  @Override
  public void sendActionBar(@NotNull Player target, @NotNull Object... args) {
    target.sendActionBar(asComponent(getLocaleOf(target), null, args));
  }

  @Override
  public void broadcastActionBar(@NotNull Object... args) {
    doBroadcast(null, args, Player::sendActionBar);
  }

  @Override
  public void broadcastActionBar(@NotNull Collection<? extends Player> targets, @NotNull Object... args) {
    doBroadcast0(targets, Player::sendActionBar, locale -> asComponent(locale, null, args));
  }

  @NotNull
  protected Title createTitleFromComponentList(@NotNull List<Component> components, @NotNull Title.Times timing) {
    return Title.title(!components.isEmpty() ? components.getFirst() : Component.empty(),
      components.size() > 1 ? components.get(1) : Component.empty(), timing);
  }

  protected void doBroadcast(@Nullable Prefix prefix, @NotNull Object[] args, @NotNull BiConsumer<Player, Component> messageSender) {
    doBroadcast0(Challenges.getInstance().getPlayerManager().getAudience(), messageSender, locale -> asComponent(locale, prefix, args));
  }

  protected void doBroadcastAsList(@NotNull Object[] args, @NotNull BiConsumer<Player, List<Component>> messageSender) {
    doBroadcast0(Challenges.getInstance().getPlayerManager().getAudience(), messageSender, locale -> asComponents(locale, args));
  }

  protected <T> void doBroadcast0(@NotNull Collection<? extends Player> targets, @NotNull BiConsumer<Player, T> messageSender, @NotNull Function<Locale, T> compute) {
    if (targets.isEmpty()) return;
    if (targets.size() == 1) { // skip overhead
      Player player = targets.iterator().next();
      messageSender.accept(player, compute.apply(getLocaleOf(player)));
      return;
    }

    Map<Locale, T> localizedCache = new HashMap<>();
    for (Player target : targets) {
      Locale locale = getLocaleOf(target);
      T formatted = localizedCache.computeIfAbsent(locale, compute);
      messageSender.accept(target, formatted);
    }
  }

  @Override
  public abstract boolean equals(Object object);

  @Override
  public abstract int hashCode();

}
