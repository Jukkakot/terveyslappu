# Tasks

Checks for every group: `./gradlew test lint assembleDebug`. Test data is made up (NFR-1). Render
tests use Robolectric `@GraphicsMode(GraphicsMode.Mode.NATIVE)`, `@Config(sdk = [35], qualifiers =
"w360dp-h780dp-xhdpi")`, light and dark colour schemes, and save PNGs to
`app/build/chart-renders/<name>-<theme>.png` (path via a system property set in
`testOptions.unitTests.all`, as in the spike); they assert only that the bitmap is not blank.

## 1. Dependency and build

- [x] 1.1 Add JitPack with `exclusiveContent` for group `com.github.PhilJay.MPAndroidChart` to `settings.gradle.kts`; add `mpandroidchart = "v4.0.1"`, `mpchart-lib` and `mpchart-compose` to `gradle/libs.versions.toml` and `app/build.gradle.kts`; remove `vico` from the catalog and the build file. Verify `./gradlew :app:dependencies --configuration debugRuntimeClasspath` resolves both from JitPack and no Vico remains.
- [x] 1.2 Check the `MPChartCompose` API: if it exposes the chart instance for configuration and `update`, use it as the host; else keep `AndroidView` and remove `mpchart-compose` again (note in the commit). Verify with a throwaway composable that compiles.
- [ ] 1.3 Add the render-test output property (`chart.renders.out` → `app/build/chart-renders`) to `testOptions.unitTests.all`; run `./gradlew assembleRelease` once locally and add `proguard-rules.pro` keep rules only if R8 breaks the library. Verify the release APK builds.

## 2. Pure chart definitions (design 2)

