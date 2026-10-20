package net.cyperia.challenges.plugin.management.menu.generator.impl.search;

import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogRegistryEntry;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import lombok.RequiredArgsConstructor;
import net.cyperia.challenges.plugin.Challenges;
import net.cyperia.challenges.plugin.challenges.type.annotation.Since;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.impl.format.ComponentArguments;
import net.cyperia.challenges.plugin.content.i18n.impl.format.rewrite.ComponentJoiner;
import net.cyperia.challenges.plugin.management.menu.format.Interaction;
import net.cyperia.challenges.plugin.management.menu.format.InteractionTooltips;
import net.cyperia.challenges.plugin.management.menu.format.ItemFormatter;
import net.cyperia.challenges.plugin.management.menu.generator.DialogMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.generator.types.ISearchMenuGenerator;
import net.cyperia.challenges.plugin.management.menu.search.SearchFindable;
import net.cyperia.commons.bukkit.utils.animation.SoundSample;
import net.cyperia.commons.bukkit.utils.menu.MenuClickType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Since("3.0")
public class SearchDialogMenuGenerator extends DialogMenuGenerator<String> implements ISearchMenuGenerator {

  private static final String QUERY_KEY = "query";

  private static final int COLUMNS = 1;        // default is 2
  private static final int BUTTON_WIDTH = 220; // 1..1024, default 150
  private static final int SPACER_WIDTH = 1;   // see buildSpacer()
  private static final int RESULT_LIMIT = 25;
  private static final int MIN_QUERY_LENGTH = 2;
  private static final int MAX_INPUT_LENGTH = 20;

  private static final ClickCallback.Options CALLBACK_OPTIONS = ClickCallback.Options.builder()
    .uses(ClickCallback.UNLIMITED_USES) // dialog is rebuilt per search, but stys unchanged if query stay the same
    .lifetime(Duration.ofMinutes(10)) // don't hold abandoned dialogs for the 12h default
    .build();

  private static final ClickCallback.Options RESULT_CALLBACK_OPTIONS = ClickCallback.Options.builder()
    .uses(ClickCallback.UNLIMITED_USES) // the dialog stays open, so results must survive a click
    .lifetime(Duration.ofMinutes(10))
    .build();

  @Override
  public void openMenu(@NotNull Player player, @NonNull String state) {
    Challenges.getInstance().runAsync(() -> {
      // search is potentially expensive, so we do it off the main thread; the dialog is rebuilt
      // with the results when the search completes
      super.openMenu(player, state);
    });
  }

  @NotNull
  @Override
  protected String createDefaultState(@NotNull Player player) {
    return "";
  }

  @Override
  protected void setupDialog(@NotNull DialogRegistryEntry.Builder dialog, @NotNull String query, @NotNull Locale locale) {
    query = query.trim();
    // the state may come from outside the dialog (e.g. /search <query>), where nothing enforces the
    // limit of the input field; an initial value longer than maxLength fails to serialize
    if (query.length() > MAX_INPUT_LENGTH) query = query.substring(0, MAX_INPUT_LENGTH);

    List<SearchFindable> matches = findResults(query, locale);
    List<SearchFindable> results = matches.stream().limit(RESULT_LIMIT).toList();

    // search button first -> it lands in the first grid cell, directly under the input
    List<ActionButton> actions = new ArrayList<>();
    actions.add(buildSearchButton(locale, query));

    if (!results.isEmpty()) {
      addSpacerRow(actions);
      for (SearchFindable result : results) {
        actions.add(buildResultButton(result, query, locale));
      }
    }

    dialog
      .type(DialogType.multiAction(actions)
        .columns(COLUMNS)
        .exitAction(ActionButton.builder(MessageKey.of("menu.search.close").asComponent(locale))
          .tooltip(ComponentJoiner.join(new ItemFormatter()
            .name(MessageKey.of("menu.search.close"))
            .tooltip(MenuClickType.LEFT, Interaction.CLOSE_MENU)
            .buildLines(locale)
          ))
          .width(BUTTON_WIDTH)
          .action(DialogAction.staticAction(ClickEvent.callback(audience -> {
            if (audience instanceof Player player) player.closeDialog();
          })))
          .build())
        .build())
      .base(DialogBase.builder(MessageKey.of("menu.search.title").asComponent(locale))
        .afterAction(DialogBase.DialogAfterAction.NONE) // keep the screen; we replace it ourselves
        .pause(false)                                   // NONE never unpauses -> paper rejects it while pause is on
        .body(buildBody(query, results.size(), matches.size(), locale)) // renders above the inputs
        .inputs(List.of(
          DialogInput.text(QUERY_KEY, Component.empty())
            .maxLength(MAX_INPUT_LENGTH)
            .width(COLUMNS * BUTTON_WIDTH) // line the field up with the button grid
            .initial(query)                // carry the query over, it resets otherwise
            .labelVisible(false)
            .build()
        ))
        .build());
  }

