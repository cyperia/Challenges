package net.cyperia.challenges.plugin.management.menu.generator.impl.custom.choose;

import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SelectableKey;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.IParentCustomGenerator;
import net.cyperia.challenges.plugin.utils.misc.MapUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SubSettingChooseMultipleMenuGenerator extends CustomChooseMultipleOptionsMenuGenerator {

  private final IParentCustomGenerator parent;
  private final String key;

  public SubSettingChooseMultipleMenuGenerator(@NotNull String key, @NotNull IParentCustomGenerator parent,
                                               @NotNull Map<String, ? extends SelectableKey> items, @NotNull LocalizableMessage title) {
    super(title, items);
    this.key = key;
    this.parent = parent;
  }

  @Override
  public void onSaveClick(@NotNull Player player, @NotNull String[] keys) {
    parent.accept(player, null, MapUtils.createStringArrayMap(key, keys));
  }

  @Override
  protected void handleNavigateOutOfMenu(@NotNull Player player) {
    parent.decline(player);
  }

}
