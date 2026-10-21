package net.cyperia.challenges.catalog;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// The format of the files is described in the catalog module's README
public final class ChallengeCatalog {

  public static final String DIRECTORY = "challenges-catalog/";

  private static final Set<String> CHALLENGE_FIELDS = Set.of("id", "key", "category", "icon", "color", "since",
    "updated", "beta", "excludeFromRandom", "featured", "registered", "class", "requires", "preset", "damageRule", "materialRule");
  private static final Set<String> DEFINITIONS = Set.of("class", "preset", "damageRule", "materialRule");
  private static final Set<String> CLASS_ONLY = Set.of("requires");
  private static final Set<String> CUSTOM_SETTING_FIELDS = Set.of("id", "class", "icon", "color", "since", "updated",
    "beta", "fallbackIds");

  private final List<Category> categories;
  private final List<Challenge> challenges;
  private final List<CustomSetting> triggers;
  private final List<CustomSetting> actions;

  private final Map<String, Category> categoriesById;
  private final Map<String, Challenge> challengesById;
  private final Map<String, Challenge> challengesByClass;
  private final Map<String, CustomSetting> triggersById;
  private final Map<String, CustomSetting> actionsById;

  public ChallengeCatalog(@NotNull List<Category> categories, @NotNull List<Challenge> challenges,
                          @NotNull List<CustomSetting> triggers, @NotNull List<CustomSetting> actions) {
    this.categories = List.copyOf(categories);
    this.challenges = List.copyOf(challenges);
    this.triggers = List.copyOf(triggers);
    this.actions = List.copyOf(actions);
    categoriesById = index(categories, Category::id, "category");
    challengesById = index(challenges, Challenge::id, "challenge");
    challengesByClass = index(challenges.stream()
      .filter(challenge -> challenge.definition() instanceof Definition.Implementation).toList(),
      challenge -> ((Definition.Implementation) challenge.definition()).className(), "challenge class");
    triggersById = index(triggers, CustomSetting::id, "trigger");
    actionsById = index(actions, CustomSetting::id, "action");
  }

  /** Reads the files {@code loader} bundles; a missing file is an empty list. */
  @NotNull
  public static ChallengeCatalog load(@NotNull ClassLoader loader) {
    JsonObject categories = read(loader, "categories.json");
    JsonObject triggers = read(loader, "triggers.json");
    JsonObject actions = read(loader, "actions.json");
    JsonObject challenges = read(loader, "challenges.json");
    return new ChallengeCatalog(
      list(categories, "categories", json -> category(json)),
      list(challenges, "challenges", json -> challenge(json, packageOf(challenges))),
      list(triggers, "triggers", json -> customSetting(json, packageOf(triggers))),
      list(actions, "actions", json -> customSetting(json, packageOf(actions)))
    );
  }

  @NotNull
  public static ChallengeCatalog load() {
    return load(ChallengeCatalog.class.getClassLoader());
  }

  /** Appends {@code other}'s entries after these; an id both declare is an error. */
  @NotNull
  public ChallengeCatalog merge(@NotNull ChallengeCatalog other) {
    return new ChallengeCatalog(concat(categories, other.categories), concat(challenges, other.challenges),
      concat(triggers, other.triggers), concat(actions, other.actions));
  }

  /** Checks what one file cannot check alone: every category, trigger and action an entry names exists. */
  @NotNull
  public ChallengeCatalog requireComplete() {
    List<String> problems = new ArrayList<>();
    for (Challenge challenge : challenges) {
      if (!categoriesById.containsKey(challenge.category())) {
        problems.add(challenge.id() + " names the unknown category " + challenge.category());
      }
      if (challenge.definition() instanceof Definition.Preset preset) {
        if (!triggersById.containsKey(preset.trigger())) {
          problems.add(challenge.id() + " names the unknown trigger " + preset.trigger());
        }
        if (!actionsById.containsKey(preset.action())) {
          problems.add(challenge.id() + " names the unknown action " + preset.action());
        }
        boolean betaPart = Stream.of(triggersById.get(preset.trigger()), actionsById.get(preset.action()))
          .anyMatch(setting -> setting != null && setting.release().beta());
        if (betaPart && !challenge.release().beta()) {
          problems.add(challenge.id() + " is built from a beta trigger or action, so it has to be beta too");
        }
      }
    }
    if (!problems.isEmpty()) throw new IllegalStateException("Inconsistent challenge catalog:\n  " + String.join("\n  ", problems));
    return this;
  }

  @NotNull
  public List<Category> categories() {
    return categories;
  }

  // In registration order, which is also the order of each menu
  @NotNull
  public List<Challenge> challenges() {
    return challenges;
  }

  @NotNull
  public List<CustomSetting> triggers() {
    return triggers;
  }

  @NotNull
  public List<CustomSetting> actions() {
    return actions;
  }

  @NotNull
  public Optional<Category> category(@NotNull String id) {
    return Optional.ofNullable(categoriesById.get(id));
  }

  @NotNull
  public Optional<Challenge> challenge(@NotNull String id) {
    return Optional.ofNullable(challengesById.get(id));
  }

  @NotNull
  public Optional<Challenge> challengeOf(@NotNull String className) {
    return Optional.ofNullable(challengesByClass.get(className));
  }

  @NotNull
  public Optional<CustomSetting> trigger(@NotNull String id) {
    return Optional.ofNullable(triggersById.get(id));
  }

  @NotNull
  public Optional<CustomSetting> action(@NotNull String id) {
    return Optional.ofNullable(actionsById.get(id));
  }

  @NotNull
  private static Category category(@NotNull JsonObject json) {
    requireFields(json, Set.of("id", "menu", "priority", "icon", "color"));
    return new Category(string(json, "id"), string(json, "menu"), json.get("priority").getAsInt(), icon(json));
  }

