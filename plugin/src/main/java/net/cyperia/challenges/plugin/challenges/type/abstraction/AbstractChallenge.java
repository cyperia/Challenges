package net.cyperia.challenges.plugin.challenges.type.abstraction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.cyperia.challenges.catalog.Challenge;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.MessageKeySpace;
import net.cyperia.challenges.plugin.management.challenges.CatalogIcons;
import net.cyperia.challenges.plugin.management.menu.SettingCategory;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.format.InteractionTooltips;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.server.scoreboard.ChallengeActionBar;
import net.cyperia.challenges.plugin.management.server.scoreboard.ChallengeBossBar;
import net.cyperia.challenges.plugin.management.server.scoreboard.ChallengeScoreboard;
import net.cyperia.challenges.plugin.utils.item.DefaultItems;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import net.cyperia.commons.common.collection.IRandom;
import net.cyperia.commons.common.config.Document;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractChallenge implements IChallenge, Listener {

  protected static final Challenges plugin = Challenges.getInstance();
  protected static final IRandom globalRandom = IRandom.create();

  private static final Map<Class<? extends AbstractChallenge>, AbstractChallenge> firstInstanceByClass = new HashMap<>();

  @Getter
  protected final ChallengeBossBar bossbar = new ChallengeBossBar();
  @Getter
  protected final ChallengeScoreboard scoreboard = new ChallengeScoreboard();
  @Getter
  protected final ChallengeActionBar actionbar = new ChallengeActionBar();

  private Challenge catalogEntry;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private boolean usesGlobalPunish;

  public AbstractChallenge() {
    this(null);
  }

  // A class registered more than once passes each instance's entry; null looks the entry up by class
  protected AbstractChallenge(@Nullable Challenge catalogEntry) {
    this.catalogEntry = catalogEntry;
    firstInstanceByClass.put(this.getClass(), this);
  }

  @NotNull
  public static <C extends AbstractChallenge> C getFirstInstance(@NotNull Class<C> classOfChallenge) {
    return Objects.requireNonNull(classOfChallenge.cast(firstInstanceByClass.get(classOfChallenge)),
      "Requested Challenge is not registered/initialized");
  }

  @NotNull
  public static <C extends AbstractChallenge> Optional<C> getFirstInstanceOptional(@NotNull Class<C> classOfChallenge) {
    return Optional.ofNullable(classOfChallenge.cast(firstInstanceByClass.get(classOfChallenge)));
  }

  /**
   * Every online player, ignored ones included. For presentational effects and for the cleanup that
   * undoes them — a player who became a spectator while an effect was applied still has to lose it.
   */
  public static void broadcastAudience(@NotNull Consumer<? super Player> action) {
    plugin.getPlayerManager().forEachAudience(action);
  }

  /**
   * Only the players a challenge may act on. The default for anything a challenge does <i>to</i> a
   * player: damage, effects, teleports, inventory changes.
   */
  public static void broadcastFiltered(@NotNull Consumer<? super Player> action) {
    plugin.getPlayerManager().forEachChallengePlayer(action);
  }

  public static void broadcastIgnored(@NotNull Consumer<? super Player> action) {
    plugin.getPlayerManager().forEachIgnoredPlayer(action);
  }

  @CheckReturnValue
  public static boolean ignorePlayer(@NotNull Player player) {
    return plugin.getPlayerManager().isIgnored(player);
  }

  @NotNull
  public Challenge getCatalogEntry() {
    if (catalogEntry == null) {
      catalogEntry = plugin.getCatalog().challengeOf(getClass().getName())
        .orElseThrow(() -> new IllegalStateException(getClass().getName() + " has no entry in the challenge catalog"));
    }
    return catalogEntry;
  }

  @NotNull
  @Override
  public SettingCategory getCategory() {
    return SettingCategory.of(getCatalogEntry().category());
  }

  @NotNull
  public ItemStack getDisplayItemPreset() {
    return CatalogIcons.item(getCatalogEntry().icon());
  }

  @NotNull
  public String getNameMessageKey() {
    return getCatalogEntry().key();
  }

  protected final void updateItems() {
    ChallengeHelper.updateItems(this);
  }

  @NotNull
  @Override
  public ItemFormatter getDisplayItem() {
    return new ItemFormatter()
      .item(getDisplayItemPreset())
      .name(getName())
      .description(getDescription())
      .tooltips(applyPunishTooltip(getDisplayTooltips()))
      .suffixIfNewUpdated(this);
  }

  /**
   * The clicks available on the item in the challenge list, where a challenge is only turned on and off.
   */
  @Nullable
  protected InteractionTooltips getDisplayTooltips() {
    return applyPunishTooltip(getSettingsTooltips());
  }

  protected InteractionTooltips applyPunishTooltip(@Nullable InteractionTooltips tooltips) {
    if (tooltips == null) return null;
    if (tooltips.has(MenuClickType.LEFT_SHIFT)) return tooltips;
    return tooltips.onIf(usesGlobalPunish, MenuClickType.LEFT_SHIFT, Interaction.PUNISHMENT);
  }

  /**
   * @implSpec If overridden, use {@link MessageKey#withArgs(Object...)} for placeholders
   */
  @NotNull
  @Override
  public LocalizableMessage getName() {
    return getChallengeMessageKey("name");
  }

  /**
   * @implSpec If overridden, use {@link MessageKey#withArgs(Object...)} for placeholders
   */
  @NotNull
  @Override
  public LocalizableMessage getDescription() {
    return getChallengeMessageKey("desc");
  }

  @NotNull
  @Contract(pure = true)
  protected MessageKey getChallengeMessageKey(@NotNull String keySuffix) {
    return MessageKey.of("challenge", getNameMessageKey(), keySuffix);
  }

  @NotNull
  @Contract(pure = true)
  protected MessageKeySpace getChallengeMessageSpace(@NotNull String keySuffix) {
    return MessageKeySpace.of("challenge", getNameMessageKey(), keySuffix);
  }

  @NotNull
  @Override
  public ItemFormatter getSettingsItem() {
    return new ItemFormatter()
      .item(isEnabled() ? getSettingsItemPreset() : getDisabledSettingsItemPreset())
      .name(isEnabled() ? getSettingsName() : ChallengeHelper.getChallengeDisabledName())
      .description(getSettingsDescription())
      .tooltips(getSettingsTooltips());
  }

  /**
   * The clicks available on the item in the settings row, where a challenge is also configured.
   */
  @Nullable
  protected abstract InteractionTooltips getSettingsTooltips();

  @NotNull
  protected ItemStack getDisabledSettingsItemPreset() {
    return DefaultItems.createDisabledPreset();
  }

  /**
   * @implNote Only used if {@link #isEnabled()}.
   * Name/Lore will be overwritten by formatting.
   * Set name in {@link #getSettingsName()} and lore in {@link #getSettingsDescription()}
   */
  @NotNull
  public ItemStack getSettingsItemPreset() {
    return DefaultItems.createEnabledPreset();
  }

  /**
   * @implNote Only used if {@link #isEnabled()}, format will be applied dynamically
   * @implSpec Wrap plain {@link net.kyori.adventure.text.Component}s with
   * {@link LocalizableMessage#wrap(Object)} to keep dynamic (e.g. client side translatable) styling
   */
  @NotNull
  public LocalizableMessage getSettingsName() {
    return ChallengeHelper.getChallengeEnabledName();
  }

  /**
   * @implNote Shown while disabled as well, so that the value a challenge would run with stays visible
   */
  @Nullable
  public LocalizableMessage getSettingsDescription() {
    return null;
  }

  @Override
  public void showFindable(@NotNull Player player) {
    getMenu().getMenuGenerator().showFinable(player, this);
  }

  /**
   * @implSpec override {@link #getUniqueName()} instead
   */
  @NotNull
  @Override
  public final String getUniqueGamestateName() {
    return getUniqueName();
  }

  @NotNull
  @Override
  public String getUniqueName() {
    return getCatalogEntry().id();
  }

  @Override
  public void handleShutdown() {
  }

  @Override
  public void writeGameState(@NotNull Document document) {
  }

  @Override
  public void loadGameState(@NotNull Document document) {
  }

  @Override
  public void writeSettings(@NotNull Document document) {
  }

  @Override
  public void loadSettings(@NotNull Document document) {
  }

  protected boolean shouldExecuteEffect() {
    return isEnabled() && ChallengeAPI.isStarted() && !ChallengeAPI.isWorldInUse();
  }

  protected boolean shouldExecuteEffect(@NotNull Player player) {
    return shouldExecuteEffect() && !ignorePlayer(player);
  }

  @NotNull
  protected final Document getGameStateData() {
    return plugin.getConfigManager().getGamestateConfig().getDocument(this.getUniqueGamestateName());
  }

  @NotNull
  protected final Document getPlayerData(@NotNull UUID player) {
    return getGameStateData().getDocument("player").getDocument(player.toString());
  }

  @NotNull
  protected final Document getPlayerData(@NotNull Player player) {
    return getPlayerData(player.getUniqueId());
  }

}
