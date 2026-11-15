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
    30 days are always readable once the app holds `READ_STEPS`, so discovery over 30 days never
    needs the history permission.
  - `AggregationResult.dataOrigins` = "origins that contributed"; whether a deduped-away origin is
    listed is not documented, so discovery does not rely on it alone.
- `minSdk 34`, `compileSdk 37`. The manifest already has `<queries>` for Samsung Health, Garmin
  Connect, Fitbit, Google Fit, Health Connect and openScale.
- Paino uses Vico 3.3.1 (`ui/WeightChart.kt`): default `VerticalAxis` labels and
  `HorizontalAxis.ItemPlacer.aligned(spacing = labelSpacing)` (`ChartData.labelSpacing` = period/5).
  Energia/Askeleet are hand-drawn Canvas bars with the top label in a `Row` above and only first
  and last date below; `PeriodWeights` (in `ui/EnergyPage.kt`) draws the weight line with only a
  top `kg` label and no legend.

## Goals / Non-Goals

**Goals:** a day's steps equal one app's own count; the default needs no action; pure, unit-tested
source rules; one axis/legend helper reusable by `combined-chart`.

**Non-Goals:** per-hour merging of our own, a source per period or per day, showing several
sources side by side, axes on the front page cards, BMI band labels, a goal line.

## Decisions

### 1. A source is an app = a set of origin packages
`StepsApp(id: String, packages: Set<String>)`. Every origin is its own app, except the phone:
origins `android` and the phone SPN are grouped into one app with id `phone` (label "Puhelin").
The SPN is recognised by (a) `getCurrentDeviceDataSource()` when
`SdkExtensions.getExtensionVersion(UPSIDE_DOWN_CAKE) >= 22`, else (b) the prefix
`com.android.healthconnect.phone.` (documented format; only a fallback). Pure:
`StepsSourceRules.group(origins: Set<String>, phoneSpn: String?): List<StepsApp>` treats
`android`, the given SPN and any origin with that prefix as the phone.
*Alternative rejected:* hide the phone and only offer watch apps (the user may have no watch).

### 2. Discovery: raw records ∪ aggregate origins, then one total per app
`HealthConnectSteps.discover(today)` over the last 30 days (today − 29 … now):
1. `readRecords(StepsRecord, pageSize = 1000, ascendingOrder = false)`, at most **5 pages**,
   collecting `metadata.dataOrigin.packageName` (newest first, so active apps are seen even when
   the phone writes many small records; `pageToken.isNullOrEmpty()` ends paging).
2. One `aggregate(COUNT_TOTAL)` over the same range without filter; add its `dataOrigins`.
3. Group (decision 1), then for each app one `aggregateGroupByPeriod` (day buckets, same range,
   `dataOriginFilter = app.packages`) → total steps and days with steps, for the pick (total) and
   the list's average per day with steps (total ÷ days, today included here since it is a
   30-day overview, not the Askeleet statistics).
Cost: ≤ 5 + 1 + N calls, run at most once a day plus on opening the setting.
Result `StepsApps(apps: List<StepsAppTotal>, at: LocalDate)`; failure → `null` and a log line.
*Alternative rejected:* raw records only (the phone's per-minute records could need ~40 pages for
30 days); aggregate origins only (semantics undocumented).

### 3. Choice, stored per phone
`StepsChoice` sealed: `Auto`, `All`, `App(id)`; encoded `auto` / `all` / `app:<id>` (id is the
package, or `phone`). Stored with the automatic pick in a new prefs file **`steps`**
(`StepsPrefs`, Android): keys `choice`, `autoPick` (id or empty), `autoPickDay` (ISO date). The
file is not in `data_extraction_rules.xml` (allow-list), so it is not backed up or transferred:
the SPN is device- and app-scoped, a moved choice could point at nothing. Unknown or corrupt value
→ `Auto`. `DataTransferRulesTest` keeps guarding the list (add `steps.xml` to its "left out"
comment).

