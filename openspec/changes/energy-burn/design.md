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

## Review decisions (binding; they settle what the spec text leaves open)

1. **Palette (fixed hex, light / dark):** sininen #1E66D0 / #8AB4F8, vihreä #2E7D32 / #81C995,
   oranssi #B85C00 / #FCAD70, punainen #C62828 / #F28B82, violetti #7B1FA2 / #D7AEFB, turkoosi
   #00838F / #78D9EC, keltainen #8A6A00 / #FDD663, harmaa #5F6368 / #BDC1C6. Light tone (treats,
   lepo) = the full shade at alpha 0.45; a "kokonais" day (no split) = alpha 0.7 with its own
   legend entry "kokonais". Unit test: each full shade ≥ 3:1 against #F3EDF7 (light) and #211F26
   (dark); if one fails, darken/lighten it minimally and note it. "Oletus" keeps today's dynamic
   theme tones (old charts unchanged). The Kulutus tab and Kulutus's Oletus use oranssi.
2. **Health Connect total may include an estimated BMR.** Before writing the reader, check the
   Health Connect docs for `TotalCaloriesBurnedRecord` aggregation (does it add BMR or a default
   when records are missing; does `dataOriginFilter` change that) and add the finding to
   `knowledge.md` "Steps in Health Connect". Always read with an origin filter: the source's
   packages; for Kaikki or Automaattinen without a pick, the origins that wrote total or active
   calorie records in the period (one raw read to find them). A day counts only when an origin
   wrote a record that day; never show a day made only of an estimate.
3. **Source:** Kulutus uses the resolved steps source; without the steps permission the chosen app
   or the stored pick, else Kaikki. Kulutus shows the same "Lähde: …" line as Askeleet (opens
   Asetukset → Askeleet); when the source has no calories the empty text names it and offers
   "Vaihda lähde".
4. **Total < active:** total = max(total, active), lepo 0; logged as `invalid=`.
5. **Weekly bars and averages:** a week's lepo = average lepo over days with a lepo value, active =
   average active over days with an active value, height = average total over days with data
   (today out). The statistic "aktiivinen keskim." averages days with an active value.
6. **Garmin import naming and rules:** one name "Garminin historia" (preview, Asetukset row, bars
   "Garmin-historia tuotu: N päivää" / "Garmin-historia poistettu" where N = days with steps or
   kcal). A ZIP with steps or kcal days is accepted. Keep a day's kcal only when active > 0 or the
   day has steps (BMR-only days before the watch are dropped). `totalKilocalories` is not read
   (total = active + bmr). Preview lines: range, "N päivää askelia", "M päivää kulutusta".
7. **Merge:** per day the larger total wins with its own split (also kokonais-only vs split);
   steps and kcal merge independently; Kulutus's Kaikki starts at its own first day.
8. **History permission and refresh:** Kulutus finds its first readable day with ascending reads of
   both record types (the earlier), shows the shared history notice as Askeleet does, never asks
   for the history permission; refresh on resume and the "ei päivitetty" line as Askeleet.
9. **Permissions:** both needed; a partial grant counts as not granted. States, wording pattern and
   the denied flow follow steps exactly ("Kulutus: anna lupa", "Kulutus: ei saatavilla"). An own
   chart's Kulutus panel button may ask too (as for steps).
10. **Card:** default size small; `HomeLayoutCodec` puts a missing `burn` in hidden; the default
    layout holds `burn` hidden, so Palauta oletus hides it again and `isDefault` stays right; from
    Lisää kortti it comes back small. Small: "2 300/pv", the "aktiivinen N" line drops first when
    it does not fit; big: scale top like "3 000 kcal".
11. **Own charts code:** `SeriesKind` order WEIGHT, ENERGY, BURN, STEPS (sorting uses it);
    `newDraft` turns on all but BURN.
12. **Readouts and texts:** Kulutus tab tap: "Ma 12.11. · 2 300 kcal · aktiivinen 450 · lepo
    1 850" (kokonais day: "2 300 kcal"); list rows open nothing; Allekkain empty panel "Ei
    kulutustietoja tällä jaksolla".
13. **Log:** `burn.read ... back=` as steps, `imported=` days taken from the import, `invalid=`;
    `steps.garmin.read ok ... kcalDays=`; `hc.permission.burn ... granted=total|active|both`.
    Rename `garmin-steps.json` → `garmin-history.json` in CLAUDE.md, backup rules (keep the old name
    listed while it can exist).
