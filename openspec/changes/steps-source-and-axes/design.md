# Design: steps-source-and-axes

## Context

- Steps today (`steps/HealthConnectSteps.kt`): `firstDay()` = ascending `readRecords` page of 1;
  `daily(from, to)` = `aggregateGroupByPeriod` with one-day `Period` slicer and `LocalDateTime`
  bounds, **no `dataOriginFilter`**. Pure read logic in `steps/StepsRead.kt` (`StepsReader`,
  `StepsRules`, `StepsUiState`), state in `ui/StepsViewModel` (widest range, re-read on resume and
  on show), screens `ui/StepsPage` (Askeleet) and `ui/HomeScreen` (card).
- Why the total is too high (see proposal): the aggregate dedupes by app priority per stretch of
  time, so a lower-priority app fills the higher one's empty hours.
- Health Connect facts (developer.android.com "Read data", checked 2026-11-15):
  - `READ_STEPS` is enough to read `StepsRecord`s with `metadata.dataOrigin` and to aggregate with
    `dataOriginFilter`; no new permission.
  - On-device steps: before the June 2026 update their origin is `android`; after it a
    device-specific, app-scoped **synthetic package name (SPN)** such as
    `com.android.healthconnect.phone.<hash>`. Values must not be hard-coded; the SPN comes from
    framework `HealthConnectManager.getCurrentDeviceDataSource()` (API 34 + SDK extension 22; not in
    the Jetpack client). To read the phone's steps, filter on `android` **and** the SPN.
  - Reads reach 30 days before the first grant; older needs `READ_HEALTH_DATA_HISTORY`. The last
    30 days are always readable once the app holds `READ_STEPS`, so the 30-day check never needs the
    history permission.
  - `AggregationResult.dataOrigins` = "origins that contributed"; whether a deduped-away origin is
    listed is not documented, so the check does not rely on it alone.
  - Health Connect rate-limits reads (quota exceptions), which is why the check runs rarely.
- `minSdk 34`, `compileSdk 37`. The manifest `<queries>` lists Samsung Health
  (`com.sec.android.app.shealth`), Garmin Connect (`com.garmin.android.apps.connectmobile`), Fitbit
  (`com.fitbit.FitbitMobile`), Google Fit (`com.google.android.apps.fitness`), Health Connect
  (`com.google.android.apps.healthdata`), openScale and Health Sync (`nl.appyhapps.healthsync`).
- Paino uses Vico 3.3.1 (`ui/WeightChart.kt`): default `VerticalAxis` labels and
  `HorizontalAxis.ItemPlacer.aligned(spacing = labelSpacing)` (`ChartData.labelSpacing` = period/5).
  Energia/Askeleet are hand-drawn Canvas bars with the top label in a `Row` above and only first
  and last date below; `PeriodWeights` (in `ui/EnergyPage.kt`) draws the weight line with only a
  top `kg` label and no legend. The weekly note ("Viikkopalkit: ...", `steps_weekly`,
  `energy_weekly`) is a text line of its own.

## Goals / Non-Goals

**Goals:** a day's steps equal one app's own count; the default needs no action; pure, unit-tested
source rules; one axis/legend helper reusable by `custom-charts`.

**Non-Goals:** per-hour or per-day merging of our own, a source per period or per day (no fallback
to another app for days the source lacks), showing several sources side by side, a first-run notice
about the source, axes on the front page cards, BMI band labels, a goal line.

## Decisions

### 1. A source is an app = a set of origin packages
`StepsApp(id: String, packages: Set<String>)`. Every origin is its own app, except the phone:
origins `android` and the phone SPN are grouped into one app with id `phone` (label "Puhelin").
The SPN is recognised by (a) `getCurrentDeviceDataSource()` when
`SdkExtensions.getExtensionVersion(UPSIDE_DOWN_CAKE) >= 22`, else (b) the prefix
`com.android.healthconnect.phone.` (documented format; only a fallback). The framework call is
wrapped in `runCatching`; an exception or null means "not available" and falls back. The phone's
packages last seen are saved in the `steps` prefs (key `phonePackages`) after every successful
lookup and used when neither the SDK call nor the prefix finds one; with nothing saved the phone is
`android` alone. Pure: `StepsSourceRules.group(origins: Set<String>, phoneSpn: String?):
List<StepsApp>` treats `android`, the given SPN and any origin with that prefix as the phone.
*Alternative rejected:* hide the phone and only offer watch apps (the user may have no watch).