  @NotNull
  private static Challenge challenge(@NotNull JsonObject json, @Nullable String classPackage) {
    String id = string(json, "id");
    requireFields(json, CHALLENGE_FIELDS);
    List<String> definitions = DEFINITIONS.stream().filter(json::has).toList();
    if (definitions.size() != 1) {
      throw new IllegalArgumentException(id + " needs exactly one of " + DEFINITIONS + ", has " + definitions);
    }
    if (!json.has("class") && CLASS_ONLY.stream().anyMatch(json::has)) {
      throw new IllegalArgumentException(id + ": " + CLASS_ONLY + " only apply to an entry with a class");
    }
    Definition definition = switch (definitions.getFirst()) {
      case "class" -> new Definition.Implementation(className(classPackage, string(json, "class")),
        optionalString(json, "requires"));
      case "preset" -> preset(json.getAsJsonObject("preset"));
      case "damageRule" -> new Definition.DamageRule(strings(json, "damageRule"));
      default -> new Definition.MaterialRule(strings(json, "materialRule"));
    };
    return new Challenge(id, string(json, "key"), string(json, "category"), icon(json), definition,
      flag(json, "excludeFromRandom"), flag(json, "featured"), !json.has("registered") || json.get("registered").getAsBoolean(),
      release(json));
  }

  @NotNull
  private static Definition.Preset preset(@NotNull JsonObject json) {
    requireFields(json, Set.of("trigger", "triggerSettings", "action", "actionSettings", "value"));
    Definition.Preset.Value value = null;
    if (json.has("value")) {
      JsonObject valueJson = json.getAsJsonObject("value");
      requireFields(valueJson, Set.of("unit", "min", "max", "default"));
      value = new Definition.Preset.Value(string(valueJson, "unit"), valueJson.get("min").getAsInt(),
        valueJson.get("max").getAsInt(), valueJson.get("default").getAsInt());
    }
    return new Definition.Preset(string(json, "trigger"), settings(json.getAsJsonObject("triggerSettings")),
      string(json, "action"), settings(json.getAsJsonObject("actionSettings")), value);
  }

  @NotNull
  private static CustomSetting customSetting(@NotNull JsonObject json, @Nullable String classPackage) {
    requireFields(json, CUSTOM_SETTING_FIELDS);
    return new CustomSetting(string(json, "id"), className(classPackage, string(json, "class")), icon(json),
      strings(json, "fallbackIds"), release(json));
  }

  @NotNull
  private static Icon icon(@NotNull JsonObject json) {
    String color = optionalString(json, "color");
    return new Icon(string(json, "icon"), color == null ? null : Integer.parseInt(color.substring(1), 16));
  }

  @NotNull
  private static Release release(@NotNull JsonObject json) {
    return new Release(optionalString(json, "since"), optionalString(json, "updated"), flag(json, "beta"));
  }

  @NotNull
  private static Map<String, List<String>> settings(@NotNull JsonObject json) {
    Map<String, List<String>> settings = new LinkedHashMap<>();
    json.keySet().forEach(key -> settings.put(key, strings(json, key)));
    return settings;
  }

  @NotNull
  private static String className(@Nullable String classPackage, @NotNull String name) {
    return classPackage == null ? name : classPackage + "." + name;
  }

  @Nullable
  private static String packageOf(@NotNull JsonObject file) {
    return optionalString(file, "package");
  }

  private static void requireFields(@NotNull JsonObject json, @NotNull Set<String> allowed) {
    Set<String> unknown = new TreeSet<>(json.keySet());
    unknown.removeAll(allowed);
    if (!unknown.isEmpty()) throw new IllegalArgumentException("Unknown fields " + unknown + " in " + json);
  }

  @NotNull
  private static String string(@NotNull JsonObject json, @NotNull String member) {
    JsonElement element = json.get(member);
    if (element == null) throw new IllegalArgumentException("Missing " + member + " in " + json);
    return element.getAsString();
  }

  @Nullable
  private static String optionalString(@NotNull JsonObject json, @NotNull String member) {
    return json.has(member) ? json.get(member).getAsString() : null;
  }

  private static boolean flag(@NotNull JsonObject json, @NotNull String member) {
    return json.has(member) && json.get(member).getAsBoolean();
  }

  // A single value may be written without the brackets of a list
  @NotNull
  private static List<String> strings(@NotNull JsonObject json, @NotNull String member) {
    JsonElement element = json.get(member);
    if (element == null) return List.of();
    if (!element.isJsonArray()) return List.of(element.getAsString());
    return element.getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList();
  }

  @NotNull
  private static <T> List<T> list(@Nullable JsonObject file, @NotNull String member, @NotNull Function<JsonObject, T> parser) {
    if (file == null || !file.has(member)) return List.of();
    JsonArray array = file.getAsJsonArray(member);
    return array.asList().stream().map(element -> parser.apply(element.getAsJsonObject())).toList();
  }

  @Nullable
  private static JsonObject read(@NotNull ClassLoader loader, @NotNull String file) {
    try (InputStream in = loader.getResourceAsStream(DIRECTORY + file)) {
      if (in == null) return null;
      return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  @NotNull
  private static <T> Map<String, T> index(@NotNull List<T> entries, @NotNull Function<T, String> id, @NotNull String kind) {
    return entries.stream().collect(Collectors.toMap(id, Function.identity(), (first, second) -> {
      throw new IllegalArgumentException("Two catalog entries for the " + kind + " " + id.apply(first));
    }, LinkedHashMap::new));
  }

  @NotNull
  private static <T> List<T> concat(@NotNull List<T> first, @NotNull List<T> second) {
    return Stream.concat(first.stream(), second.stream()).toList();
  }

}
