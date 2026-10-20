package net.cyperia.challenges.plugin.challenges.custom.settings.sub.builder;

import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SubSettingsBuilder;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.generator.AbstractMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.IParentCustomGenerator;
import org.bukkit.entity.Player;

public abstract class GeneratorSubSettingsBuilder extends SubSettingsBuilder {

  public GeneratorSubSettingsBuilder(String key) {
    super(key);
  }

  public GeneratorSubSettingsBuilder(String key, SubSettingsBuilder parent) {
    super(key, parent);
  }

  public boolean open(Player player, IParentCustomGenerator parentGenerator, LocalizableMessage title) {

    if (hasSettings()) {
      LocalizableMessage subTitle = MessageKey.of("menu.title-name-format-sub").withArgs(title, getKeyTranslation(this.getKey()));
      AbstractMenuGenerator generator = getGenerator(player, parentGenerator, subTitle);
      if (generator == null) return false;
      generator.openMenu(player);
      return true;
    }

    return false;
  }

  public abstract AbstractMenuGenerator getGenerator(Player player, IParentCustomGenerator parentGenerator, LocalizableMessage title);

}
