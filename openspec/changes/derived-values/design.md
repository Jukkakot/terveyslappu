# Design

## Context

Weights come from Health Connect as `WeightEntry` (time, kg); the owner details live in
`ProfilePrefs` (`OwnerProfile`: birth year, height cm, gender, all nullable). The Historia chart
(`ui/WeightChart`, Vico) is fed by `chart/ChartData` and `chart/PeriodStats`, both built on `kg`;
the y-scale is `chart/WeightScale`. The home card is `chart/HomeSummary` + `ui/HomeScreen`.

## Goals / Non-Goals

**Goals:** one pure place for the formulas and classes; the chart, stats and scale work on any
quantity without copying them; values recompute when the owner details change.

**Non-Goals:** storing derived values anywhere; a chart with several quantities at once; age from a
full birth date (only the year is known).

## Decisions

- **New pure package `derived/`**: `Bmi` (value, `BmiClass` with word and `Tone` GREEN/YELLOW/RED,
  normal range for a height), `Bmr` (Mifflin-St Jeor), and `Quantity` (WEIGHT, BMI, BMR) with
  `valueOf(entry, profile): Double?` and `available(profile)`. The adult check (age in the weight's
  year ≥ 18) sits here too. Alternative: put them in `chart/`; rejected, the home row uses them
  without any chart. No library: each formula is one line and no library adds anything.
- **Chart, stats and scale take a value selector**: `ChartData`, `periodStats` and the scale get
  the quantity's values instead of reading `kg`; weight keeps today's output exactly (existing tests
  stay green as the guard). Alternative: copy `WeightEntry`s with `kg` replaced by the derived
  value; rejected as a lie in the type.
- **Scale rule by "kg worth"**: `WeightScale` becomes a general rule with `perKg` (1 for weight,
  1/h² for BMI, 10 for kcal) and a rounding step (1, 1, 10). Room 0.5 kg and least span 4 kg stay
  the meaning, so a change looks as large on every tab.
- **Swipe with Compose's `HorizontalPager`** over chart + stats, `TabRow` above it bound to the
  pager state; the list stays outside the pager. The pager's page count is the available
  quantities. Historia opens on page 0 (Paino); nothing is remembered.
- **BMI bands with Vico's `HorizontalBox` decorations**, one per class range clipped to the scale,
  in the tone colour at low alpha so the line and dots stay readable.
- **Tone colours in the theme**: one green, yellow and red per light and dark theme, tuned for text
  contrast on the card; the bands use the same hue faded. The class word is always next to the
  tone (no colour-only meaning).
- **Home row** is computed in `HomeSummary` (or beside it) from the latest weight and the profile;
  the hint navigates to Asetukset and scrolls to Omat tiedot (a focus flag on the Settings screen
  state in `MainViewModel`).
- **Recompute on change**: the view model combines the weights flow with the profile flow, so a new
  height redraws home and Historia at once.

## Risks / Trade-offs

- [Vico's chart may consume horizontal drags (scroll, marker) and fight the pager] → keep the chart
  unscrollable (fixed range already) and test the swipe; tabs work even if a drag is eaten.
- [Age from the year only is up to a year off] → at most 5 kcal/day in the equation; accepted.
- [BMI with today's height on old weights is wrong if the height changed] → adult height is stable;
  the user chose this (2026-11-09).
- [Red/yellow could worry] → the user wants the absolute classes in traffic-light colours; the word
  says exactly which class.
