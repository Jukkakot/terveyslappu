# Design

## Context

Syöty, Askeleet and Kulutus (tabs, big front page cards) and own charts with Keskiarvo carry one
flat average per series (`SeriesSpec.average`, drawn by `HistoryChartView` as a dashed limit-like
data set). Paino and BMI have no average line. Periods are `Period.Last(7|30|90|365)`, `All` and
`Custom`; bars turn weekly over 90 days (`StepsSeries`, `EnergySeries`, `BurnSeries`). x is days from
the axis start, day centre (`ChartAxis`).

## Goals / Non-Goals

**Goals:** one pure trend rule shared by every chart; library smoothing; same look and legend
everywhere.

**Non-Goals:** no trend on 1 vk / 1 kk; no setting for smoothness; no trend number in the
statistics; no change to the scale rule.

## Decisions

1. **LOESS from Apache Commons Math** (`commons-math3` 3.6.1, Apache-2.0,
   `org.apache.commons.math3.analysis.interpolation.LoessInterpolator.smooth(x, y)`): local
   linear regression with tricube weights and 2 robustness iterations, so a half-logged day or one
   odd weight does not bend the line. It follows the data at the edges without the lag of a
   trailing average. Alternatives: centred moving average (jagged, needs edge rules), exponential
   moving average (lags behind, trailing only), hand-written LOESS (libraries-first rule: the
   library is tested; R8 strips the rest of the jar since `isMinifyEnabled = true`).
2. **Smoothness by period length.** Window `W = max(21, spanDays / 6)` days, where `spanDays` is
   the segment's first to last day with a value (3 kk ≈ 21 d, 1 v ≈ 61 d, 2 v ≈ 122 d). Bandwidth
   for the library = `clamp(W / spanDays, 0, 1)` raised so that it covers at least 4 points
   (`4.0 / n`); the library needs `bandwidth * n >= 2`. One constant (`TREND_WINDOW_DIVISOR = 6`)
   to tune later.
3. **Input and output.** Pure `chart/TrendLine.of(points: List<Pair<LocalDate, Double>>):
   List<TrendSegment>`; points are the same daily values the average uses (today already left out
   by the caller where the average leaves it out). Split into segments at gaps over 30 days;
   segments with fewer than 10 points are dropped; each segment is smoothed separately and returns
   one value per input day. The caller maps days to x with the chart's `ChartAxis`, so bars,
   crosshair and trend share x. `TrendLine.at(day)` interpolates linearly between the segment's
   days for the marker card row (weeks: the value at the week's middle day).
4. **Which line.** `ChartSpec` gets `SeriesSpec.trend: List<List<ChartPoint>>?` next to
   `average`. A helper `averageOrTrend(period, values)` returns either the flat average (period ≤ 31
   days, or < 10 values) or the trend segments; `TabCharts`, `CardCharts` and `CustomChartSpec` call
   it instead of setting `average` directly. Paino/BMI: trend only, never a flat average.
5. **Look.** `HistoryChartView` draws each trend segment as a `LineDataSet` without circles, 2.5 dp
   (the data's own line is 2 dp), `onSurfaceVariant` (data tone in Päällekkäin), cubic off (the
   LOESS values are already smooth), not highlightable, above bars and the value line. Card mode:
   1.5 dp. Legend `LegendLabel.TREND` "trendi", `LegendForm.LINE`.
6. **Marker card.** `TabReadouts` adds a "trendi" row (unit as the data's average row) when the
   tapped day lies inside a segment.
7. **Own charts' switch.** The label stays "Keskiarvo"; its supporting text says "Yli kuukauden
   jaksoilla trendiviiva" so the change of line is not a surprise.

## Risks / Trade-offs

- Smoothness is a taste; the divisor is one constant and screenshots of made-up data let us tune
  it before release.
- LOESS can overshoot slightly at a segment's ends; robustness iterations and the minimum of 10
  points keep it small. The scale is not widened for the trend (it stays inside the data range in
  practice); a value outside is clipped by the plot.
- Performance: Kaikki over 3 years ≈ 1 100 points; LOESS is O(n·k), well under 50 ms; computed in
  the pure series step, remembered with the spec.
