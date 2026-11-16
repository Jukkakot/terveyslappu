# Design

## Context

- Historia: `chart/ChartSpec` (`ChartSpec` → `PanelSpec` → `SeriesSpec` with `SeriesShape` Bars /
  StackedBars / Line, `AxisSpec`, average lines, tones) built by `chart/TabCharts` and
  `chart/CustomChartSpec`, drawn by `ui/HistoryChartView` (`HistoryCombinedChart.bind(spec, panel,
  style)`) inside `ui/HistoryChart` (one `AndroidView` per panel, Compose marker card, gestures,
  TalkBack).
- Cards today: `ui/HomeScreen` `Sparkline` (weights by time, dots when they fit, the latest as a
  bigger dot), `FoodBars`, `StepsBars`, `BurnBars` (daily bars, big: average line), and
  `ui/CustomChartView` card mode (Allekkain panels and the Päällekkäin plot with guidelines and
  big-card scale tops), all on Canvas through `ui/ChartPanels`. Card data: `chart/HomeSummary`,
  `chart/HomeFood`, `HomeSteps` (`steps/StepsSeries.kt`), `HomeBurn` (`burn/BurnSeries.kt`),
  `chart/CustomChartData`.
- Card gestures: the card's `clickable`, the long-press into edit mode and drag through
  `ui/HomeEdit` `LongPressEntry` (Reorderable). A chart inside must not take any touch.
- Library facts (knowledge.md "MPAndroidChart 4 in practice"): no null gaps in a line (split data
  sets), text sizes in dp, no accessibility, the Compose wrapper animates (not used: we host the
  view ourselves).

## Goals / Non-Goals

**Goals:** one chart definition and one drawing path for every chart in the app; cards look as
specified today; the card's taps, long-press and drag unchanged; TalkBack on the card unchanged.

**Non-Goals:** new card features (marker, zoom, axes on small cards), any change to Historia's
look, any change to the card layout, texts or periods.

## Decisions

1. **Card chart definitions in `chart/CardCharts`** (pure): `weight(summary)`, `food(home)`,
   `steps(home)`, `burn(home)`, each returning a one-panel `ChartSpec`; own chart cards reuse
   `CustomChartSpec.of` with a card flag. Card rules that differ from Historia stay explicit there:
   daily bars at every period (never weekly), no average series below big size, Allekkain treats /
   lepo tones as the card draws today. Alternative rejected: a `card` flag inside `TabCharts`
   (mixes two sets of rules in one builder).
2. **Weight line by time:** the weight card's x is fractional days from the period start
   (`(time − from) / 86 400 s`), so several weighings a day keep their own place as on today's
   sparkline; the latest weight gets the bigger end dot (a `Dots.LastBig` style or a one-point
   series), the others dots when they fit (`ChartDots.fit`).
3. **Card mode of `bind`:** `PanelStyle` gains `card: Boolean`. In card mode: `setTouchEnabled(false)`,
   no highlight, no drag/scale/pinch, legend off, x-axis labels off, y-axis labels off (big-card
   scale tops stay Compose overlays as today), grid lines only where the card draws guidelines today
   (own chart cards), no description, no extra offsets (`setViewPortOffsets` 0 or the dot radius
   so end dots are not clipped), no animation.
4. **Host:** `ui/CardChart` composable = `AndroidView` of `HistoryCombinedChart` that calls `bind`
   in `update`; `Modifier` from the card; the view is `importantForAccessibility = no` (the card's
   own semantics speak), not focusable, not clickable, so touches go to the Compose parent.
   Compose test: a click on the chart area opens what the card's tap opens, a long-press on it
   enters edit mode.
5. **Bars at least 1 dp:** the card host sets the bar width per layout: `max(share, 1 dp in x
   units)` from the view's width and the axis days, as the Canvas did (home-screen card bars and
   weight-chart "at least 1 dp wide on a card").
6. **Remove the Canvas path** after every card is switched: `ui/ChartPanels`, `Sparkline`,
   `FoodBars`, `StepsBars`, `BurnBars`, the card drawing in `CustomChartView` (its card layout,
   labels and legend stay, drawing via `CardChart`). `DateAxis` / helpers only used by them go.

## Risks / Trade-offs

- **Touch passing through an `AndroidView`** in a card with Reorderable drag (device-only
  behaviour; Robolectric covers the Compose side). Mitigation: decision 4 plus Compose tests; the
  implementer is opus (CLAUDE.md: untestable behaviour).
- **Many chart views on the front page** (up to ~6): one bind per data change, no animation; fine
  for a few hundred points each.
- **Pixel differences** (anti-aliasing, round caps vs library line joins): accepted when the
  specified look (shapes, tones, sizes, dots, average line) holds; a screenshot of each card at
  each size (Robolectric NATIVE graphics) is compared before/after by eye once.
