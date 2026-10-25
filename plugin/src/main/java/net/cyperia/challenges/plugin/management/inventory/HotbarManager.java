package net.cyperia.challenges.plugin.management.inventory;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.content.loader.LanguageLoader;
import net.cyperia.challenges.plugin.management.scheduler.policy.ExtraWorldPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.TimerTask;
import net.cyperia.challenges.plugin.management.scheduler.timer.TimerStatus;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.challenges.plugin.spigot.events.PlayerIgnoreStatusChangeEvent;
import net.cyperia.challenges.plugin.spigot.events.PlayerRespawnedEvent;
import net.cyperia.challenges.plugin.spigot.events.SettingToggleEvent;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.FileDocument;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Hands out the hotbar players get while the timer is paused and takes it back once it runs.
 *
 * <p>Items are recognised by {@link HotbarItemTag}, never by name or material. What sits in a slot is
 * decided per player by {@link HotbarLayout#select}, so a missing permission, a disabled item or a
 * setting changes the whole arrangement rather than leaving a hole.
 *
 * <p>An inventory holding anything besides hotbar items belongs to a run: a paused run gets the
 * hotbar only where it does not displace run items, and never at all if it would.
 */
public final class HotbarManager implements Listener {

  private static final String CONFIG_FILE = "hotbar.yml";
  private static final int JOIN_HELD_SLOT = 4;

  private final Map<String, HotbarItem> items = new HashMap<>();
  private final List<Predicate<ItemStack>> lobbyItemFilters = new CopyOnWriteArrayList<>();
  private final Set<String> disabled;
  private final List<HotbarLayout> layouts;

  public HotbarManager() {
    FileDocument config = Challenges.getInstance().getConfig(CONFIG_FILE);
    Document itemConfig = section(config, "items");

    disabled = itemConfig.children().entrySet().stream()
      .filter(entry -> !entry.getValue().getBoolean("enabled", true))
      .map(Map.Entry::getKey)
      .collect(Collectors.toUnmodifiableSet());
    layouts = loadLayouts(config.getDocument("layouts"));
    registerBuiltIns(itemConfig);

    Challenges.getInstance().registerListener(this);
    ChallengeAPI.registerScheduler(this);
    // Names are translated, so the first render has to wait for the language
    ChallengeAPI.subscribeLoader(LanguageLoader.class, this::refreshAll);
  }

  /**
   * Adds an item layouts can reference by its id, or replaces the item with that id, built-ins
   * included.
   */
  public void register(@NotNull HotbarItem item) {
    items.put(item.getId(), item);
    refreshAll();
  }

  /**
   * @return every slot the layouts put this item into, empty if none references it
   */
  @NotNull
  public Set<Integer> getSlots(@NotNull String id) {
    return layouts.stream()
      .flatMap(layout -> layout.slots().entrySet().stream())
      .filter(entry -> entry.getValue().equals(id))
      .map(Map.Entry::getKey)
      .collect(Collectors.toCollection(TreeSet::new));
  }

  /**
   * Declares stacks another plugin hands out while the timer is paused, such as cosmetics. They do
   * not make an inventory belong to a run, and they are taken away when the timer starts.
   */
  public void registerLobbyItems(@NotNull Predicate<ItemStack> filter) {
    lobbyItemFilters.add(filter);
  }

  public boolean isLobbyItem(@Nullable ItemStack stack) {
    if (HotbarItemTag.isTagged(stack)) return true;
    for (Predicate<ItemStack> filter : lobbyItemFilters) {
      if (filter.test(stack)) return true;
    }
    return false;
  }

  /**
   * @return whether the inventory, armor and off hand included, holds nothing but lobby items, i.e.
   * nothing a run could have put there
   */
  public boolean isLobbyInventory(@NotNull Player player) {
    for (ItemStack stack : player.getInventory().getContents()) {
      if (!isEmpty(stack) && !isLobbyItem(stack)) return false;
    }
    return true;
  }

  /**
   * Re-evaluates every player's hotbar. Timer, settings, game mode, vanish, respawns, permissions and
   * language are watched already.
   */
  public void refreshAll() {
    Challenges.getInstance().getPlayerManager().forEachAudience(this::refresh);
  }

  public void refresh(@NotNull Player player) {
    refresh(player, false);
  }

  private void refresh(@NotNull Player player, boolean join) {
    if (!player.isOnline() || player.isDead() || !LanguageLoader.isLoaded()) return;

    if (ChallengeAPI.isStarted()) {
      removeItems(player, this::isLobbyItem);
      return;
    }

    Map<Integer, ItemStack> stacks = hasHotbar(player) ? render(player) : Map.of();
    if (!canPlace(player.getInventory(), stacks)) stacks = Map.of();
    place(player.getInventory(), stacks);

    if (join && !stacks.isEmpty()) player.getInventory().setHeldItemSlot(JOIN_HELD_SLOT);
  }

  /**
   * Whether the player's game mode gets a hotbar at all. Plugins placing hotbar stacks themselves,
   * such as a reserved item's owner, should follow it.
   */
  public boolean hasHotbar(@NotNull Player player) {
    GameMode gameMode = player.getGameMode();
    return gameMode != GameMode.CREATIVE && gameMode != GameMode.SPECTATOR;
  }

  /**
   * @return the stacks of the player's layout by slot, reserved slots excluded
   */
  @NotNull
  private Map<Integer, ItemStack> render(@NotNull Player player) {
    boolean lobbyInventory = isLobbyInventory(player);
    Set<String> available = new HashSet<>();
    for (HotbarLayout layout : layouts) {
      for (String id : layout.slots().values()) {
        HotbarItem item = items.get(id);
        if (item != null && !disabled.contains(id) && (lobbyInventory || !item.isLobbyOnly()) && item.isAvailable(player)) {
          available.add(id);
        }
      }
    }

    HotbarLayout layout = HotbarLayout.select(layouts, available);
    if (layout == null) return Map.of();

    Map<Integer, ItemStack> stacks = new HashMap<>();
    layout.slots().forEach((slot, id) -> {
      if (!available.contains(id)) return;
      ItemStack stack = items.get(id).render(player);
      if (stack != null) stacks.put(slot, HotbarItemTag.tag(stack, id));
    });
    return stacks;
  }

  private boolean canPlace(@NotNull PlayerInventory inventory, @NotNull Map<Integer, ItemStack> stacks) {
    for (int slot : stacks.keySet()) {
      ItemStack current = inventory.getItem(slot);
      if (!isEmpty(current) && !HotbarItemTag.isTagged(current)) return false;
    }
    return true;
  }

  private void place(@NotNull PlayerInventory inventory, @NotNull Map<Integer, ItemStack> stacks) {
    ItemStack[] contents = inventory.getContents();
    for (int slot = 0; slot < contents.length; slot++) {
      if (HotbarItemTag.isTagged(contents[slot]) && !stacks.containsKey(slot)) inventory.setItem(slot, null);
    }
    stacks.forEach((slot, stack) -> {
      if (!stack.isSimilar(inventory.getItem(slot))) inventory.setItem(slot, stack);
    });
  }

  private void removeItems(@NotNull Player player, @NotNull Predicate<ItemStack> filter) {
    PlayerInventory inventory = player.getInventory();
    ItemStack[] contents = inventory.getContents();
    for (int slot = 0; slot < contents.length; slot++) {
      if (!isEmpty(contents[slot]) && filter.test(contents[slot])) inventory.setItem(slot, null);
    }
  }

  private static boolean isEmpty(@Nullable ItemStack stack) {
    return stack == null || stack.getType() == Material.AIR;
  }

  private void registerBuiltIns(@NotNull Document config) {
    for (CommandHotbarItem item : createBuiltIns(config)) {
      items.put(item.getId(), item);
    }
  }

  @NotNull
  private List<CommandHotbarItem> createBuiltIns(@NotNull Document config) {
    Document teams = section(config, "teams");
    // No team setting exists yet, so force is the only way to show it for now
    boolean forceTeams = teams.getBoolean("force", false);
    return List.of(
      new CommandHotbarItem("timer", material(config, "timer", Material.CLOCK), "timer", false,
        player -> player.hasPermission(ChallengesPermissions.COMMAND_TIMER)),
      new CommandHotbarItem("challenges", material(config, "challenges", Material.BOOK), "challenges", false,
        player -> player.hasPermission(ChallengesPermissions.COMMAND_CHALLENGES)),
      new CommandHotbarItem("start", material(config, "start", Material.LIME_DYE), "start", false,
        player -> player.hasPermission(ChallengesPermissions.COMMAND_TIMER_START)),
      new CommandHotbarItem("teams", material(config, "teams", Material.WHITE_BED), "teams", true,
        _ -> forceTeams)
    );
  }

  @NotNull
  private static Material material(@NotNull Document config, @NotNull String id, @NotNull Material def) {
    return section(config, id).getEnum("material", def);
  }

  // Reading through getDocument would create the section in the cached config
  @NotNull
  private static Document section(@NotNull Document config, @NotNull String id) {
    return config.isDocument(id) ? config.getDocument(id) : Document.empty();
  }

  @NotNull
  private List<HotbarLayout> loadLayouts(@NotNull Document config) {
    List<HotbarLayout> layouts = new ArrayList<>();
    config.children().forEach((name, layout) -> {
      Map<Integer, String> slots = new HashMap<>();
      for (String id : layout.keys()) {
        putSlot(name, slots, layout.getInt(id, -1), id);
      }
      layouts.add(new HotbarLayout(slots));
    });
    return layouts;
  }

  private void putSlot(@NotNull String layout, @NotNull Map<Integer, String> slots, int slot, @NotNull String id) {
    if (slot < 0 || slot > 8) {
      Logger.warn("Hotbar layout '{}' puts '{}' into slot {}, which is not a hotbar slot (0-8)", layout, id, slot);
    } else if (slots.containsKey(slot)) {
      Logger.warn("Hotbar layout '{}' puts both '{}' and '{}' into slot {}", layout, slots.get(slot), id, slot);
    } else {
      slots.put(slot, id);
    }
  }

  public void handleDisable() {
    // Nothing can be scheduled once the plugin is disabling. The disabling thread passes every ownership
    // check, so each player is cleaned inline.
    Bukkit.getOnlinePlayers().forEach(player -> Regions.entity(Challenges.getInstance(), player, () -> removeItems(player, HotbarItemTag::isTagged)));
  }

  // The default world policy would skip a timer change made while an extra world is in use
  @TimerTask(status = {TimerStatus.PAUSED, TimerStatus.RUNNING}, async = false, worldPolicy = ExtraWorldPolicy.ALWAYS)
  public void onTimerStatusChange() {
    refreshAll();
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onJoin(@NotNull PlayerJoinEvent event) {
    // Next tick, once join handling elsewhere (game mode, cosmetics) has settled
    Player player = event.getPlayer();
    Regions.nextTick(Challenges.getInstance(), player, () -> refresh(player, true));
  }

  @EventHandler
  public void onQuit(@NotNull PlayerQuitEvent event) {
    removeItems(event.getPlayer(), HotbarItemTag::isTagged);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onGameModeChange(@NotNull PlayerGameModeChangeEvent event) {
    // The new game mode is only applied after the event
    refreshLater(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onRespawn(@NotNull PlayerRespawnedEvent event) {
    refreshLater(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onCommandsUpdate(@NotNull PlayerCommandSendEvent event) {
    // Sent whenever permissions are recalculated
    refreshLater(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onLocaleChange(@NotNull PlayerLocaleChangeEvent event) {
    refreshLater(event.getPlayer());
  }

  // Vanishing changes what other plugins' items allow, e.g. cosmetics
  @EventHandler(priority = EventPriority.MONITOR)
  public void onIgnoreStatusChange(@NotNull PlayerIgnoreStatusChangeEvent event) {
    refreshLater(event.getPlayer());
  }

  @EventHandler
  public void onSettingToggle(@NotNull SettingToggleEvent event) {
    refreshAll();
  }

  private void refreshLater(@NotNull Player player) {
    Regions.nextTick(Challenges.getInstance(), player, () -> refresh(player));
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onInteract(@NotNull PlayerInteractEvent event) {
    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      use(event.getPlayer(), event.getItem(), event);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onInteractEntity(@NotNull PlayerInteractEntityEvent event) {
    use(event.getPlayer(), event.getPlayer().getInventory().getItem(event.getHand()), event);
  }

  private void use(@NotNull Player player, @Nullable ItemStack stack, @NotNull Cancellable event) {
    String id = HotbarItemTag.idOf(stack);
    if (id == null) return;
    event.setCancelled(true);

    HotbarItem item = items.get(id);
    if (item == null || ChallengeAPI.isStarted()) {
      refresh(player); // a stale stack, from an item that is gone or a timer that is running
      return;
    }
    item.onUse(player);
  }

  // Moving, dropping and losing hotbar items on death is prevented by the RestrictionListener, which
  // locks every inventory while the timer is paused

}
