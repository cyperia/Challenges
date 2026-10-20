package net.cyperia.challenges.plugin.challenges.custom.settings.sub;

import lombok.Getter;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.builder.*;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.IParentCustomGenerator;
import net.cyperia.commons.common.misc.StringUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

@Getter
public abstract class SubSettingsBuilder {

  private String key;
  private SubSettingsBuilder parent;
  private SubSettingsBuilder child;

  protected SubSettingsBuilder(String key) {
    this.key = key;
    parent = null;
  }

  protected SubSettingsBuilder(String key, SubSettingsBuilder parent) {
    this.key = key;
    this.parent = parent;
  }

  public static ChooseItemSubSettingsBuilder createChooseItem(String key) {
    return new ChooseItemSubSettingsBuilder(key);
  }

  public static ValueSubSettingsBuilder createValueItem() {
    return new ValueSubSettingsBuilder();
  }

  public static ChooseMultipleItemSubSettingBuilder createChooseMultipleItem(String key) {
    return new ChooseMultipleItemSubSettingBuilder(key);
  }

  public static TextInputSubSettingsBuilder createTextInput(String key,
                                                            Consumer<Player> onOpen,
                                                            BiPredicate<Player, String> isValid) {
    return new TextInputSubSettingsBuilder(key, onOpen, isValid);
  }

  public static EmptySubSettingsBuilder createEmpty() {
    return new EmptySubSettingsBuilder();
  }

  public abstract boolean open(Player player, IParentCustomGenerator parentGenerator, LocalizableMessage title);

  @NotNull
  protected abstract Collection<SubSettingDisplay> getCurrentDisplayFor(@NotNull Map<String, String[]> activated);

  public abstract boolean hasSettings();

  @NotNull
  public final Collection<SubSettingDisplay> collectCurrentDisplayFor(@NotNull Map<String, String[]> activated) {
    List<SubSettingDisplay> display = new LinkedList<>();
    for (SubSettingsBuilder setting : getAllChildren()) {
      display.addAll(setting.getCurrentDisplayFor(activated));
    }
    return display;
  }

  public SubSettingsBuilder setParent(SubSettingsBuilder parent) {

    SubSettingsBuilder parentBuilder = getParent();
    if (parentBuilder != null) {
      return parentBuilder.setParent(parent);
    } else {
      this.parent = parent;
      return this;
    }

  }

  public SubSettingsBuilder setKey(String key) {
    this.key = key;
    return this;
  }

  @NotNull
  protected static LocalizableMessage getKeyTranslation(@NotNull String key) {
    MessageKey nameKey = MessageKey.find("custom.setting." + key + ".name");
    if (nameKey != null) return nameKey;
    // TODO remove fallback, everything should be translated!
    return LocalizableMessage.wrap(StringUtils.getEnumName(key));
  }

  public List<SubSettingsBuilder> getAllChildren() {
    LinkedList<SubSettingsBuilder> children = new LinkedList<>(Collections.singleton(this));

    SubSettingsBuilder last = this;

    while (last != null) {
      if (last.getChild() != null) {
        children.add(last.getChild());
      }
      last = last.getChild();
    }

    return children;
  }

  /**
   * @return the first parent that was created.
   * Only required if first builder has one ore more children.
   */
  public SubSettingsBuilder build() {
    return parent == null ? this : parent.build();
  }

  /**
   * Sets the highest parent of a child as the child of this builder.
   *
   * @param child one of the child builders that are added.
   * @return the highest parent of that child
   */
  public SubSettingsBuilder addChild(SubSettingsBuilder child) {
    this.child = child.setParent(this);
    return child;
  }

  public ChooseItemSubSettingsBuilder createChooseItemChild(String key) {
    ChooseItemSubSettingsBuilder builder = new ChooseItemSubSettingsBuilder(key, this);
    this.child = builder;
    return builder;
  }

  public ValueSubSettingsBuilder createValueChild() {
    ValueSubSettingsBuilder builder = new ValueSubSettingsBuilder(this);
    this.child = builder;
    return builder;
  }

  public ChooseMultipleItemSubSettingBuilder createChooseMultipleChild(String key) {
    ChooseMultipleItemSubSettingBuilder builder = new ChooseMultipleItemSubSettingBuilder(key, this);
    this.child = builder;
    return builder;
  }

  public TextInputSubSettingsBuilder createTextInputChild(String key, Consumer<Player> onOpen,
                                                          BiPredicate<Player, String> isValid) {
    TextInputSubSettingsBuilder builder = new TextInputSubSettingsBuilder(key,
      this, onOpen, isValid);
    this.child = builder;
    return builder;
  }

  public record SubSettingDisplay(@NotNull LocalizableMessage keyName, @NotNull LocalizableMessage valueFormatted) {
  }

}
