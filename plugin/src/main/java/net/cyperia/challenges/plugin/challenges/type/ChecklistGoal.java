package net.cyperia.challenges.plugin.challenges.type;

import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.commons.bukkit.utils.item.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ChecklistGoal extends IGoal {

  @NotNull
  List<ChecklistEntry> getChecklist(@NotNull Player viewer);

  // Parts of the checklist the menu can show on their own, empty for a goal with one kind of entry
  @NotNull
  default List<ChecklistCategory> getChecklistCategories(@NotNull Player viewer) {
    return List.of();
  }

  // {0} is the names of the players who got an entry, like "Killed by {0}"
  @NotNull
  MessageKey getFinderMessage();

  /**
   * @param foundAt timer time in seconds when the entry was done, {@code null} if not done or not known
   */
  record ChecklistEntry(@NotNull Object name, @NotNull ItemStack icon, boolean done, @NotNull List<UUID> finders,
                        @Nullable Long foundAt) {

    public ChecklistEntry(@NotNull Object name, @NotNull ItemStack icon, boolean done, @NotNull List<UUID> finders) {
      this(name, icon, done, finders, null);
    }

    @NotNull
    public static ChecklistEntry of(@NotNull Object value, boolean done, @NotNull Collection<UUID> finders) {
      return of(value, done, finders, null);
    }

    @NotNull
    public static ChecklistEntry of(@NotNull Object value, boolean done, @NotNull Collection<UUID> finders, @Nullable Long foundAt) {
      return new ChecklistEntry(value, new ItemStack(ItemUtils.getIcon(value)), done, List.copyOf(finders), foundAt);
    }

  }

  record ChecklistCategory(@NotNull LocalizableMessage name, @NotNull ItemStack icon, @NotNull List<ChecklistEntry> entries) {

    public long doneCount() {
      return entries.stream().filter(ChecklistEntry::done).count();
    }

  }

}
