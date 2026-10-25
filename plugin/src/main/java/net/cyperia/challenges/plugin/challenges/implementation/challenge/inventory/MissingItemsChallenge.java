package net.cyperia.challenges.plugin.challenges.implementation.challenge.inventory;

import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ValueUnit;
import net.cyperia.challenges.plugin.challenges.type.abstraction.TimedChallenge;
import net.cyperia.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.content.i18n.TranslationManager;
import net.cyperia.challenges.plugin.management.command.ChallengeCommand;
import net.cyperia.challenges.plugin.management.command.CommandInfo;
import net.cyperia.challenges.plugin.management.command.CommandProvider;
import net.cyperia.challenges.plugin.management.menu.format.InteractionTooltips;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.scheduler.policy.ChallengeStatusPolicy;
import net.cyperia.challenges.plugin.management.scheduler.task.ScheduledTask;
import net.cyperia.challenges.plugin.permission.ChallengesPermissions;
import net.cyperia.challenges.plugin.spigot.command.ShortcutCommand;
import net.cyperia.challenges.plugin.utils.misc.ExperimentalUtils;
import net.cyperia.challenges.plugin.utils.misc.InventoryUtils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.item.ItemUtils;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import net.cyperia.commons.bukkit.utils.menu.MenuPosition;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MissingItemsChallenge extends TimedChallenge implements CommandProvider {

  private static final int GUESS_SECONDS = 150;
  private static final int SOUND_WARNING_SECONDS = 5;
  private static final int INVENTORY_SIZE = 6 * 9;
  private static final int STACKED_DECOY_PERCENT = 20;

  // the round of every player currently guessing; written on the main thread, but also read by the
  // (asynchronous) bossbar update
  private final Map<UUID, Game> games = new ConcurrentHashMap<>();
  private List<Material> decoyMaterials;

  public MissingItemsChallenge() {
    super(1, 10, 5, false);
    setValueUnit(ValueUnit.INTERVAL_MINUTES.jitter(10));
    setUsesGlobalPunish(true);
  }

  @Override
  protected void onEnable() {
    bossbar.setContent((bossbar, player) -> {
      Game game = games.get(player.getUniqueId());
      if (game == null) {
        bossbar.setColor(BossBar.Color.YELLOW);
        bossbar.setTitle(getChallengeMessageKey("bossbar.waiting"));
        return;
      }
      int secondsLeft = game.secondsLeft; // single read, the countdown runs on another thread
      bossbar.setColor(BossBar.Color.RED);
      bossbar.setProgress((float) secondsLeft / GUESS_SECONDS);
      bossbar.setTitle(getChallengeMessageKey("bossbar.guessing"), ChallengeAPI.formatTime(secondsLeft));
    });
    bossbar.show();
  }

  @Override
  protected void onDisable() {
    games.values().forEach(game -> Regions.entity(plugin, game.player, game::end)); // hand every hidden item back instead of losing it
    bossbar.hide();
  }

  @Override
  protected void onTimeActivation() {
    broadcastFiltered(this::startGame);
    // while rounds are running the countdown stays stopped, the last one ending starts it again
    if (games.isEmpty()) restartTimer();
  }

  @ScheduledTask(ticks = 20, async = false, challengePolicy = ChallengeStatusPolicy.ENABLED)
  public void handleGuessSecond() {
    if (games.isEmpty()) return; // the waiting bossbar is refreshed by the scoreboard manager
    games.values().forEach(game -> Regions.entity(plugin, game.player, game::tick));
    bossbar.update();
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onQuit(@NotNull PlayerQuitEvent event) {
    Game game = games.get(event.getPlayer().getUniqueId());
    // hands the item back while the inventory is still saved, and keeps a round nobody can finish
    // anymore from blocking the countdown to the next one
    if (game != null) game.end();
  }

  @NotNull
  @Override
  public List<ChallengeCommand> getCommands() {
    return List.of(ShortcutCommand.forPlayer(CommandInfo.command("openmissingitems", ChallengesPermissions.COMMAND_OPENMISSINGITEMS),
      (player, context) -> openGame(player)));
  }

  private void openGame(@NotNull Player player) {
    Game game = games.get(player.getUniqueId());
    if (game == null) {
      getChallengeMessageKey("command.none").send(player, Prefix.CHALLENGES);
      SoundSample.BASS_OFF.play(player);
      return;
    }

    game.open();
    SoundSample.OPEN.play(player);
  }

  private void startGame(@NotNull Player player) {
    // changing the interval in the menu restarts the countdown mid round, so this can be reached
    // while the player is still guessing; a second round would drop the first item for good
    if (games.containsKey(player.getUniqueId())) return;

    PlayerInventory inventory = player.getInventory();
    int slot = InventoryUtils.getRandomFullSlot(inventory);
    if (slot == -1) return;

    ItemStack missingItem = inventory.getItem(slot);
    if (missingItem == null) return;

    Game game = new Game(player, missingItem); // the menu comes first: only take the item once it can be won back
    inventory.setItem(slot, null);
    games.put(player.getUniqueId(), game);

    getChallengeMessageKey("start").send(player, Prefix.CHALLENGES);
    SoundSample.PLOP.play(player);
  }

  @NotNull
  private ItemStack createDecoy(@NotNull ItemStack hiddenItem) {
    while (true) {
      ItemStack decoy = new ItemStack(globalRandom.choose(getDecoyMaterials()));
      short maxDurability = decoy.getType().getMaxDurability();

      if (maxDurability > 1 && decoy.getItemMeta() instanceof Damageable damageable) {
        damageable.setDamage(globalRandom.range(1, maxDurability));
        decoy.setItemMeta(damageable); // the meta is a copy and has to be written back
      } else if (decoy.getMaxStackSize() > 1 && globalRandom.nextInt(100) <= STACKED_DECOY_PERCENT) {
        decoy.setAmount(globalRandom.range(1, decoy.getMaxStackSize()));
      }

      // a decoy the hidden item cannot be told apart from would leave the round without a right answer
      if (!decoy.isSimilar(hiddenItem) || decoy.getAmount() != hiddenItem.getAmount()) return decoy;
    }
  }

  @NotNull
  private List<Material> getDecoyMaterials() {
    if (decoyMaterials == null) {
      // resolved on first use: which materials exist depends on the features the game world enables
      decoyMaterials = Arrays.stream(ExperimentalUtils.getMaterials())
        .filter(Material::isItem)
        .filter(ItemUtils::isObtainableInSurvival)
        .toList();
    }
    return decoyMaterials;
  }

  private final class Game implements MenuPosition {

    private final Player player;
    private final ItemStack missingItem;
    private final Inventory inventory;
    private final int correctSlot = globalRandom.nextInt(INVENTORY_SIZE);
    private int secondsLeft = GUESS_SECONDS;

    private Game(@NotNull Player player, @NotNull ItemStack missingItem) {
      this.player = player;
      this.missingItem = missingItem;
      this.inventory = Bukkit.createInventory(MenuPosition.HOLDER, INVENTORY_SIZE,
        getChallengeMessageKey("inventory").asComponent(player));

      Locale locale = TranslationManager.getLocaleOf(player);
      InteractionTooltips tooltips = new InteractionTooltips()
        .on(MenuClickType.LEFT, getChallengeMessageKey("interact.guess"));

      for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
        ItemStack item = slot == correctSlot ? missingItem : createDecoy(missingItem);
        inventory.setItem(slot, new ItemFormatter(item).tooltips(tooltips).build(locale));
      }
    }

    private void open() {
      player.openInventory(inventory);
      MenuPosition.set(player, this);
    }

    @Override
    public void handleClick(@NotNull MenuClickInfo info) {
      Material guessed = info.getClickedMaterial();
      boolean guessedRight = info.getSlot() == correctSlot;
      if (!end()) return;

      bossbar.update(player);

      if (guessedRight) {
        getChallengeMessageKey("guess.right").send(player, Prefix.CHALLENGES, guessed);
        SoundSample.LEVEL_UP.play(player);
        return;
      }

      getChallengeMessageKey("guess.wrong").send(player, Prefix.CHALLENGES, guessed, missingItem.getType());
      ChallengeHelper.punish(player);
    }

    private void tick() {
      // whoever left or turned into a creative/spectator player is not expected to guess anymore,
      // and killing them would contradict the very check that kept them out of this round
      if (!player.isOnline() || ignorePlayer(player)) {
        end();
        return;
      }

      if (--secondsLeft > 0) {
        if (secondsLeft <= SOUND_WARNING_SECONDS) {
          new SoundSample().addSound(Sound.BLOCK_NOTE_BLOCK_BASS, 0.5F, (float) (secondsLeft - 1) / 10 + 1).play(player);
        }
        return;
      }

      if (!end()) return;
      getChallengeMessageKey("time-up").send(player, Prefix.CHALLENGES, missingItem.getType());
      ChallengeHelper.punish(player);
    }

    /**
     * @return whether this call ended the round, {@code false} if it was over already
     */
    private boolean end() {
      // dropping the round first keeps a failure below from stopping the challenge for good
      if (!games.remove(player.getUniqueId(), this)) return false;

      InventoryUtils.giveItem(player, missingItem);
      for (HumanEntity viewer : List.copyOf(inventory.getViewers())) {
        viewer.closeInventory();
      }

      if (games.isEmpty()) restartTimer();
      return true;
    }

  }

}
