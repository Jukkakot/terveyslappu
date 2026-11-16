# Proposal

## Why

After adding food in the Lisää annos sheet the user wants to look at what they ate, but the add
panel (heading, up to three chip rows, field) takes half the screen and long names wrap to two
lines, so only a few rows fit (user 2026-11-16, screenshot of the sheet).

## What Changes

- The add panel gets a handle on its top edge: a drag down (or a tap) folds it to the handle
  alone (the field goes too), a drag up (or a tap) brings the heading and chips back. Every open of the sheet starts
  unfolded. Dragging the panel never closes the sheet.
- Food rows drop the logging time in the sheet and on Ruoka ("150 g" instead of "150 g · 17.51").
- An item's name stays on one line and is shortened with "…" on narrow rows, in the sheet and on
  Ruoka (the existing rule in "Item row buttons" that the code did not follow).
- Later idea, not now (user): a row shows only name and kcal with a details button.

## Capabilities

### New Capabilities

### Modified Capabilities
- `food-log`: "Ruoka day screen" (no logging time) and "Adding an item" (compact rows
  without time, folding add panel).

## Parts touched

App screens only: the Lisää annos sheet (in the app, from the widget and the shortcut) and the
Ruoka screen's rows. No Health Connect, scale, widget or build change.
