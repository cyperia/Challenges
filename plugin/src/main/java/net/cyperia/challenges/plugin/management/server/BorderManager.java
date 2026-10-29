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
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
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
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The one world border of a challenge round, the one /worldborder sets and every border challenge changes.
 * <p>
 * Challenges never touch {@link World#getWorldBorder()}: a challenge whose border starts small and
 * grows with the game state sets how far it has {@link #setGrowth(AbstractChallenge, double, boolean)
 * grown} it, one that only makes it bigger or smaller {@link #changeSize(double) changes} it, and all of
 * them add up on top of the size /worldborder {@link #setSize(double, long) set}. Going through the world
 * border instead would mean the last writer wins, so enabling a second border challenge would silently
 * turn the first one into a no-op.
 * <p>
 * What the players see is a border of this manager's own, sent per player. The world keeps its vanilla
 * border, because the server spawns no mobs in chunks outside the world's border.
 */
public final class BorderManager implements GamestateSaveable, Listener {

  public static final double MIN_SIZE = 1;
  public static final double MAX_SIZE = 59_999_968;
  private static final double START_SIZE = 1;

  private final Map<World, WorldBorder> apiBorders = new ConcurrentHashMap<>();
  private final Map<World, Location> centers = new ConcurrentHashMap<>();

  // Players a challenge has given a border of their own. The shared border leaves them alone until it
  // is handed back, so a challenge that traps somebody is not undone by one that shrinks the border.
  private final Set<UUID> teleporting = ConcurrentHashMap.newKeySet();
  private final Map<UUID, WorldBorder> overrides = new ConcurrentHashMap<>();

  // How far each challenge whose border starts small has grown it, by unique challenge name. Not saved:
  // each of them restores its own on load, and a reset loads this manager after the challenges.
  private final Map<String, Double> growth = new ConcurrentHashMap<>();
  private volatile double offset;

  // The size the border is sliding from and to, so players are held to the line they see moving
  // rather than to where it ends up. NaN while there is no border.
  private volatile double drawnTarget = Double.NaN;
  private volatile double slideFrom;
  private volatile long slideStart;
  private volatile long slideEnd;

  // The size /worldborder set, null while nobody did
  private volatile Double size;

  // Unlike vanilla's 5 blocks, no buffer: a border that shrinks by a block has to hurt whoever it passes
  private static final double DAMAGE_PER_BLOCK = 0.2;
  private volatile double damageBuffer = 0;
  private volatile double damagePerBlock = DAMAGE_PER_BLOCK;
  private volatile Containment containment = Containment.DAMAGE;

  // One bar for every challenge that drives the border, so two of them never stack two bars
  private final ChallengeBossBar bossbar = new ChallengeBossBar();

  public void enable() {
    bossbar.setContent((bar, player) -> bar.setTitle(MessageKey.of("bossbar.border-size"), (int) getSize(player.getWorld())));
    Challenges.getInstance().getChallengeManager().registerGameStateSaver(this);
    Challenges.getInstance().getScheduler().register(this);
    Challenges.getInstance().registerListener(this);
  }

  /**
   * Whether there is a border at all: one set with /worldborder or one a challenge grows. While there is
   * none, players keep the border their world came with.
   */
  public boolean isActive() {
    return isGrowing() || size != null;
  }

  private boolean isGrowing() {
    return !growth.isEmpty();
  }

  /**
   * Shrinks the border down to a single block while any challenge has set a growth, and grows it by the
   * sum of all of them. A border /worldborder set is grown from its own size instead.
   * Passing the same growth twice is free.
   */
  public void setGrowth(@NotNull AbstractChallenge challenge, double blocks, boolean animate) {
    Double previous = growth.put(challenge.getUniqueName(), blocks);
    if (previous != null && previous == blocks) return;
    update(animate ? 1 : 0);
  }

  public void clearGrowth(@NotNull AbstractChallenge challenge) {
    if (growth.remove(challenge.getUniqueName()) == null) return;
    update(0);
  }

  /**
   * Grows or shrinks the border by {@code delta}, on top of whatever size it has otherwise.
   * The border slides to the new size rather than jumping to it, the way it does when the level border
   * follows a level. Without a border there is nothing to see, but the change is kept for when there is.
   */
  public void changeSize(double delta) {
    offset += delta;
    update(1);
  }

  /**
   * Makes the border {@code target} blocks wide over {@code seconds}, the way /worldborder set does.
   * Challenges that change the border go on from there.
   */
  public void setSize(double target, long seconds) {
    double clamped = Math.clamp(target, MIN_SIZE, MAX_SIZE);
    size = clamped - (isGrowing() ? grown() : 0) - offset;
    update(seconds);
  }

  /**
   * Takes back what /worldborder set, so without a challenge that grows one there is no border any more.
   */
  public void reset() {
    size = null;
    centers.clear();
    update(0);
  }

  public void setCenter(@NotNull World world, @NotNull Location center) {
    centers.put(world, center.clone());
    if (isActive()) updateBorder(world);
  }

  public void setDamage(double buffer, double perBlock) {
    damageBuffer = Math.max(0, buffer);
    damagePerBlock = Math.max(0, perBlock);
  }

  public void setContainment(@NotNull Containment containment) {
    this.containment = containment;
  }

  @NotNull
  public Containment getContainment() {
    return containment;
  }

  public double getDamageBuffer() {
    return damageBuffer;
  }

  public double getDamagePerBlock() {
    return damagePerBlock;
  }

  /**
   * The size the border has in this world once it stops sliding.
   */
  public double getSize(@NotNull World world) {
    if (!isActive()) return world.getWorldBorder().getSize();
    return targetSize();
  }

  private double targetSize() {
    double base = size != null ? size : START_SIZE;
    return Math.clamp(base + (isGrowing() ? grown() : 0) + offset, MIN_SIZE, MAX_SIZE);
  }

  /**
   * The size the border is drawn at right now, somewhere between where a slide started and
   * {@link #getSize(World)}.
   */
  public double getDrawnSize(@NotNull World world) {
    if (!isActive()) return getSize(world);
    return drawnSize(System.currentTimeMillis());
  }

  private double drawnSize(long now) {
    if (now >= slideEnd) return drawnTarget;
    double progress = (now - slideStart) / (double) (slideEnd - slideStart);
    return slideFrom + (drawnTarget - slideFrom) * progress;
  }

  private void startSlide(long seconds) {
    long now = System.currentTimeMillis();
    double from = Double.isNaN(drawnTarget) ? Double.NaN : drawnSize(now);
    double target = isActive() ? targetSize() : Double.NaN;
    if (seconds > 0 && !Double.isNaN(from) && !Double.isNaN(target)) {
      slideFrom = from;
      slideStart = now;
      slideEnd = now + seconds * 1000;
    } else {
      slideEnd = 0;
    }
    drawnTarget = target;
  }

  private double grown() {
    return growth.values().stream().mapToDouble(Double::doubleValue).sum();
  }

  @NotNull
  public Location getCenter(@NotNull World world) {
    if (!isActive()) return world.getWorldBorder().getCenter();

    Location center = centers.get(world);
    if (center != null) return center.clone();
    // a border that starts small opens where players spawn, one /worldborder set where vanilla's would be
    if (!isGrowing()) return new Location(world, 0, 0, 0);

    Location spawn = world.getSpawnLocation();
    spawn.setX(spawn.getBlockX() + 0.5);
    spawn.setZ(spawn.getBlockZ() + 0.5);
    return spawn;
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
    double radius = getDrawnSize(world) / 2;
    return isOutside(location.getBlockX(), center.getX(), radius)
      || isOutside(location.getBlockZ(), center.getZ(), radius);
  }

  private static boolean isOutside(int block, double center, double radius) {
    return block < Math.floor(center - radius) || block >= Math.ceil(center + radius);
  }

  /**
   * How far this location lies past the area the client lets a player walk in, 0 inside it. Measured
   * from the same whole-block bounds as {@link #isOutside(Location)}, so standing where the border let
   * a player walk never hurts.
   */
  public double distanceOutside(@NotNull Location location) {
    World world = location.getWorld();
    if (world == null || !isActive()) return 0;

    Location center = getCenter(world);
    double radius = getDrawnSize(world) / 2;
    return Math.max(0, Math.max(
      distanceOutside(location.getX(), center.getX(), radius),
      distanceOutside(location.getZ(), center.getZ(), radius)));
  }

  private static double distanceOutside(double position, double center, double radius) {
    return Math.max(Math.floor(center - radius) - position, position - Math.ceil(center + radius));
  }

  /**
   * Moves a player to the spot inside the drawn border closest to where they are.
   */
  public void teleportInside(@NotNull Player player) {
    Location location = player.getLocation();
    Location center = getCenter(player.getWorld());
    double radius = getDrawnSize(player.getWorld()) / 2;
    double margin = Math.min(player.getWidth() / 2, radius);

    location.setX(Math.clamp(location.getX(), center.getX() - radius + margin, center.getX() + radius - margin));
    location.setZ(Math.clamp(location.getZ(), center.getZ() - radius + margin, center.getZ() + radius - margin));
    if (!teleporting.add(player.getUniqueId())) return;
    player.setFallDistance(0);

    // The ground at the target belongs to another region than the player's, so it is read there.
    Plugin plugin = Challenges.getInstance();
    Regions.at(plugin, location, () -> {
      try {
        Location spot = standingSpot(location);
        if (spot != null) {
          teleportTo(player, spot);
        } else {
          Regions.at(plugin, center, () -> teleportTo(player, surface(center, player.getWorld())));
        }
      } catch (RuntimeException e) {
        teleporting.remove(player.getUniqueId());
        throw e;
      }
    });
  }

  // One teleport per player at a time, so a player who stays outside until it lands does not queue another every tick.
  private void teleportTo(@NotNull Player player, @NotNull Location spot) {
    CompletableFuture<Boolean> teleport = player.teleportAsync(spot);
    teleport.whenComplete((done, error) -> teleporting.remove(player.getUniqueId()));
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
   * Gives a player the border of the world they are in, or the world's own border while there is none
   * or the player left the game worlds behind.
   * <p>
   * Sending must not rewrite a border somebody else is already watching, or a join would cut short an
   * animated resize for the whole world. It only writes one it has never written before: a world can
   * join the game worlds long after the border went active - a world generator challenge swaps them -
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

    if (!hasBorder(world)) applyTo(world);
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
    // player still stands in the world they came from.
    World world = center.getWorld() == null ? player.getWorld() : center.getWorld();
    setCenter(border, world, center);
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

  /**
   * Writes the border onto every game world over {@code seconds}, 0 for at once.
   */
  public void update(long seconds) {
    startSlide(seconds);
    for (World world : ChallengeAPI.getGameWorlds()) {
      updateBorder(world);
    }
    updateBossBar();
  }

  private void updateBossBar() {
    if (!isGrowing()) {
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
    if (!isGrowing() || !ChallengeAPI.isStarted() || ChallengeAPI.isWorldInUse() || event.getTo() == null) return;
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

  /**
   * Puts every player who stands outside the border inside it, for a border that appears around players
   * who never crossed it.
   */
  public void containPlayers() {
    Challenges.getInstance().getPlayerManager().forEachAudience(this::contain);
  }

  // Before the start, a new player spawns scattered around the world spawn, outside a small border, and
  // is put inside whatever the containment says.
  @ScheduledTask(ticks = 10, async = false, timerPolicy = TimerPolicy.ALWAYS)
  public void holdPlayers() {
    if (!ChallengeAPI.isStarted() || containment == Containment.PUSH) {
      containPlayers();
      return;
    }
    Challenges.getInstance().getPlayerManager().forEachAudience(this::hurtOutside);
  }

  private void contain(@NotNull Player player) {
    if (!isHeld(player)) return;
    if (isOutside(player.getLocation())) teleportInside(player);
  }

  private void hurtOutside(@NotNull Player player) {
    if (!isHeld(player) || damagePerBlock <= 0) return;
    double beyond = distanceOutside(player.getLocation()) - damageBuffer;
    if (beyond <= 0) return;
    // vanilla's own formula, at least half a heart per hit
    player.damage(Math.max(1, Math.floor(beyond * damagePerBlock)), DamageSource.builder(DamageType.OUTSIDE_BORDER).build());
  }

  private boolean isHeld(@NotNull Player player) {
    if (!isActive() || ChallengeAPI.isWorldInUse()) return false;
    if (overrides.containsKey(player.getUniqueId())) return false;
    return !player.isDead() && !AbstractChallenge.ignorePlayer(player);
  }

  private void updateBorder(@NotNull World world) {
    if (!isActive()) {
      forEachPlayerIn(world, this::resetFor);
      return;
    }

    // apply once for the whole world: re-applying per player would overwrite the animated transition
    // with an instant resize before anyone is told about it
    applyTo(world);
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
  private void applyTo(@NotNull World world) {
    WorldBorder border = apiBorder(world);
    setCenter(border, world, getCenter(world));
    // Written from where the slide is now, never from what the border object held before: a border
    // that is new or was last used before a reset holds a size nobody is shown any more.
    long now = System.currentTimeMillis();
    long remaining = slideEnd - now;
    border.setSize(drawnSize(now));
    if (remaining > 0) border.changeSize(getSize(world), Math.max(1, remaining / 50));
    border.setWarningDistance(0);
    border.setWarningTime(0);
  }

  // the client places a nether border at an eighth of the coordinates it is given
  private static void setCenter(@NotNull WorldBorder border, @NotNull World world, @NotNull Location center) {
    double factor = world.getEnvironment() == World.Environment.NETHER ? 8 : 1;
    border.setCenter(center.getX() * factor, center.getZ() * factor);
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

  /**
   * What happens to a player the border moves over: vanilla border damage, or being put back inside.
   */
  public enum Containment {
    DAMAGE,
    PUSH
  }

  @Override
  public String getUniqueGamestateName() {
    return "world-border";
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    document.set("offset", offset);
    if (size != null) document.set("size", size);
    document.set("damage-buffer", damageBuffer);
    document.set("damage-per-block", damagePerBlock);
    document.set("containment", containment.name());

    GsonDocument worlds = new GsonDocument();
    centers.forEach((world, center) -> worlds.set(world.getName(), center));
    document.set("centers", worlds);
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    offset = document.getDouble("offset");
    damageBuffer = 0;
    damagePerBlock = DAMAGE_PER_BLOCK;
    containment = Containment.DAMAGE;
    size = document.contains("size") ? document.getDouble("size") : null;
    if (document.contains("damage-buffer")) damageBuffer = document.getDouble("damage-buffer");
    if (document.contains("damage-per-block")) damagePerBlock = document.getDouble("damage-per-block");
    if (document.contains("containment")) containment = Containment.valueOf(document.getString("containment"));

    centers.clear();
    Document worlds = document.getDocument("centers");
    for (String worldName : worlds.keys()) {
      World world = Bukkit.getWorld(worldName);
      if (world == null) continue;
      centers.put(world, worlds.getInstance(worldName, Location.class));
    }

    update(0);
  }

}
