# Proposal

## Why

The Lisää annos sheet repeats the Ruoka day list on another background, stacks two sheets with two
handles, and its search results look like the eaten rows, so the user mixes them up (user
2026-11-19, screen recording). Android's usual way to add from a list is a full-screen search
that shows only search content and returns to the list.

## What Changes

- **BREAKING (look):** Lisää annos opens a full-screen search instead of the add sheet: a top bar
  with back, the search field (keyboard up), a clear ✕ and ⋮ (Kopioi tuontiohje, Tuo
  ruoka-annokset). No day list, total, summary line, chips or fold handle in it.
- Empty field: "Usein tähän aikaan" as up to eight rows (the chips' ranking). Typing: matching
  ruuat top-down, best first, then "Lisää ”<text>” uutena". Every row starts with a + icon and
  reads as something to add, not as an eaten annos.
- Adding keeps the search open (field cleared) with a bar "<name> lisätty · Kumoa". Back returns
  to the Ruoka screen of that day with the added annokset highlighted; with nothing added back
  returns where the search was opened.
- From the front page and Historia the search also returns to Ruoka after an add.
- Widget and launcher shortcut open the same search full screen; back after an add opens the app
  on Ruoka, back with nothing added returns to the home screen.
- A line "Lisätään: <day>" shows only when the search adds to another day than today.
- Editing and deleting eaten annokset happens on Ruoka only.

Touches app screens (Ruoka, front page and Historia menus, the add search) and the widget and
shortcut's entry. No Health Connect, data file, log format or widget face change.

## Capabilities

### New Capabilities

### Modified Capabilities
- `food-log`: "Adding an item", "Clipboard offer", "Widget and shortcut for adding", "Adding from
  the home screen", "Food only inside the app", "Item row buttons", "Undoing a correction", and
  the sheet requirements renamed for the search (messages and loading, permission, denied
  permission, midnight, import, removing a ruoka).

## Impact

`ui/FoodSheet.kt` (sheet, panel, chips, fold → search screen), `ui/FoodAddDialog.kt`,
`FoodAddActivity.kt` (full-screen theme, exit to the app), `ui/MainScreen.kt` (host),
`ui/FoodScreen.kt` (highlight of added annokset), `ui/FoodViewModel.kt` (search session: added
ids, exit target), strings, `openspec/context/product.md`. Library: Material 3's full-screen
search bar (already in the Compose Material 3 dependency). Depends on `food-row-info` being
archived first (its "Item row buttons" text is the base here).
