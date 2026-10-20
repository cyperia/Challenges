package net.cyperia.challenges.plugin.management.menu.generator.impl.custom.choose;

import lombok.Getter;
import net.cyperia.challenges.plugin.challenges.custom.settings.IChallengeSetting;
import net.cyperia.challenges.plugin.challenges.custom.settings.SettingType;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SelectableKey;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SubSettingsBuilder;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.generator.impl.custom.IParentCustomGenerator;
import net.cyperia.challenges.plugin.utils.misc.MapUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Lets the player choose a trigger or action, then walks its {@link SubSettingsBuilder} chain
 * collecting the sub-setting data before handing it back to the parent generator.
 * Replaces the legacy {@code custom.CustomMainSettingsMenuGenerator}.
 */
public class CustomMainSettingsMenuGenerator extends CustomChooseOptionMenuGenerator implements IParentCustomGenerator {

  @Getter
  private final IParentCustomGenerator parent;
  private final SettingType type;
  private final LocalizableMessage baseTitle;
  private final String key;
  private final Function<String, IChallengeSetting> instanceGetter;
  private IChallengeSetting setting;
  private SubSettingsBuilder subSettingsBuilder;
  private Map<String, String[]> subSettings;

  public CustomMainSettingsMenuGenerator(@NotNull IParentCustomGenerator parent, @NotNull SettingType type,
                                         @NotNull String key, @NotNull LocalizableMessage baseTitle,
                                         @NotNull Map<String, ? extends SelectableKey> items,
                                         @NotNull Function<String, IChallengeSetting> instanceGetter) {
    super(baseTitle, items);
    this.parent = parent;
    this.type = type;
    this.baseTitle = baseTitle;
    this.key = key;
    this.instanceGetter = instanceGetter;
    this.subSettings = new HashMap<>();
  }

  @NotNull
  @Override
  public LocalizableMessage getMenuName() {
    return createSubMenuTitle(MessageKey.of("menu.custom.menu-short"), baseTitle);
  }

  @Override
  public void onItemClick(@NotNull Player player, @NotNull String itemKey) {
    this.setting = instanceGetter.apply(itemKey);
    this.subSettingsBuilder = setting.getSubSettingsBuilder();

    subSettings.put(key, new String[]{setting.getUniqueName()});

    if (!openSubSettingsMenu(player)) {
      parent.accept(player, type, subSettings);
    }
  }

  @Override
  public void accept(@NotNull Player player, @Nullable SettingType type, @NotNull Map<String, String[]> data) {
    subSettings.putAll(data);

    if (!openSubSettingsMenu(player)) {
      parent.accept(player, this.type, subSettings);
    }
  }

  private boolean openSubSettingsMenu(@NotNull Player player) {
    if (subSettingsBuilder != null && subSettingsBuilder.hasSettings()) {
      subSettingsBuilder.open(player, this, baseTitle);
      subSettingsBuilder = subSettingsBuilder.getChild();
      return true;
    }
    return false;
  }

  @Override
  protected void handleNavigateOutOfMenu(@NotNull Player player) {
    parent.decline(player);
  }

  @Override
  public void decline(@NotNull Player player) {
    if (setting != null) {
      this.subSettings = MapUtils.createStringArrayMap(key, setting.getUniqueName());
    }
    openMenu(player);
  }

}
