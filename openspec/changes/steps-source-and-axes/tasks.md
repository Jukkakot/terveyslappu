# Tasks

Read `design.md` first; decision numbers below refer to it. Test data is made up (NFR-1). Checks
after each group: `./gradlew test lint assembleDebug`. Commit after each group.

## 1. Source rules (pure)

- [ ] 1.1 New `steps/StepsSources.kt`: `StepsApp(id, packages)`, `StepsAppTotal(app, total, days)`,
  `StepsChoice` (`Auto`, `All`, `App(id)`) with `encode()`/`decode()` (corrupt or empty → `Auto`),
  `StepsSourceRules.group(origins, phoneSpn)` (decision 1: `android`, the SPN and the
  `com.android.healthconnect.phone.` prefix → one app `phone`), `pick(totals, current)` (decision 4:
  most steps, 10 % margin for the current app, ties → lowest id, nothing > 0 → null) and
  `resolve(choice, autoPick, apps)` → `StepsSourceState(packages: Set<String>?, line)` (decision 5;
  a chosen app missing from `apps` still filters on its packages and is marked `noRecent`).
  Verify: `StepsSourcesTest` covers grouping (old `android` + SPN + prefix → one phone app; other
  packages separate), pick (240k vs 90k; 200k current vs 210k keeps; 100k current vs 150k switches;
  tie; empty → null; current absent), resolve (Auto/All/App present/App absent/`phone` absent →
  `android` + SPN), choice encode/decode round trip and corrupt input.
- [ ] 1.2 `SettingsSummary.steps(access, state)` → `StepsLine` (decision 7) with `warn` only for a
  chosen app without recent steps. Verify: cases added to `SettingsSummaryTest`.

## 2. Health Connect reads and state

- [ ] 2.1 `StepsSource` interface: `firstDay(packages: Set<String>?)`, `daily(from, to, packages)`,
  `discover(today): List<StepsAppTotal>` and `phoneSpn(): String?`; `HealthConnectSteps` implements
  them (decision 2: ≤ 5 newest-first record pages of 1 000 over today−29…now, ∪ the no-filter
  aggregate's `dataOrigins`, group, one day-bucket aggregate per app with `dataOriginFilter`;
  `getCurrentDeviceDataSource()` only when SDK extension ≥ 22, else null). Keep `LocalDateTime`
  bounds for the day buckets. Update the fake in `StepsReadTest`.
- [ ] 2.2 `StepsReader.read(wanted, today, packages)` passes the filter to `firstDay` and `daily`
  and adds `source=` (`all`, `phone` or the package) to `steps.read`. Verify: `StepsReadTest` checks
  the fake receives the filter and the log field, and the existing fallback cases still pass.
