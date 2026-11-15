# Proposal

## Why

Two consistency gaps the user found in v1.44.0: the add sheet's day rows have no edit/delete
buttons while other food lists do, and Historia's Energia, Askeleet and Kulutus lists are flat day
lists while the weight list is grouped by year, month and week.

## What Changes

- Food item rows in the add sheet's day list and on the Ruoka screen get the same pencil and red
  trash buttons as the other food lists (`RowActions`): pencil opens the item editor, trash deletes
  at once with Kumoa. Tapping the row still opens the editor.
- Energia, Askeleet and Kulutus day lists are grouped exactly as the weight list: year, month, week
  (Mon–Sun, cut at month borders), newest first, every group its own row, the year level left out
  when all days are in one year, flat when the period has at most 14 days, all groups closed when
  shown and on a period change.
- A group row shows its name as the weight list does, the per-day average of the tab's main value,
  and the number of days.

## Capabilities

### New Capabilities

### Modified Capabilities
- `weight-chart`: new requirement "Grouped day lists" for the Energia, Askeleet and Kulutus lists.
- `food-log`: new requirement "Item row buttons" (sheet day list and Ruoka screen).

## Impact

App screens only: `ui/HistoryScreen` (lists), `chart/ListGroups` (grouping generalised from weights
to days), `ui/FoodSheet`, `ui/FoodScreen`. No Health Connect, widget or build change.
