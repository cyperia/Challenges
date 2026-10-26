package net.cyperia.challenges.catalog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.flattener.FlattenerListener;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Pattern;

// The registered challenges that are not hidden, and their categories with their texts in every bundled language, plain and as
// coloured runs the way the menu shows them, for readers without the plugin such as a website. The file is
// committed, so it can be read at a stable URL.
public final class CatalogExport {

  public static final String FILE = "export/catalog.json";

  private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
  private static final String NAME_FORMAT = "menu.generic.format.name";
  private static final String DESCRIPTION_FORMAT = "menu.generic.format.desc";
  private static final Pattern ARGUMENT = Pattern.compile("\\{\\d+}");
  private static final Pattern TRAILING_ARGUMENT = Pattern.compile(".*:\\s*\\{\\d+}");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  private CatalogExport() {
  }

  public static void main(String[] args) throws IOException {
    Path file = Path.of(args[0]);
    Files.createDirectories(file.getParent());
    Files.writeString(file, render(ChallengeCatalog.load().requireComplete()));
  }

  @NotNull
  public static String render(@NotNull ChallengeCatalog catalog) {
    Map<String, Map<String, String[]>> bundles = new LinkedHashMap<>();
    languages().forEach(language -> bundles.put(language, Locales.load(language)));

    JsonArray categories = new JsonArray();
    for (Category category : catalog.categories()) {
      JsonObject json = new JsonObject();
      json.addProperty("id", category.id());
      json.addProperty("menu", category.menu());
      json.addProperty("priority", category.priority());
      json.add("icon", icon(category.icon()));
      json.add("name", name(bundles, language -> category.nameKey()));
      json.add("description", description(bundles, category.descriptionKey()));
      categories.add(json);
    }

    JsonArray challenges = new JsonArray();
    for (Challenge challenge : catalog.challenges()) {
      if (!challenge.registered() || challenge.hidden()) continue;
      JsonObject json = new JsonObject();
      json.addProperty("id", challenge.id());
      if (!challenge.fallbackIds().isEmpty()) {
        JsonArray fallbackIds = new JsonArray();
        challenge.fallbackIds().forEach(fallbackIds::add);
        json.add("fallbackIds", fallbackIds);
      }
      json.addProperty("category", challenge.category());
      json.add("icon", icon(challenge.icon()));
      if (challenge.featured()) json.addProperty("featured", true);
      // Players pick a challenge at its defaults, so a name with the default value spelled out wins
      Function<String, String> nameKey = language -> bundles.get(language).containsKey(challenge.catalogNameKey())
        ? challenge.catalogNameKey() : challenge.nameKey();
      json.add("name", name(bundles, nameKey));
      json.add("description", description(bundles, challenge.descriptionKey()));
      json.add("styled", styled(bundles, nameKey, challenge.descriptionKey()));
      if (challenge.definition() instanceof Definition.Preset preset) {
        JsonObject built = new JsonObject();
        built.addProperty("trigger", preset.trigger());
        built.addProperty("action", preset.action());
        json.add("preset", built);
      }
      if (challenge.pairing() != null) {
        JsonObject pairing = new JsonObject();
        pairing.addProperty("trigger", challenge.pairing().trigger());
        pairing.addProperty("action", challenge.pairing().action());
        json.add("pairing", pairing);
      }
      challenges.add(json);
    }

    JsonObject root = new JsonObject();
    root.add("categories", categories);
    root.add("challenges", challenges);
    root.add("triggers", customSettings(bundles, catalog.triggers(), "trigger"));
    root.add("actions", customSettings(bundles, catalog.actions(), "action"));
    return GSON.toJson(root) + "\n";
  }

  // What custom challenges are built from: a trigger and an action, named under custom.<kind>.<id>
  @NotNull
  private static JsonArray customSettings(@NotNull Map<String, Map<String, String[]>> bundles,
                                          @NotNull List<CustomSetting> settings, @NotNull String kind) {
    JsonArray array = new JsonArray();
    for (CustomSetting setting : settings) {
      String key = "custom." + kind + "." + setting.id();
      JsonObject json = new JsonObject();
      json.addProperty("id", setting.id());
      json.add("icon", icon(setting.icon()));
      json.add("name", name(bundles, language -> key + ".name"));
      json.add("description", description(bundles, key + ".desc"));
      json.add("styled", styled(bundles, language -> key + ".name", key + ".desc"));
      array.add(json);
    }
    return array;
  }

  @NotNull
  private static JsonObject icon(@NotNull Icon icon) {
    JsonObject json = new JsonObject();
    json.addProperty("material", icon.material());
    if (icon.rgb() != null) json.addProperty("rgb", String.format("#%06X", icon.rgb()));
    return json;
  }

  @NotNull
  private static JsonObject name(@NotNull Map<String, Map<String, String[]>> bundles,
                                 @NotNull Function<String, String> key) {
    JsonObject json = new JsonObject();
    bundles.forEach((language, bundle) -> {
      List<String> paragraphs = paragraphs(bundle.get(key.apply(language)));
      if (!paragraphs.isEmpty()) json.addProperty(language, String.join(" ", paragraphs));
    });
    return json;
  }

