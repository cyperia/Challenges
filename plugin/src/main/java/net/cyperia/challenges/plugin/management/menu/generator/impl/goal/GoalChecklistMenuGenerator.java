package net.cyperia.challenges.plugin.management.menu.generator.impl.goal;

import net.cyperia.challenges.plugin.challenges.type.ChecklistGoal;
import net.cyperia.challenges.plugin.challenges.type.ChecklistGoal.ChecklistEntry;
import net.cyperia.challenges.plugin.challenges.type.helper.GoalLeaderboard;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.menu.generator.UncachedMultiPageSelectMenuGenerator;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class GoalChecklistMenuGenerator extends UncachedMultiPageSelectMenuGenerator<ChecklistEntry> {

  private static final int COLUMNS = 7;
  private static final int MAX_ROWS = 4;

  private final ChecklistGoal goal;
  private final Player viewer;
  private final int size;
  private final int[] slots;

  public GoalChecklistMenuGenerator(@NotNull ChecklistGoal goal, @NotNull Player viewer) {
    super(goal.getChecklist(viewer).stream()
      .sorted(Comparator.comparing(ChecklistEntry::done)) // stable, so each goal's own order holds within missing and done
      .toArray(ChecklistEntry[]::new));
    this.goal = goal;
    this.viewer = viewer;
    int rows = Math.clamp(Math.ceilDiv(elements.length, COLUMNS), 1, MAX_ROWS);
    this.size = (rows + 2) * 9;
    this.slots = calcSlots(rows, COLUMNS, size);
  }

  @NotNull
  public Set<UUID> getFinders() {
    return Arrays.stream(elements).flatMap(entry -> entry.finders().stream()).collect(Collectors.toSet());
  }

  @NotNull
  @Override
  public LocalizableMessage getMenuName() {
    return createSubMenuTitle(MenuType.GOAL.getMenuName(), goal.getName());
  }

  @Override
  public void handleElementClick(@NotNull ChecklistEntry element, @NotNull MenuClickInfo info) {
  }

  @NotNull
  @Override
  public ItemStack createDisplayItem(@NotNull ChecklistEntry element, @NotNull Locale locale) {
    return new ItemFormatter(element.icon())
      .name(MessageKey.of("menu.goal.entry").withArgs(element.name()))
      .suffix(MessageKey.of(element.done() ? "suffix.selected" : "suffix.unselected"))
      .description(element.finders().isEmpty() ? null : goal.getFinderMessage().withArgs(finderNames(element)))
      .build(locale);
  }

  // rank colors for online players and the viewer underlined, like the goal leaderboards
  @NotNull
  private Component finderNames(@NotNull ChecklistEntry element) {
    return Component.join(JoinConfiguration.commas(true), element.finders().stream()
      .map(Bukkit::getOfflinePlayer)
      .sorted(Comparator.comparing(player -> Objects.requireNonNullElse(player.getName(), ""), String.CASE_INSENSITIVE_ORDER))
      .map(player -> GoalLeaderboard.nameComponent(player, viewer))
      .toList());
  }

  @Override
  public int[] getSlots() {
    return slots;
  }

  @Override
  public int getInventorySize() {
    return size;
  }

  // /goal is open to every player, so the home button closes instead of opening the settings menu
  @NotNull
  @Override
  public GeneratorMenuPosition createMenuPosition(int page, @NotNull Player player) {
    return new UncachedMultiPageMenuPosition(page, player) {
      @Override
      public void handleClick(@NotNull MenuClickInfo info) {
        if (info.getSlot() != getNavigateHomeSlot()) {
          super.handleClick(info);
          return;
        }
        SoundSample.OPEN.play(info.getPlayer());
        info.getPlayer().closeInventory();
      }
    };
  }

}