### 2. The source check: one filtered 30-day read per candidate
`HealthConnectSteps.check(today)` over the last 30 days (today − 29 … now). **Candidates**, always
checked, each with one `aggregateGroupByPeriod` (day buckets, `dataOriginFilter = app.packages`):
the phone (`android` + SPN or saved phone packages), the manifest `<queries>` step apps (Samsung
Health, Garmin Connect, Fitbit, Google Fit; Health Connect itself and openScale are not step
sources, Health Sync is found by the second step if it writes steps), the stored choice's packages
and the stored pick's packages. Then **unknown apps**:
1. `readRecords(StepsRecord, pageSize = 1000, ascendingOrder = false)`, at most **5 pages**,
   collecting `metadata.dataOrigin.packageName` (newest first; `pageToken.isNullOrEmpty()` ends).
2. One unfiltered `aggregate(COUNT_TOTAL)` over the range; add its `dataOrigins`.
3. Group (decision 1); every origin not already a candidate gets its own filtered aggregate.
"Wrote steps in 30 days" = that app's filtered total > 0 (today included). For each app the check
returns `StepsAppTotal(app, total, daysWithSteps, averageExToday)`: `total` (today included) for
the pick and the list order; the list's average = (total − today's steps) ÷ (days with steps − 1 if
today has steps), so today, being incomplete, never drags it. Cost: ≤ 7 candidate reads + ≤ 5 + 1 +
unknowns, run at most once a day (decision 4) and on entering the sub-screen. A quota exception or
any other failure = a failed check (`null`, log line); no partial list is used. An app seen with
total > 0 gets `seen.<id>` = today in the `steps` prefs (the last day the app was seen with steps;
kept for the apps of the stored choice and pick, others pruned) for the sub-screen's "ei askelia 30
päivään" line.
*Alternative rejected:* raw records only (the phone's per-minute records could need ~40 pages for
30 days); aggregate origins only (semantics undocumented).

### 3. Choice, stored per phone
`StepsChoice` sealed: `Auto`, `All`, `App(id)`; encoded `auto` / `all` / `app:<id>` (id is the
package, or `phone`). Stored in a new prefs file **`steps`** (`StepsPrefs`, Android): keys
`choice`, `autoPick` (id or empty), `autoPickDay` (ISO local date of the last **successful**
check), `phonePackages` and `seen.<id>`. The file is not in `data_extraction_rules.xml`
(allow-list), so it is not backed up or transferred: the SPN is device- and app-scoped, a moved
choice could point at nothing. Unknown or corrupt value → `Auto`. `DataTransferRulesTest` keeps
guarding the list (add `steps.xml` to its "left out" comment).

### 4. Pick rule and check timing
**Pick (pure)** `StepsSourceRules.pick(totals, current): String?`: the app with the highest 30-day
total; ties → lowest id (stable); the `current` app stays unless the best total > current's total ×
1.10; `current` missing counts as 0; no app with steps > 0 → `null` (read as All). The ranking,
the pick and the list order all use the 30-day total; only the list's displayed average leaves
today out, so order and averages can disagree (a spec scenario shows it).

**Timing.** The check runs **under every choice** (Auto, App, Kaikki): under App/All it never
changes the source, it keeps the stored pick and each app's recent-steps flag current.
- *Due* when `autoPickDay != today` (local date) and the read is a **foreground** read (resume,
  show, change); never from the background. A failed check does not write `autoPickDay`; the next
  resume at least one hour after the failure (in-memory timestamp) tries again. A successful
  check on entering the sub-screen counts as today's (it writes `autoPickDay`).
- *Order:* with no stored pick and the choice `Auto` (the read depends on the pick) the check runs
  first, then the read. In every other case (later days; App/All any time) the read uses what is
  stored at once, the check runs after it, and the range is re-read only if the resulting source
  changed.
- *Entering the sub-screen* (not a resume) runs a check, and so does access turning GRANTED while
  the sub-screen is visible; no sub-screen check starts when the last check (any outcome) finished
  under a minute ago, and the sub-screen then shows that result.
- *Unknown until checked:* `noRecent` is `null` (unknown, no warning tint) until a check has
  succeeded **today** in this process; afterwards `total == 0` → true.
*Alternative rejected:* check on every resume (≥ 7 reads each return; quota); notify on a switch
(noise; the source line shows it).

### 5. Resolution, filtered reads and races
`StepsSourceRules.resolve(choice, autoPick, apps?, checkedToday): StepsSourceState` →
`packages: Set<String>?` (null = no filter), `key` (see below), and a label model (`Auto(app?)`,
`App(app, noRecent: Boolean?)`, `All`) for the row, the sub-screen and the Askeleet line. For
`App(id)` the packages come from the checked app with that id, else `{id}` (or the phone's saved
packages for `phone`), so a silent or uninstalled app still filters.
`StepsSource.firstDay(packages)` and `daily(from, to, packages)` take the filter
(`dataOriginFilter = packages ?: emptySet()`). **Whole history, one source:** the read range starts
at the later of the period and `firstDay(packages)`; no per-day fallback to other apps; Kaikki on
Askeleet therefore starts at the chosen app's first day, not the phone's. `StepsReader.read` gains
the filter and logs `source=`.

