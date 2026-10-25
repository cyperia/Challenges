package net.cyperia.challenges.plugin.challenges.type.abstraction.menu;

import net.cyperia.commons.bukkit.utils.misc.Regions;
import lombok.Getter;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.IModifier;
import net.cyperia.challenges.plugin.challenges.type.ISetting;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.Setting;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.MessageKeySpace;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.format.InteractionTooltips;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.menu.info.ChallengeMenuClickInfo;
import net.cyperia.challenges.plugin.management.menu.search.SearchCategory;
import net.cyperia.challenges.plugin.management.menu.search.SearchFindable;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.item.ItemBuilder;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.version.Version;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class MenuSetting extends Setting implements SearchCategory {

  @Getter
  private final SubSettingsMenuGenerator menuGenerator;
  private final Map<String, SubSetting> settings = new LinkedHashMap<>();

  public MenuSetting() {
    this(false);
  }

  public MenuSetting(boolean enabledByDefault) {
    super(enabledByDefault);
    this.menuGenerator = new SubSettingsMenuGenerator(this);
  }

  @NotNull
  protected final <T extends SubSetting> T registerSetting(@NotNull String key, @NotNull T setting) {
    if (key.equals("enabled")) throw new IllegalArgumentException("SubSetting key 'enabled' is reserved");
    settings.put(key, setting);
    menuGenerator.addToCache(setting);
    Challenges.getInstance().registerListener(setting);
    SearchFindable.register(setting);
    return setting;
  }

  @NotNull
  // A sub setting newer than this setting shows this setting as updated in that version
  protected final <T extends SubSetting> T registerSetting(@NotNull String key, @NotNull String since, @NotNull T setting) {
    ((SubSetting) setting).since = since;
    return registerSetting(key, setting);
  }

  protected final <T extends SubSetting> T registerSetting(@NotNull String key, @NotNull OneEnabledGroup group, @NotNull T setting) {
    group.add(setting);
    return registerSetting(key, setting);
  }

  @Nullable
  public final SubSetting getSetting(@NotNull String name) {
    return settings.get(name);
  }

  @NotNull
  protected MessageKeySpace getSubSettingSpace(@NotNull String settingKey) {
    return getChallengeMessageSpace("sub." + settingKey);
  }

  @Override
  public void handleClick(@NotNull ChallengeMenuClickInfo info) {
    if (info.isUpperItemClick() && info.getType() == MenuClickType.LEFT_SHIFT && isUsesGlobalPunish()) {
      SoundSample.CLICK.play(info.getPlayer());
      ChallengeHelper.openPunishmentMenu(info.getPlayer());
    } else if (isEnabled() && !info.isRightClick() && info.isLowerItemClick()) {
      openMenu(info);
      SoundSample.CLICK.play(info.getPlayer());
    } else {
      super.handleClick(info);
    }
  }

  protected void openMenu(@NotNull ChallengeMenuClickInfo event) {
    menuGenerator.openMenu(event.getPlayer());
  }

  @NotNull
  @Override
  public ItemStack getSettingsItemPreset() {
    return DefaultItems.createCustomizePreset();
  }

  @NotNull
  @Override
  public LocalizableMessage getSettingsName() {
    return MessageKey.of("generic.customize");
  }

  @NotNull
  @Override
  protected InteractionTooltips getDisplayTooltips() {
    return new InteractionTooltips().on(MenuClickType.LEFT, Interaction.toggle(isEnabled()));
  }

  @NotNull
  @Override
  protected InteractionTooltips getSettingsTooltips() {
    // the settings item opens the sub menu once enabled, see #handleClick
    return new InteractionTooltips().on(MenuClickType.LEFT, isEnabled() ? Interaction.OPEN : Interaction.ENABLE);
  }

  @NotNull
  @Override
  public LocalizableMessage getSearchCategoryName() {
    return getName();
  }

  @Override
  public void writeSettings(@NotNull Document document) {
    super.writeSettings(document);
    for (Map.Entry<String, SubSetting> entry : settings.entrySet()) {
      Document subDocument = document.getDocument(entry.getKey());
      entry.getValue().writeSettings(subDocument);
    }
  }

  @Override
  public void loadSettings(@NotNull Document document) {
    super.loadSettings(document);
    for (Map.Entry<String, SubSetting> entry : settings.entrySet()) {
      if (!document.contains(entry.getKey())) continue;
      Document subDocument = document.getDocument(entry.getKey());
      entry.getValue().loadSettings(subDocument);
    }
  }

  @Override
  public void restoreDefaults() {
    super.restoreDefaults();
    settings.values().forEach(SubSetting::restoreDefaults);
  }

  @Override
  public boolean isDefaultSettings() {
    return super.isDefaultSettings() && settings.values().stream().allMatch(SubSetting::isDefaultSettings);
  }

  @Nullable
  public final String getNewestSubSettingSince() {
    String newest = null;
    for (SubSetting setting : settings.values()) {
      if (setting.since != null && (newest == null || Version.parse(setting.since).isNewerThan(Version.parse(newest)))) newest = setting.since;
    }
    return newest;
  }

  // TODO extract duplicate logic with AbstractChallenge
  public abstract class SubSetting implements Listener, ISetting, SearchFindable {

    private final MessageKeySpace nameMessageKeySpace;

    @Getter
    protected final ItemStack displayItemPreset;

    private final boolean enabledByDefault;
    private OneEnabledGroup group;
    @Getter
    @Nullable
    private String since;

    /** The own state, {@link #isEnabled()} respects the setting this belongs to as well. */
    protected boolean enabled;

    public SubSetting(@NotNull ItemStack displayItemPreset, @NotNull MessageKeySpace nameMessageKeySpace) {
      this(displayItemPreset, nameMessageKeySpace, false);
    }

    public SubSetting(@NotNull ItemStack displayItemPreset, @NotNull MessageKeySpace nameMessageKeySpace, boolean enabledByDefault) {
      this.displayItemPreset = displayItemPreset;
      this.nameMessageKeySpace = nameMessageKeySpace;
      this.enabledByDefault = enabledByDefault;
      this.enabled = enabledByDefault; // assigned directly, no hook may run before the implementation is constructed
    }

    public final void updateItems() {
      menuGenerator.updateElementDisplay(this);
    }

    @NotNull
    public ItemBuilder getDisplayItem(@NotNull Locale locale) {
      return new ItemFormatter(displayItemPreset)
        .name(getName())
        .description(getDescription())
        .tooltips(getDisplayTooltips())
        .suffixIfNewUpdated(this)
        .builder(locale);
    }

    @NotNull
    public ItemBuilder getSettingsItem(@NotNull Locale locale) {
      return new ItemFormatter(isEnabled() ? getSettingsItemPreset() : DefaultItems.createDisabledPreset())
        .name(isEnabled() ? getSettingsName() : ChallengeHelper.getChallengeDisabledName())
        .description(getSettingsDescription())
        .tooltips(getSettingsTooltips())
        .builder(locale);
    }

    /**
     * @implNote Only used if {@link #isEnabled()}.
     * Name/Lore will be overwritten by formatting.
     * Set name in {@link #getSettingsName()} and lore in {@link #getSettingsDescription()}
     */
    @NotNull
    public abstract ItemStack getSettingsItemPreset();

    @NotNull
    protected abstract InteractionTooltips getSettingsTooltips();

    @NotNull
    protected InteractionTooltips getDisplayTooltips() {
      return getSettingsTooltips();
    }

    @NotNull
    @Override
    public LocalizableMessage getName() {
      return nameMessageKeySpace.getChildKey("name");
    }

    @NotNull
    @Override
    public LocalizableMessage getDescription() {
      return nameMessageKeySpace.getChildKey("desc");
    }

    @NotNull
    public LocalizableMessage getSettingsName() {
      return ChallengeHelper.getChallengeEnabledName();
    }

    @Nullable
    public LocalizableMessage getSettingsDescription() {
      return null;
    }

    @NotNull
    @Override
    public SearchCategory getCategory() {
      return SearchCategory.specify(SearchCategory.SUB_SETTING, MenuSetting.this);
    }

    @Override
    public void showFindable(@NotNull Player player) {
      if (MenuSetting.this.isEnabled()) {
        menuGenerator.showFinable(player, this);
      } else {
        MenuSetting.this.showFindable(player);
      }
    }

    /**
     * Hidden along with the setting it belongs to: a sub setting is registered as a findable of its
     * own, but neither its menu nor the fallback of {@link #showFindable(Player)} is reachable once
     * the setting itself is gone from the menus.
     */
    @Override
    public boolean isVisible() {
      return MenuSetting.this.isVisible();
    }

    @Override
    public boolean isVisibleTo(@NotNull Player viewer) {
      return MenuSetting.this.isVisibleTo(viewer);
    }

    @Override
    public boolean isEnabled() {
      return MenuSetting.this.isEnabled() && enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
      if (this.enabled == enabled) return;
      if (group != null) {
        group.select(enabled ? this : null);
        return;
      }

      applyEnabled(enabled);
    }

    private void applyEnabled(boolean enabled) {
      this.enabled = enabled;

      Regions.global(Challenges.getInstance(), () -> {
        if (enabled) this.onEnable();
        else this.onDisable();
      });

      this.updateItems();
    }

    /**
     * @return whether a click may change the state, a group can require that one setting stays enabled
     */
    private boolean canToggle() {
      return group == null || !enabled || !group.requiresOneEnabled;
    }

    protected final void handleToggleClick(@NotNull ChallengeMenuClickInfo info) {
      if (!canToggle()) {
        SoundSample.BASS_OFF.play(info.getPlayer());
        return;
      }

      this.setEnabled(!enabled);
      SoundSample.playStatusSound(info.getPlayer(), enabled);
      this.playStatusUpdateTitle();
    }

    @Override
    public void playStatusUpdateTitle() {
      ChallengeHelper.playSubSettingToggleTitle(MenuSetting.this, this);
    }

    @NotNull
    protected final InteractionTooltips createToggleTooltips() {
      return new InteractionTooltips().onIf(canToggle(), MenuClickType.LEFT, Interaction.toggle(enabled));
    }

    public int getAsInt() {
      return enabled ? 1 : 0;
    }

    public boolean getAsBoolean() {
      return enabled;
    }

    public void restoreDefaults() {
      restoreEnabledDefault();
    }

    public boolean isDefaultSettings() {
      return isEnabledDefault();
    }

    protected final void restoreEnabledDefault() {
      if (group != null) group.restoreDefaults();
      else setEnabled(enabledByDefault);
    }

    protected final boolean isEnabledDefault() {
      return enabled == enabledByDefault;
    }

    public void loadSettings(@NotNull Document document) {
      setEnabled(document.getBoolean("enabled", enabled));
    }

    public void writeSettings(@NotNull Document document) {
      document.set("enabled", enabled);
    }

    public abstract void handleClick(@NotNull ChallengeMenuClickInfo info);

    protected void onEnable() {
    }

    protected void onDisable() {
    }

  }

  public class BooleanSubSetting extends SubSetting {

    public BooleanSubSetting(@NotNull ItemStack displayItemPreset, @NotNull MessageKeySpace nameMessageKeySpace) {
      this(displayItemPreset, nameMessageKeySpace, false);
    }

    public BooleanSubSetting(@NotNull ItemStack displayItemPreset, @NotNull MessageKeySpace nameMessageKeySpace, boolean enabledByDefault) {
      super(displayItemPreset, nameMessageKeySpace, enabledByDefault);
    }

    @NotNull
    @Override
    public ItemStack getSettingsItemPreset() {
      return DefaultItems.createEnabledPreset();
    }

    @NotNull
    @Override
    protected InteractionTooltips getSettingsTooltips() {
      return createToggleTooltips();
    }

    @Override
    public final void handleClick(@NotNull ChallengeMenuClickInfo info) {
      handleToggleClick(info);
    }

  }

  public class NumberSubSetting extends SubSetting implements IModifier {

    private final int max, min;
    private final int defaultValue;
    @Getter
    private int value;
    private ValueUnit valueUnit = ValueUnit.VALUE;

    public NumberSubSetting(@NotNull ItemStack displayItemPreset, @NotNull MessageKeySpace nameMessageKeySpace) {
      this(displayItemPreset, nameMessageKeySpace, 64);
    }

    public NumberSubSetting(@NotNull ItemStack displayItemPreset, @NotNull MessageKeySpace nameMessageKeySpace, int max) {
      this(displayItemPreset, nameMessageKeySpace, 1, max);
    }

    public NumberSubSetting(@NotNull ItemStack displayItemPreset, @NotNull MessageKeySpace nameMessageKeySpace,
                            int min, int max) {
      this(displayItemPreset, nameMessageKeySpace, min, max, min);
    }

    public NumberSubSetting(@NotNull ItemStack displayItemPreset, @NotNull MessageKeySpace nameMessageKeySpace,
                            int min, int max, int defaultValue) {
      this(displayItemPreset, nameMessageKeySpace, min, max, defaultValue, false);
    }

    protected NumberSubSetting(@NotNull ItemStack displayItemPreset, @NotNull MessageKeySpace nameMessageKeySpace,
                               int min, int max, int defaultValue, boolean enabledByDefault) {
      super(displayItemPreset, nameMessageKeySpace, enabledByDefault);
      if (max <= min) throw new IllegalArgumentException("max <= min");
      if (min < 0) throw new IllegalArgumentException("min < 0");
      if (defaultValue > max) throw new IllegalArgumentException("defaultValue > max");
      if (defaultValue < min) throw new IllegalArgumentException("defaultValue < min");
      this.value = defaultValue;
      this.defaultValue = defaultValue;
      this.max = max;
      this.min = min;
    }

    /**
     * @implSpec Call while registering the setting; the unit is read whenever the item is rendered.
     */
    @NotNull
    public NumberSubSetting unit(@NotNull ValueUnit valueUnit) {
      this.valueUnit = valueUnit;
      return this;
    }

    @NotNull
    @Override
    public ValueUnit getValueUnit() {
      return valueUnit;
    }

    @NotNull
    @Override
    public ItemStack getSettingsItemPreset() {
      return DefaultItems.createValuePreset(value);
    }

    @NotNull
    @Override
    public LocalizableMessage getSettingsName() {
      return valueUnit.name(value);
    }

    @NotNull
    @Override
    protected InteractionTooltips getSettingsTooltips() {
      return ChallengeHelper.createModifierTooltips(this);
    }

    @Override
    public void restoreDefaults() {
      this.setValue(defaultValue);
    }

    @Override
    public boolean isDefaultSettings() {
      return value == defaultValue;
    }

    public void setValue(int value) {
      if (this.value == value) return;
      this.value = value;

      updateItems();
      Regions.global(Challenges.getInstance(), this::onValueChange);
    }

    @Override
    public int getAsInt() {
      return value;
    }

    @Override
    public boolean getAsBoolean() {
      return value > 0;
    }

    /** A value of 0 is a valid setting, only the setting this belongs to can disable it. */
    @Override
    public boolean isEnabled() {
      return MenuSetting.this.isEnabled();
    }

    @Override
    public void handleClick(@NotNull ChallengeMenuClickInfo info) {
      ChallengeHelper.handleModifierClick(info, this);
    }

    @Override
    public void loadSettings(@NotNull Document document) {
      this.setValue(document.getInt("value", value));
    }

    @Override
    public void writeSettings(@NotNull Document document) {
      document.set("value", value);
    }

    protected void onValueChange() {
    }

    @Override
    public final int getMinValue() {
      return min;
    }

    @Override
    public final int getMaxValue() {
      return max;
    }

    @Override
    public void playValueChangeTitle() {
      ChallengeHelper.playSubSettingValueTitle(MenuSetting.this, this);
    }
  }

  public class NumberAndBooleanSubSetting extends NumberSubSetting {

    public NumberAndBooleanSubSetting(@NotNull ItemStack displayItemPreset, @NotNull MessageKeySpace nameMessageKeySpace) {
      super(displayItemPreset, nameMessageKeySpace);
    }

    public NumberAndBooleanSubSetting(@NotNull ItemStack displayItemPreset, @NotNull MessageKeySpace nameMessageKeySpace, int max) {
      super(displayItemPreset, nameMessageKeySpace, max);
    }

    public NumberAndBooleanSubSetting(@NotNull ItemStack displayItemPreset, @NotNull MessageKeySpace nameMessageKeySpace, int min, int max) {
      super(displayItemPreset, nameMessageKeySpace, min, max);
    }

    public NumberAndBooleanSubSetting(@NotNull ItemStack displayItemPreset, @NotNull MessageKeySpace nameMessageKeySpace, int min, int max, int defaultValue) {
      super(displayItemPreset, nameMessageKeySpace, min, max, defaultValue);
    }

    public NumberAndBooleanSubSetting(@NotNull ItemStack displayItemPreset, @NotNull MessageKeySpace nameMessageKeySpace, int min, int max, int defaultValue, boolean enabledByDefault) {
      super(displayItemPreset, nameMessageKeySpace, min, max, defaultValue, enabledByDefault);
    }

    @NotNull
    @Override
    public ItemStack getSettingsItemPreset() {
      return DefaultItems.createEnabledValuePreset(getValue());
    }

    @NotNull
    @Override
    protected InteractionTooltips getDisplayTooltips() {
      return createToggleTooltips();
    }

    @NotNull
    @Override
    protected InteractionTooltips getSettingsTooltips() {
      // while disabled the settings item toggles instead of modifying, see #handleClick
      return enabled ? super.getSettingsTooltips() : getDisplayTooltips();
    }

    @Override
    public boolean getAsBoolean() {
      return enabled;
    }

    @Override
    public boolean isEnabled() {
      return MenuSetting.this.isEnabled() && enabled;
    }

    @Override
    public void restoreDefaults() {
      super.restoreDefaults();
      restoreEnabledDefault();
    }

    @Override
    public boolean isDefaultSettings() {
      return super.isDefaultSettings() && isEnabledDefault();
    }

    @Override
    public void handleClick(@NotNull ChallengeMenuClickInfo info) {
      if (info.isUpperItemClick() || !enabled) {
        handleToggleClick(info);
      } else {
        super.handleClick(info);
      }
    }

    @Override
    public void loadSettings(@NotNull Document document) {
      super.loadSettings(document);
      this.setEnabled(document.getBoolean("enabled", enabled));
    }

    @Override
    public void writeSettings(@NotNull Document document) {
      super.writeSettings(document);
      document.set("enabled", enabled);
    }

  }

  /**
   * Ties sub settings together so that only one of them is enabled: enabling one switches the previously
   * enabled one off. With {@link #requireOneEnabled()} the enabled one can only be replaced, never switched
   * off, which also removes it from the tooltips and rejects the click.
   *
   * @implSpec The setting the group starts with is the one registered as enabled by default,
   * see {@link MenuSetting#registerSetting(String, OneEnabledGroup, SubSetting)}
   */
  public static final class OneEnabledGroup {

    private SubSetting enabled, defaultEnabled;
    private boolean requiresOneEnabled;

    @NotNull
    public OneEnabledGroup requireOneEnabled() {
      this.requiresOneEnabled = true;
      return this;
    }

    void add(@NotNull SubSetting setting) {
      if (setting.group != null) throw new IllegalArgumentException("SubSetting is already part of a group");
      setting.group = this;

      if (!setting.enabled) return;
      if (defaultEnabled != null)
        throw new IllegalArgumentException("Only one SubSetting of a group can be enabled by default");
      this.enabled = this.defaultEnabled = setting;
    }

    void select(@Nullable SubSetting setting) {
      if (enabled == setting) return;
      if (setting == null && requiresOneEnabled) return; // nothing to switch to, so the current one stays

      SubSetting previous = enabled;
      this.enabled = setting;

      if (previous != null) previous.applyEnabled(false); // switched off first, so never two are enabled at once
      if (setting != null) setting.applyEnabled(true);
    }

    void restoreDefaults() {
      select(defaultEnabled);
    }

  }

}
