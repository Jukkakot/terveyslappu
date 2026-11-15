# Proposal

## Why

The Lisää annos sheet lists suggestions as rows with kcal, an edit and a delete button, so it reads
like the day's log: the user keeps mistaking it for what they ate today. The sheet should show the
day's real items and keep adding clearly apart from them (mockup B, chosen 2026-11-15).

## What Changes

- The sheet opens on the day it adds to: header "<Tänään|Eilen|date> · <total> kcal" with the
  Ruoka screen's summary line under it, then the day's items in eating order (same rows as Ruoka,
  tap opens the item editor). The item just added is highlighted.
- Adding moves to a panel docked at the bottom of the sheet: the "Muuta · Kumoa" line, the heading
  "Lisää – usein tähän aikaan", the frequent products as chips "+ <name> <kcal>" (one tap adds), and
  the search field at the very bottom.
- Typing shows the existing match rows (edit, delete, "Lisää ”…” uutena") above the field, over the
  day list.
- A long press on a chip opens the same new-item editor as a row's edit button.
- The keyboard no longer rises when the sheet opens; it rises when the field is tapped.
- "Tuo ruoka-annokset" moves from the bottom of the sheet into the ⋮ menu.
- Same sheet everywhere: front page, Ruoka, widget and launcher shortcut.

## Capabilities

### New Capabilities

### Modified Capabilities
- `food-log`: Adding an item (sheet layout: day list, docked add panel, chips), Sheet messages and
  loading (keyboard on open), Removing a product from the sheet (delete only on typed match rows).

## Impact

App screens only: the add sheet (`ui/FoodSheet`), used by the Ruoka screen, the front page and
`FoodAddActivity` (widget, shortcut). No Health Connect, scale, widget look or build/CI change.
