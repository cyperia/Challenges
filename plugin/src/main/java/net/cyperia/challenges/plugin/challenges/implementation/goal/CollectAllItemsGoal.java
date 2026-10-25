package net.cyperia.challenges.plugin.challenges.implementation.goal;

import lombok.Getter;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.ChecklistGoal;
import net.cyperia.challenges.plugin.challenges.type.abstraction.SharedCollectionGoal;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.server.ChallengeEndCause;
import net.cyperia.challenges.plugin.spigot.events.PlayerInventoryClickEvent;
import net.cyperia.challenges.plugin.spigot.events.PlayerPickupItemEvent;
import net.cyperia.challenges.plugin.utils.item.CollectableItem;
import net.cyperia.challenges.plugin.utils.misc.VariantIds;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.misc.Regions;
import net.cyperia.commons.common.config.Document;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class CollectAllItemsGoal extends SharedCollectionGoal implements ChecklistGoal {

  private static final String SEED_KEY = "seed";
  private static final String FOUND_ITEMS_KEY = "found-items";
  private static final String FOUND_COUNT_KEY = "found";
  private static final String FINDERS_KEY = "finders";
  private static final String FOUND_AT_KEY = "found-at";
  private static final String FOUND_IN_ORDER_KEY = "found-in-order";
  private static final String CURRENT_ITEM_KEY = "current";

  private final BooleanSubSetting itemVariantsSetting;
  private List<CollectableItem> allItemsToFind;
  private Set<String> allIds;

  private final Set<CollectableItem> collectedItems = ConcurrentHashMap.newKeySet();
  /**
   * Every collected item by {@link CollectableItem#id()}, in the order it was found. Kept by id so that neither a server
   * version change nor a reshuffled remaining order can alter it, and so that items unknown to this
   * version are written back untouched when the server is downgraded temporarily.
   */
  private final List<String> foundOrder = new CopyOnWriteArrayList<>();
  private final Map<String, UUID> finders = new ConcurrentHashMap<>();
  // Timer seconds per collected item, by id like the found order
  private final Map<String, Long> foundAt = new ConcurrentHashMap<>();

  private List<CollectableItem> itemsToFind;
  private long seed;
  @Getter
  private CollectableItem currentItem;

  public CollectAllItemsGoal() {
    super(true);
    itemVariantsSetting = registerItemVariantsSetting(false, this::onItemVariantsChange);
    setItemsToFind();
    reset(ThreadLocalRandom.current().nextLong());
  }

  private void reset(long seed) {
    this.seed = seed;
    collectedItems.clear();
    foundOrder.clear();
    finders.clear();
    foundAt.clear();
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
  private List<CollectableItem> getSeededOrder() {
    return seededOrder(allItemsToFind, seed);
  }

  // Collected items are kept by id, so a book found with variants off still counts once they are off again
  private void onItemVariantsChange() {
    setItemsToFind();
    inheritBaseFinds();
    CollectableItem previous = currentItem;
    rebuildItemsToFind();
    currentItem = previous != null && itemsToFind.remove(previous) ? previous : pollNextItem();
    if (!isEnabled()) return;
    bossbar.update();
    updateRanking();
    if (currentItem == null && ChallengeAPI.isStarted()) ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED);
  }

  private void setItemsToFind() {
    allItemsToFind = CollectableItem.obtainableInSurvival(itemVariantsSetting.getAsBoolean());
    allIds = allItemsToFind.stream().map(CollectableItem::id).collect(Collectors.toSet());
  }

  // A book found with variants on is an enchanted book all the same once they are off
  private void inheritBaseFinds() {
    VariantIds.forEachCovered(foundOrder, allIds, (id, target) -> {
      if (foundOrder.contains(target)) return;
      CollectableItem item = CollectableItem.parse(target);
      if (item == null) return;
      collectedItems.add(item);
      foundOrder.add(target);
      if (finders.containsKey(id)) finders.put(target, finders.get(id));
      if (foundAt.containsKey(id)) foundAt.put(target, foundAt.get(id));
    });
  }

  /**
   * Marks the current item as collected and requests the next one, ending the challenge if there is none left.
   */
  private void nextItem() {
    if (currentItem != null) {
      collectedItems.add(currentItem);
      foundOrder.add(currentItem.id());
    }
    currentItem = pollNextItem();
    if (currentItem == null) {
      ChallengeAPI.endChallenge(ChallengeEndCause.GOAL_REACHED);
    }
  }

  @Nullable
  private CollectableItem pollNextItem() {
    return itemsToFind.isEmpty() ? null : itemsToFind.removeFirst();
  }

  @Override
  protected void enableGoal() {
    bossbar.setContent((bossbar, _) -> {
      if (currentItem == null) {
        styleBossbar(bossbar, false, 1);
        bossbar.setTitle(getChallengeMessageKey("bossbar-finished"));
        return;
      }
      int collected = allItemsToFind.size() - itemsToFind.size() - 1;
      styleBossbar(bossbar, isRandomOrder(), (float) collected / allItemsToFind.size());
      if (!isRandomOrder()) {
        bossbar.setTitle(getChallengeMessageKey("bossbar"), collected, allItemsToFind.size());
        return;
      }
      int currentItemNumber = allItemsToFind.size() - itemsToFind.size();
      bossbar.setTitle(getChallengeMessageKey("bossbar-next"),
        currentItem.displayName(), currentItemNumber, allItemsToFind.size());
    });
    bossbar.show();
  }

  @Override
  protected void disableGoal() {
    bossbar.hide();
  }

  @Nullable
  @Override
  protected Object skipCurrentTarget() {
    CollectableItem skipped = currentItem;
    if (skipped == null) return null;
    foundAt.put(skipped.id(), Challenges.getInstance().getChallengeTimer().getTime());
    nextItem();
    return skipped.displayName();
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onClick(@NotNull PlayerInventoryClickEvent event) {
    ItemStack item = event.getCurrentItem();
    if (item == null) return;
    handleNewItem(item, event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPickUp(@NotNull PlayerPickupItemEvent event) {
    handleNewItem(event.getItem().getItemStack(), event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onInteract(@NotNull PlayerInteractEvent event) {
    Player player = event.getPlayer();
    if (!shouldExecuteEffect(player)) return;
    // The held item may only change once the interaction has been processed (buckets, bottles, ...)
    Regions.later(plugin, player, () ->
      handleNewItem(player.getInventory().getItemInMainHand(), player), 1);
  }

  // A book with several enchantments collects every one of them
  private void handleNewItem(@NotNull ItemStack stack, @NotNull Player player) {
    for (CollectableItem item : CollectableItem.of(stack)) {
      handleNewItem(item, player);
    }
  }

  private void handleNewItem(@NotNull CollectableItem item, @NotNull Player player) {
    if (currentItem == null) return;
    boolean current = item.equals(currentItem);
    if (!current && (isRandomOrder() || collectedItems.contains(item) || !itemsToFind.contains(item))) return;
    if (!shouldExecuteEffect(player)) return;
    announceFind(FindType.ITEM, item.displayName(), player);
    SoundSample.PLING.broadcast();
    finders.put(item.id(), player.getUniqueId());
    foundAt.put(item.id(), Challenges.getInstance().getChallengeTimer().getTime());
    if (current) {
      nextItem();
    } else {
      // Found out of order: the seeded order skips it from now on, the current item stays
      itemsToFind.remove(item);
      collectedItems.add(item);
      foundOrder.add(item.id());
    }
    bossbar.update();
    updateRanking();
  }

  @Override
  public void getWinnersOnEnd(@NotNull List<Player> winners) {
  }

  @NotNull
  @Override
  protected Collection<UUID> getFinders() {
    List<UUID> inPool = new ArrayList<>();
    finders.forEach((id, finder) -> {
      if (allIds.contains(id)) inPool.add(finder);
    });
    return inPool;
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
    foundAt.clear();

    if (document.contains(FOUND_ITEMS_KEY)) {
      List<String> ids = document.getStringList(FOUND_ITEMS_KEY).stream().map(CollectAllItemsGoal::normalizeId).toList();
      if (!document.getBoolean(FOUND_IN_ORDER_KEY)) {
        // Written before the found order was kept, the list is in declaration order instead
        ids = sortBySeededOrder(ids, getSeededOrder());
      }
      readCollectedItems(ids);
      rebuildItemsToFind();
    } else {
      // Gamestates written before 2.4 only stored how many items were found, not which ones. Those
      // are the first n items of the order, which is reproducible as long as the item pool did not
      // change since the gamestate was written.
      rebuildItemsToFind();
      int foundCount = Math.clamp(document.getInt(FOUND_COUNT_KEY), 0, itemsToFind.size());
      readCollectedItems(itemsToFind.subList(0, foundCount).stream().map(CollectableItem::id).toList());
      rebuildItemsToFind(); // Now that the collected items are known, drop them from the order
    }

    Document finderDocument = document.getDocument(FINDERS_KEY);
    Document foundAtDocument = document.getDocument(FOUND_AT_KEY);
    for (String key : finderDocument.keys()) {
      UUID finder = finderDocument.getUUID(key);
      if (finder != null) finders.put(normalizeId(key), finder);
    }
    for (String key : foundAtDocument.keys()) {
      foundAt.put(normalizeId(key), foundAtDocument.getLong(key));
    }

    inheritBaseFinds();
    rebuildItemsToFind();
    // A version change reshuffles the remaining order, which must not swap the item players are looking for
    String savedCurrentId = document.getString(CURRENT_ITEM_KEY);
    CollectableItem savedCurrentItem = savedCurrentId == null ? null : CollectableItem.parse(savedCurrentId);
    currentItem = savedCurrentItem != null && itemsToFind.remove(savedCurrentItem) ? savedCurrentItem : pollNextItem();
  }

  /**
   * Restores the collected items by id instead of by position, so that progress survives a server
   * version change: neither reordered nor added nor removed items can shift an item into or out
   * of the collected items.
   */
  private void readCollectedItems(@NotNull List<String> ids) {
    for (String id : ids) {
      foundOrder.add(id);
      CollectableItem item = CollectableItem.parse(id);
      if (item != null) collectedItems.add(item);
    }
  }

  // Gamestates written before item variants existed key everything by material name
  @NotNull
  static String normalizeId(@NotNull String id) {
    CollectableItem item = CollectableItem.parse(id);
    return item == null ? id : item.id();
  }

  @Override
  public void writeGameState(@NotNull Document document) {
    super.writeGameState(document);

    document.set(SEED_KEY, seed);
    document.set(FOUND_ITEMS_KEY, foundOrder);
    document.set(FOUND_IN_ORDER_KEY, true);
    document.set(FOUND_COUNT_KEY, foundOrder.size()); // Only read by versions before 2.4
    document.set(CURRENT_ITEM_KEY, currentItem == null ? null : currentItem.id());
    Document finderDocument = document.getDocument(FINDERS_KEY);
    finders.forEach((name, uuid) -> finderDocument.set(name, uuid.toString()));
    Document foundAtDocument = document.getDocument(FOUND_AT_KEY);
    foundAt.forEach(foundAtDocument::set);
  }

  // In a random order the future order stays hidden: only the current item and the ones already found are listed
  @NotNull
  @Override
  public List<ChecklistEntry> getChecklist(@NotNull Player viewer) {
    if (!isRandomOrder()) {
      return allItemsToFind.stream()
        .map(item -> checklistEntry(item, collectedItems.contains(item)))
        .toList();
    }
    List<ChecklistEntry> entries = new ArrayList<>(foundOrder.size() + 1);
    if (currentItem != null) {
      entries.add(checklistEntry(currentItem, false));
    }
    for (String id : foundOrder.reversed()) {
      if (!allIds.contains(id)) continue;
      CollectableItem item = CollectableItem.parse(id);
      if (item != null) entries.add(checklistEntry(item, true));
    }
    return entries;
  }

  @NotNull
  private ChecklistEntry checklistEntry(@NotNull CollectableItem item, boolean done) {
    return new ChecklistEntry(item.displayName(), item.icon(), done,
      Optional.ofNullable(finders.get(item.id())).stream().toList(), foundAt.get(item.id()));
  }

  // Items are found strictly in the seeded order, so sorting by it restores the found order as long as the item pool did not change
  @NotNull
  static List<String> sortBySeededOrder(@NotNull List<String> ids, @NotNull List<CollectableItem> seededOrder) {
    Map<String, Integer> positions = new HashMap<>();
    for (int i = 0; i < seededOrder.size(); i++) {
      positions.put(seededOrder.get(i).id(), i);
    }
    return ids.stream().sorted(Comparator.comparingInt(id -> positions.getOrDefault(id, -1))).toList();
  }

  @NotNull
  @Override
  public MessageKey getFinderMessage() {
    return MessageKey.of("menu.goal.collected-by");
  }

}
