package net.cyperia.challenges.plugin.challenges.custom.settings.sub.builder;

import lombok.Getter;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SelectableKey;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SubSettingsBuilder;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.management.menu.generator.AbstractMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.IParentCustomGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.choose.SubSettingChooseMultipleMenuGenerator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

@Getter
public class ChooseMultipleItemSubSettingBuilder extends GeneratorSubSettingsBuilder {

  protected final LinkedHashMap<String, SelectableKey.Option> settings = new LinkedHashMap<>();

  public ChooseMultipleItemSubSettingBuilder(String key) {
    super(key);
  }

  public ChooseMultipleItemSubSettingBuilder(String key, SubSettingsBuilder parent) {
    super(key, parent);
  }

  @Override
  public AbstractMenuGenerator getGenerator(Player player, IParentCustomGenerator parentGenerator, LocalizableMessage title) {
    return new SubSettingChooseMultipleMenuGenerator(getKey(), parentGenerator, getSettings(), title);
  }

  @NotNull
  @Override
  public Collection<SubSettingDisplay> getCurrentDisplayFor(@NotNull Map<String, String[]> activated) {
    String[] values = activated.get(getKey());
    if (values == null) return Collections.emptyList();

    List<Object> valueNameArgs = new ArrayList<>(values.length);
    for (String value : values) {
      SelectableKey.Option option = settings.get(value);
      if (option != null) valueNameArgs.add(option.getLocalizableName());
    }

    SubSettingDisplay display = new SubSettingDisplay(getKeyTranslation(this.getKey()), LocalizableMessage.joinList(2, valueNameArgs));
    return List.of(display);
  }

  public ChooseMultipleItemSubSettingBuilder addSetting(@NotNull SelectableKey.Option value) {
    settings.put(value.getKey(), value);
    return this;
  }

  public ChooseMultipleItemSubSettingBuilder fill(Consumer<ChooseMultipleItemSubSettingBuilder> actions) {
    actions.accept(this);
    return this;
  }

  public boolean hasSettings() {
    return !settings.isEmpty();
  }

}
