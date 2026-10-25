package net.cyperia.challenges.plugin.management.menu.generator.impl.challenge.categorised;

import org.bukkit.permissions.Permissible;
import net.cyperia.challenges.plugin.challenges.type.IChallenge;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.SettingCategory;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.menu.generator.impl.challenge.ChallengeListMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.impl.challenge.ChallengesMenuGenerator;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CategorisedMenuGenerator extends ChallengesMenuGenerator {

  public static final int SIZE = 4 * 9;
  public static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
  public static final int MAX_ITEMS_PER_ROW = 7;

  protected final Map<SettingCategory, CategorisedListMenuGenerator> categoryGenerators = new HashMap<>();
  protected final List<SettingCategory> orderedCategories = new ArrayList<>();

  @Override
  public void addToCache(@NotNull IChallenge challenge) {
    CategorisedListMenuGenerator generator = categoryGenerators.computeIfAbsent(challenge.getCategory(), forCategory -> {
      orderedCategories.add(forCategory);
      orderedCategories.sort(Comparator.comparingInt(SettingCategory::getPriority));
      return new CategorisedListMenuGenerator(menuType, forCategory);
    });
    generator.addToCache(challenge);
  }

  @Override
  public void removeFromCache(@NotNull IChallenge challenge) {
    CategorisedListMenuGenerator generator = categoryGenerators.get(challenge.getCategory());
    if (generator != null) {
      generator.removeFromCache(challenge);
    }
  }

  @Override
  public void resetCache() {
    for (CategorisedListMenuGenerator generator : categoryGenerators.values()) {
      generator.resetCache();
    }
  }

  @Override
  public int getCachedCount() {
    int count = 0;
    for (CategorisedListMenuGenerator generator : categoryGenerators.values()) {
      count += generator.getCachedCount();
    }
    return count;
  }

  @Override
  public int getEnabledCount() {
    int count = 0;
    for (CategorisedListMenuGenerator generator : categoryGenerators.values()) {
      count += generator.getEnabledCount();
    }
    return count;
  }

  @Override
  public int getModifiedCount() {
    int count = 0;
    for (CategorisedListMenuGenerator generator : categoryGenerators.values()) {
      count += generator.getModifiedCount();
    }
    return count;
  }

  @NotNull
  @Override
  public Optional<IChallenge> getFirstEnabledChallenge() {
    for (CategorisedListMenuGenerator generator : categoryGenerators.values()) {
      Optional<IChallenge> challenge = generator.getFirstEnabledChallenge();
      if (challenge.isPresent()) {
        return challenge;
      }
    }
    return Optional.empty();
  }

  @Override
  public boolean isCached(@NotNull IChallenge challenge) {
    CategorisedListMenuGenerator generator = categoryGenerators.get(challenge.getCategory());
    return generator != null && generator.isCached(challenge);
  }

  @Override
  public void updateElementDisplay(@NotNull IChallenge challenge) {
    SettingCategory category = challenge.getCategory();
    CategorisedListMenuGenerator generator = categoryGenerators.get(category);
    if (generator != null) {
      generator.updateElementDisplay(challenge);
      updatePages();
    }
  }

  @Override
  public void updateInventoryContent(@NotNull Inventory inventory, int page, @NotNull Player viewer) {
    int[] slots = getSlots();
    List<SettingCategory> categories = getCategoriesForPage(viewer, page, slots);
    int len = categories.size();
    for (int i = 0; i < len; i++) {
      SettingCategory category = categories.get(i);
      setCategoryDisplayAt(category, inventory, slots, i, viewer);
    }
  }

  private void setCategoryDisplayAt(@NotNull SettingCategory category, @NotNull Inventory inventory, int[] slots, int slotIndex, @NotNull Player viewer) {
    inventory.setItem(slots[slotIndex], createCategoryDisplayItem(viewer, category));
    setCategoryUpdateItemsAt(category, inventory, slots, slotIndex, viewer);
  }

  private void setCategoryUpdateItemsAt(@NotNull SettingCategory category, @NotNull Inventory inventory, int[] slots, int slotIndex, @NotNull Player viewer) {
    CategorisedListMenuGenerator generator = categoryGenerators.get(category);
    if (generator != null) {
      // This will break if there are more than 2 rows!
      int row = slotIndex / getMaxItemsPerRow();
      int offset = row == 0 ? -9 : +9;
      if (generator.getBetaCount(viewer) > 0) {
        inventory.setItem(slots[slotIndex] + offset, ChallengeListMenuGenerator.BETA_CHALLENGE_ITEM);
      } else if (generator.isNewSuffix() && generator.getNewCount(viewer) > 0) {
        inventory.setItem(slots[slotIndex] + offset, ChallengeListMenuGenerator.NEW_CHALLENGE_ITEM);
      } else if (generator.isUpdatedSuffix() && generator.getUpdatedCount(viewer) > 0) {
        inventory.setItem(slots[slotIndex] + offset, ChallengeListMenuGenerator.UPDATED_CHALLENGE_ITEM);
      }
    }
  }

  @NotNull
  protected ItemStack createCategoryDisplayItem(@NotNull Player viewer, @NotNull SettingCategory category) {
    ItemFormatter item = new ItemFormatter()
      .item(category.getDisplayItemPreset())
      .name(category.getName())
      .description(category.getDescription())
      .tooltip(MenuClickType.LEFT, Interaction.OPEN);

    CategorisedListMenuGenerator generator = categoryGenerators.get(category);
    if (generator != null) {
      item.suffixIfNewUpdated(generator, viewer);

      item.state(generator.formatInfo(category.getDescriptionInfo()));
    }

    return item.build(localeOf(viewer));
  }

  // A category of which the viewer may see nothing is left out for them
  @NotNull
  protected List<SettingCategory> getListedCategories(@NotNull Player viewer) {
    return orderedCategories.stream()
      .filter(category -> !categoryGenerators.get(category).getListedChallenges(viewer).isEmpty())
      .toList();
  }

  @NotNull
  protected List<SettingCategory> getCategoriesForPage(@NotNull Player viewer, int page, @NotNull int[] slots) {
    List<SettingCategory> listed = getListedCategories(viewer);
    int startIndex = page * slots.length;
    int endIndex = Math.min(startIndex + slots.length, listed.size());
    return startIndex >= endIndex ? List.of() : listed.subList(startIndex, endIndex);
  }

  @NotNull
  @Override
  public GeneratorMenuPosition createMenuPosition(int page, @NotNull Player player) {
    return new CategorisedOverviewMenuPosition(page);
  }

  @Override
  public int getNewCount() {
    int count = 0;
    for (CategorisedListMenuGenerator menuGenerator : categoryGenerators.values()) {
      count += menuGenerator.getNewCount();
    }
    return count;
  }

  @Override
  public int getBetaCount() {
    int count = 0;
    for (CategorisedListMenuGenerator menuGenerator : categoryGenerators.values()) {
      count += menuGenerator.getBetaCount();
    }
    return count;
  }

  @Override
  public int getUpdatedCount() {
    int count = 0;
    for (CategorisedListMenuGenerator menuGenerator : categoryGenerators.values()) {
      count += menuGenerator.getUpdatedCount();
    }
    return count;
  }

  @Override
  public int getNewCount(@NotNull Permissible viewer) {
    return categoryGenerators.values().stream().mapToInt(generator -> generator.getNewCount(viewer)).sum();
  }

  @Override
  public int getBetaCount(@NotNull Permissible viewer) {
    return categoryGenerators.values().stream().mapToInt(generator -> generator.getBetaCount(viewer)).sum();
  }

  @Override
  public int getUpdatedCount(@NotNull Permissible viewer) {
    return categoryGenerators.values().stream().mapToInt(generator -> generator.getUpdatedCount(viewer)).sum();
  }

  @Override
  public void openPageOf(@NotNull Player player, @NotNull IChallenge challenge) {
    SettingCategory category = challenge.getCategory();
    CategorisedListMenuGenerator generator = categoryGenerators.get(category);
    if (generator == null) return; // should never happen
    generator.openPageOf(player, challenge);
  }

  public void openMenu(@NotNull Player player, @NotNull SettingCategory category) {
    CategorisedListMenuGenerator generator = categoryGenerators.get(category);
    if (generator != null) {
      generator.openMenu(player, 0);
    }
  }

  @Nullable
  @Override
  public ChallengeListMenuGenerator getChallengeListMenuGenerator(@NotNull IChallenge challenge) {
    return categoryGenerators.get(challenge.getCategory());
  }

  @NotNull
  public int[] getSlots() {
    return SLOTS;
  }

  public int getMaxItemsPerRow() {
    return MAX_ITEMS_PER_ROW;
  }

  @Override
  public int getInventorySize() {
    return SIZE;
  }

  @Override
  public int getPageCount(@NotNull Player viewer) {
    return Math.max(1, Math.ceilDiv(getListedCategories(viewer).size(), getSlots().length));
  }

  public class CategorisedOverviewMenuPosition extends GeneratorMenuPosition {

    public CategorisedOverviewMenuPosition(int page) {
      super(page);
    }

    @Override
    public boolean handleMenuClick(@NotNull MenuClickInfo info) {
      int[] slots = getSlots();
      for (int i = 0; i < slots.length; i++) {
        if (info.getSlot() != slots[i]) continue;

        List<SettingCategory> categories = getCategoriesForPage(info.getPlayer(), page, slots);
        if (i >= categories.size()) return false; // empty slot; page not full
        SettingCategory category = categories.get(i);
        CategorisedListMenuGenerator generator = categoryGenerators.get(category);
        if (generator != null) {
          generator.openMenu(info.getPlayer(), 0);
          SoundSample.CLICK.play(info.getPlayer());
        }
        return true;
      }

      return false;
    }
  }
}
