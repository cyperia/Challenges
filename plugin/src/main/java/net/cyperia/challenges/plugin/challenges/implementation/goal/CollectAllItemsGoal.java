package net.cyperia.challenges.plugin.challenges.implementation.goal;

import lombok.Getter;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ChecklistGoal;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SettingGoal;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.Prefix;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import net.cyperia.challenges.plugin.spigot.events.PlayerInventoryClickEvent;
import net.cyperia.challenges.plugin.spigot.events.PlayerPickupItemEvent;
import net.cyperia.challenges.plugin.utils.bukkit.command.SenderCommand;
import net.cyperia.challenges.plugin.utils.misc.ExperimentalUtils;
import net.cyperia.challenges.plugin.utils.misc.Utils;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.item.ItemUtils;
import net.cyperia.commons.common.config.Document;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CollectAllItemsGoal extends SettingGoal implements SenderCommand, ChecklistGoal {

  private static final String SEED_KEY = "seed";
  private static final String FOUND_ITEMS_KEY = "found-items";
  private static final String FOUND_COUNT_KEY = "found";
  private static final String FINDERS_KEY = "finders";
  private static final String FOUND_IN_ORDER_KEY = "found-in-order";
  private static final String CURRENT_ITEM_KEY = "current";

  @Getter
  private final List<Material> allItemsToFind;
  @Getter
  private final int totalItemsCount;

  private final Set<Material> collectedItems = EnumSet.noneOf(Material.class);
  /**
   * Every collected item by name, in the order it was found. Kept by name so that neither a server
   * version change nor a reshuffled remaining order can alter it, and so that items unknown to this
   * version are written back untouched when the server is downgraded temporarily.
   */
  private final List<String> foundOrder = new ArrayList<>();
  private final Map<String, UUID> finders = new HashMap<>();

  private List<Material> itemsToFind;
  private long seed;
  @Getter
  private Material currentItem;

  public CollectAllItemsGoal() {
    allItemsToFind = loadItemsObtainableInSurvival();
    totalItemsCount = allItemsToFind.size();
    reset(ThreadLocalRandom.current().nextLong());
  }

  @NotNull
  private static List<Material> loadItemsObtainableInSurvival() {
    Material[] materials = ExperimentalUtils.getMaterials();
    List<Material> items = new ArrayList<>(materials.length);
    for (Material material : materials) {
      if (!material.isItem()) continue;
      if (!ItemUtils.isObtainableInSurvival(material)) continue;
      items.add(material);
    }
    return List.copyOf(items);
  }

  private void reset(long seed) {
    this.seed = seed;
    collectedItems.clear();
    foundOrder.clear();
    finders.clear();
    rebuildItemsToFind();
    currentItem = pollNextItem();
  }

  /**
   * Rebuilds the order in which the remaining items have to be found and clears the current item,
   * as it is the first item of that order. The order is derived from {@link #seed} only, so it
   * stays the same across restarts as long as {@link #allItemsToFind} does. Everything already
   * collected is dropped up front, which makes the order independent of <i>when</i> an item was
   * collected.
   */
  private void rebuildItemsToFind() {
    currentItem = null;
    itemsToFind = getSeededOrder();
    itemsToFind.removeIf(collectedItems::contains);
  }

  @NotNull
  private List<Material> getSeededOrder() {
    List<Material> order = new ArrayList<>(allItemsToFind);
    Collections.shuffle(order, new Random(seed));
    return order;
  }

  /**
   * Marks the current item as collected and requests the next one, ending the challenge if there is none left.
   */
  private void nextItem() {
    if (currentItem != null) {
      collectedItems.add(currentItem);
      foundOrder.add(currentItem.name());
    }
    currentItem = pollNextItem();
    if (currentItem == null) {
      ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED);
    }
  }

  @Nullable
  private Material pollNextItem() {
    return itemsToFind.isEmpty() ? null : itemsToFind.removeFirst();
  }

  @Override
  protected void onEnable() {
    bossbar.setContent((bossbar, _) -> {
      if (currentItem == null) {
        bossbar.setTitle(getChallengeMessageKey("bossbar-finished"));
        bossbar.setColor(BossBar.Color.GREEN);
        return;
      }
      int currentItemNumber = totalItemsCount - itemsToFind.size();
      bossbar.setTitle(getChallengeMessageKey("bossbar-current"),
        currentItem, currentItemNumber, totalItemsCount);
    });
    bossbar.show();
  }

  @Override
  protected void onDisable() {
    bossbar.hide();
  }

  @Override
  public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) throws Exception {
    if (!isEnabled()) {
      MessageKey.of("challenge.generic.disabled").send(sender, Prefix.CHALLENGES);
      SoundSample.BASS_OFF.playIfPlayer(sender);
      return;
    }
    if (currentItem == null) {
      getChallengeMessageKey("already-finished").send(sender, Prefix.CHALLENGES);
      SoundSample.BASS_OFF.playIfPlayer(sender);
      return;
    }

    getChallengeMessageKey("skipped").broadcast(Prefix.CHALLENGES, currentItem);
    SoundSample.PLING.broadcast();
    nextItem();
    bossbar.update();
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onClick(@NotNull PlayerInventoryClickEvent event) {
    if (event.isCancelled()) return;
    ItemStack item = event.getCurrentItem();
    if (item == null) return;
    handleNewItem(item.getType(), event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPickUp(@NotNull PlayerPickupItemEvent event) {
    handleNewItem(event.getItem().getItemStack().getType(), event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onInteract(@NotNull PlayerInteractEvent event) {
    Player player = event.getPlayer();
    if (!shouldExecuteEffect(player)) return;
    // The held item may only change once the interaction has been processed (buckets, bottles, ...)
    Bukkit.getScheduler().runTaskLater(plugin, () ->
      handleNewItem(player.getInventory().getItemInMainHand().getType(), player), 1);
  }

  protected void handleNewItem(@Nullable Material material, @NotNull Player player) {
    if (material == null || material != currentItem) return;
    if (!shouldExecuteEffect(player)) return;
    getChallengeMessageKey("found").broadcast(Prefix.CHALLENGES, currentItem, player);
    SoundSample.PLING.broadcast();
    finders.put(currentItem.name(), player.getUniqueId());
    nextItem();
    bossbar.update();
  }

  @Override
  public void getWinnersOnEnd(@NotNull List<Player> winners) {
  }

  @Override
  public void loadGameState(@NotNull Document document) {
    super.loadGameState(document);

    if (!document.contains(SEED_KEY)) { // Fresh game or reset gamestate
      reset(ThreadLocalRandom.current().nextLong());
      return;
    }

    seed = document.getLong(SEED_KEY);
    collectedItems.clear();
    foundOrder.clear();
    finders.clear();

    if (document.contains(FOUND_ITEMS_KEY)) {
      List<String> names = document.getStringList(FOUND_ITEMS_KEY);
      if (!document.getBoolean(FOUND_IN_ORDER_KEY)) {
        // Written before the found order was kept, the list is in declaration order instead
        names = sortBySeededOrder(names, getSeededOrder());
      }
      readCollectedItems(names);
      rebuildItemsToFind();
    } else {
      // Gamestates written before 2.4 only stored how many items were found, not which ones. Those
      // are the first n items of the order, which is reproducible as long as the item pool did not
      // change since the gamestate was written.
      rebuildItemsToFind();
      int foundCount = Math.clamp(document.getInt(FOUND_COUNT_KEY), 0, itemsToFind.size());
      readCollectedItems(itemsToFind.subList(0, foundCount).stream().map(Material::name).toList());
      rebuildItemsToFind(); // Now that the collected items are known, drop them from the order
    }

    Document finderDocument = document.getDocument(FINDERS_KEY);
    for (String name : foundOrder) {
      UUID finder = finderDocument.getUUID(name);
      if (finder != null) finders.put(name, finder);
    }

    // A version change reshuffles the remaining order, which must not swap the item players are looking for
    Material savedCurrentItem = Utils.getMaterial(document.getString(CURRENT_ITEM_KEY));
    currentItem = savedCurrentItem != null && itemsToFind.remove(savedCurrentItem) ? savedCurrentItem : pollNextItem();
  }

  /**
   * Restores the collected items by name instead of by position, so that progress survives a server
   * version change: neither reordered nor added nor removed materials can shift an item into or out
   * of the collected items.
   */
  private void readCollectedItems(@NotNull List<String> names) {
    for (String name : names) {
      foundOrder.add(name);
      Material item = Utils.getMaterial(name);
      if (item != null) collectedItems.add(item);
    }
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    super.writeGameState(document);

    document.set(SEED_KEY, seed);
    document.set(FOUND_ITEMS_KEY, foundOrder);
    document.set(FOUND_IN_ORDER_KEY, true);
    document.set(FOUND_COUNT_KEY, foundOrder.size()); // Only read by versions before 2.4
    document.set(CURRENT_ITEM_KEY, currentItem == null ? null : currentItem.name());
    Document finderDocument = document.getDocument(FINDERS_KEY);
    finders.forEach((name, uuid) -> finderDocument.set(name, uuid.toString()));
  }

  // The future order stays hidden: only the current item and the ones already found are listed
  @NotNull
  @Override
  public List<ChecklistEntry> getChecklist(@NotNull Player viewer) {
    List<ChecklistEntry> entries = new ArrayList<>(foundOrder.size() + 1);
    if (currentItem != null) {
      entries.add(ChecklistEntry.of(currentItem, false, List.of()));
    }
    for (String name : foundOrder.reversed()) {
      Material item = Utils.getMaterial(name);
      if (item == null) continue;
      entries.add(ChecklistEntry.of(item, true, Optional.ofNullable(finders.get(name)).stream().toList()));
    }
    return entries;
  }

  // Items are found strictly in the seeded order, so sorting by it restores the found order as long as the item pool did not change
  @NotNull
  static List<String> sortBySeededOrder(@NotNull List<String> names, @NotNull List<Material> seededOrder) {
    Map<String, Integer> positions = new HashMap<>();
    for (int i = 0; i < seededOrder.size(); i++) {
      positions.put(seededOrder.get(i).name(), i);
    }
    return names.stream().sorted(Comparator.comparingInt(name -> positions.getOrDefault(name, -1))).toList();
  }

  @NotNull
  @Override
  public MessageKey getFinderMessage() {
    return MessageKey.of("menu.goal.collected-by");
  }

}
