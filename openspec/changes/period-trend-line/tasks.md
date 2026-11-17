# Tasks

## 1. Trend rule

- [x] 1.1 Add `commons-math3` 3.6.1 to `gradle/libs.versions.toml` and `app/build.gradle.kts` (through the Google mirror like the other deps); check the release build's R8 keeps working.
- [x] 1.2 `chart/TrendLine`: segments at `daysBetween > 30`, drop segments under 10 points, window `W = max(21, axisDays / 6)`, bandwidth `clamp(W / spanDays, 4 / n, 1)` per segment, `at(day)` interpolation, clamp into a scale; check x strictly increasing, catch library exceptions and non-finite output (treated as no trend).
- [x] 1.3 Unit tests with made-up data: a rising noisy year follows the rise within a tolerance; a gap of 30 days joins and 31 splits; under 10 points gives nothing; no segment with 10+ values gives "too few" (caller falls back, decided after segmenting); one outlier barely moves the line; 3 kk is less smooth than 2 v (window); a failing/non-finite segment gives no trend and no exception.

## 2. Chart definitions (Historia only)

- [x] 2.1 `SeriesSpec.trend` (own field, excluded from tap picking and highlight), `LegendLabel.TREND`; the shared `averageOrTrend` helper (axis over 31 days and a segment with 10+ values → trend, else flat average; Paino/BMI none).
- [x] 2.2 `TabCharts`: Syöty, Askeleet, Kulutus use the helper (daily values also when bars are weekly; end before today; scale top = max(highest bar, average, trend's highest)); Paino and BMI get the trend (points at the time of each day's last weight, today included, values clamped into the scale).
- [x] 2.3 `CustomChartSpec` tabs: Keskiarvo on → helper per drawn data (not for data not drawn), shared "trendi" legend entry next to "keskiarvo" when both kinds. `CardCharts` and the front page stay as today (flat line at any period).
- [x] 2.4 `TabReadouts`: trend row after the data's row ("trendi 8 200 askelta/pv"; own charts "<data> trendi"), formatted like the keskiarvo rows, dot in the trend tone; weekly bar: week's middle day inside the axis, else nearest covered day, else none; no row for today on Syöty/Askeleet/Kulutus.
- [x] 2.5 Tests: spec choice by axis days (Oma 31 flat, 32 trend, short Kaikki flat, few values flat, no segment with 10+ flat), legend entries (both kinds), readout rows (week, today, own chart names), bar scale top covers the trend, tapping between weights still picks the nearest weight, a card spec never carries a trend.

## 3. Drawing

- [x] 3.1 `HistoryChartView`: trend segments as solid 2.5 dp lines over bars, under the Paino/BMI dots and the crosshair ring, not highlightable; own chart tabs in both layouts like their average lines (Päällekkäin data tone at 60 % opacity through the shared scale).
- [x] 3.2 Chart editor: the Keskiarvo switch's supporting text "Vaakaviiva jakson keskiarvon kohdalla, yli kuukauden jaksoilla trendiviiva".
- [x] 3.3 One smoke test that a long-period tab binds a trend data set.

## 4. Context and release

- [ ] 4.1 `product.md`: replace "never smoothed values" with the trend rule (user 2026-11-17, Historia only); glossary term **trendi**.
- [ ] 4.2 Ohje: one sentence on the trend line where the charts are described.
- [ ] 4.3 Global `~/.claude/knowledge.md` Charts: a bullet on LOESS trend lines over noisy daily data with a source (claude-config repo).
- [ ] 4.4 Roadmap backlog entry done; bump MINOR in `gradle.properties`.
