# Proposal

## Why

Logging a meal now takes opening the app and tapping Lisää annos. The user wants to add the day's
meals as quickly as weighing: one tap on the home screen, pick or type, done (user 2026-11-14,
backlog "food widget"). The earlier decision "no food widget, food only inside the app" was about
not *showing* food or kcal on the home screen; an add-only button keeps that.

## What Changes

- A second 1x1 home-screen widget "Lisää annos": the same round plate as Punnitse with a new
  cutlery line icon. It never shows food, kcal, a count or a state mark.
- A tap opens the add field as a sheet over whatever is on screen (not the app's main screen),
  keyboard up, adding to today: the same suggestions, search, enter, the "<name> · Muuta · Kumoa"
  line, and the field stays open for the next item. Closing the sheet shows what was behind it.
- Muuta opens the item's editor in the same sheet. Tallenna, Peruuta and Poista all return to the
  open add field. This now also holds in the app (front page, Ruoka): today the add field closes
  when the editor opens.
- "Tuo ruoka-annokset" and the clipboard offer's Näytä in the widget sheet finish the dialog and
  open the app on the import preview over the front page (the share path); after Lisää or Peruuta
  the user stays in the app. The clipboard is read once when the dialog gains focus.
- Writes survive closing the sheet; a failed write or a delete's Kumoa show as a line in the sheet.
- Permission and Health Connect states are handled in the sheet (see design).
- A launcher shortcut "Lisää annos" after Punnitse on the app icon's long-press menu. No Quick
  Settings tile for food.
- "Food only inside the app" is reworded: food and kcal are never *shown* on always-visible
  surfaces (widget face, tile, shortcut label, notification); the app's own dialog counts as inside
  the app; an add-only button is allowed.
- Help page: the food section mentions the widget and the shortcut.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `food-log`: new requirements for adding from the home screen; "Adding an item" (editor return),
  "Clipboard offer" (the widget sheet named) and "Food only inside the app" reworded.
- `help-page`: the food section names the widget and shortcut.

## Impact

Widget (new provider, layout, preview, cutlery icon), a new dialog activity in its own task like
the weigh-in dialog, `res/xml/shortcuts.xml` (+ `ic_shortcut_*` icon), the manifest,
`FoodSheet`/`FoodViewModel` reused (host hooks for the import buttons; the editor-return rule
changed for the app too; writes moved to an app-level scope), `MainActivity` opened on the import
preview, `strings.xml`, `HelpScreen`, the project CLAUDE.md log map. Health Connect: same
nutrition writes as before. No scale, CI or data-file change. See `design.md`.
