# Design

## Context

Syöty, Askeleet and Kulutus (tabs, front page cards) and own charts with Keskiarvo carry one
flat average per series (`SeriesSpec.average`, drawn by `HistoryChartView` as a dashed limit-like
data set). Paino and BMI have no average line. Periods are `Period.Last(7|30|90|365)`, `All` and
`Custom`; bars turn weekly over 90 days (`StepsSeries`, `EnergySeries`, `BurnSeries`). x is days from
the axis start, day centre (`ChartAxis`).

## Goals / Non-Goals

**Goals:** one pure trend rule shared by every chart; library smoothing; same look and legend
everywhere.

**Non-Goals:** no trend on the front page cards (user 2026-11-17); no show/hide switch for the trend on Historia tabs (decided 2026-11-17: shown by the period rule, a quiet neutral line; revisit if it disturbs in use); no trend on periods of 31 days or less; no setting for smoothness; no trend number in the
statistics; no change to the Paino/BMI scale rule.

## Decisions

1. **LOESS from Apache Commons Math** (`commons-math3` 3.6.1, Apache-2.0,
   `org.apache.commons.math3.analysis.interpolation.LoessInterpolator.smooth(x, y)`): local
   linear regression with tricube weights and 2 robustness iterations, so a half-logged day or one
   odd weight does not bend the line. It follows the data at the edges without the lag of a
   trailing average. Alternatives: centred moving average (jagged, needs edge rules), exponential
   moving average (lags behind, trailing only), hand-written LOESS (libraries-first rule: the
   library is tested; R8 strips the rest of the jar since `isMinifyEnabled = true`).
2. **Smoothness by period length.** Period length = the chart axis's day count `axisDays` (Kaikki:
   its axis), the same for every segment. Window `W = max(21, axisDays / 6)` days (3 kk 21 d, 1 v
   61 d, 2 v 122 d). LOESS bandwidth per segment = `clamp(W / segmentSpanDays, 4 / n, 1)`, `n` = the
   segment's points (the library needs `bandwidth * n >= 2`). On sparse data the window therefore
   becomes point-based; accepted. One constant (`TREND_WINDOW_DIVISOR = 6`) to tune later. Zoom never
   recomputes it; data or axis changes do.
3. **Input and output.** Pure `chart/TrendLine.of(points: List<Pair<LocalDate, Double>>, axisDays):
   List<TrendSegment>`; points are the same daily values the average uses (today left out by the
   caller for Syöty, Askeleet, Kulutus; included for Paino/BMI, whose points sit at the time of the
   day's last weight). Segments break where `daysBetween > 30` (same as the lone-dot rule); segments
   under 10 points are dropped. **If no segment has 10+ values** the data has too few values: the
   caller draws the flat average line (Syöty, Askeleet, Kulutus, own charts) or nothing (Paino, BMI);
   decided after segmenting. Each segment is smoothed separately, one value per input day. Callers map
   days to x with the chart's `ChartAxis`. `TrendLine.at(day)` interpolates linearly for the marker
   row; a weekly bar uses the week's middle day inside the axis, else the nearest covered day of that
   week, else no row. Trend values are clamped into the scale.
4. **Which line.** `ChartSpec` gets `SeriesSpec.trend: List<List<ChartPoint>>?` as its own field next
   to `average`, excluded from tap picking and highlight (a test: tapping between weights still picks
   the nearest weight). A helper `averageOrTrend(axisDays, values)` returns the flat average (axis
   31 days or less, or too few values) or the trend segments; `TabCharts` and `CustomChartSpec` call
   it. `CardCharts` and the front page do not use it and stay as today.
5. **Look.** `HistoryChartView` draws each trend segment as a `LineDataSet` without circles, solid,
   cubic off, 2.5 dp on fixed tabs, over bars, under the Paino/BMI line's dots and the crosshair
   ring, not highlightable. Own chart tabs: same placement as their average lines in both layouts;
   Päällekkäin in the data's tone at 60 % opacity, mapped through the shared scale. Neutral tone
   (onSurfaceVariant) on fixed tabs and Allekkain. Bar charts' scale top = max(highest bar, average,
   trend's highest value); the Paino/BMI scale rule is unchanged. Legend `LegendLabel.TREND` "trendi",
   `LegendForm.LINE`, solid neutral swatch; an own chart with both kinds shows "keskiarvo" and
   "trendi".
6. **Marker card.** `TabReadouts` adds a trend row after the data's row, dot in the trend tone,
   formatted like the keskiarvo rows (kg and BMI one decimal; kcal and steps "/pv"); own charts name
   it "<data> trendi". Syöty/Askeleet/Kulutus trends end before today, so today's tap has no trend
   row.
7. **Own charts' switch.** The label stays "Keskiarvo"; its supporting text is exactly "Vaakaviiva
   jakson keskiarvon kohdalla, yli kuukauden jaksoilla trendiviiva".
8. **Failure.** Check x strictly increasing before calling the library; catch its exceptions and
   treat non-finite output as no trend for that segment (then fallback per 3). A bad segment never
   crashes a tab. No trend for data not drawn (loading, no permission, BMI without height).

## Risks / Trade-offs

- Smoothness is a taste; the divisor is one constant and screenshots of made-up data let us tune
  it before release.
- LOESS can overshoot slightly at a segment's ends; robustness iterations and the minimum of 10
  points keep it small. Bar scales cover the trend's highest value; elsewhere values are clamped.
- Performance: Kaikki over 3 years ≈ 1 100 points; LOESS is O(n·k), well under 50 ms; computed in
  the pure series step, remembered with the spec.

## Implementation choices (apply, 2026-11-17)

Points the spec left open, settled the simplest way:

- Syöty tab's marker card: the trend row goes right after the "syöty" row, before "herkut".
- Weekly bar's trend row: the week's days inside the axis, nearest to the middle (Thursday)
  first, earlier day on a tie; the first one the line covers gives the value.
- Bar tabs clamp the trend into 0..top (top already covers its highest value).
- Kulutus tab: one trend per panel, carried by its first bar series (as the average was).
- Päällekkäin own charts: trend 2.5 dp like the fixed tabs; its marker row dot is the data's tone
  at 60 %. Allekkain: neutral tone in the chart and the marker row.
- Paino/BMI trend input = each day's last weight in the chart's data (the "?" weights are not
  there); its point x = that weight's time.