**Races.** `StepsViewModel` holds a source `key` (the resolved choice and pick, e.g. `app:phone`) and
a generation counter bumped on every change. Each read and check captures the generation and key
when it starts; a result whose key is no longer current is dropped. Only one check runs at a time
(a second request while one runs is ignored; the running result is used). `setChoice(choice)`
stores, bumps the generation, **cancels a running read**, resets `days` to null and re-reads. After
a change a failed first read shows the existing read-failure state, never the previous source's
numbers (the old `days` are gone). Askeleet and the front page card share this state.

### 6. Readable app names
`StepsAppNames` (Android): `phone` → "Puhelin"; else `PackageManager.getApplicationLabel` of
`getApplicationInfo(pkg, 0)`; `NameNotFoundException` → the package name. Package visibility: add
to `<queries>` an `<intent>` for `androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE` and one for
`android.intent.action.VIEW_PERMISSION_USAGE` + category `android.intent.category.HEALTH_PERMISSIONS`
(apps that integrate with Health Connect declare these), keeping the existing `<package>` entries.
No `QUERY_ALL_PACKAGES`.

### 7. Settings UI and states
`SettingsPage.STEPS` (row after Ruokakirjaus, before Ulkoasu, icon `ic_walk`, sub-screen tag
`stepsSourcePage`). The row's state line is pure `SettingsSummary.steps(access, sourceState)` →
`StepsLine` (`NoAccess`, `Unavailable`, `Blank`, `Auto(name?)`, `App(name, warn)`, `All`):
- Access NEEDS and DENIED → "Ei lupaa"; CHECKING → blank; Health Connect unavailable → "Ei
  saatavilla".
- Before any check has finished in this process the row is built from the **stored pick**
  ("Automaattinen: Garmin Connect"), not from a check result.
