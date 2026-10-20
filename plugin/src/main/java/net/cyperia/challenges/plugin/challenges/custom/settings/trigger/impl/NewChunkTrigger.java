package net.cyperia.challenges.plugin.challenges.custom.settings.trigger.impl;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.custom.TriggerActionChallenge;
import net.cyperia.challenges.plugin.challenges.custom.settings.ChallengeExecutionData;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SelectableKey;
import net.cyperia.challenges.plugin.challenges.custom.settings.sub.SubSettingsBuilder;
import net.cyperia.challenges.plugin.challenges.custom.settings.trigger.ChallengeTrigger;
import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import net.cyperia.challenges.plugin.challenges.type.abstraction.AbstractChallenge;
import net.cyperia.challenges.plugin.management.challenges.entities.GamestateSaveable;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.spigot.events.PlayerIgnoreStatusChangeEvent;
import net.cyperia.challenges.plugin.utils.bukkit.misc.ChunkSet;
import net.cyperia.commons.bukkit.utils.logging.Logger;
import net.cyperia.commons.common.config.Document;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fires when a player enters a chunk: every time, the first time anybody enters it, or the first time
 * each player enters it.
 */
public class NewChunkTrigger extends ChallengeTrigger implements GamestateSaveable {

  public static final String CHUNK = "chunk";
  public static final String NEW = "new";
  public static final String NEW_PERSONAL = "new_personal";
  public static final String EVERY = "every";

  // Keyed by the challenge's unique name: two challenges using this trigger track "new" independently,
  // so a chunk another challenge already converted still counts as new for this one.
  private final Map<String, Memory> memories = new HashMap<>();

  // A join that also teleports fires two handlers for the same entry, each scheduling its own delayed
  // fire() for the same tick; without this, "every" mode would run its action for that chunk twice.
  private final Map<UUID, LastFire> lastFired = new HashMap<>();