### 4. Automatic pick rule (pure)
`StepsSourceRules.pick(totals: List<StepsAppTotal>, current: String?): String?`:
the app with the highest 30-day total; ties → lowest id (stable); the `current` app stays unless the
best total > current's total × 1.10; `current` missing from the list counts as 0; no app with
steps > 0 → `null` (read as All). Re-checked when `autoPickDay != today` at the first read of the
day, and on every opening of the sub-screen; on failure the stored pick stays (or All when none).
A change re-reads the range (state `days` reset to null → spinner) and logs.
*Alternative rejected:* re-check on every resume (5+ calls on every return to the app); notify on
switch (noise; the source line shows it).

### 5. Resolution and filtered reads
`StepsSourceRules.resolve(choice, autoPick, apps): StepsSourceState` → `packages: Set<String>?`
(null = no filter), `label` model (`Auto(app?)`, `App(app, noRecent)`, `All`) for the row, the
sub-screen and the Askeleet line. For `App(id)` the packages come from the discovered app with that
id, else `{id}` (or `{android}` ∪ SPN for `phone`), so a silent or uninstalled app still filters.
`StepsSource.firstDay(packages)` and `daily(from, to, packages)` take the filter
(`dataOriginFilter = packages ?: emptySet()`). `StepsReader.read` gains the filter argument and logs
`source=`. `StepsViewModel` resolves the filter before each read: it loads prefs, runs discovery
when due, then reads; a choice change from the setting calls `setChoice(choice)` → store, reset
`days`, re-read. Askeleet and the front page card share this state, so both follow.

### 6. Readable app names
`StepsAppNames` (Android): `phone` → "Puhelin"; else `PackageManager.getApplicationLabel` of
`getApplicationInfo(pkg, 0)`; `NameNotFoundException` → the package name. Package visibility: add
to `<queries>` an `<intent>` for `androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE` and one for
`android.intent.action.VIEW_PERMISSION_USAGE` + category `android.intent.category.HEALTH_PERMISSIONS`
(apps that integrate with Health Connect declare these), keeping the existing `<package>` entries.
No `QUERY_ALL_PACKAGES`.