- `warn` only for a chosen app with `noRecent == true` (never for unknown).
The sub-screen: radio rows (Material `RadioButton` + `selectable` rows, 48 dp min), "Automaattinen"
with a second line "Nyt: <name>" (or "ei askelia 30 päivään"), one row per app ("Garmin Connect" /
"keskim. 8 200 askelta/pv"; a no-recent app shows "ei askelia 30 päivään" and, if known, "viimeksi
d.M."), then "Kaikki (Health Connectin järjestys)". While a check runs a small progress indicator
sits under "Lähde"; on failure one line "Sovelluksia ei voitu lukea", the choices Automaattinen /
Kaikki / the chosen app, and, under Auto, "Nyt:" still shows the stored pick. Without access: reuse
`StepsReason` (make it `internal`).

**Askeleet source line** (`stepsSource` tag): shown **whenever access is granted** (loading, empty
and stale states too), directly under the chart (or the empty-period message) and above the
statistics; `labelMedium`, `onSurfaceVariant` (the warning colour for `noRecent == true`), 48 dp tap
target; opens the sub-screen with the existing link rule (`withSettings(SettingsPage.STEPS,
Screen.HISTORY)`, back returns to Historia on Askeleet: keep `historyOnSteps`). The empty-period
message for one app, **chosen or Auto-picked**, is "<app>: ei askelia tällä jaksolla" + button
"Vaihda lähde"; All, or Auto with no pick, keeps the connect-Garmin text. The front page card keeps
its existing empty text and shows no source line.

### 8. Shared axis rule (pure `chart/AxisTicks`)
- `AxisTicks.bars(max: Double, step: Double): List<Double>` → `[0, top/2, top]`, `top =
  max(2·step·ceil(max·1.1 / (2·step)), 2·step)`; steps `step = 1000` (top multiple of 2 000), kcal
  `step = 250` (top multiple of 500): the same tops as today's code.
- `AxisTicks.line(min: Double, max: Double): List<Double>` → `[min, (min+max)/2, max]` (the weight
  scale stays as is: whole kilos, ≥ 4 kg span; BMI likewise).
- `AxisTicks.dates(from, to, count): List<LocalDate>`: `span = days(from, to)`; span 0 → `[from]`,
  1 → `[from, to]`; else `count` (clamped to 3..5 and ≤ span + 1) dates at `from + round(i·span /
  (count−1))`, evenly by **days** (also for weekly bars: positions are plain day positions, not
  week starts). `AxisTicks.fitCount(from, to, plotWidth, gap, measure: (LocalDate) -> Float): Int`
  picks the largest of 5, 4, 3 whose labels, measured (first start-aligned, last end-aligned,
  middle centred), keep at least `gap` between neighbours; 3 if none fits. The count is therefore
  chosen by **measured fit**, not by divisibility (the week's 4 labels 9., 11., 13., 15.11. fit at
  normal width; 5 would put one 1.5 days off).
- Date format: `d.M.` when the span is < 365 days, `M/yyyy` when ≥ 365 days (1 v included).
  Formatting stays in `ui/Format` (`formatSteps`, `formatKcal`, `formatKg` with ≤ 1 decimal).
- Canvas charts (Energia, Askeleet, `PeriodWeights`): a `Row` of a label column of fixed width
  `AXIS_LABEL_WIDTH` (measured once from the widest label of the bar chart and the weight line on
  that tab, via `rememberTextMeasurer`, so both rows share it) + the plot. Labels right-aligned,
  vertically centred on their guideline, the top and bottom labels **clamped inside** the plot's
  vertical bounds (never half outside). The weight line under the bars gets a strip of about
  **80 dp** (decided: all three labels, not only top and bottom, so it matches the other charts).
  If three labels with units still do not fit that height at large text, **the unit stays on the top
  label only** (middle and bottom bare). The date row is under the plot only (offset by the label
  column) at `x(day)` = centre of the day's slot, as the bars.
- Paino/BMI (Vico): `VerticalAxis.rememberStart(itemPlacer = <custom VerticalAxis.ItemPlacer
  returning AxisTicks.line(minY, maxY)>, guideline = faint line)` and `HorizontalAxis` with a custom
  `HorizontalAxis.ItemPlacer` whose label values are the **same `AxisTicks.dates` values** (days
  since `origin`; for Kaikki `to` = today) with the count from `fitCount`. If Vico 3.3.1's placer
  interface cannot return explicit values, set `labelSpacing`/offset so the labels fall on the same
  dates and say so in the task. A test compares the Vico placer's values with `AxisTicks.dates` for
  the same period. `ChartData.labelSpacing` goes when unused.
- The helper takes no Compose types, so `custom-charts` reuses it for its panels.

### 9. Legends
One shared `ChartLegend(entries: List<LegendEntry>)` composable (`FlowRow`, swatch box for bars,
2 dp line for lines, `labelSmall`), moved out of `EnergyPage`'s private `Legend`. Paino: "paino";
BMI: "BMI"; Energia: ruoka, herkut, 7 pv keskiarvo, paino; Askeleet: askeleet, 7 pv keskiarvo,
paino. "paino" only when `PeriodWeights` draws something. **"viikoittain"** (periods over 90 days)
moves into the legend row as a plain-text entry and replaces the separate "Viikkopalkit: ..." note
lines (`steps_weekly`, `energy_weekly`); the older wording is dropped. One legend per tab, under the
date row (replacing Energia's and Askeleet's legend between the bars and the weight line, so the
two plots sit together on one time axis). Strings lower-case as the existing ones.

### 10. Logging (no step counts, ever)
- `steps.sources ok apps=<n> records=<n> pages=<n> ms=` / `steps.sources failed cause= ms=` (the
  check; `reason=start|daily|retry|settings|granted`)
- `steps.source choice=auto|all|app pick=<pkg|phone|none> reason=start|daily|settings|user`
  once per resolution change; `steps.source switch from=<pkg|phone|none> to=<…>` on an automatic
  change.
- `steps.read ok ... source=<pkg|phone|all>` (existing line gains the field).
The SPN is never logged (`phone`). Add the events to the log list in `.claude/CLAUDE.md`.

## Risks / Trade-offs

- [Garmin writes some hours late or misses blocks (knowledge: 10–20 % undercounted days)] → with
  Garmin chosen those hours are simply missing, by the user's choice; "Kaikki" stays available.
- [Picking an app hides the history other apps hold: Garmin picked on a phone with 2 years of phone
  steps starts Kaikki at Garmin's first day] → by decision; Ohje says Kaikki (Health Connectin
  järjestys) shows the full history; no first-run notice.
- [A user without the phone SPN API (old SDK extension) gets new phone steps under an SPN not
  matched] → prefix fallback, then the saved phone packages; if none match, the SPN shows as its
  own app under its package name (still choosable), not lost.
- [Up to ~13 reads per check] → once a day, foreground only; a failure is retried after an hour;
  sub-screen checks are rate-limited to one a minute.
- [The 10 % margin delays a real change of habit by days] → accepted; the user can pick an app.
- [Label column takes ~60–80 dp of a 360 dp phone] → the same as Paino's Vico axis today; legibility
  of units was the user's ask; `fitCount` drops dates before overlap and the unit can shrink to the
  top label.
- [Weight midpoint like 97,5 kg] → fine and honest; no change to the weight scale.

## Migration Plan

No data migration: missing prefs = `Auto`. First start after the update has no stored pick, so
`Auto` runs the check before the first steps read (the card shows its spinner a moment longer).
Rollback: older build ignores the `steps` prefs file.

## Open Questions

- Whether `AggregationResult.dataOrigins` lists deduped-away origins: check from the
  `steps.sources` log (`apps=` vs. what Health Connect's settings show); no change to the approach.
