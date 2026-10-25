package net.cyperia.challenges.plugin.management.menu.search;

import net.cyperia.challenges.plugin.content.i18n.LocalizableMessage;
import net.cyperia.challenges.plugin.content.i18n.MessageKey;
import net.cyperia.challenges.plugin.content.i18n.TranslationManager;
import net.cyperia.challenges.plugin.content.i18n.impl.format.ComponentFormatter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Registry of everything the search menu can find. {@link SearchQuery} decides whether a text matches
 * and how well; this class decides what is searched and how the hits are ordered.
 *
 * <h2>Visibility</h2>
 * A findable stays registered for its whole lifetime, but {@link SearchFindable#isVisibleTo(org.bukkit.entity.Player)} may flip
 * at any point, so it is asked once per search rather than cached anywhere.
 *
 * <h2>Ranking</h2>
 * Scores are stacked into bands, so the coarse ordering is guaranteed rather than emergent:
 * everything found in the player's language outranks every fallback hit ({@link #LOCALE_BAND}), and a
 * name hit outranks every entry that only matched in its description ({@link #FIELD_BAND}). Within a
 * band the {@link SearchQuery} score decides.
 *
 * <h2>Cost</h2>
 * Rendering a translation to plain text means parsing MiniMessage, far too expensive to redo per query,
 * so it is cached per findable and language. The cache revalidates itself against the raw translation
 * value, so a reloaded language or a renamed custom challenge is picked up without anyone having to
 * invalidate anything.
 */
public final class SearchRegistry {

  /**
   * Number of hits in the player's own language below which {@link TranslationManager#FALLBACK_LOCALE}
   * is searched as well.
   */
  private static final int FALLBACK_THRESHOLD = 5;

  // both bands are wider than everything that can be scored below them, which is what makes the
  // ordering between languages and between fields a guarantee instead of a matter of tuning
  private static final long FIELD_BAND = SearchQuery.MAX_SCORE * 2L;
  private static final long LOCALE_BAND = FIELD_BAND * 2L;

  /**
   * Hits are packed into one {@code long} for sorting: the score in the high bits, the index of the
   * findable in the low ones. That primitive sort is what keeps the result set free of boxing and of a
   * comparator, at the cost of a ceiling of {@value #INDEX_MASK} registered findables.
   */
  private static final int INDEX_BITS = 20;
  private static final int INDEX_MASK = (1 << INDEX_BITS) - 1;

  private static final SearchFindable[] NO_FINDABLES = new SearchFindable[0];

  private final ArrayList<SearchFindable> findables = new ArrayList<>();

  /** {@link #findables} as an array, so a query can iterate it without any bounds or modification checks. */
  private SearchFindable[] snapshot = NO_FINDABLES;
  private final Map<Locale, Entry[]> indexByLocale = new HashMap<>(4);

  public synchronized void register(@NotNull SearchFindable findable) {
    findables.add(findable);
    snapshot = NO_FINDABLES;
  }

  public synchronized void registerAll(@NotNull SearchFindable... findable) {
    Collections.addAll(findables, findable);
    snapshot = NO_FINDABLES;
  }

  /**
   * @return every findable that matches, best first and without a limit. The caller decides how many of
   * them to show and may use the full size as the hit count.
   */
  @NotNull
  public synchronized List<SearchFindable> search(@NotNull String query, @NotNull Player viewer, @NotNull Locale locale) {
    SearchQuery searchQuery = new SearchQuery(query);
    if (searchQuery.isEmpty() || findables.isEmpty()) return List.of();

    SearchFindable[] entries = snapshot();
    long[] hits = new long[entries.length];
    boolean[] skipped = hidden(entries, viewer);

    Locale playerLocale = TranslationManager.normalizeLocale(locale);
    int count = collect(searchQuery, playerLocale, entries, hits, skipped, 0, true);

    // the player's language alone barely found anything: the query is probably english (or the
    // language file is incomplete), so look through the fallback language as well
    Locale fallbackLocale = TranslationManager.FALLBACK_LOCALE;
    if (count < FALLBACK_THRESHOLD && !playerLocale.getLanguage().equals(fallbackLocale.getLanguage())) {
      count = collect(searchQuery, fallbackLocale, entries, hits, skipped, count, false);
    }
    if (count == 0) return List.of();

    Arrays.sort(hits, 0, count); // ascending, so the best hit ends up last
    SearchFindable[] result = new SearchFindable[count];
    for (int i = 0; i < count; i++) {
      result[i] = entries[INDEX_MASK - (int) (hits[count - 1 - i] & INDEX_MASK)];
    }
    return List.of(result);
  }

  public synchronized void clear() {
    findables.clear();
    indexByLocale.clear();
    snapshot = NO_FINDABLES;
  }

  /**
   * Scores every findable this search has not skipped yet and appends its hit to {@code hits}.
   *
   * @param skipped        the findables an earlier pass has matched or that are hidden, updated in place
   * @param playerLanguage whether these hits belong into the upper {@link #LOCALE_BAND}
   * @return the new hit count
   */
  private int collect(@NotNull SearchQuery query, @NotNull Locale locale, @NotNull SearchFindable[] entries,
                      @NotNull long[] hits, @NotNull boolean[] skipped, int count, boolean playerLanguage) {
    Entry[] index = index(locale, entries);
    long localeBand = playerLanguage ? LOCALE_BAND : 0L;

    for (int i = 0; i < index.length; i++) {
      if (skipped[i]) continue;
      Entry entry = index[i];
      entry.revalidate(locale);

      int nameScore = query.score(entry.name, entry.nameMask);
      int descriptionScore = query.score(entry.description, entry.descriptionMask);
      // matching in both is a slightly better hit than matching in the name alone
      long score = nameScore > 0 ? FIELD_BAND + nameScore + descriptionScore / 8 : descriptionScore;
      if (score == 0) continue;

      skipped[i] = true;
      // the inverted index keeps equally scored hits in registration order once the array is read back
      hits[count++] = ((localeBand + score) << INDEX_BITS) | (INDEX_MASK - i);
    }
    return count;
  }

  /**
   * Asks every findable whether it currently wants to be found. That answer may change at any time, so
   * it is never cached; asking once up front instead of per pass keeps a findable that flips mid search
   * from being scored in one language but not the other, and costs one call per findable and search.
   *
   * @return which findables this search leaves out, indexed like {@code entries}
   */
  @NotNull
  private static boolean[] hidden(@NotNull SearchFindable[] entries, @NotNull Player viewer) {
    boolean[] hidden = new boolean[entries.length];
    for (int i = 0; i < entries.length; i++) {
      hidden[i] = !entries[i].isVisibleTo(viewer);
    }
    return hidden;
  }

  @NotNull
  private SearchFindable[] snapshot() {
    SearchFindable[] current = snapshot;
    if (current.length != findables.size()) {
      snapshot = current = findables.toArray(NO_FINDABLES);
    }
    return current;
  }

  /**
   * @return the per-language cache for {@code entries}, carrying over everything that is already
   * rendered. Registering more findables only appends, so the shared prefix stays valid.
   */
  @NotNull
  private Entry[] index(@NotNull Locale locale, @NotNull SearchFindable[] entries) {
    Entry[] index = indexByLocale.get(locale);
    if (index != null && index.length == entries.length
      && (index.length == 0 || index[0].findable == entries[0])) {
      return index;
    }

    Entry[] updated = new Entry[entries.length];
    int reused = 0;
    if (index != null) {
      int shared = Math.min(index.length, entries.length);
      while (reused < shared && index[reused].findable == entries[reused]) reused++;
      System.arraycopy(index, 0, updated, 0, reused);
    }
    for (int i = reused; i < entries.length; i++) {
      updated[i] = new Entry(entries[i]);
    }

    indexByLocale.put(locale, updated);
    return updated;
  }

  /**
   * The plain, unformatted text of a translation, or an empty string when this language does not
   * translate it. Without that check an untranslated key would be searchable as its placeholder
   * {@code "challenge.no-jump.name/de"}.
   */
  @NotNull
  private static String plainText(@NotNull LocalizableMessage message, @NotNull Locale locale) {
    MessageKey key = message.getLocalizableKey();
    if (key != null && !key.isCached(locale)) return "";
    return ComponentFormatter.serializePlainText(message.asComponent(locale));
  }

  /**
   * A cheap fingerprint of everything the rendered text is built from: the raw translation value, whose
   * array is replaced whenever a language is (re)loaded, plus the message arguments, which is where a
   * custom challenge carries its user given name. Two equal stamps mean the cached plain text is still
   * current.
   */
  @NotNull
  private static Object[] stamp(@NotNull LocalizableMessage message, @NotNull Locale locale) {
    MessageKey key = message.getLocalizableKey();
    Object[] rawValue = key != null && key.isCached(locale) ? key.localizeRawValue(locale) : null;

    Object[] arguments = message.getLocalizableArgs();
    if (arguments.length == 0) return new Object[]{rawValue};

    Object[] stamp = new Object[arguments.length + 1];
    stamp[0] = rawValue;
    System.arraycopy(arguments, 0, stamp, 1, arguments.length);
    return stamp;
  }

  /** One findable, rendered into the plain text of one language. */
  private static final class Entry {

    private final SearchFindable findable;

    private String name = "";
    private String description = "";
    private long nameMask;
    private long descriptionMask;
    private Object[] nameStamp;
    private Object[] descriptionStamp;

    /**
     * {@link MessageKey} instances are shared and immutable, so the lookup (which concatenates the key
     * path on every call) is only worth doing once. Anything else may be rebuilt per call and carry
     * live state, so it is re-fetched.
     */
    private LocalizableMessage nameMessage;
    private LocalizableMessage descriptionMessage;

    private Entry(@NotNull SearchFindable findable) {
      this.findable = findable;
    }

    private void revalidate(@NotNull Locale locale) {
      LocalizableMessage name = nameMessage != null ? nameMessage : findable.getName();
      if (name instanceof MessageKey) nameMessage = name;
      Object[] nameStamp = stamp(name, locale);
      if (!Arrays.equals(nameStamp, this.nameStamp)) {
        this.nameStamp = nameStamp;
        this.name = SearchQuery.normalize(plainText(name, locale));
        this.nameMask = SearchQuery.charMask(this.name);
      }

      LocalizableMessage description = descriptionMessage != null ? descriptionMessage : findable.getDescription();
      if (description instanceof MessageKey) descriptionMessage = description;
      Object[] descriptionStamp = stamp(description, locale);
      if (!Arrays.equals(descriptionStamp, this.descriptionStamp)) {
        this.descriptionStamp = descriptionStamp;
        this.description = SearchQuery.normalize(plainText(description, locale));
        this.descriptionMask = SearchQuery.charMask(this.description);
      }
    }
  }

}