- [ ] 2.3 `StepsPrefs` (prefs file `steps`: `choice`, `autoPick`, `autoPickDay`), singleton next to
  the others in `TerveysApp`; `steps.xml` stays out of `data_extraction_rules.xml` (extend its
  comment and `DataTransferRulesTest`'s expectation of what is left out, if it lists them).
- [ ] 2.4 `StepsViewModel`: before a read, resolve the source (discovery when `autoPickDay != today`
  under `Auto`, or always when asked by the sub-screen via `discoverNow()`); expose
  `sources: StateFlow<StepsSourcesUi>` (apps with names and averages, loading, failed, choice,
  resolved state); `setChoice(choice)` stores, resets `days` to null and re-reads; an automatic
  switch resets and re-reads too. Logging per decision 10 (`steps.sources`, `steps.source`,
  `steps.source switch`; SPN logged as `phone`; never totals). Discovery failure keeps the stored
  pick. Verify: a unit test with the fake source and a fake prefs/clock: first start runs discovery
  once, the second read on the same day does not, the next day does; a switch re-reads; failure
  keeps the earlier pick; a log capture holds no step numbers.
- [ ] 2.5 `StepsAppNames` (decision 6) and the two `<intent>` entries in the manifest's `<queries>`.
  Verify: Robolectric test: unknown package → its package name, `phone` → "Puhelin".

## 3. Asetukset → Askeleet and the Askeleet source line

- [ ] 3.1 `SettingsPage.STEPS`, the row "Askeleet" (`rowSteps`, `ic_walk`) after Ruoka and before
  Ulkoasu with the `StepsLine` text (warning look as the other rows), the sub-screen
  (`stepsSourcePage`) per decision 7: radio rows with Automaattinen + "Nyt: …", one row per app with
  "keskim. N askelta/pv", "Kaikki (Health Connectin järjestys)", loading and failure states,
  `StepsReason` without access; opening it calls `discoverNow()`. Back rule unchanged
  (`MainUiState.back()`).
- [ ] 3.2 Askeleet: the source line under the chart (`stepsSource`), tap → `withSettings(STEPS,
  HISTORY)` returning to Askeleet; the empty-period message per source ("Garmin Connect: ei askelia
  tällä jaksolla" + "Vaihda lähde" button) vs. the existing connect-Garmin text for All/Auto-none.
  The home card only follows the state (no line).
- [ ] 3.3 Smoke tests (`SettingsScreenTest`, `StepsScreenTest`): the row shows "Automaattinen:
  Garmin Connect" with a fake state; the sub-screen lists the apps and a tap on an app calls
  `setChoice`; Askeleet shows the source line and its tap opens the sub-screen; back returns to
  Askeleet (`MainUiStateTest`).

## 4. Chart axes and legends

- [ ] 4.1 New pure `chart/AxisTicks.kt` (decision 8: `bars`, `line`, `dates`). Verify:
  `AxisTicksTest`: 9 400 steps → 0/6 000/12 000; kcal 2 200 → 0/1 250/2 500; 95..100 → 95/97.5/100;
  dates for 1, 2, 7 (4 labels: 9., 11., 13., 15.11.), 30, 90, 365 and 3-year spans: first = from,
  last = to, 3–5 labels, even spacing (±1 day).
- [ ] 4.2 Canvas charts: Energia and Askeleet bars and `PeriodWeights` get the label column (shared
  width per tab), three guidelines and labels with units, and the `AxisTicks.dates` row under the
  plot; replace the existing top-label `Row`s and the first/last date `Row`. Use `formatKg` with at
  most one decimal for the weight labels.
- [ ] 4.3 Paino/BMI (Vico): custom vertical and horizontal item placers from `AxisTicks.line` and
  `AxisTicks.dates`; BMI labels stay bare numbers; remove `ChartData.labelSpacing` if unused
  (update `ChartDataTest`).
- [ ] 4.4 Shared `ChartLegend` (decision 9) used by all four tabs; "paino" only when weights are
  drawn; FlowRow wraps.
- [ ] 4.5 Smoke tests: Askeleet shows `0 askelta` and the top label and 3–5 date labels; Energia's
  legend has "paino" with weights and not without; Paino shows its legend. Check
  `LargeTextTest` still passes (labels at 1.3× font scale must not overlap on 360 dp: if they do,
  shorten to 3 dates on narrow widths and note it in the task).

## 5. Help, context, version

- [ ] 5.1 Ohje: replace the priority-list advice in `help_steps_body` with the source setting (one
  app's own count; Automaattinen = most steps in 30 days; Kaikki = Health Connect's merge).
- [ ] 5.2 Context: `.claude/CLAUDE.md` map (`StepsSources`, `StepsPrefs`, `StepsAppNames`,
  `chart/AxisTicks`, `ChartLegend`, `SettingsPage.STEPS`) and log list (decision 10);
  `openspec/context/knowledge.md` "Steps in Health Connect": add the gap-filling finding, the SPN
  rule (`android` + SPN, `getCurrentDeviceDataSource`, don't hard-code) and the
  `READ_STEPS`-reads-origins fact, with sources; product.md decision line for the one-source rule;
  roadmap: note the item done. Leave `context/questions.md` alone unless the master says otherwise.
- [ ] 5.3 Bump MINOR of `appVersion` in `gradle.properties` (1.29 → 1.30) as the last commit; run
  `./gradlew test lint assembleDebug`.

## Workflow follow-up

- Before `combined-chart` is applied, update its design (item 6 and "Three panels are small") to
  use `AxisTicks` and `ChartLegend`.
- Archive: steps "Two sources, one total" scenario is replaced by the modified requirement.
