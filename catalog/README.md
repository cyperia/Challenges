# Challenge Catalog

Everything about a challenge that is data rather than code: its id, icon, category, menu order and
release, and the whole definition of challenges built from data (presets, damage and material rules).
The plugin registers exactly what the catalog lists, in its order, and the module is Bukkit-free, so
anything outside the server can list the challenges with the plugin's own texts (`Locales`).

On a server without Challenges, `catalog-plugin` builds `ChallengesCatalog.jar`, a plugin carrying only
this module. A plugin that lists challenges there declares `depend: [ChallengesCatalog]` and compiles
against the catalog `compileOnly`, so it always reads the catalog of the latest Challenges build instead
of one bundled into its own jar. A change to the catalog's classes that breaks their API breaks those
plugins at runtime, not at their build.

The files live in `src/main/resources/challenges-catalog/`. A file may name a `package` its classes are
relative to.

| file | holds |
|---|---|
| `categories.json` | `id` (messages under `category.<id>`), `menu` (`challenges`, `goal`, `settings`, `custom`), `priority`, `icon` |
| `triggers.json`, `actions.json` | what custom challenges and presets are built from: `id`, `class`, `icon`, `fallbackIds` |
| `challenges.json` | every challenge in registration order, which is also its menu order |

## A challenge

```json
{
  "id": "cutclean",
  "key": "cut-clean",
  "category": "utility",
  "icon": "IRON_AXE",
  "class": "setting.CutCleanSetting",
  "since": "2.0"
}
```

- `id` is the key its settings are saved under. To rename it, move the old id into `fallbackIds`: the
  plugin then still loads settings and game states saved under it, and a lookup by it finds the entry.
- `key` is the message key below `challenge` (`challenge.cut-clean.name`, `.desc`).
- `icon` is a `Material` constant, `color` (`"#RRGGBB"`) tints a potion or leather item.
- `since`, `updated` mark the entry new or updated in the menu; `beta` leaves its class out of the
  public release, which shows the entry locked instead.
- `excludeFromRandom` keeps the random challenge from picking it.
- `permission` locks the entry for a player without that permission: it shows locked and cannot be
  changed, and the random challenge never picks it. With `"hidden": true` such a player does not see it
  at all. This keeps an entry in the same build as everything else while only some players can use it,
  for example an event goal or a challenge of a rank.
- `featured` puts it among the highlighted entries of a listing outside the plugin, such as the export below.
- `"registered": false` keeps a challenge out of the plugin without losing its entry, for one that is
  broken or not ready; anything listing challenges should skip it too.

Exactly one definition:

| field | registers |
|---|---|
| `class` | a class with a constructor without parameters; `requires` names a plugin it needs. Its commands come from the class (`CommandProvider`). `pairing` (`trigger`, `action`) names the custom challenge it plays at heart, its own rules on top, for a builder outside the plugin to point to it |
| `preset` | `trigger`, `triggerSettings`, `action`, `actionSettings` and an optional `value` (`unit` is a `ValueUnit` constant, `min`, `max`, `default`) filled into a setting written `${value}` |
| `damageRule` | the `DamageCause` constants it covers, `"*"` for all |
| `materialRule` | the `Material` constants it covers |

Unknown fields, a second definition or a reference to a missing category, trigger or action fail
loading. `CatalogClassesTest` in the plugin checks that every class and constant exists and that every
challenge class has an entry.

## Export

`export/catalog.json` holds the registered challenges and the categories with their names and descriptions
in every bundled language, for readers without the plugin such as a website, which can read it from the
repository. Each challenge also carries `styled`: its name, its description lines and its item tooltip as
run through the menu's item formats, each line a list of text runs with the colour (hex) and bold the game
renders them with. `fallbackIds` lists the ids a challenge had before, for a reader that linked to it under one. A preset names its `trigger` and `action`, a challenge with a `pairing` names it the same way under `pairing`, and the top-level `triggers` and `actions`
list what custom challenges are built from, with the same texts. Lore lines are joined into paragraphs, and a value the plugin fills in at
runtime reads as an ellipsis or is left out. The file is committed: after changing the catalog or a challenge's texts, run
`./gradlew :catalog:exportCatalog` and commit the result. `CatalogExportTest` fails while it is out of date.

## Modules

A plugin adding challenges ships its own `challenges-catalog/` and registers it with
`Challenges.getInstance().getChallengeLoader().registerModule(getClass().getClassLoader())`. Its entries
may use the plugin's categories or bring their own; ids have to be unique across both.
