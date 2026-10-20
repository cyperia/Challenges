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

- `id` is the key its settings are saved under. **Never change it**, saved settings would be lost.
- `key` is the message key below `challenge` (`challenge.cut-clean.name`, `.desc`).
- `icon` is a `Material` constant, `color` (`"#RRGGBB"`) tints a potion or leather item.
- `since`, `updated` mark the entry new or updated in the menu; `beta` leaves its class out of the
  public release, which shows the entry locked instead.
- `excludeFromRandom` keeps the random challenge from picking it.
- `"registered": false` keeps a challenge out of the plugin without losing its entry, for one that is
  broken or not ready; anything listing challenges should skip it too.

Exactly one definition:

| field | registers |
|---|---|
| `class` | a class with a constructor without parameters; `command` registers it as a command, `requires` names a plugin it needs |
| `preset` | `trigger`, `triggerSettings`, `action`, `actionSettings` and an optional `value` (`unit` is a `ValueUnit` constant, `min`, `max`, `default`) filled into a setting written `${value}` |
| `damageRule` | the `DamageCause` constants it covers, `"*"` for all |
| `materialRule` | the `Material` constants it covers |

Unknown fields, a second definition or a reference to a missing category, trigger or action fail
loading. `CatalogClassesTest` in the plugin checks that every class and constant exists and that every
challenge class has an entry.

## Modules

A plugin adding challenges ships its own `challenges-catalog/` and registers it with
`Challenges.getInstance().getChallengeLoader().registerModule(getClass().getClassLoader())`. Its entries
may use the plugin's categories or bring their own; ids have to be unique across both.
