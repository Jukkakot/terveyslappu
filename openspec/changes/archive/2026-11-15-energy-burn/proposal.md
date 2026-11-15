# Proposal

## Why

The user wants burned energy as its own measured thing next to weight, steps and eaten energy
(user, 2026-11-15). Garmin already splits each day's burn into active and resting kcal (the export
holds `activeKilocalories`, `bmrKilocalories`, `totalKilocalories`, and total = active + rest on
every day); the app shows Garmin's numbers and computes nothing itself (no BMR formula). Eaten and
burned must be clearly separate; the user may combine them in an own chart. Own charts also get a
colour choice per data.

## What Changes

- Naming (standard split: energy intake vs energy expenditure, as Apple Health's dietary / active /
  resting energy): the Energia tab and data become **Syöty**; the new one is **Kulutus** with the
  parts **aktiivinen** and **lepo**. No plus/minus signs, no balance.
- New Historia tab Kulutus after Askeleet: one bar per day, rest below and active on top in two
  tones of one colour (bar height = total), the flat average line, stats, day list; weekly bars
  over 90 days as on the other tabs.
- New front page card Kulutus (hidden by default, added from Lisää kortti), built like the steps
  card.
- Data from Health Connect (total and active calories burned, read only, the same source app as
  steps) plus the Garmin export import, which now also keeps each day's active and resting kcal.
- Own charts: Kulutus as a fourth data; every data gets a colour chosen from a fixed palette.

Parts touched: app screens (Historia, front page, own charts and editor, Asetukset → Askeleet,
Tuo tiedot), Health Connect (two new read permissions), app storage (Garmin history file).

## Capabilities

### New Capabilities
- `energy-burn`: burned energy (active + rest) from Health Connect and the Garmin import: reading,
  permission, days, Kulutus tab, stats, list, card.

### Modified Capabilities
- `weight-chart`: the Energia label becomes Syöty; own charts get Kulutus and per-data colours.
- `home-screen`: the Kulutus card in the grid (hidden by default).
- `steps`: the Garmin import also keeps kcal; the source setting also applies to Kulutus.

## Impact

New `burn/` package (pure series, rules, controller; Android reader), `steps/GarminSteps*` →
Garmin history with kcal, `chart/CustomCharts` + codec (kind, colour), `ui/` (Kulutus page, card,
editor colour row, `ChartPanels` stacked two-tone bars), strings, manifest permissions, Health
Connect permission request, help text, log events.
