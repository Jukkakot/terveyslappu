# Design

## Reading from Health Connect

- Permissions `READ_TOTAL_CALORIES_BURNED` and `READ_ACTIVE_CALORIES_BURNED` (manifest + the
  request contract; the privacy page lists them). Daily aggregates `TotalCaloriesBurnedRecord
  .ENERGY_TOTAL` and `ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL`, grouped by local day as
  steps are, filtered to the resolved steps source's origins (none = Health Connect's merge).
- Garmin may write active kcal only for workouts (sources disagree; see knowledge "Steps in Health
  Connect"). The app does not correct for this; `burn.read` logs split / total-only / active-only
  day counts so the first real log shows what Garmin writes. A follow-up decides if needed.
- Lepo = total − active, floored at 0; never computed from a formula.

## Structure

- New package `burn/`, mirroring `steps/`: pure `BurnDay(day, total, active?)`, `BurnSeries`
  (tab: bars incl. weekly, average, stats, list), `HomeBurn` (card), `BurnController`
  (access, read, merge with the import; Android-free, reuses `StepsSourceState` from the steps
  controller), Android reader `HealthConnectBurn`, `ui/BurnViewModel`, `ui/BurnPage`, card in
  `ui/HomeScreen`.
- The Garmin store becomes one history file `garmin-history.json` holding per day steps?, active?,
  rest?; an existing `garmin-steps.json` is read once as steps-only and rewritten on the next
  import (keep reading it until then). Android backup rules and `DataTransferRulesTest` follow the
  new name (both names listed while the old can exist).
- Two-tone bars: `ChartPanels.drawBars` already draws a stacked share (treats); Kulutus uses it
  with lepo as the base and aktiivinen on top.
- Colours: `ChartSeries` gets `colour: SeriesColour` (enum of the eight); codec field `colour`,
  missing → the kind's default. The palette lives in the theme (light and dark shade per colour,
  checked for ≥ 3:1 against the card surface in a unit test).
- Kind `SeriesKind.BURN`; order constant Paino, Syöty, Kulutus, Askeleet.
- Home layout: card id `burn`; `HomeLayoutCodec` treats a missing `burn` as hidden (not appended),
  the default layout lists it hidden.

## Naming

Energy intake vs expenditure is the standard split (Apple Health: dietary, active and resting
energy). UI words: Syöty, Kulutus, aktiivinen, lepo, kokonais.