  public NewChunkTrigger(String name) {
    super(name, SubSettingsBuilder.createChooseItem(CHUNK).fill(builder -> {
      builder.addSetting(SelectableKey.ofNameDesc(NEW, Material.FILLED_MAP, "chunk.new"));
      builder.addSetting(SelectableKey.ofNameDesc(NEW_PERSONAL, Material.PLAYER_HEAD, "chunk.new_personal"));
      builder.addSetting(SelectableKey.ofNameDesc(EVERY, Material.MAP, "chunk.every"));
    }));
    Challenges.getInstance().getChallengeManager().registerGameStateSaver(this);
    Challenges.getInstance().getScheduler().register(this);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMove(PlayerMoveEvent event) {
    handleMove(event.getPlayer(), event.getFrom(), event.getTo());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onTeleport(PlayerTeleportEvent event) {
    handleMove(event.getPlayer(), event.getFrom(), event.getTo());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onIgnoreStatusChange(PlayerIgnoreStatusChangeEvent event) {
    if (event.isIgnored() || !canTrigger()) return;
    // This fires inside PlayerGameModeChangeEvent, before the new mode is applied, so the player is
    // still ignored here. The one-tick delay in fire() means execute() runs once the new mode is set.
    fire(event.getPlayer(), EVERY, NEW, NEW_PERSONAL);
  }

  // A respawn is an entry into the chunk it lands in, same as a move.
  @EventHandler(priority = EventPriority.MONITOR)
  public void onRespawn(PlayerRespawnEvent event) {
    if (!canTrigger()) return;
    Player player = event.getPlayer();
    if (AbstractChallenge.ignorePlayer(player)) return;
    fire(player, EVERY, NEW, NEW_PERSONAL);
  }

  // A join is an entry into the chunk the player logs in at, same as a move.
  @EventHandler(priority = EventPriority.MONITOR)
  public void onJoin(PlayerJoinEvent event) {
    if (!canTrigger()) return;
    Player player = event.getPlayer();
    if (AbstractChallenge.ignorePlayer(player)) return;
    fire(player, EVERY, NEW, NEW_PERSONAL);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onQuit(PlayerQuitEvent event) {
    lastFired.remove(event.getPlayer().getUniqueId());
  }

  /**
   * Catches the chunk a player is standing in without having entered it: a challenge switched on
   * mid-game, or the timer starting or resuming.
   */
  @ScheduledTask(ticks = 20, async = false)
  public void onSecond() {
    if (!canTrigger()) return;

    for (Player player : ChallengeAPI.getIngamePlayers()) {
      if (AbstractChallenge.ignorePlayer(player)) continue;
      createData().entity(player).data(CHUNK, NEW, NEW_PERSONAL).execute();
    }
  }

  private void handleMove(@NotNull Player player, @NotNull Location from, @Nullable Location to) {
    if (to == null) return;
    // Comparing the coordinates keeps this off Location#getChunk, which loads the chunk it names, and
    // it discards the moves inside one chunk that almost every move event is.
    if (to.getWorld() == from.getWorld()
      && to.getBlockX() >> 4 == from.getBlockX() >> 4
      && to.getBlockZ() >> 4 == from.getBlockZ() >> 4) return;
    if (!canTrigger()) return;
    if (AbstractChallenge.ignorePlayer(player)) return;

    fire(player, EVERY, NEW, NEW_PERSONAL);
  }

  // The player is still standing where they came from while a move event runs, so the actions are
  // handed the trigger a tick later, when the player is in the chunk they entered.
  private void fire(@NotNull Player player, @NotNull String... chunk) {
    Bukkit.getScheduler().runTask(Challenges.getInstance(), () -> {
      // A move followed by a quit in the same tick would otherwise run execute() against an offline player.
      if (!player.isOnline()) return;
      if (alreadyFiredThisTick(player)) return;
      createData()
        .entity(player)
        .data(CHUNK, chunk)
        .execute();
    });
  }

  // Two handlers can each schedule a fire() for the same player and tick, such as a join that also
  // teleports. Both delayed callbacks then run in the same later tick and would see the same chunk, so
  // only the first one is let through.
  private boolean alreadyFiredThisTick(@NotNull Player player) {
    Chunk chunk = player.getLocation().getChunk();
    LastFire now = new LastFire(Bukkit.getCurrentTick(), chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    return now.equals(lastFired.put(player.getUniqueId(), now));
  }

  private record LastFire(int tick, @NotNull String world, int chunkX, int chunkZ) {
  }

  // Whether an enabled challenge is listening. Recording chunks nobody asked about would grow the game
  // state on every server forever, and would leave chunks walked while the challenge was off unable to
  // ever count as new again.
  private boolean canTrigger() {
    if (!ChallengeAPI.isStarted() || ChallengeAPI.isWorldInUse()) return false;
    Challenges plugin = Challenges.getInstance();
    return plugin.getCustomChallengesLoader().isTriggerEnabled(this)
      || plugin.getPresetChallengeLoader().isTriggerEnabled(this);
  }

  /**
   * "every" is always met, but still records the chunk into the shared "new" set: nothing about being
   * met depends on that record, it exists so a caller with no player of its own, such as an explosion,
   * can still ask whether anybody has ever been in the chunk. "new"/"new_personal" are met the first
   * time the chunk the entity stands in is recorded, globally or per player. A challenge without a
   * chosen mode falls back to "every".
   */
  @Override
  public boolean isTriggerMet(@NotNull TriggerActionChallenge challenge, @NotNull ChallengeExecutionData data) {
    if (!challenge.isTriggerMet(data)) return false;

    String mode = modeOf(challenge);
    if (!(data.getEntity() instanceof Player player)) return EVERY.equals(mode);

    Chunk chunk = player.getLocation().getChunk();
    Memory memory = memories.computeIfAbsent(challenge.getUniqueName(), name -> new Memory(new ChunkSet(), new HashMap<>()));

    if (EVERY.equals(mode)) {
      memory.entered.add(chunk);
      return true;
    }
    if (NEW_PERSONAL.equals(mode)) {
      return memory.players.computeIfAbsent(player.getUniqueId(), id -> new ChunkSet()).add(chunk);
    }
    return memory.entered.add(chunk);
  }

  /**
   * Reads the "new"/"new_personal" memory without recording anything, for a caller that needs to know
   * whether a chunk already counts as entered rather than to record entering it. "every" tracks no
   * memory and is always met.
   */
  public boolean hasEntered(@NotNull TriggerActionChallenge challenge, @NotNull Player player, @NotNull Chunk chunk) {
    String mode = modeOf(challenge);
    if (EVERY.equals(mode)) return true;

    Memory memory = memories.get(challenge.getUniqueName());
    if (memory == null) return false;

    if (NEW_PERSONAL.equals(mode)) {
      ChunkSet playerChunks = memory.players.get(player.getUniqueId());
      return playerChunks != null && playerChunks.contains(chunk);
    }
    return memory.entered.contains(chunk);
  }

  /**
   * Whether any player has entered the given chunk under this challenge, for a caller with no player of
   * its own to ask about, such as an explosion. "new_personal" is met by the union of every player's
   * set; "new" and "every" share the same durable set, since {@link #isTriggerMet} records into it for
   * both.
   */
  public boolean hasAnyoneEntered(@NotNull TriggerActionChallenge challenge, @NotNull Chunk chunk) {
    Memory memory = memories.get(challenge.getUniqueName());
    if (memory == null) return false;

    if (NEW_PERSONAL.equals(modeOf(challenge))) {
      return memory.players.values().stream().anyMatch(playerChunks -> playerChunks.contains(chunk));
    }
    return memory.entered.contains(chunk);
  }

  /**
   * Makes the chunk count as never entered again for this challenge, by anybody.
   */
  public void forget(@NotNull TriggerActionChallenge challenge, @NotNull String world, long chunkKey) {
    Memory memory = memories.get(challenge.getUniqueName());
    if (memory == null) return;

    memory.entered.remove(world, chunkKey);
    for (ChunkSet playerChunks : memory.players.values()) playerChunks.remove(world, chunkKey);
  }

  @NotNull
  private static String modeOf(@NotNull TriggerActionChallenge challenge) {
    String[] modes = challenge.getSubTriggers().get(CHUNK);
    return modes == null || modes.length == 0 ? EVERY : modes[0];
  }

  @Override
  public String getUniqueGamestateName() {
    return "trigger-" + getUniqueName();
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    document.clear();

    for (IChallenge challenge : Challenges.getInstance().getChallengeManager().getChallenges()) {
      if (!(challenge instanceof TriggerActionChallenge triggerActionChallenge)) continue;
      if (triggerActionChallenge.getTrigger() != this) continue;

      Memory memory = memories.get(challenge.getUniqueName());
      if (memory == null) continue;

      Document challengeDocument = document.getDocument(challenge.getUniqueName());
      memory.entered.writeGameState(challengeDocument, "entered-chunks");

      Document playersDocument = challengeDocument.getDocument("players");
      for (Map.Entry<UUID, ChunkSet> entry : memory.players.entrySet()) {
        entry.getValue().writeGameState(playersDocument, entry.getKey().toString());
      }
    }
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    memories.clear();

    for (String uniqueName : document.keys()) {
      Document challengeDocument = document.getDocument(uniqueName);
      Memory memory = new Memory(new ChunkSet(), new HashMap<>());
      memory.entered.loadGameState(challengeDocument, "entered-chunks");

      Document playersDocument = challengeDocument.getDocument("players");
      for (String uuid : playersDocument.keys()) {
        try {
          ChunkSet playerChunks = new ChunkSet();
          playerChunks.loadGameState(playersDocument, uuid);
          memory.players.put(UUID.fromString(uuid), playerChunks);
        } catch (IllegalArgumentException exception) {
          Logger.error("Invalid player uuid in {}: {}", getUniqueGamestateName(), uuid);
        }
      }

      memories.put(uniqueName, memory);
    }
  }

  private record Memory(@NotNull ChunkSet entered, @NotNull Map<UUID, ChunkSet> players) {
  }

}