### 7. Settings UI
`SettingsPage.STEPS` (row after Ruoka, before Ulkoasu, icon `ic_walk`, sub-screen tag
`stepsSourcePage`). State line from pure `SettingsSummary.steps(access, sourceState)` →
`StepsLine` (`NoAccess`, `Unavailable`, `Auto(name?)`, `App(name, warn)`, `All`; `warn` for an app
without recent steps). The sub-screen: radio rows (Material `RadioButton` + `selectable` rows,
48 dp min), "Automaattinen" with a second line "Nyt: <name>" (or "ei askelia 30 päivään"), one row
per app ("Garmin Connect" / "keskim. 8 200 askelta/pv"), then "Kaikki (Health Connectin
järjestys)". While discovery runs, the list shows a small progress indicator under "Lähde"; on
failure one line "Sovelluksia ei voitu lukea" and only Automaattinen/Kaikki/the chosen app.
Without access: reuse `StepsReason` (make it `internal`).
The Askeleet source line (`stepsSource` tag) is `labelMedium`, `onSurfaceVariant` (warning tint per
the app's existing warning colour), a 48 dp tap target, and opens the sub-screen with the existing
link rule (`withSettings(SettingsPage.STEPS, Screen.HISTORY)`, back returns to Historia on Askeleet:
keep `historyOnSteps`).

### 8. Shared axis rule (pure `chart/AxisTicks`)
- `AxisTicks.bars(max: Double, step: Double): List<Double>` → `[0, top/2, top]`, `top =
  max(2·step·ceil(max·1.1 / (2·step)), 2·step)`; steps `step = 1000` (top multiple of 2 000), kcal
  `step = 250` (top multiple of 500): the same tops as today's code.
- `AxisTicks.line(min: Double, max: Double): List<Double>` → `[min, (min+max)/2, max]` (the weight
  scale stays as is: whole kilos, ≥ 4 kg span; BMI likewise).
- `AxisTicks.dates(from: LocalDate, to: LocalDate): List<LocalDate>`: `span = days(from, to)`;
  span 0 → `[from]`, 1 → `[from, to]`; else `n` = the first of 5, 4, 3 where `span % (n−1) == 0`,
  otherwise 5; dates at `from + round(i·span/(n−1))`. (7 days → 4 labels 2 days apart; 30 → 5.)
- Formatting stays in `ui/Format` (`formatSteps`, `formatKcal`, `formatKg` with ≤ 1 decimal).
- Canvas charts (Energia, Askeleet, `PeriodWeights`): a `Row` of a label column of fixed width
  `AXIS_LABEL_WIDTH` (measured once from the widest label of the bar chart and the weight line on
  that tab, via `rememberTextMeasurer`, so both rows share it) + the plot. Labels right-aligned,
  vertically centred on their guideline (top label clamped inside). The date row is under the plot
  only (offset by the label column), first label start-aligned, last end-aligned, middle centred on
  their x (`x(day)` centre of the day's slot, the same as the bars).
- Paino/BMI (Vico): `VerticalAxis.rememberStart(itemPlacer = <custom VerticalAxis.ItemPlacer
  returning AxisTicks.line(minY, maxY)>, guideline = faint line)` and `HorizontalAxis` with a custom
  `HorizontalAxis.ItemPlacer` whose label values are the `AxisTicks.dates` offsets (days since
  `origin`; for Kaikki `to` = today). If Vico 3.3.1's placer interface cannot return explicit
  values, set `labelSpacing`/offset so the labels fall on the same dates and say so in the task.
  `ChartData.labelSpacing` goes when unused.
- The helper takes no Compose types, so `combined-chart` reuses it for its panels.

### 9. Legends
One shared `ChartLegend(entries: List<LegendEntry>)` composable (`FlowRow`, swatch box for bars,
2 dp line for lines, `labelSmall`), moved out of `EnergyPage`'s private `Legend`. Paino: "paino";
BMI: "BMI"; Energia: ruoka, herkut, 7 pv keskiarvo, paino; Askeleet: askeleet, 7 pv keskiarvo,
paino. "paino" only when `PeriodWeights` draws something. One legend per tab, under the date row
(replacing Energia's and Askeleet's legend between the bars and the weight line, so the two plots
sit together on one time axis).
Strings lower-case as the existing ones.

### 10. Logging (no step counts, ever)
- `steps.sources ok apps=<n> records=<n> pages=<n> ms=` / `steps.sources failed cause= ms=`
- `steps.source choice=auto|all|app pick=<pkg|phone|none> reason=start|daily|settings|user`
  once per resolution change; `steps.source switch from=<pkg|phone|none> to=<…>` on an automatic
  change.
- `steps.read ok ... source=<pkg|phone|all>` (existing line gains the field).
The SPN is never logged (`phone`). Add the events to the log list in `.claude/CLAUDE.md`.

## Risks / Trade-offs

- [Garmin writes some hours late or misses blocks (knowledge: 10–20 % undercounted days)] → with
  Garmin chosen those hours are simply missing, by the user's choice; "Kaikki" stays available.
- [A user without the phone SPN API (old SDK extension) gets new phone steps under an SPN not
  matched] → prefix fallback; if neither matches, the SPN shows as its own app under its package
  name (still choosable), not lost.
- [Discovery's 5 pages miss a rarely writing app] → the aggregate `dataOrigins` union adds it when
  Health Connect lists it; a chosen app is always kept in the list.
- [The 10 % margin delays a real change of habit by days] → accepted; the user can pick an app.
- [Label column takes ~60–80 dp of a 360 dp phone] → the same as Paino's Vico axis today; legibility
  of units was the user's ask.
- [Weight midpoint like 97,5 kg] → fine and honest; no change to the weight scale.

## Migration Plan

No data migration: missing prefs = `Auto`. First start after the update runs discovery before the
first steps read (the card shows its spinner a moment longer). Rollback: older build ignores the
`steps` prefs file.

## Open Questions

- Whether `AggregationResult.dataOrigins` lists deduped-away origins: check from the
  `steps.sources` log (`apps=` vs. what Health Connect's settings show); no change to the approach.
