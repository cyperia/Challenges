# Localization Files

`global.json` + one file per language (`en.json`, `de.json`, …), listed in `../languages.json`.
All files are merged into **one flat bundle** at load time (`TranslationManager#populateLanguageFromDocument`):
nesting is purely for readability, the runtime key is the dot-joined path.

```
"challenge": { "cut-clean": { "sub": { "iron": { "name": "*Iron*" } } } }   ->   challenge.cut-clean.sub.iron.name
```

`global.json` is loaded first, the language file second, so a language file **overrides** global for the same key.

---

## What goes where

| file | holds | never holds |
|---|---|---|
| `global.json` | everything language independent: symbols, argument/item/menu **formats**, colours (`*colorize*`), reference-only entries | translated prose |
| `<lang>.json` | translated prose only | colour-only or format-only definitions |

Rule of thumb: if changing the value would be wrong in every language, it belongs in `global.json`.
If a colour needs to differ per entry, add a `*colorize*` there instead of hard-coding the tag in every language.

---

## Naming

* **kebab-case** for every hand-authored path segment: `no-permission`, `force-item-battle`, `damage-cause`.
* **verbatim identifiers** where the segment mirrors a java enum constant or a persisted id — these keep
  their underscores because the code derives them with `name().toLowerCase()` or reads them back from
  `custom_challenges.json`: `menu.generic.interact.back_menu` (`Interaction.BACK_MENU`),
  `custom.action.boost_in_air`, `category.misc_challenge`.
* Depth stays at **3–4 segments**; 5 only for the `…​.sub.<name>.<leaf>` layer.

### Leaf vocabulary

The same leaf name means the same thing everywhere:

| leaf | meaning |
|---|---|
| `name` | the **coloured** display name shown on an item |
| `menu` | the **plain** inventory title of the menu this entry opens; `name` is usually built from it (`"name": "*{@menu}*"`) |
| `label` | the plain text a `name` is built from when there is no menu title to reuse |
| `desc` | description / item lore, usually a list of lines |
| `info` | extra status line in the lore (e.g. `Enabled 3/12`) |
| `sub` | container for a setting's sub-settings |
| `page` | container for the titles of a multi-page menu |
| `*colorize*` | colour meta tag, `global.json` only — see below |

So `MenuType`, `SettingCategory`, `MenuSetting.SubSetting` and `AbstractChallenge` all read
`name` / `desc` / `menu`; there is no `display`, `name-state` or `sub-name` variant.

---

## Namespaces

| namespace | contents |
|---|---|
| `generic` | reusable words (`seconds`, `enabled`, `radius`) used as message arguments |
| `language` | display names of the selectable languages, keyed by language tag |
| `symbol`, `arg-format`, `prefix`, `actionbar`, `bossbar`, `scoreboard` | *(global)* glyphs and surface formats |
| `suffix` | badges appended to item names (`new`, `updated`, `selected`) |
| `title` | fullscreen title/subtitle pairs |
| `stats` | statistic names (`stats.type.<statistic>`) and the stats/leaderboard screens |
| `timer` | `timer.bar.*` (action bar), `timer.messages.*` (chat), `timer.time.format.*` *(global)* |
| `menu` | `menu.generic.*` shared item/tooltip formats, then one document per `MenuType` |
| `category` | one document per `SettingCategory`, plus `category.generic.*` shared descriptions/info lines |
| `challenge` | one document per challenge/setting/goal, keyed by its `nameMessageKey`, plus `challenge.generic.*` for messages several goals share |
| `custom` | the custom-challenge builder: `custom.trigger.*`, `custom.action.*`, `custom.setting.*` |
| `command` | command feedback, one document per command (`command.heal.done`, `command.reset.confirm`) |
| `server` | server/plugin lifecycle notifications (join/quit, update + config warnings, `server.reset.*`) |
| `challenge-end` | the end-of-challenge summary screens |
| `translatable` | fallbacks for bukkit types the client cannot translate itself |
| `legacy` | **deprecated.** Keys still read by the `@Deprecated` `Message.forName` path; delete along with those classes. |

`custom.setting.<key>` uses the sub-setting key from `SubSettingsHelper`/`SubSettingsBuilder`, so the
option group and the sub-setting title share one document
(`custom.setting.target_entity.name` + `custom.setting.target_entity.console.name`).

---

## Values

### References — `{@key}`

Resolved relative to the referencing key's own path, walking outwards to the global scope, so
`category.randomizer.info` can say `{@generic.info.count}` and reach `category.generic.info.count`.
Arguments are passed with `{@key('arg0', 'arg1')}`. Multi-line targets expand into multiple lines.

### Positional arguments — `{0}`, `{1}`

Filled from the `Object...` passed at the call site. Indices are **not** shifted by references.

### Colorize — `*text*`

`*…*` marks a span to be wrapped by the nearest `*colorize*` entry, looked up the same way references
are. This keeps one colour definition in `global.json` next to N translations:

```jsonc
// global.json
"challenge": { "timber": { "*colorize*": "<aqua>{0}</aqua>" } }
// en.json
"challenge": { "timber": { "name": "*Timber*" } }
```

**Prefer this over writing `<aqua>` into every language file.**

### Chat messages must open with a colour tag

Anything sent with `MessageKey#send`/`#broadcast` goes to chat unwrapped, so a value starting with bare
text renders **white**. Start the first non-blank line with a tag — `<gray>` unless there is a reason not to:

```jsonc
"done": "<gray>You have been <green>healed</green></gray>"   // correct
"done": "You have been <green>healed</green>"                // renders white
```

Item names, lore and message *arguments* do not need this: they are wrapped by
`menu.generic.format.*` or by the message they are substituted into.

---

## Adding a language

1. Copy `en.json`, translate the values, keep every key.
2. Add the language tag to `../languages.json`.
3. Add its display name under `language.<tag>` in **every** language file.

Keys must exist in all languages — there is no per-key fallback, a missing key renders as `key/locale`.

---

## Legacy

Installations before 2.4 download their flat-key files from the `master` branch on GitHub, which holds nothing else. It is frozen; do not port changes into it.
