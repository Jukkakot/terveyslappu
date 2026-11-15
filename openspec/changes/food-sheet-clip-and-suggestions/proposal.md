# Proposal

## Why

Importing a meal estimate copied from a chat will be the most common way to log lunch and dinner,
but the clipboard offer is easy to miss: the same clipboard content is offered only once, so the
widget sheet often shows nothing (user, 2026-11-15, screenshot). The sheet's suggestion buttons show
only names, so the user cannot see what one tap will add, and adjusting before adding means adding
first and then Muuta.

## What Changes

- Clipboard offer: shown every time the front page, Ruoka or the Lisää annos sheet opens while the
  clipboard holds food JSON, until that content has been imported (saved on the preview) or
  skipped with Ohita. Replaces "offered once per clipboard content".
- In the sheet and on Ruoka the offer is a prominent button under the field:
  "Tuo leikepöydältä: 4 annosta · 850 kcal" with a small Ohita; the front page keeps its notice
  with the same text.
- Empty-field suggestions become a list: one row per product with name, usual amount and kcal
  (and the treat sign), in the existing order (most often logged around this time, then most
  often logged).
- Each suggestion row (empty field and typed matches) gets an edit button: it opens an editor for
  a new item (amount, kcal, Herkku) with Lisää and Peruuta, so the amount can be set before
  adding. Tapping the row itself still adds at once.
- A log line for every clipboard check, so a missing offer can be explained from the log.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `food-log`: "Adding an item" (list rows, edit before adding), "Clipboard offer" (shown until
  imported, button form), "Import from the home-screen sheet" (offer wording in the scenario).

## Impact

- App screens: the Lisää annos sheet (in the app and over the home screen from the widget and
  shortcut), the Ruoka screen's offer, the front page's offer notice, the AI import preview's
  save (marks the content imported).
- Widget: the sheet opened from the food widget changes; the widget itself does not.
- No Health Connect, scale or build/CI changes. Help page text on the clipboard offer is updated.
