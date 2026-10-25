package net.cyperia.challenges.plugin.management.menu.generator.impl.challenge;

import java.util.function.Predicate;
import org.bukkit.permissions.Permissible;
import lombok.Getter;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.EntryAccess;
import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import net.cyperia.challenges.plugin.challenges.type.ReleaseInfo;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.files.ConfigManager;
import net.cyperia.challenges.plugin.management.menu.MenuManager;
import net.cyperia.challenges.plugin.management.menu.generator.VersionedContentContainer;
import net.cyperia.challenges.plugin.management.menu.info.ChallengeMenuClickInfo;
import net.cyperia.commons.bukkit.utils.item.ItemBuilder;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.common.config.Document;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ChallengeListMenuGenerator extends ChallengesMenuGenerator {

  public static final int SIZE = 4 * 9;
  public static final int[] DISPLAY_SLOTS = {10, 11, 12, 13, 14, 15, 16}; // settings at +9 each by default
  public static final int SLOT_OFFSET_UPPER = -9;
  public static final int SLOT_OFFSET_DISPLAY = 0;
  public static final int SLOT_OFFSET_SETTINGS = 9;
  public static final int SLOT_OFFSET_LOWER = 2 * 9;
  private static final int[] SLOT_OFFSETS = {SLOT_OFFSET_UPPER, SLOT_OFFSET_DISPLAY, SLOT_OFFSET_SETTINGS, SLOT_OFFSET_LOWER};

  public static final ItemStack UPDATED_CHALLENGE_ITEM = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE, Component.empty()).build();
  public static final ItemStack NEW_CHALLENGE_ITEM = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE, Component.empty()).build();
  public static final ItemStack BETA_CHALLENGE_ITEM = new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE, Component.empty()).build();

  protected final List<IChallenge> assignedChallengesCache = new ArrayList<>();

  @Getter
  protected final boolean newSuffix;
  @Getter
  protected final boolean updatedSuffix;
  protected final boolean displayNewInFront;
  protected final boolean displayUpdatedInFront;

  public ChallengeListMenuGenerator() {
    Document config = Challenges.getInstance().getConfigDocument();
    newSuffix = config.getBoolean(ConfigManager.Keys.NEW_SUFFIX);
    updatedSuffix = config.getBoolean(ConfigManager.Keys.UPDATED_SUFFIX);
    displayNewInFront = config.getBoolean(ConfigManager.Keys.NEW_IN_FRONT);
    displayUpdatedInFront = config.getBoolean(ConfigManager.Keys.UPDATED_IN_FRONT);
  }

  @Override
  public void addToCache(@NotNull IChallenge challenge) {
    int betaInFront = displayNewInFront ? getBetaCount() : 0;
    if (displayNewInFront && ReleaseInfo.isBeta(challenge)) {
      assignedChallengesCache.add(betaInFront, challenge);
    } else if (displayNewInFront && ReleaseInfo.isNew(challenge)) {
      assignedChallengesCache.add(betaInFront + getNewCount(), challenge);
    } else if (displayUpdatedInFront && ReleaseInfo.isUpdated(challenge)) {
      assignedChallengesCache.add(betaInFront + getNewCount() + getUpdatedCount(), challenge);
    } else {
      assignedChallengesCache.add(challenge);
    }
  }

  @Override
  public boolean isCached(@NotNull IChallenge challenge) {
    return assignedChallengesCache.contains(challenge);
  }

  @Override
  public int getCachedCount() {
    return assignedChallengesCache.size();
  }

  @Override
  public int getEnabledCount() {
    return (int) assignedChallengesCache.stream()
      .filter(IChallenge::isEnabled)
      .count();
  }

  @Override
  public int getModifiedCount() {
    return (int) assignedChallengesCache.stream()
      .filter(challenge -> !challenge.isDefaultSettings())
      .count();
  }

  @NotNull
  @Override
  public Optional<IChallenge> getFirstEnabledChallenge() {
    return assignedChallengesCache.stream()
      .filter(IChallenge::isEnabled)
      .findFirst();
  }

  @Override
  public void removeFromCache(@NotNull IChallenge challenge) {
    assignedChallengesCache.remove(challenge);
  }

  @Override
  public void resetCache() {
    assignedChallengesCache.clear();
  }

  @NotNull
  @Override
  public GeneratorMenuPosition createMenuPosition(int page, @NotNull Player player) {
    return new ChallengeListMenuPosition(page);
  }

  /**
   * @param slotOffset See {@link #SLOT_OFFSETS}
   * @return Whether an action can be executed at this offset (otherwise fallback behavior/sound will be played)
   */
  public boolean executeChallengeClickAction(@NotNull IChallenge challenge, int slotOffset, @NotNull MenuClickInfo info) {
    if (slotOffset == SLOT_OFFSET_DISPLAY || slotOffset == SLOT_OFFSET_SETTINGS) {
      challenge.handleClick(new ChallengeMenuClickInfo(info, slotOffset == SLOT_OFFSET_DISPLAY));
      return true;
    }
    return false;
  }

  @Override
  public void updateInventoryContent(@NotNull Inventory inventory, int page, @NotNull Player viewer) {
    Locale locale = localeOf(viewer);
    List<IChallenge> challengesForPage = getChallengesForPage(viewer, page);
    for (int i = 0; i < challengesForPage.size(); i++) {
      IChallenge challenge = challengesForPage.get(i);
      if (EntryAccess.isLocked(challenge, viewer)) {
        setLockedChallengeItemsAt(challenge, inventory, i, locale);
      } else {
        setChallengeItemsAt(challenge, inventory, i, locale);
      }
      setChallengeUpdateItemsAt(challenge, inventory, i, page, viewer);
    }
  }

  // Where a challenge sits differs between viewers once some are hidden, so every open page is drawn again
  @Override
  public void updateElementDisplay(@NotNull IChallenge challenge) {
    if (assignedChallengesCache.contains(challenge)) {
      updatePages();
    }
  }

  protected void setChallengeUpdateItemsAt(@NotNull IChallenge challenge, @NotNull Inventory inventory, int slotIndex, int page, @NotNull Player viewer) {
    ItemStack pane = releasePane(challenge, newSuffix, updatedSuffix);
    if (pane == null) return;
    setItemSafely(inventory, DISPLAY_SLOTS[slotIndex] + SLOT_OFFSET_UPPER, pane, page, viewer);
    setItemSafely(inventory, DISPLAY_SLOTS[slotIndex] + SLOT_OFFSET_LOWER, pane, page, viewer);
  }

  // The pane above and below an entry that marks it beta, new or updated
  @Nullable
  public static ItemStack releasePane(@NotNull Object entry, boolean newSuffix, boolean updatedSuffix) {
    if (ReleaseInfo.isBeta(entry)) return BETA_CHALLENGE_ITEM;
    if (newSuffix && ReleaseInfo.isNew(entry)) return NEW_CHALLENGE_ITEM;
    if (updatedSuffix && ReleaseInfo.isUpdated(entry)) return UPDATED_CHALLENGE_ITEM;
    return null;
  }

  protected void setLockedChallengeItemsAt(@NotNull IChallenge challenge, @NotNull Inventory inventory, int slotsIndex, @NotNull Locale locale) {
    MessageKey locked = MessageKey.of("menu.restricted.state");
    inventory.setItem(DISPLAY_SLOTS[slotsIndex] + SLOT_OFFSET_DISPLAY, challenge.getDisplayItem().state(locked).build(locale));
    inventory.setItem(DISPLAY_SLOTS[slotsIndex] + SLOT_OFFSET_SETTINGS, challenge.getSettingsItem().state(locked).build(locale));
  }

  protected void setChallengeItemsAt(@NotNull IChallenge challenge, @NotNull Inventory inventory, int slotsIndex, @NotNull Locale locale) {
    inventory.setItem(DISPLAY_SLOTS[slotsIndex] + SLOT_OFFSET_DISPLAY, createChallengeDisplayItem(challenge, locale));
    inventory.setItem(DISPLAY_SLOTS[slotsIndex] + SLOT_OFFSET_SETTINGS, createChallengeSettingsItem(challenge, locale));
  }

  @NotNull
  protected ItemStack createChallengeDisplayItem(@NotNull IChallenge challenge, @NotNull Locale locale) {
    return challenge.getDisplayItem().build(locale);
  }

  @NotNull
  protected ItemStack createChallengeSettingsItem(@NotNull IChallenge challenge, @NotNull Locale locale) {
    return challenge.getSettingsItem().build(locale);
  }

  // Every challenge of this menu the viewer may see, in menu order
  @NotNull
  public List<IChallenge> getListedChallenges(@NotNull Player viewer) {
    return assignedChallengesCache.stream().filter(challenge -> challenge.isVisibleTo(viewer)).toList();
  }

  @NotNull
  protected List<IChallenge> getChallengesForPage(@NotNull Player viewer, int page) {
    List<IChallenge> listed = getListedChallenges(viewer);
    int startIndex = page * DISPLAY_SLOTS.length;
    int endIndex = Math.min(startIndex + DISPLAY_SLOTS.length, listed.size());
    return startIndex >= endIndex ? List.of() : listed.subList(startIndex, endIndex);
  }

  public int getDisplaySlotOfChallenge(@NotNull Player viewer, @NotNull IChallenge challenge) {
    int index = getListedChallenges(viewer).indexOf(challenge);
    if (index == -1) return -1; // challenge not registered here?
    return DISPLAY_SLOTS[index % DISPLAY_SLOTS.length];
  }

  public int getPageOfChallenge(@NotNull Player viewer, @NotNull IChallenge challenge) {
    int index = getListedChallenges(viewer).indexOf(challenge);
    if (index == -1) return -1; // challenge not registered here?
    return index / DISPLAY_SLOTS.length;
  }

  @Override
  public void openPageOf(@NotNull Player player, @NotNull IChallenge challenge) {
    int page = getPageOfChallenge(player, challenge);
    if (page == -1) return; // challenge not registered here?
    openMenu(player, page);
  }

  @Nullable
  @Override
  public ChallengeListMenuGenerator getChallengeListMenuGenerator(@NotNull IChallenge challenge) {
    if (!assignedChallengesCache.contains(challenge)) return null;
    return this;
  }

  @Override
  public int getPageCount(@NotNull Player viewer) {
    // must have at least one page!
    return Math.max(1, Math.ceilDiv(getListedChallenges(viewer).size(), DISPLAY_SLOTS.length));
  }

  @Override
  public int getNewCount(@NotNull Permissible viewer) {
    return countListed(viewer, ReleaseInfo::isNew);
  }

  @Override
  public int getUpdatedCount(@NotNull Permissible viewer) {
    return countListed(viewer, ReleaseInfo::isUpdated);
  }

  @Override
  public int getBetaCount(@NotNull Permissible viewer) {
    return countListed(viewer, ReleaseInfo::isBeta);
  }

  private int countListed(@NotNull Permissible viewer, @NotNull Predicate<IChallenge> filter) {
    return (int) assignedChallengesCache.stream()
      .filter(challenge -> filter.test(challenge) && EntryAccess.isListed(challenge, viewer))
      .count();
  }

  @Override
  public int getNewCount() {
    return VersionedContentContainer.Helper.countNew(displayNewInFront, displayNewInFront ? getBetaCount() : 0, assignedChallengesCache);
  }

  @Override
  public int getBetaCount() {
    return VersionedContentContainer.Helper.countBeta(assignedChallengesCache);
  }

  @Override
  public int getUpdatedCount() {
    int firstUpdatedIndex = displayNewInFront ? getBetaCount() + getNewCount() : 0;
    return VersionedContentContainer.Helper.countUpdated(displayUpdatedInFront, firstUpdatedIndex, assignedChallengesCache);
  }

  @Override
  public int getInventorySize() {
    return SIZE;
  }

  protected int findSlotOffset(int clickedSlot, int checkForSlot) {
    for (int offset : SLOT_OFFSETS) {
      if (clickedSlot == checkForSlot + offset) {
        return offset;
      }
    }
    return -1;
  }

  public class ChallengeListMenuPosition extends GeneratorMenuPosition {

    public ChallengeListMenuPosition(int page) {
      super(page);
    }

    @Override
    public boolean handleMenuClick(@NotNull MenuClickInfo info) {
      for (int i = 0; i < DISPLAY_SLOTS.length; i++) {
        int slot = DISPLAY_SLOTS[i];
        int offset = findSlotOffset(info.getSlot(), slot);
        if (offset == -1) continue;

        List<IChallenge> challengesForPage = getChallengesForPage(info.getPlayer(), page);
        if (i >= challengesForPage.size()) return false; // page not full

        IChallenge challenge = challengesForPage.get(i);
        MenuManager menuManager = Challenges.getInstance().getMenuManager();
        if (menuManager.denySettingsChange(info.getPlayer()) || menuManager.denyLockedChange(info.getPlayer(), challenge)) return true;
        return executeChallengeClickAction(challenge, offset, info);
      }

      return false;
    }
  }

}
