package net.cyperia.challenges.plugin.management.menu.generator.impl.custom.choose;

import net.cyperia.challenges.plugin.challenges.custom.settings.sub.ValueSetting;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.IParentCustomGenerator;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SubSettingValueMenuGenerator extends CustomChooseValueMenuGenerator {

  private final IParentCustomGenerator parent;

  public SubSettingValueMenuGenerator(@NotNull IParentCustomGenerator parent,
                                      @NotNull Map<ValueSetting, String> settings, @NotNull LocalizableMessage title) {
    super(title, settings);
    this.parent = parent;
  }

  @Override
  public void onSaveItemClick(@NotNull Player player) {
    Map<String, String[]> map = new HashMap<>();
    for (Entry<ValueSetting, String> entry : getSettings().entrySet()) {
      map.put(entry.getKey().getKey(), new String[]{entry.getValue()});
    }
    parent.accept(player, null, map);
  }

  @Override
  protected void handleNavigateOutOfMenu(@NotNull Player player) {
    parent.decline(player);
  }

}
