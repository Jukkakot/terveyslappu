# Design

## Context

- Historia: `chart/ChartSpec` (`ChartSpec` → `PanelSpec` → `SeriesSpec` with `SeriesShape` Bars /
  StackedBars / Line, `AxisSpec`, average lines, tones) built by `chart/TabCharts` and
  `chart/CustomChartSpec`, drawn by `ui/HistoryChartView` (`HistoryCombinedChart.bind(spec, panel,
  style)`) inside `ui/HistoryChart` (one `AndroidView` per panel, Compose marker card, gestures,
  TalkBack).
- Cards today: `ui/HomeScreen` `Sparkline`, `FoodBars`, `StepsBars`, `BurnBars`, and
  `ui/CustomChartView` card mode, all on Canvas through `ui/ChartPanels` (`drawGuidelines` lives in
  `ui/EnergyPage`). Card data: `chart/HomeSummary`, `chart/HomeFood`, `HomeSteps`
  (`steps/StepsSeries.kt`), `HomeBurn` (`burn/BurnSeries.kt`), `chart/CustomChartData`.
- Card gestures: the card's `clickable` (food: split tap areas), long-press into edit mode and drag
  through `ui/HomeEdit` `LongPressEntry` (Reorderable lazy grid).
- Library facts (knowledge.md "MPAndroidChart 4 in practice"): no null gaps in a line, text sizes
  in dp, no accessibility, data clipped to the content rect; `HistoryCombinedChart` already sets
  `importantForAccessibility = NO` and requests disallow-intercept on sideways moves.

## Goals / Non-Goals

**Goals:** one chart definition model and one drawing path for every chart; every card looks as it
does today (the card rules below are the contract, written from today's Canvas code); taps,
long-press, drag and TalkBack unchanged.

**Non-Goals:** new card features (marker, zoom, axes on small cards), any change to Historia's
look, card layout, texts or periods.

## Decisions

1. **`chart/CardCharts`** (pure) builds one-panel `ChartSpec`s: `weight(summary)`, `food(home,
   size)`, `steps(home, size)`, `burn(home, size)`; own chart cards use `CustomChartSpec.of(...,
   card = true)` with no-op readout and access. Card rules (today's look, not Historia's):
   - Bars daily at every period, never weekly.
   - Feature cards' scale: small and wide = 0 to the highest bar (the tallest fills the height);
     big = the tab's rounded top (`AxisTicks.bars`, as today) with the dashed average.
   - Weight line tone PRIMARY (as `Sparkline`), not Historia's secondary.
   - Food: treat share in its tone; Kulutus: lepo/aktiivinen two-tone and kokonais days as today.
   - Own chart cards: the average line over the bars and under the value lines; Päällekkäin
     side-by-side bars spread over the whole day: sub-slot `1/slots` of the day, each bar 70 % of
     its sub-slot, centred in it (today's `drawBars` slot rule), not Historia's touching bars.
2. **Weight line by time:** a `ChartSpec` x-range override (`xMin`, `xMax` in days) used by `bind`
   instead of the `ChartAxis` range; x = `(t − sparkFrom) / 86 400 s`, range 0 ..
   `(sparkTo − sparkFrom) / 86 400 s`. Line 2.5 dp, other dots radius 2.5 dp when they fit
   (`ChartDots.fit` on the width minus the insets), the latest weight always a 4 dp dot (a one-point
   series or `Dots.LastBig`). Viewport offsets 4 dp on every side so the end dot is not clipped. No
   line with one weight, just the dot.
3. **Card mode of `bind`:** `PanelStyle.card = true`:
   - touch fully off: `HistoryCombinedChart.dispatchTouchEvent` returns false at once in card mode
     (no disallow-intercept), `setTouchEnabled(false)`, not clickable, long-clickable or focusable;
   - no highlight, marker, zoom, legend, description, animation, x labels;
   - y axes stay enabled with min/max from the card spec (so no library auto-scale and averages
     still draw) but `isDrawLabelsEnabled = false`; grid off on feature cards, on at the three
     ticks on own chart cards (library grid width 0.5 dp, the 1 dp bottom line drawn as an x-axis
     line; a half-clipped top grid line is accepted);
   - feature cards: a 1 dp baseline in the bar tone at 40 % (x-axis line at 0);
   - extra offsets 0 (weight card 4 dp, decision 2);
   - averages in card mode are 2-point flat dashed `LineDataSet`s (1 dp, 4/4 dp dash) placed
     before the value lines, so bars → average → lines;
   - line widths and dot radii from the card spec (own chart card line 2 dp, dots 3 dp as today).
4. **Bars at least 1 dp:** in `bind`'s card branch, `barWidth = max(share, 1 dp / px per x)`,
   recomputed on the existing `onSizeChanged` rebind; per Päällekkäin sub-slot `max(0.7 × sub-slot,
   1 dp)`.
5. **Host `ui/CardChart`:** an `AndroidView` of `HistoryCombinedChart` binding in `update` (and on
   `onReset` reuse in the lazy grid). The Modifier keeps today's `semantics { contentDescription }`
   (`home_spark`, `home_food_bars`, `home_steps_bars`, `home_burn_bars`,
   `custom_chart_description`) and test tags (`plot-<KIND>`, `plot-OVERLAID`, `plot-0`). No view
   is created where today's code draws nothing (empty bars, no `sparkScale`); the `Spacer` and the
   Compose messages (`PanelMessage`, `chart_empty`, `DatesAndLegend`, scale-top labels) stay.
   Allekkain own chart cards: one `CardChart` per drawn panel in today's `weight(1f)` / min 24 dp
   layout; `PanelSpec.heightDp` is ignored on cards.
6. **No needless rebinds:** specs and `PanelStyle` are `remember`ed by (data, period, size,
   colours, texts); a recomposition (e.g. during an edit-mode drag) does not rebind.
7. **Remove the Canvas path** after every card is switched: `ui/ChartPanels` (with `BurnBarSpec`
   and `BurnBar.spec()` in `ui/BurnPage`), `Sparkline`, `FoodBars`, `StepsBars`, `BurnBars`, the
   card drawing in `CustomChartView`; `drawGuidelines` / `BAR_PLOT` only if nothing else uses them.

## Risks / Trade-offs

- **Touch through an `AndroidView`** in a Reorderable, clickable card: device-only behaviour;
  decision 3 plus Compose tests (tap, long-press, long-press-then-drag on the chart; steps card and
  the food card's split areas). Implementer: opus.
- **Many chart views** (up to ~6): decision 6.
- **Pixel drift:** line caps and grid widths may differ by a fraction of a dp; accepted when the
  rules in decision 1–4 hold in the before/after screenshots.
