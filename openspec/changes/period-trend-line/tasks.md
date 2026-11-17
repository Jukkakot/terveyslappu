# Tasks

## 1. Trend rule

- [ ] 1.1 Add `commons-math3` 3.6.1 to `gradle/libs.versions.toml` and `app/build.gradle.kts` (through the Google mirror like the other deps); check the release build's R8 keeps working.
- [ ] 1.2 `chart/TrendLine`: segments at gaps over 30 days, drop segments under 10 points, LOESS per segment with the window rule of design 2, `at(day)` interpolation.
- [ ] 1.3 Unit tests with made-up data: a rising noisy year follows the rise within a tolerance; a gap splits the line; under 10 points gives nothing; one outlier barely moves the line; 3 kk is less smooth than 2 v (window).

## 2. Chart definitions

- [ ] 2.1 `SeriesSpec.trend`, `LegendLabel.TREND`; the shared `averageOrTrend` helper (period over 31 days and at least 10 values → trend, else flat average).
- [ ] 2.2 `TabCharts`: Syöty, Askeleet, Kulutus use the helper (daily values also when bars are weekly); Paino and BMI get the trend on long periods.
- [ ] 2.3 `CardCharts`: big food, steps, Kulutus and weight cards on 3 kk; `CustomChartSpec`: Keskiarvo on → helper per drawn data, shared "trendi" legend entry.
- [ ] 2.4 `TabReadouts`: "trendi" row on the marker card.
- [ ] 2.5 Tests: spec choice by period (1 kk flat, 3 kk trend, few values flat), legend entries, readout row.

## 3. Drawing

- [ ] 3.1 `HistoryChartView`: trend segments as solid lines (design 5), card mode width, over bars and value lines, not highlightable.
- [ ] 3.2 Chart editor: the Keskiarvo switch's supporting text.
- [ ] 3.3 One smoke test that a long-period tab binds a trend data set.

## 4. Context and release

- [ ] 4.1 `product.md`: replace "never smoothed values" with the trend rule (user 2026-11-17); glossary term **trendi**.
- [ ] 4.2 Ohje: one sentence on the trend line where the charts are described.
- [ ] 4.3 Global `~/.claude/knowledge.md` Charts: a bullet on LOESS trend lines over noisy daily data with a source (claude-config repo).
- [ ] 4.4 Roadmap backlog entry done; bump MINOR in `gradle.properties`.
