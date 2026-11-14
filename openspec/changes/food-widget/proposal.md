# Proposal

## Why

Logging a meal now takes opening the app and tapping Lisää annos. The user wants to add the day's
meals as quickly as weighing: one tap on the home screen, pick or type, done (user 2026-11-14,
backlog "food widget"). The earlier decision "no food widget, food only inside the app" was about
not *showing* food or kcal on the home screen; an add-only button keeps that.

## What Changes

- A second 1x1 home-screen widget "Lisää annos" in the same style as the Punnitse widget (round
  plate, line icon, label). It never shows food, kcal or a count.
- A tap opens the add field as a sheet over whatever is on screen (not the app's main screen),
  keyboard up, adding to today: the same suggestions, search, enter, the "<name> · Muuta · Kumoa"
  line, and the field stays open for the next item. Closing the sheet returns to the home screen.
- Muuta opens the item's editor in the same sheet; saving or closing it returns to the add field.
- "Tuo ruoka-annokset" and the clipboard offer's Näytä open the app on the import preview (the
  share path already lands there); the clipboard offer is read when the sheet opens, as on Ruoka.
- Without the nutrition permission the sheet explains why and offers to grant it in place.
- A launcher shortcut "Lisää annos" next to Punnitse on the app icon's long-press menu.
- The rule "Food only inside the app" is reworded: food and kcal are still never *shown* outside
  the app; an add-only button is allowed.
- Help page: the food section mentions the widget and the shortcut.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `food-log`: new requirement "Adding from the home screen"; "Food only inside the app" reworded.
- `help-page`: the food section names the widget and shortcut.

## Impact

Widget (new provider, layout, preview, icon), a new dialog activity in its own task like the
weigh-in dialog, `res/xml/shortcuts.xml`, the manifest, `FoodSheet`/`FoodViewModel` reused (the
sheet's AI buttons and editor hooks made usable outside the main screen), `MainActivity` opened on
the import preview, `strings.xml`, `HelpScreen`. Health Connect: same nutrition writes as before.
No scale, CI or data-file change.