- [ ] 2.1 Add `chart/ChartSpec.kt` (`ChartSpec`, `PanelSpec`, `AxisSpec`, `SeriesSpec` with Bars / StackedBars / Line, `ToneRef`, `BandSpec`, `LegendItem`) and the x mapping (day centre = index, axis `-0.5 .. days - 0.5`, weight at time of day, weekly bar at the week's centre). Verify `ChartSpecTest` covers the x of a day, a weight at 7.05, a weekly bar and `dayAt(x)` round trips.
- [ ] 2.2 Add `chart/TabCharts` builders for Paino, BMI (bands from `Derived.BANDS`, clipped to the scale), Syöty (food + treat stack, average, weekly), Askeleet (bars, average, weekly), Kulutus (lepo/aktiivinen stack, kokonais set, average, weekly). Verify `TabChartsTest`: y ranges equal `WeightScale` / `AxisTicks.bars`, three ticks, no entry on gap days, weekly over 90 days, lone and dense dot rules, legend entries per "Chart legends" (no keskiarvo without an average, "viikoittain" when weekly).
- [ ] 2.3 Add `chart/CustomChartSpec.of(CustomChartData)`: Allekkain one panel per kind (heights 160/136/112, messages for empty / gated / loading panels), Päällekkäin one panel in fraction units with left/right axis on the first two drawn data, hidden scale for the rest, side-by-side bar slots, per-data tones and averages; one drawn data = the stacked single panel. Verify `CustomChartSpecTest` against the "Stacked layout", "Overlaid layout", "Kulutus in own charts" and "Chart data without values" scenarios.
- [ ] 2.4 Reshape `TabReadouts` and `ChartReadout` text into a head + rows model (`ReadoutCard(head, rows: (tone, name, value))`) per the Point details, Crosshair on every Historia chart and Kulutus tab rows; remove the joined-string form. Verify `TabReadoutsTest` with the spec's example rows (Paino with time, BMI, Syöty with herkut, weekly "keskim.", Askeleet, Kulutus split / kokonais / no data, own chart "ei painoa" / "ei kirjauksia" / "ei tietoa").

## 3. Library chart renderer (design 3, 4, 5 marker, 6)

- [ ] 3.1 Add `ui/HistoryChart.kt`: one `CombinedChart` per panel from a `PanelSpec` (bar, stacked, grouped bars, line, scatter for lone/last dots, limit-line averages, y axes with 3 forced labels and unit formatters, legend from the spec with word wrap, transparent background, colours from the colour scheme, text sizes × fontScale, zero extra x margin). Verify with render tests `ChartRenderTest` saving `paino`, `energia`, `askeleet-weekly`, `kulutus` PNGs in light and dark.
- [ ] 3.2 Add `BandYAxisRenderer` (bands behind grid and data) and use it for BMI. Verify render test `bmi` PNG shows the green/yellow bands.
- [ ] 3.3 Add `ReadoutMarker : IMarker` (card, colour dots, name/value columns, left of the day near the right edge, clamped into the content rect, drawn once per chart). Verify render tests `marker-first-day` and `marker-last-day` and a unit test that the card rect stays inside the content rect for x at both ends.
- [ ] 3.4 X labels: granularity 1 (7 weekly), `labelCount` from `AxisTicks.fitCount` measured with the chart's paint, weekday labels forced for ≤ 7 days, formatter weekday / d.M. / M/yyyy, avoid first/last clipping. Verify `LargeTextTest`'s Askeleet case (360 dp, font scale 1.3) rewritten to check the chart's computed label rects do not intersect and stay inside the view, plus a unit test of the formatter.
- [ ] 3.5 Stacked panels: equal `axisLeft.minWidth`, x labels and legend only on the last panel, highlight of the chosen day on every drawn panel, marker on the topmost drawn panel. Verify render tests `own-stacked-3` and `own-overlaid-3` PNGs and a test that the panels' content left/right are equal.
- [ ] 3.6 Accessibility and actions: semantics on the host (description, "Edellinen päivä" / "Seuraava päivä" via the existing step logic, hidden polite live-region text of the marker); no "Avaa päivä" link on the chart (dropped). Verify a Compose test runs the "Edellinen päivä" action and finds today's marker text in the live region.

## 4. Screens on the new chart (design 5, 7)

- [ ] 4.1 Paino and BMI (`QuantityPage` / `PeriodPanel`): replace `WeightChart` with `HistoryChart(TabCharts.weight|bmi)`; keep range line, stats and list. Verify a smoke test in `HistoryChartsTest`: Paino and BMI show the chart node and a tap selects a day (marker text in the live region).
- [ ] 4.2 Syöty, Askeleet, Kulutus tabs: replace the Canvas charts in `EnergyPage`, `StepsPage`, `BurnPage`; keep empty, permission, source line and stats. Verify one smoke test each (existing `StepsScreenTest`, `BurnScreenTest`, Energia in `HistoryChartsTest`) and that a Syöty list day opens Ruoka.
- [ ] 4.3 Own chart tabs (`ChartPage`, tab mode of `CustomChartView` → `HistoryChart(CustomChartSpec)`); card mode untouched. Verify `CustomChartViewTest` tab cases (stacked, overlaid, gated panel's grant button still clickable) and that `HomeChartCardsTest` still passes unchanged.
- [ ] 4.4 Selection state in `HistoryScreen`: `onChoose` from the library listener, re-highlight after data changes, clear on tab/period change, editor save, leaving Historia and day off axis; remove `crosshairGestures`, `ReadoutSlot`, `TabReadout` and the Historia uses of `YAxisLabels` / `DateAxis` / `ChartLegend` (keep what the cards use). Verify `HistoryChartsTest`: swipe on the stats switches the tab, drag on the plot keeps the tab, a period change clears the choice, a reload keeps it.
- [ ] 4.5 Zoom: x-only pinch and double-tap zoom, pan when zoomed, stacked panels synced by the gesture listener, `fitScreen()` on period/tab change. Verify a unit/Robolectric test that zooming one panel sets the same visible x range on the other, and that a period change restores the full range.
- [ ] 4.6 Remove now-dead code (`WeightChart` canvas, chart Canvas bodies, unused `AxisTicks.labelBounds` users) and run lint. Verify `./gradlew test lint assembleDebug` passes and `grep -r crosshairGestures app/src/main` is empty.

## 5. Version and docs

- [ ] 5.1 Bump MINOR `appVersion` in `gradle.properties` (1.45 → 1.46). Verify `./gradlew printVersionName` shows `v1.46.<build>`.
- [ ] 5.2 Update `openspec/context/product.md` (charts on MPAndroidChart, library defaults taken, marker card, zoom; cards later), `openspec/context/roadmap.md` (backlog item done, follow-up "front page cards on the chart library"), `openspec/context/knowledge.md` "Android" chart library entry (MPAndroidChart 4 findings from the spike: no null gaps, x positions need a renderer, touch claims drags, no semantics, dp text sizes; source: spike + library docs) and the `.claude/CLAUDE.md` map (`chart/ChartSpec`, `TabCharts`, `CustomChartSpec`, `ui/HistoryChart`, `ReadoutMarker`, `BandYAxisRenderer`; Crosshair now only actions; ChartPanels card-only). Verify the files mention no removed class.

## Workflow follow-up

- Archive the change right after it is merged to `main` and pushed.
