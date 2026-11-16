# Proposal

## Why

Historia's charts run on MPAndroidChart since charts-on-mpandroidchart (v1.46), but the front page
cards still draw their small charts by hand on a Compose Canvas (`ui/ChartPanels`, four Canvas
charts in `ui/HomeScreen`, the card branch of `ui/CustomChartView`). Two drawing paths for the
same data drift apart: every chart rule (scales, bar shares, averages, colours) is written twice
and can disagree between a card and its Historia tab, which breaks "Data reads one way only".
"Stay on the library" (user 2026-11-16) asks for one path.

## What Changes

- The weight, food, steps, Kulutus and own chart cards draw their charts with the same library
  chart component and chart definitions (series, scales, tones, average lines) as Historia, in a
  card mode: not interactive (the card's own tap, long-press edit mode and drag keep working), no
  marker, no zoom, no animation, no library legend or date labels (the cards keep their own date
  text, legend and scale-top labels in Compose).
- The cards look the same as today; the rules in the home-screen and weight-chart specs (sizes,
  daily bars at every period, at least 1 dp bars, dots when they fit, big-card average line and
  scale top) are unchanged.
- The hand-written Canvas chart code goes: `ui/ChartPanels`, the Canvas charts in
  `ui/HomeScreen` and the card drawing in `ui/CustomChartView`.

## Capabilities

### New Capabilities

### Modified Capabilities

None: no requirement changes (the change is marked `skip_specs`).

## Impact

- App screens: the front page cards (weight, Syöty, Askeleet, Kulutus, own charts) at every size.
  Not Historia, Health Connect, scale, widget or build/CI.
- Code: new card chart definitions in `chart/` (built from `HomeSummary`, `HomeFood`,
  `StepsSeries`/`HomeSteps`, `BurnSeries`/`HomeBurn` and `CustomChartData`), a card mode of
  `ui/HistoryChartView` (`bind`), a small Compose host for one card chart; removal of
  `ui/ChartPanels` and the Canvas branches. Tests: `HomeCardsTest`, `HomeChartCardsTest`,
  `HomeEditTest`, `CustomChartViewTest`.
