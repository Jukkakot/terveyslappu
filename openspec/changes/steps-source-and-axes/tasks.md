# Tasks

Read `design.md` first; decision numbers below refer to it. Test data is made up (NFR-1). Checks
after each group: `./gradlew test lint assembleDebug`. Commit after each group.

## 1. Source rules (pure)

- [x] 1.1 New `steps/StepsSources.kt`: `StepsApp(id, packages)`, `StepsAppTotal(app, total, days,
  averageExToday)`, `StepsChoice` (`Auto`, `All`, `App(id)`) with `encode()`/`decode()` (corrupt or
  empty → `Auto`), `StepsSourceRules.group(origins, phoneSpn)` (decision 1: `android`, the SPN and
  the `com.android.healthconnect.phone.` prefix → one app `phone`), `pick(totals, current)`
  (decision 4: most steps, 10 % margin for the current app, ties → lowest id, nothing > 0 → null),
  `averageExToday` (today's steps left out of total and day count) and `resolve(choice, autoPick,
  apps, checkedToday)` → `StepsSourceState(packages: Set<String>?, key, line)` (decision 5; a
  chosen app missing from `apps` still filters on its packages; `noRecent` is null until a check
  succeeded today, then `total == 0`). Verify: `StepsSourcesTest` covers grouping (old `android` +
  SPN + prefix → one phone app; other packages separate), pick (240k vs 90k; 200k current vs 210k
  keeps; 100k current vs 150k switches; tie; empty → null; current absent), list order by total
  next to averages (150 000 on 30 days vs 120 000 on 12 days: order Garmin, phone; averages
  5 000 and 10 000), today left out of the average, resolve (Auto/All/App present/App absent/`phone`
  absent → `android` + saved packages; `noRecent` null before the check), choice encode/decode round
  trip and corrupt input.
- [x] 1.2 `SettingsSummary.steps(access, state)` → `StepsLine` (decision 7: "Ei lupaa" for NEEDS
  and DENIED, blank for CHECKING, Unavailable, the row from the stored pick before any check, `warn`
  only for `noRecent == true`). Verify: cases added to `SettingsSummaryTest`.
- [x] 1.3 Pure check scheduling `StepsCheckRule.due(today, autoPickDay, lastFailureAt, now,
  foreground)` and `subScreenAllowed(lastFinishedAt, now)` (decision 4: due when `autoPickDay !=
  today` on a foreground read; after a failure not before 1 h; sub-screen check not within 1 min of
  the last finished check). Verify: `StepsCheckRuleTest`: same-day no repeat, next day due, failure
  at 10.00 → no at 10.30, yes at 11.15 (a failure does not write `autoPickDay`), background never,
  sub-screen within a minute no.

## 2. Health Connect reads and state

- [x] 2.1 `StepsSource` interface: `firstDay(packages: Set<String>?)`, `daily(from, to, packages)`,
  `check(today): List<StepsAppTotal>` and `phonePackages(): Set<String>`; `HealthConnectSteps`
  implements them (decision 2: one day-bucket `aggregateGroupByPeriod` with `dataOriginFilter` per
  candidate — phone, the manifest step apps Samsung Health / Garmin Connect / Fitbit / Google Fit,
  stored choice, stored pick — then ≤ 5 newest-first record pages of 1 000 ∪ the unfiltered
  aggregate's `dataOrigins` for unknown apps; a quota or any exception fails the whole check).
  `getCurrentDeviceDataSource()` only when SDK extension ≥ 22, wrapped in `runCatching` (null =
  fall back to the prefix, then the saved packages, then `android`). Keep `LocalDateTime` bounds
  for the day buckets. Update the fake in `StepsReadTest`.
- [x] 2.2 `StepsReader.read(wanted, today, packages)` passes the filter to `firstDay` and `daily`
  and adds `source=` (`all`, `phone` or the package) to `steps.read`; the range starts at the later
  of the period and the source's first day, no per-day fallback. Verify: `StepsReadTest` checks the
  fake receives the filter and the log field, a source with only recent history leaves earlier days
  as gaps even though the fake's other app has them, and the existing fallback cases still pass.
- [x] 2.3 `StepsPrefs` (prefs file `steps`: `choice`, `autoPick`, `autoPickDay`, `phonePackages`,
  `seen.<id>`), singleton next to the others in `TerveysApp`; `steps.xml` stays out of
  `data_extraction_rules.xml` (extend its comment and `DataTransferRulesTest`'s expectation of what
  is left out, if it lists them).
- [x] 2.4 `StepsViewModel` (decisions 4, 5, 7): resolve the source before a read; run the check per
  `StepsCheckRule` under every choice (first read with no stored pick under `Auto`: check, then
  read; otherwise read at once, check after, re-read only when the source changed); entering the
  sub-screen (not a resume) calls `checkNow()`, and so does access becoming GRANTED while it is
  visible; one check at a time; a source key and generation drop stale read/check results;
  `setChoice(choice)` stores, bumps the generation, cancels a running read, resets `days` to null
  and re-reads; a failed read after a change shows the read-failure state, never the old numbers.
  Expose `sources: StateFlow<StepsSourcesUi>` (apps with names, averages and last seen, checking,
  failed, choice, resolved state, stored pick for Auto). Logging per decision 10 (SPN as `phone`;
  never totals). Verify: unit tests with a fake source, prefs and clock: first start checks once
  then reads, the second read the same day does not check, the next day does (read first, check
  after); a failed check retries after an hour and keeps the earlier pick; the check runs under App
  and All and leaves the source alone; a switch re-reads; a late result of an older source is
  dropped; `setChoice` during a running read cancels it; a failed read after `setChoice` is the
  failure state; a log capture holds no step numbers.
- [x] 2.5 `StepsAppNames` (decision 6) and the two `<intent>` entries in the manifest's `<queries>`.
  Verify: Robolectric test: unknown package → its package name, `phone` → "Puhelin".

## 3. Asetukset → Askeleet and the Askeleet source line

- [x] 3.1 `SettingsPage.STEPS`, the row "Askeleet" (`rowSteps`, `ic_walk`) after Ruokakirjaus and
  before Ulkoasu with the `StepsLine` text (warning look as the other rows), the sub-screen
  (`stepsSourcePage`) per decision 7: radio rows with Automaattinen + "Nyt: …", one row per app with
  "keskim. N askelta/pv" (and "ei askelia 30 päivään" / "viimeksi d.M." for a no-recent app),
  "Kaikki (Health Connectin järjestys)", checking and failure states, `StepsReason` without access
  (wire the same grant / open-settings actions as Askeleet; no request on open); entering it with
  access calls `checkNow()`, as does access turning GRANTED while it is shown. Apps ordered by
  30-day total, most first; a chosen app missing from the check last. While the source changes, the
  home card and Askeleet show their existing loading state. Back rule unchanged
  (`MainUiState.back()`).
- [x] 3.2 Askeleet: the source line (`stepsSource`) whenever access is granted (loading, empty and
  stale states too), directly under the chart or the empty-period message and above the statistics;
  tap → `withSettings(STEPS, HISTORY)` returning to Askeleet. The empty-period message for one app,
  chosen or Auto-picked: "<app>: ei askelia tällä jaksolla" + "Vaihda lähde" button; All or Auto
  with no pick keeps the connect-Garmin text. The home card only follows the state (no line, its
  empty text unchanged).
- [x] 3.3 Smoke tests (`SettingsScreenTest`, `StepsScreenTest`): the row shows "Automaattinen:
  Garmin Connect" from the stored pick before any check; "Ei lupaa" for NEEDS and DENIED; the
  sub-screen lists the apps and a tap on an app calls `setChoice`; Askeleet shows the source line
  (also while loading) and its tap opens the sub-screen; Auto-picked app with no steps in the period
  shows "Vaihda lähde"; back returns to Askeleet (`MainUiStateTest`). The Asetukset list has seven
  rows in the spec's order.

## 4. Chart axes and legends

- [x] 4.1 New pure `chart/AxisTicks.kt` (decision 8: `bars`, `line`, `dates(from, to, count)`,
  `fitCount(...)`, date format switch at span ≥ 365 days). Verify: `AxisTicksTest`: 9 400 steps →
  0/6 000/12 000; kcal 2 200 → 0/1 250/2 500; 95..100 → 95/97.5/100; dates for spans 0, 1, 2, 7
  (4 labels: 9., 11., 13., 15.11.), 30, 90, 364, 365, 3 years: first = from, last = to, 3–5
  labels, even spacing (±1 day), the same values for weekly and daily bars; `fitCount` takes 5 when
  they fit, fewer for wide labels, never fewer than 3; 364 days → `d.M.`, 365 days → `M/yyyy`.
- [x] 4.2 Canvas charts: Energia and Askeleet bars and `PeriodWeights` get the label column (shared
  width per tab), three guidelines and labels with units (top and bottom labels clamped inside the
  plot), the weight strip of about 80 dp with all three labels (the unit on the top label only when
  three labels with units do not fit), and the `AxisTicks.dates` row under the plot with the count
  from `fitCount` measured via `rememberTextMeasurer`; replace the existing top-label `Row`s and the
  first/last date `Row`. Use `formatKg` with at most one decimal for the weight labels.
- [x] 4.3 Paino/BMI (Vico): custom vertical and horizontal item placers from `AxisTicks.line` and
  `AxisTicks.dates` (same count rule); BMI labels stay bare numbers; remove
  `ChartData.labelSpacing` if unused (update `ChartDataTest`). Verify: a test compares the Vico
  placer's label values with `AxisTicks.dates` for the same periods (1 vk, 1 kk, 3 kk, 1 v, Kaikki).
  *Done:* Vico 3.3.1's placer interfaces take explicit values (`FixedXPlacer`, `FixedYPlacer` in
  `ui/WeightChart.kt`); the placer's x values are `ChartData.labelXs`, which `AxisTicksTest` compares
  with `AxisTicks.dates`. Vico centres its labels (extremes shifted inside), so `fitCount`'s
  start/end alignment is an approximation there.
- [x] 4.4 Shared `ChartLegend` (decision 9) used by all four tabs; "paino" only when weights are
  drawn; FlowRow wraps; "viikoittain" in the legend row over 90 days, replacing the
  `steps_weekly` / `energy_weekly` note lines.
- [x] 4.5 Smoke tests: Askeleet shows `0 askelta` and the top label and 3–5 date labels; Energia's
  legend has "paino" with weights and not without; Paino shows its legend; the 1 v period shows
  month/year labels. `LargeTextTest` at 360 dp and font scale 1.3: label bounds (y labels, date
  labels, legend) do not intersect; if they do, reduce dates or drop the units from the middle and
  bottom labels per decision 8 and note it in the task.
  *Done:* no overlap at 360 dp / 1.3 for 1 vk, 1 kk and 1 v, nothing reduced. The test runs in
  Robolectric's native graphics mode (legacy mode measures text with fake widths).

## 5. Help, context, version

- [x] 5.1 Ohje: replace the priority-list advice in `help_steps_body` with the source setting (one
  app's own count; Automaattinen = most steps in 30 days; Kaikki (Health Connectin järjestys) =
  Health Connect's merge and shows the full history, one sentence).
- [x] 5.2 Context: `.claude/CLAUDE.md` map (`StepsSources`, `StepsCheckRule`, `StepsPrefs`,
  `StepsAppNames`, `chart/AxisTicks`, `ChartLegend`, `SettingsPage.STEPS`) and log list (decision
  10); `openspec/context/knowledge.md` "Steps in Health Connect": add the gap-filling finding, the
  SPN rule (`android` + SPN, `getCurrentDeviceDataSource`, don't hard-code) and the
  `READ_STEPS`-reads-origins fact, with sources; product.md decision line for the one-source rule;
  roadmap: note the item done. Leave `context/questions.md` alone unless the master says otherwise.
- [ ] 5.3 Bump MINOR of `appVersion` in `gradle.properties` (MINOR +1) as the last commit; run
  `./gradlew test lint assembleDebug`.

## Workflow follow-up

- Order: this change, then `home-card-layout`, then `custom-charts`.
- Before `custom-charts` is applied: `custom-charts` design 5–8 use `AxisTicks`, `ChartLegend` and
  the shared label column; extract the drawers from the new look.
- Archive: steps "Two sources, one total" scenario is replaced by the modified requirement; the
  home-screen "Asetukset screen" requirement is replaced by the modified one.
