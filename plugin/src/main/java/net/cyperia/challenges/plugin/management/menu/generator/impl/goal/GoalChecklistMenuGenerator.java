package net.cyperia.challenges.plugin.management.menu.generator.impl.goal;

import org.jetbrains.annotations.Nullable;
import net.cyperia.challenges.plugin.ChallengeAPI;
import net.cyperia.challenges.plugin.challenges.type.ChecklistGoal;
import net.cyperia.challenges.plugin.challenges.type.ChecklistGoal.ChecklistEntry;
import net.cyperia.challenges.plugin.challenges.type.helper.GoalLeaderboard;
import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.management.menu.MenuType;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.menu.generator.MultiPageSelectMenuGenerator;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import org.bukkit.inventory.Inventory;
import org.bukkit.Material;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class GoalChecklistMenuGenerator extends MultiPageSelectMenuGenerator<ChecklistEntry> {

  private static final int COLUMNS = 7;
  private static final int MAX_ROWS = 4;
  private static final Map<GoalChecklistView.Sort, Material> SORT_ITEMS = Map.of(
    GoalChecklistView.Sort.DEFAULT, Material.REPEATER,
    GoalChecklistView.Sort.TIME, Material.CLOCK,
    GoalChecklistView.Sort.FINDER, Material.PLAYER_HEAD);
  private static final Map<GoalChecklistView.Filter, Material> FILTER_ITEMS = Map.of(
    GoalChecklistView.Filter.ALL, Material.ENDER_EYE,
    GoalChecklistView.Filter.MISSING, Material.GRAY_DYE,
    GoalChecklistView.Filter.FOUND, Material.LIME_DYE);

  // Kept while the server runs, so /goal opens the way it was left
  private static final Map<UUID, GoalChecklistView> views = new ConcurrentHashMap<>();

  private final ChecklistGoal goal;
  private final Player viewer;
  private final GoalChecklistView view;
  private final int size;
  private final int[] slots;

  public GoalChecklistMenuGenerator(@NotNull ChecklistGoal goal, @NotNull Player viewer) {
    this(goal, viewer, views.getOrDefault(viewer.getUniqueId(), GoalChecklistView.DEFAULT));
  }

  private GoalChecklistMenuGenerator(@NotNull ChecklistGoal goal, @NotNull Player viewer, @NotNull GoalChecklistView view) {
    super(view.apply(goal.getChecklist(viewer), uuid -> Bukkit.getOfflinePlayer(uuid).getName())
      .toArray(ChecklistEntry[]::new));
    this.goal = goal;
    this.viewer = viewer;
    this.view = view;
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
      .description(describe(element))
      .build(locale);
  }

  @Nullable
  private LocalizableMessage describe(@NotNull ChecklistEntry element) {
    LocalizableMessage finders = element.finders().isEmpty() ? null : goal.getFinderMessage().withArgs(finderNames(element));
    if (element.foundAt() == null) return finders;
    LocalizableMessage time = ChallengeAPI.formatTime(element.foundAt());
    return finders == null
      ? MessageKey.of("menu.goal.found-at").withArgs(time)
      : MessageKey.of("menu.goal.found-by-at").withArgs(finders, time);
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
  public void updateInventoryContent(@NotNull Inventory inventory, int page, @NotNull Player viewer) {
    super.updateInventoryContent(inventory, page, viewer);
    Locale locale = localeOf(viewer);
    String sort = view.sort().name().toLowerCase(Locale.ROOT);
    inventory.setItem(getSortSlot(), new ItemFormatter(SORT_ITEMS.get(view.sort()))
      .name(MessageKey.of("menu.goal.view.sort." + sort))
      .tooltip(MenuClickType.LEFT, MessageKey.of("menu.goal.view.switch-sort").withArgs(
        MessageKey.of("menu.goal.view.sort-name." + view.sort().next().name().toLowerCase(Locale.ROOT))))
      .build(locale));
    inventory.setItem(getDirectionSlot(), new ItemFormatter(view.descending() ? Material.HOPPER : Material.DROPPER)
      .name(MessageKey.of(view.descending() ? "menu.goal.view.descending" : "menu.goal.view.ascending"))
      .tooltip(MenuClickType.LEFT, MessageKey.of(view.descending() ? "menu.goal.view.switch-ascending" : "menu.goal.view.switch-descending"))
      .build(locale));
    String filter = view.filter().name().toLowerCase(Locale.ROOT);
    inventory.setItem(getFilterSlot(), new ItemFormatter(FILTER_ITEMS.get(view.filter()))
      .name(MessageKey.of("menu.goal.view.filter." + filter))
      .tooltip(MenuClickType.LEFT, MessageKey.of("menu.goal.view.switch-filter").withArgs(
        MessageKey.of("menu.goal.view.filter-name." + view.filter().next().name().toLowerCase(Locale.ROOT))))
      .build(locale));
  }

  private int getSortSlot() {
    return getInventorySize() - 6;
  }

  private int getDirectionSlot() {
    return getInventorySize() - 5;
  }

  private int getFilterSlot() {
    return getInventorySize() - 4;
  }

  private void changeView(@NotNull Player player, @NotNull GoalChecklistView changed) {
    views.put(player.getUniqueId(), changed);
    SoundSample.CLICK.play(player);
    new GoalChecklistMenuGenerator(goal, player, changed).openMenu(player, 0);
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
    return new MultiPageSelectMenuPosition(page, player) {
      @Override
      public void handleClick(@NotNull MenuClickInfo info) {
        Player player = info.getPlayer();
        int slot = info.getSlot();
        if (slot == getSortSlot()) {
          changeView(player, new GoalChecklistView(view.filter(), view.sort().next(), view.descending()));
        } else if (slot == getDirectionSlot()) {
          changeView(player, new GoalChecklistView(view.filter(), view.sort(), !view.descending()));
        } else if (slot == getFilterSlot()) {
          changeView(player, new GoalChecklistView(view.filter().next(), view.sort(), view.descending()));
        } else if (slot == getNavigateHomeSlot()) {
          SoundSample.OPEN.play(player);
          player.closeInventory();
        } else {
          super.handleClick(info);
        }
      }
    };
  }

}
