package net.cyperia.challenges.plugin.management.server;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.challenges.entities.GamestateSaveable;
import net.cyperia.challenges.plugin.management.scheduler.policy.TimerPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.management.server.scoreboard.ChallengeBossBar;
import net.cyperia.challenges.plugin.spigot.events.PlayerRespawnedEvent;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import net.cyperia.commons.common.config.Document;
import net.cyperia.commons.common.config.document.GsonDocument;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The one world border every challenge shares.
 * <p>
 * Challenges never touch {@link World#getWorldBorder()}: a challenge whose border starts small and
 * grows with the game state sets how far it has {@link #setGrowth(AbstractChallenge, double, boolean)
 * grown} it, one that only makes it bigger or smaller {@link #changeSize(double) changes} it, and all of
 * them add up. Going through the world border instead would mean the last writer wins, so enabling a
 * second border challenge would silently turn the first one into a no-op.
 * <p>
 * What the players see is a border of this manager's own, sent per player, so the world keeps the
 * border it was created with and a reset needs nothing but forgetting the numbers here.
 */
public final class BorderManager implements GamestateSaveable, Listener {

  private static final double MIN_SIZE = 1;
  private static final double START_SIZE = 1;

  private final Map<World, WorldBorder> apiBorders = new ConcurrentHashMap<>();
  private final Map<World, Location> centers = new ConcurrentHashMap<>();

  // Players a challenge has given a border of their own. The shared border leaves them alone until it
  // is handed back, so a challenge that traps somebody is not undone by one that shrinks the border.
  private final Map<UUID, WorldBorder> overrides = new ConcurrentHashMap<>();

  // How far each challenge whose border starts small has grown it, by unique challenge name. Not saved:
  // each of them restores its own on load, and a reset loads this manager after the challenges.
  private final Map<String, Double> growth = new ConcurrentHashMap<>();
  private volatile double offset;

  // One bar for every challenge that drives the border, so two of them never stack two bars
  private final ChallengeBossBar bossbar = new ChallengeBossBar();

  public void enable() {
    bossbar.setContent((bar, player) -> bar.setTitle(MessageKey.of("bossbar.border-size"), (int) getSize(player.getWorld())));
    Challenges.getInstance().getChallengeManager().registerGameStateSaver(this);
    Challenges.getInstance().getScheduler().register(this);
    Challenges.getInstance().registerListener(this);
  }

  /**
   * Whether any challenge is currently driving the border. While nothing is, players keep the border
   * their world came with.
   */
  public boolean isActive() {
    return !growth.isEmpty() || offset != 0;
  }

  /**
   * Shrinks the border down to a single block while any challenge has set a growth, and grows it by the
   * sum of all of them. Passing the same growth twice is free.
   */
  public void setGrowth(@NotNull AbstractChallenge challenge, double blocks, boolean animate) {
    Double previous = growth.put(challenge.getUniqueName(), blocks);
    if (previous != null && previous == blocks) return;
    update(animate);
  }

  public void clearGrowth(@NotNull AbstractChallenge challenge) {
    if (growth.remove(challenge.getUniqueName()) == null) return;
    update(false);
  }

  /**
   * Grows or shrinks the border by {@code delta}, on top of whatever size it has otherwise.
   * The border slides to the new size rather than jumping to it, the way it does when the level border
   * follows a level.
   */
  public void changeSize(double delta) {
    offset += delta;
    update(true);
  }

  /**
   * The size players are actually held to in this world.
   */
  public double getSize(@NotNull World world) {
    double base = growth.isEmpty()
      ? world.getWorldBorder().getSize()
      : START_SIZE + growth.values().stream().mapToDouble(Double::doubleValue).sum();
    return Math.max(MIN_SIZE, base + offset);
  }

  @NotNull
  public Location getCenter(@NotNull World world) {
    Location center = centers.get(world);
    if (center != null) return center.clone();

    Location spawn = world.getSpawnLocation();
    spawn.setX(spawn.getBlockX() + 0.5);
    spawn.setZ(spawn.getBlockZ() + 0.5);
    return spawn;
  }

  public void setCenter(@NotNull World world, @NotNull Location center) {
    centers.put(world, center.clone());
    if (isActive()) updateBorder(world, false);
  }

  /**
   * Remembers a center only if the world does not have one yet, for challenges that follow the player
   * into a world rather than deciding where its border belongs.
   */
  public void setCenterIfAbsent(@NotNull World world, @NotNull Location center) {
    if (centers.containsKey(world)) return;
    setCenter(world, center);
  }

  /**
   * Whether the block at this location lies outside the area the client lets a player walk in.
   * <p>
   * Vanilla collides with the border rounded outwards to whole blocks, so at a size like 5.1 a player
   * walks up to a block past the drawn line. Holding them to the line itself would throw them back for
   * a step the border allowed.
   */
  public boolean isOutside(@NotNull Location location) {
    World world = location.getWorld();
    if (world == null || !isActive()) return false;

    Location center = getCenter(world);
    double radius = getSize(world) / 2;
    return isOutside(location.getBlockX(), center.getX(), radius)
      || isOutside(location.getBlockZ(), center.getZ(), radius);
  }

  private static boolean isOutside(int block, double center, double radius) {
    return block < Math.floor(center - radius) || block >= Math.ceil(center + radius);
  }

  /**
   * Moves a player to the spot inside the drawn border closest to where they are.
   */
  public void teleportInside(@NotNull Player player) {
    Location location = player.getLocation();
    Location center = getCenter(player.getWorld());
    double radius = getSize(player.getWorld()) / 2;
    double margin = Math.min(player.getWidth() / 2, radius);

    location.setX(Math.clamp(location.getX(), center.getX() - radius + margin, center.getX() + radius - margin));
    location.setZ(Math.clamp(location.getZ(), center.getZ() - radius + margin, center.getZ() + radius - margin));
    Location spot = standingSpot(location);
    player.setFallDistance(0);
    player.teleportAsync(spot != null ? spot : surface(center, player.getWorld()));
  }

  @NotNull
  private static Location surface(@NotNull Location center, @NotNull World world) {
    center.setWorld(world);
    center.setY(world.getHighestBlockYAt(center.getBlockX(), center.getBlockZ()));
    return world.getEnvironment() == World.Environment.NORMAL ? center.add(0, 1, 0) : center;
  }

  // The spot keeps the height the player left at, which next to the border can be inside a hill or
  // above a drop, so it moves up out of anything solid and down onto whatever is below it. Null over
  // the void, where there is nothing to stand on.
  @Nullable
  private static Location standingSpot(@NotNull Location location) {
    World world = location.getWorld();
    Block feet = location.getBlock();
    while (!(feet.isPassable() && feet.getRelative(BlockFace.UP).isPassable()) && feet.getY() < world.getMaxHeight()) {
      feet = feet.getRelative(BlockFace.UP);
    }
    while (feet.getRelative(BlockFace.DOWN).isEmpty()) {
      if (feet.getY() <= world.getMinHeight()) return null;
      feet = feet.getRelative(BlockFace.DOWN);
    }

    if (feet.getY() != location.getBlockY()) location.setY(feet.getY());
    return location;
  }

  /**
   * Gives a player the border of the world they are in, or the world's own border while no challenge is
   * driving one or the player left the game worlds behind.
   * <p>
   * Sending must not rewrite a border somebody else is already watching, or a join would cut short an
   * animated resize for the whole world. It only writes one it has never written before: a world can
   * join the game worlds long after the border went active - {@code /world void} creates one on demand -
   * and would otherwise hand out the untouched 59,999,968 block default.
   */
  public void sendTo(@NotNull Player player) {
    if (overrides.containsKey(player.getUniqueId())) {
      bind(player);
      return;
    }

    World world = player.getWorld();
    if (!isActive() || !ChallengeAPI.getGameWorlds().contains(world)) {
      resetFor(player);
      return;
    }

    if (!hasBorder(world)) applyTo(world, false);
    bind(player);
  }

  /**
   * Gives one player a border of their own until it is {@link #clearOverride(Player) handed back}, for a
   * challenge that holds a single player somewhere. Neither the shared border's size nor its containment
   * applies to them while it is in place.
   */
  public void override(@NotNull Player player, @NotNull Location center, double size) {
    WorldBorder border = Bukkit.createWorldBorder();
    // The world of the center, not of the player: a teleport hands this its destination while the
    // player still stands in the world they came from. The client places a nether border at an eighth
    // of the coordinates it is given.
    World world = center.getWorld() == null ? player.getWorld() : center.getWorld();
    double factor = world.getEnvironment() == World.Environment.NETHER ? 8 : 1;
    border.setCenter(center.getX() * factor, center.getZ() * factor);
    border.setSize(size);
    border.setWarningDistance(0);
    border.setWarningTime(0);

    overrides.put(player.getUniqueId(), border);
    player.setWorldBorder(border);
  }

  public void clearOverride(@NotNull Player player) {
    if (overrides.remove(player.getUniqueId()) == null) return;
    if (player.isOnline()) sendTo(player);
  }

  public void update(boolean animate) {
    for (World world : ChallengeAPI.getGameWorlds()) {
      updateBorder(world, animate);
    }
    updateBossBar();
  }

  private void updateBossBar() {
    if (growth.isEmpty()) {
      if (bossbar.isShown()) bossbar.hide();
    } else if (bossbar.isShown()) {
      bossbar.update();
    } else {
      bossbar.show();
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onJoin(@NotNull PlayerJoinEvent event) {
    // the server sends the world's own border again after the join event, over whatever was sent during it
    Regions.nextTick(Challenges.getInstance(), event.getPlayer(), () -> sendTo(event.getPlayer()));
    contain(event.getPlayer());
  }

  // An override belongs to a session: the challenge that set it has no state on the other side of a
  // relog, so a stale one would bind a returning player to a border nothing can lift.
  @EventHandler(priority = EventPriority.MONITOR)
  public void onQuit(@NotNull PlayerQuitEvent event) {
    overrides.remove(event.getPlayer().getUniqueId());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onWorldChange(@NotNull PlayerChangedWorldEvent event) {
    sendTo(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onRespawn(@NotNull PlayerRespawnedEvent event) {
    // the client drops its border on respawn, so it has to be told again
    Regions.nextTick(Challenges.getInstance(), event.getPlayer(), () -> {
      sendTo(event.getPlayer());
      // a bed refuses a respawn outside the border, the random spawn around the world spawn does not
      contain(event.getPlayer());
    });
  }

  // A small border in the nether or the end opens where the first player comes through the portal,
  // not around that world's spawn.
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPortal(@NotNull PlayerTeleportEvent event) {
    if (!isActive() || !ChallengeAPI.isStarted() || ChallengeAPI.isWorldInUse() || event.getTo() == null) return;
    if (event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL
      && event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) return;
    if (AbstractChallenge.ignorePlayer(event.getPlayer())) return;
    World world = event.getTo().getWorld();
    if (world == null || world == Challenges.getInstance().getWorldManager().getExtraWorld()) return;
    setCenterIfAbsent(world, event.getTo().getBlock().getLocation().add(0.5, 0, 0.5));
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onDamageAcrossBorder(@NotNull EntityDamageByEntityEvent event) {
    if (!event.isCancelled() || !isActive() || !ChallengeAPI.isStarted() || ChallengeAPI.isWorldInUse()) return;
    if (event.getDamager().getType() != EntityType.PLAYER) return;
    if (isOutside(event.getEntity().getLocation()) && !isOutside(event.getDamager().getLocation())) {
      event.setCancelled(false);
    }
  }

  // Before the start too: a new player spawns scattered around the world spawn, outside a small border
  @ScheduledTask(ticks = 20, async = false, timerPolicy = TimerPolicy.ALWAYS)
  public void containPlayers() {
    Challenges.getInstance().getPlayerManager().forEachAudience(this::contain);
  }

  private void contain(@NotNull Player player) {
    if (!isActive() || ChallengeAPI.isWorldInUse()) return;
    if (overrides.containsKey(player.getUniqueId())) return;
    if (player.isDead() || AbstractChallenge.ignorePlayer(player)) return;
    if (isOutside(player.getLocation())) teleportInside(player);
  }

  private void updateBorder(@NotNull World world, boolean animate) {
    if (!isActive()) {
      forEachPlayerIn(world, this::resetFor);
      return;
    }

    // apply once for the whole world: re-applying per player would overwrite the animated transition
    // with an instant resize before anyone is told about it
    applyTo(world, animate);
    forEachPlayerIn(world, this::bind);
  }

  private void forEachPlayerIn(@NotNull World world, @NotNull Consumer<Player> action) {
    Challenges.getInstance().getPlayerManager().forEachAudience(player -> {
      if (player.getWorld() == world) action.accept(player);
    });
  }

  /**
   * Points a player at this world's border object, leaving its current size and center alone.
   */
  private void bind(@NotNull Player player) {
    WorldBorder override = overrides.get(player.getUniqueId());
    if (override != null) {
      // also the path that gives it back after a respawn, where the client drops whatever it was shown
      player.setWorldBorder(override);
      return;
    }

    player.setWorldBorder(apiBorder(player.getWorld()));
  }

  /**
   * Writes the current size and center onto the world's border object, without sending it anywhere.
   */
  private void applyTo(@NotNull World world, boolean animate) {
    Location center = getCenter(world);
    double size = getSize(world);

    WorldBorder border = apiBorder(world);
    // the client places a nether border at an eighth of the coordinates it is given
    double factor = world.getEnvironment() == World.Environment.NETHER ? 8 : 1;
    border.setCenter(center.getX() * factor, center.getZ() * factor);
    if (animate) border.setSize(size, 1);
    else border.setSize(size);
    border.setWarningDistance(0);
    border.setWarningTime(0);
  }

  private void resetFor(@NotNull Player player) {
    if (overrides.containsKey(player.getUniqueId())) return;

    player.setWorldBorder(null);
  }

  private boolean hasBorder(@NotNull World world) {
    return apiBorders.containsKey(world);
  }

  @NotNull
  private WorldBorder apiBorder(@NotNull World world) {
    return apiBorders.computeIfAbsent(world, _ -> Bukkit.createWorldBorder());
  }

  @Override
  public String getUniqueGamestateName() {
    return "world-border";
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    document.set("offset", offset);

    GsonDocument worlds = new GsonDocument();
    centers.forEach((world, center) -> worlds.set(world.getName(), center));
    document.set("centers", worlds);
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    offset = document.getDouble("offset");

    centers.clear();
    Document worlds = document.getDocument("centers");
    for (String worldName : worlds.keys()) {
      World world = Bukkit.getWorld(worldName);
      if (world == null) continue;
      centers.put(world, worlds.getInstance(worldName, Location.class));
    }

    update(false);
  }

}
