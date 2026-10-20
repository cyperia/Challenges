package net.cyperia.challenges.plugin.content.i18n.impl.dynamic;

import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.impl.AbstractMessageKey;
import net.cyperia.challenges.plugin.content.i18n.impl.format.ComponentArguments;
import net.cyperia.challenges.plugin.content.i18n.impl.format.rewrite.ComponentSplitter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

@ApiStatus.Internal
@ApiStatus.Experimental
public class WrappedObjMessageImpl implements LocalizableMessage {

  public static final LocalizableMessage EMPTY = new WrappedObjMessageImpl(Component.empty());

  public WrappedObjMessageImpl(@NotNull Object wrappedObj) {
    this.wrappedObj = wrappedObj;
  }

  private final Object wrappedObj;

  @NotNull
  @Override
  public Component asComponent(@NotNull Locale locale) {
    return ComponentArguments.convertToComponent(AbstractMessageKey.localizeArg(locale, wrappedObj));
  }

  @NotNull
  @Override
  public List<Component> asComponents(@NotNull Locale locale) {
    return ComponentSplitter.split(asComponent(locale));
  }

  @NotNull
  @Override
  public LocalizableMessage withArgs(@NotNull Object... args) {
    if (args.length != 1)
      throw new IllegalArgumentException("WrappedObjMessageImpl must wrap a single object, but got " + args.length + " arguments.");
    return new WrappedObjMessageImpl(args[0]);
  }

  @Nullable
  @Override
  public MessageKey getLocalizableKey() {
    return null;
  }

  @NotNull
  @Override
  public Object[] getLocalizableArgs() {
    return new Object[]{wrappedObj};
  }
}
