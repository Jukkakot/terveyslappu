# Proposal: food-log

## Why

Meals vary from day to day and the calories of everyday portions are unknown, so the user cannot
tell whether everyday eating matches the goal (weight going down). A light food log for a learning
period, written to Health Connect like the weight, shows what the usual meals contain and how
eating and weight move together (product.md "Food log: why", BR1–BR6).

## What Changes

- New **Ruoka** screen for one day: the slots Aamupala, Lounas, Päivällinen, Iltapala and Väliin,
  each with its items and kcal, the day's total, and arrows to earlier days. Opened from a small
  "Syöty tänään" row on the home screen; the weigh-in stays the home screen's only filled action.
- Logging in a slot: frequent items of that slot first, saved meals (several items in one tap),
  saved dishes (kcal per 100 g with a usual portion, only the grams change), fixed items (kcal per
  serving) and quick add of a bare kcal number. A new dish is saved once with its name and kcal
  (the user asks an AI chat for the number); the kitchen scale is used only to learn the usual
  portion.
- **Herkku** mark on an item (a saved item carries it, quick add can set it): treats are counted
  apart from meals everywhere they are summed (BR4).
- Every logged item is written to Health Connect as a nutrition record with the slot's meal type
  (Väliin and Iltapala as snack) and its energy; edit and delete with undo, as for weights.
- Historia gets a third tab **Energia** next to Paino and BMI: daily kcal bars (treat share in its
  own tone), a 7-day average line, and the period's weight line in a small chart under it on the
  same time axis (BR2), plus period statistics (average kcal per logged day, treats per day, logged
  days). Days without a log are gaps, not zeros. No budget, no "calories left", no conclusions, no
  streaks or red numbers (BR2, BR5).
- The data file (Vie/Tuo tiedot, backup) carries two new kinds: the logged foods and the saved
  items, so a phone move and a restore keep the learnt dishes.
- **Asetukset regrouped** (user 2026-11-14): the long page becomes a list of rows (icon, name,
  one line of current state, chevron), each opening its own sub-screen: Omat tiedot, Automaattinen
  punnitus, Ruokakirjaus (new: manage saved items and meals), Tiedot ja varmuuskopio, Ulkoasu,
  Tuki. Warnings show on the main list's row. Links into a section open its sub-screen directly;
  back returns where the user came from. Sub-screen contents stay as they are.
- Ohje gets a section on the food log. Version 1.21.
- Steps (BR2's "moved") are **not** in this change: a separate change after this one (open
  question 1).

## Capabilities

### New Capabilities
- `food-log`: the day screen, slots, saved items and meals, quick add, the treat mark, Health
  Connect nutrition write/read/edit/delete, and the home screen's entry row.

### Modified Capabilities
- `weight-chart`: Historia's tabs gain Energia with its chart, statistics and list.
- `home-screen`: the simple home screen gains the "Syöty tänään" row; the Asetukset screen
  becomes a list of sub-screens.
- `data-file`: two new data kinds, `foods` and `food_items`.
- `help-page`: an Ohje section on the food log.

## Impact

- App screens: new Ruoka day screen and item picker/editor dialogs; home screen row; Historia
  Energia tab; Asetukset split into a main list and six sub-screens; Ohje text.
- Health Connect: new permissions READ_NUTRITION and WRITE_NUTRITION (asked when the food log is
  first opened, not at app start); reads only this app's nutrition records.
- App storage: saved items and meals, and each logged item's slot and treat mark (Health Connect's
  meal type cannot tell Iltapala from Väliin, nor a treat).
- Data file, backup, import: new parts in CSV and JSON, version stays 2 (unknown parts are already
  skipped by older readers).
- Scale, widget, tile, background weigh-in: untouched. Build/CI: no new dependency.