  /**
   * The search status. Body is always rendered above the inputs, so this sits on top of the search
   * field while the results themselves are the action grid below it.
   *
   * @param shown how many results the action grid actually got
   * @param total how many results the query matched in the first place
   */
  @NotNull
  private List<DialogBody> buildBody(@NotNull String query, int shown, int total, @NotNull Locale locale) {
    List<DialogBody> body = new ArrayList<>();
    body.add(buildBodyLine(buildResultCount(query, total, shown, locale)));
    return body;
  }

  @NotNull
  private DialogBody buildBodyLine(@NotNull Component contents) {
    return DialogBody.plainMessage(contents, COLUMNS * BUTTON_WIDTH);
  }

  @NotNull
  private Component buildResultCount(@NotNull String query, int total, int shown, @NotNull Locale locale) {
    if (query.length() < MIN_QUERY_LENGTH) {
      return MessageKey.of("menu.search.results.min_chars").asComponent(locale, MIN_QUERY_LENGTH);
    }
    if (total == 0) {
      return MessageKey.of("menu.search.results.none").asComponent(locale);
    }
    if (shown < total) {
      return MessageKey.of("menu.search.results.count_shown").asComponent(locale, total, MessageKey.pluralize("generic.result", total), shown);
    }
    return MessageKey.of("menu.search.results.count").asComponent(locale, total, MessageKey.pluralize("generic.result", total));
  }

  @NotNull
  private ActionButton buildSearchButton(@NotNull Locale locale, @NotNull String currentQuery) {
    return ActionButton.builder(MessageKey.of("menu.search.search").asComponent(locale))
      .tooltip(ComponentJoiner.join(new ItemFormatter()
        .name(MessageKey.of("menu.search.search"))
        .tooltip(MenuClickType.LEFT, Interaction.SEARCH)
        .buildLines(locale)
      ))
      .width(BUTTON_WIDTH)
      .action(DialogAction.customClick(new SearchButtonCallbackImpl(currentQuery), CALLBACK_OPTIONS))
      .build();
  }

  /**
   * Fills up the current grid row and appends a blank one. Dialogs have no padding, margin or
   * separator fields, so the only thing that fits between two rows of the action grid is another
   * grid cell.
   */
  private void addSpacerRow(@NotNull List<ActionButton> actions) {
    while (actions.size() % COLUMNS != 0) actions.add(buildSpacer());
    for (int column = 0; column < COLUMNS; column++) actions.add(buildSpacer());
  }

  @NotNull
  private ActionButton buildSpacer() {
    return ActionButton.builder(Component.empty())
      .width(SPACER_WIDTH)
      .build();
  }

  @NotNull
  private ActionButton buildResultButton(@NotNull SearchFindable findable, @NotNull String query, @NotNull Locale locale) {
    return ActionButton.builder(Component.text()
        .append(ComponentArguments.getItemSpriteOrEmpty(findable.getDisplayItemPreset()))
        .append(findable.getName().asComponent(locale))
        .build())
      .width(BUTTON_WIDTH)
      .tooltip(ComponentJoiner.join(new ItemFormatter()
        .name(findable.getName())
        .description(findable.getDescription())
        .tooltips(new InteractionTooltips().on(MenuClickType.LEFT, findable.getFindableInteraction()))
        .state(MessageKey.of("menu.search.entry.type").withArgs(findable.getCategory().getSearchCategoryName()))
        .buildLines(locale)))
      .action(DialogAction.customClick((_, audience) -> {
        if (!(audience instanceof Player player)) return;
        // results outlive the search that built them, so a findable that hid itself since then is
        // not opened; the query is run again instead, which rebuilds the dialog without it
        if (!findable.isVisible()) {
          SoundSample.BASS_OFF.play(player);
          openMenu(player, query);
          return;
        }
        findable.showFindable(player);
      }, RESULT_CALLBACK_OPTIONS))
      .build();
  }

  /**
   * @return every match, unlimited; the caller cuts it down to {@link #RESULT_LIMIT} and keeps the
   * full size around for the result count. Empty below {@link #MIN_QUERY_LENGTH} characters,
   * so a short query never dumps the whole challenge list.
   */
  @NotNull
  private List<SearchFindable> findResults(@NotNull String query, @NotNull Locale locale) {
    if (query.length() < MIN_QUERY_LENGTH) return List.of();
    return Challenges.getInstance().getSearchRegistry().search(query, locale);
  }

  @RequiredArgsConstructor
  public class SearchButtonCallbackImpl implements DialogActionCallback {

    private final String currentQuery;
    private volatile boolean clicked = false;

    @Override
    public void accept(@NotNull DialogResponseView response, @NotNull Audience audience) {
      if (clicked) return; // prevent spam click as button is reusable
      if (!(audience instanceof Player player)) return;
      String query = response.getText(QUERY_KEY);
      String effectiveQuery = query == null ? "" : query;
      if (effectiveQuery.equals(currentQuery)) return; // no change -> no rebuild
      clicked = true;
      openMenu(player, effectiveQuery);
    }
  }

}