  @NotNull
  private static JsonObject description(@NotNull Map<String, Map<String, String[]>> bundles, @NotNull String key) {
    JsonObject json = new JsonObject();
    bundles.forEach((language, bundle) -> {
      JsonArray array = new JsonArray();
      paragraphs(bundle.get(key)).forEach(array::add);
      if (!array.isEmpty()) json.add(language, array);
    });
    return json;
  }

  // The name, the description lines and the item tooltip, which runs both through the menu's item formats
  @NotNull
  private static JsonObject styled(@NotNull Map<String, Map<String, String[]>> bundles,
                                   @NotNull Function<String, String> nameKey, @NotNull String descriptionKey) {
    JsonObject json = new JsonObject();
    bundles.forEach((language, bundle) -> {
      String[] nameLines = bundle.get(nameKey.apply(language));
      if (nameLines == null) return;
      String name = String.join(" ", lines(nameLines));
      List<String> description = lines(bundle.get(descriptionKey));

      JsonArray descriptionRuns = new JsonArray();
      description.forEach(line -> descriptionRuns.add(runs(line)));
      JsonArray tooltip = new JsonArray();
      format(bundle, NAME_FORMAT, List.of(name)).forEach(line -> tooltip.add(runs(line)));
      if (!description.isEmpty()) format(bundle, DESCRIPTION_FORMAT, description).forEach(line -> tooltip.add(runs(line)));

      JsonObject texts = new JsonObject();
      texts.add("name", runs(name));
      texts.add("description", descriptionRuns);
      texts.add("tooltip", tooltip);
      json.add(language, texts);
    });
    return json;
  }

  // A format line holding the argument repeats once per argument line, as a multi-line argument does in the menu
  @NotNull
  private static List<String> format(@NotNull Map<String, String[]> bundle, @NotNull String key,
                                     @NotNull List<String> argument) {
    List<String> lines = new ArrayList<>();
    for (String line : bundle.getOrDefault(key, new String[] {"{0}"})) {
      if (line.contains("{0}")) argument.forEach(value -> lines.add(line.replace("{0}", value)));
      else lines.add(line);
    }
    return lines;
  }

  // Text runs with their colour as hex and bold where set; a gradient arrives as one run per character
  @NotNull
  static JsonArray runs(@NotNull String miniMessage) {
    JsonArray runs = new JsonArray();
    Deque<Style> styles = new ArrayDeque<>(List.of(Style.empty()));
    ComponentFlattener.basic().flatten(MINI_MESSAGE.deserialize(miniMessage), new FlattenerListener() {
      @Override
      public void pushStyle(@NotNull Style style) {
        styles.push(styles.peek().merge(style));
      }

      @Override
      public void component(@NotNull String text) {
        if (text.isEmpty()) return;
        Style style = styles.peek();
        TextColor color = style.color();
        boolean bold = style.decoration(TextDecoration.BOLD) == TextDecoration.State.TRUE;
        JsonObject last = runs.isEmpty() ? null : runs.get(runs.size() - 1).getAsJsonObject();
        if (last != null && sameStyle(last, color, bold)) {
          last.addProperty("text", last.get("text").getAsString() + text);
          return;
        }
        JsonObject run = new JsonObject();
        run.addProperty("text", text);
        if (color != null) run.addProperty("color", color.asHexString());
        if (bold) run.addProperty("bold", true);
        runs.add(run);
      }

      @Override
      public void popStyle(@NotNull Style style) {
        styles.pop();
      }
    });
    return runs;
  }

  private static boolean sameStyle(@NotNull JsonObject run, @Nullable TextColor color, boolean bold) {
    String runColor = run.has("color") ? run.get("color").getAsString() : null;
    return run.has("bold") == bold && (color == null ? runColor == null : color.asHexString().equals(runColor));
  }

  // Lore lines are joined, since their breaks only fit an item tooltip; an empty line starts a new paragraph
  @NotNull
  private static List<String> paragraphs(@Nullable String[] raw) {
    List<String> paragraphs = new ArrayList<>();
    StringJoiner paragraph = new StringJoiner(" ");
    for (String line : lines(raw)) {
      String text = plain(line);
      if (!text.isEmpty()) {
        paragraph.add(text);
      } else if (paragraph.length() > 0) {
        paragraphs.add(paragraph.toString());
        paragraph = new StringJoiner(" ");
      }
    }
    if (paragraph.length() > 0) paragraphs.add(paragraph.toString());
    return paragraphs;
  }

  // The plugin fills positional arguments at runtime: a line that only appends a value is left out, any other
  // value reads as an ellipsis
  @NotNull
  private static List<String> lines(@Nullable String[] raw) {
    if (raw == null) return List.of();
    List<String> lines = new ArrayList<>();
    for (String line : raw) {
      if (TRAILING_ARGUMENT.matcher(plain(line)).matches()) continue;
      lines.add(ARGUMENT.matcher(line).replaceAll("…"));
    }
    return lines;
  }

  @NotNull
  static String plain(@NotNull String miniMessage) {
    return MINI_MESSAGE.stripTags(miniMessage).strip();
  }

  @NotNull
  private static List<String> languages() {
    try (InputStream in = CatalogExport.class.getClassLoader().getResourceAsStream("language/languages.json")) {
      if (in == null) throw new IllegalStateException("The catalog is missing its languages.json");
      return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonArray().asList().stream()
        .map(JsonElement::getAsString)
        .filter(language -> !language.equals(Locales.GLOBAL))
        .toList();
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

}
