# Design

## Context

See proposal.md for why. Today each Historia chart is its own Compose `Canvas`:
`ui/WeightChart` (Paino, BMI with `ChartBand`s), `EnergyPage`, `StepsPage`, `BurnPage` (bars via
`ChartPanels.drawBars` / `drawBurnBars`, `drawAverageLine`), and `CustomChartView` (tab and card
mode, stacked panels or one overlaid plot with values normalised by `KindData.fraction`). Axes and
legend are Compose rows (`ChartAxes`: `YAxisLabels`, `DateAxis` with `AxisTicks.fitCount`,
`ChartLegend`). The crosshair is our own gesture detector (`Crosshair.crosshairGestures`: tap,
long-press then scrub, swipe left to the `HorizontalPager` in `HistoryScreen`), TalkBack actions and
a one-line `ReadoutSlot` fed by `chart/TabReadouts` and `ChartPage.readoutText`. The chosen day
lives in `HistoryScreen` (`crosshair: LocalDate?`), cleared on tab and period change.

Pure rules that stay as they are: `WeightScale`, `AxisTicks.bars/line/rangeText/periodDates`,
`ChartDots.fit`, `EnergySeries`, `StepsSeries`, `BurnSeries`, `CustomChartData` (values, ticks,
averages), `ChartReadout`, `Derived.BANDS`, `ChartPalette`.

A spike (scratchpad `mpspike`, `MpChartSpikeTest`) proved with MPAndroidChart 4.0.1:
`CombinedChart` with bars on the left axis and a line on the right; a ~40 line `IMarker` card that
clamps itself into `viewPortHandler`'s content rect; Robolectric `@GraphicsMode(NATIVE)` renders the
view to a `Bitmap`/PNG. Found limits: no null gaps in a line (a gap needs split data sets); x label
positions cannot be set without a custom `XAxisRenderer`; its touch listener claims a drag at once
(`requestDisallowInterceptTouchEvent`), which beats the pager; no accessibility semantics; text
sizes are dp and ignore `fontScale`.

The front page cards (`HomeScreen` bars and lines, `ChartCard` = `CustomChartView` card mode) are
out of scope and keep `ChartPanels` and the card branch of `CustomChartView`.

## Goals / Non-Goals

**Goals:**
- One library-backed chart component for every Historia chart, fed by a pure chart definition.
- Library defaults for selection, highlight, marker placement, x labels, zoom/pan, legend layout.
- Our rules kept where the user did not ask to change them: y-scales and three y labels with units,
  plot from the axis to the edge, dots when they fit, weekly bars, average line, colours, themes,
  TalkBack, range line, empty and permission states.

**Non-Goals:**
- Front page cards (a later change can feed the same definitions to the card size).
- New chart kinds or new data; any change to statistics or lists.
- Animations beyond the library default (none unless `animate*` is called: we call none).

## Decisions

### 1. Dependency and repository
`libs.versions.toml`: `mpandroidchart = "v4.0.1"`, libraries `mpchart-lib`
(`com.github.PhilJay.MPAndroidChart:MPChartLib`) and `mpchart-compose` (`...:MPChartCompose`).
`settings.gradle.kts` adds JitPack inside `exclusiveContent { forRepository { maven("https://jitpack.io") } filter { includeGroup("com.github.PhilJay.MPAndroidChart") } }`
(as in the spike), so nothing else can resolve from JitPack. Remove `vico` from the catalog and
`implementation(libs.vico.compose.m3)` (no source imports it). The Compose module is used as the
host when it exposes the underlying chart for configuration and updates (checked in task 1.2);
otherwise the host is a plain `AndroidView { CombinedChart(it) }` and `MPChartCompose` is dropped
again with a one-line note in the commit. Release build: if R8 breaks anything, add keep rules in
`proguard-rules.pro` (checked with `assembleRelease` once locally).

### 2. Pure chart definition (`chart/ChartSpec.kt`)
```
ChartSpec(axis: ChartAxis, panels: List<PanelSpec>, legend: List<LegendItem>, weeklyNote: Boolean)
PanelSpec(heightDp, left: AxisSpec?, right: AxisSpec?, series: List<SeriesSpec>, bands: List<BandSpec>, message: PanelMessage?)
AxisSpec(min, max, ticks(3), label: (Double) -> String, tone: ToneRef?)
SeriesSpec(id, name, unit, tone: ToneRef, side: LEFT|RIGHT|HIDDEN,
           kind: Bars(points, widthDays) | StackedBars(points with parts, partTones) | Line(points, dots: All|None|Lone|Last),
           average: Double?)
```
Points are `(x: Double, y: Double, day: LocalDate)`; x is "days since axis.from" with the day's
centre at `index` (axis from `-0.5` to `days - 0.5`), a weight at `index - 0.5 + timeOfDay`, a
weekly bar at the week's centre. `ToneRef` is a symbolic tone (primary, secondary, tertiary,
onSurfaceVariant, palette colour, light/total alpha variants) resolved to an ARGB int in the UI from
`MaterialTheme.colorScheme` / `ChartPalette`, so the spec stays pure and theme-free.

Builders, all pure and unit-tested: `TabCharts.weight(ChartData, bands=false)`, `TabCharts.bmi(...)`
(bands from `Derived.BANDS`), `TabCharts.energy(EnergySeries)`, `TabCharts.steps(StepsSeries)`,
`TabCharts.burn(BurnSeries)`, `CustomChartSpec.of(CustomChartData, overlaid)`. Readout rows come from
`ChartSpec.rowsAt(day)` built on the existing `TabReadouts` / `ChartReadout` value rules
(`TabReadouts` is reshaped from one joined string to head + rows; the old joined form goes).

Alternative: configure MPAndroidChart directly in each page. Rejected: five copies of styling and
no pure test surface.

### 3. One renderer (`ui/HistoryChart.kt`)
`HistoryChart(spec, chosen: LocalDate?, onChoose: (LocalDate?) -> Unit, zoomKey)` draws one
`CombinedChart` per panel (one for Paino, BMI, the fixed bar tabs and an overlaid own chart; one per
panel for Allekkain) and maps the spec:
- Bars → `BarDataSet`; `StackedBars` → `BarEntry(x, floatArrayOf(...))` with stack colours
  (treats on food; lepo + aktiivinen on Kulutus); kokonais days go in their own `BarDataSet` in the
  total tone (stack colours are per data set, not per entry). `barWidth = 0.7 × widthDays`.
- Overlaid bars side by side: `BarData.groupBars(fromX, groupSpace, barSpace)` (library feature).
- Line → `LineDataSet` (2 dp, no values, `setDrawCircles` from `ChartDots.fit`); lone weights and
  an own chart's last dot → a `ScatterDataSet` (circle) in the same tone. No line has gaps (every
  current requirement joins consecutive values), so the null-gap limit does not bite.
- Overlaid: every series is plotted on the left axis in fraction units 0..1 (`KindData.fraction`,
  as today); the left and right `YAxis` run 0..1 with `labelCount = 3` forced and a
  `ValueFormatter` that maps the fraction back to the first / second drawn data's value with its
  unit, in that data's tone. A third and fourth data share the plot without labels. This avoids
  MPAndroidChart's two-axis limit without a renderer.
- Y axes: `axisMinimum/axisMaximum` from the spec, `labelCount = 3`, `isForceLabelsEnabled`, grid
  lines at those labels (the three guidelines); `axisRight` disabled unless an overlaid second data.
- Average → `LimitLine` on the series' axis, dashed (`enableDashedLine(4dp, 4dp)`), 1 dp, no label,
  drawn over the data (library default `drawLimitLinesBehindData = false`).
- BMI bands → `BandYAxisRenderer` (subclass of `YAxisRenderer`, ~25 lines) that fills the bands'
  rects before `renderGridLines`, so they lie behind grid and data.
- Legend → library `Legend` below the chart, `isWordWrapEnabled = true`, custom entries from
  `spec.legend` (form SQUARE for bars, LINE for lines, LINE with dash effect for keskiarvo, NONE for
  "viikoittain"). Allekkain: legend only on the last panel, with all entries.
- Stacked alignment: every panel gets the same `axisLeft.minWidth` (the widest panel's labels) and
  only the last panel draws x labels; the plots start and end at the same x.
- Theme: colours resolved from the colour scheme on each recomposition; `setBackgroundColor`
  transparent (the card's surface shows), text colours `onSurfaceVariant`, grid `outlineVariant`.
- Font scale: every text size = typography size in sp × `LocalDensity.fontScale` passed as dp
  (MPAndroidChart's sizes are dp), so text follows the system font size.
- Plot edge: `minOffset = 0`, `setExtraOffsets` small, x range exactly the axis, so the period's
  start sits on the y axis and its end on the right edge.

### 4. X labels: library placement, our count
`xAxis.granularity = 1` (7 on weekly bars), `isGranularityEnabled`, `labelCount` = the result of
the existing `AxisTicks.fitCount` measure (3–5, measured with the chart's own text paint), not
forced; `isAvoidFirstLastClippingEnabled = true`; a `ValueFormatter` gives weekday / "d.M." /
"M/yyyy" from the x. A period of ≤ 7 days: `labelCount = days`, forced, so each day centre is
labelled. `DateAxis` and `labelBounds` go unused by Historia (kept while the cards use them).
Alternative: a custom `XAxisRenderer` that draws our exact dates. Rejected for the library default
(user decision); open question 3 keeps the choice visible.

### 5. Selection, marker, gestures
- Library defaults: `isHighlightPerTapEnabled`, `isHighlightPerDragEnabled` (drag scrubs while
  fully zoomed out, pans once zoomed), tapping the same value again deselects, a tap with no value
  within `maxHighlightDistance` deselects. A press-and-hold released in place selects too:
  `OnChartGestureListener.onChartLongPressed` highlights `getHighlightByTouchPoint` (the library
  itself ignores a long press); no haptic tick.
- The chosen day stays in `HistoryScreen` (state hoisting, removal rules unchanged). An
  `OnChartValueSelectedListener` maps the entry's x to a `LocalDate` and calls `onChoose`; an
  `update` block re-applies `highlightValues` for `chosen` after every data change (setting data
  clears the library's highlight; this keeps "kept on reload"). Allekkain: the same day is
  highlighted in every panel that has a value; the marker is drawn by one panel only (the topmost
  drawn).
- Marker: `ReadoutMarker : IMarker` as in the spike: rounded card in `surfaceContainerHighest`,
  outline border, head in bold, rows with a colour dot, name left and value right; left of the day
  when it would cross `contentRight`, clamped into the content rect; pinned to the plot top.
- Bars: library highlight (`highlightColor` = onSurface, `highlightAlpha` ~ 90) on the chosen bar;
  lines: vertical highlight line (1 dp, onSurfaceVariant, no horizontal line) and a ring
  (`highlightCircle`/ scatter ring) on the point.
- Syöty's "Avaa päivä ›" is dropped (user 2026-11-16); the Syöty day list opens Ruoka.
- Pager: the chart's touch listener claims the gesture inside the plot (library behaviour), so the
  pager swipes outside the plot (range line, stats, list). `HistoryScreen` drops
  `crosshairGestures`; nothing to do in the pager.
- Zoom: `isScaleXEnabled = true`, `isScaleYEnabled = false`, `isPinchZoomEnabled = false` (x-only
  pinch), double-tap zoom on (default), `isDragEnabled = true`. Stacked panels sync through an
  `OnChartGestureListener` that copies the touched chart's matrix to the others
  (`viewPortHandler.refresh(matrix, chart, true)`). `fitScreen()` on a period or tab change (key).

### 6. Accessibility
The `AndroidView` gets Compose semantics: `contentDescription` (the current chart description
strings), `customActions` "Edellinen päivä" / "Seuraava päivä" (the existing `crosshairActions`
step logic, which calls `onChoose`), and a visually hidden `Text` node with `liveRegion = Polite`
holding the marker's text (head + rows joined by ", "), updated only on tap, drag end and actions.
The view itself is `importantForAccessibility = NO` so TalkBack does not stop on a bare view.

### 7. What goes
`ui/Crosshair.crosshairGestures`, `ReadoutSlot`, `TabReadout`, the Historia use of `YAxisLabels`,
`DateAxis`, `ChartLegend`, `WeightChart`'s canvas, the Canvas bodies of `EnergyChart`, `StepsChart`,
`BurnChart` and the tab branch of `CustomChartView`. Kept: `ChartPanels` (cards), card mode of
`CustomChartView`, `ClearOffAxis`, `RangeLine`, `crosshairActions` (moved onto the new host).

## Requirements changed to library defaults

Each is a MODIFIED requirement in `specs/weight-chart` or `specs/energy-burn`:

1. **Readout** ("Chart crosshair", "Crosshair on every Historia chart", "Point details", energy-burn
   "Kulutus tab"): the one-line text above the chart becomes the library marker card at the day,
   head + one row per series. The reserved readout row and its hint "Napauta päivää nähdäksesi
   arvot" are gone; tapping the readout no longer clears (a canvas marker is not tappable).
2. **Gestures** ("Chart crosshair"): tap selects the nearest value, drag scrubs at once (library
   highlight-per-drag) without a hold first; a hold released in place still selects (a small
   gesture-listener hook), but its haptic tick and "hold owns both directions" are gone.
3. **Clearing** ("Chart crosshair"): tapping the chosen value again or empty plot clears (library
   toggle); other removal rules stay.
4. **Nearest value** ("Crosshair on every Historia chart"): a tap on Paino between weights picks the
   nearest weight instead of showing "ei painoa" for the tapped day (TalkBack stepping still lands
   on empty days).
5. **Bar highlight**: the chosen bar takes the library highlight tint; the others no longer dim to
   60 %; bars get no vertical crosshair line (lines keep the library's vertical highlight line).
6. **Pager swipe** ("One quantity at a time", "Chart crosshair"): a sideways swipe on the plot no
   longer switches the tab; the chart owns it (selection, pan). Swiping elsewhere on the page does.
7. **X labels** ("Chart axes", "Stacked layout"): 3–5 labels placed by the library's spacing at
   whole days, no longer pinned to the period's first and last day; the count still comes from our
   no-overlap measure; the "Month" and "Same dates on both chart kinds" scenarios go.
8. **Average line order** ("Custom charts"): on Historia tabs the dashed average line is drawn over
   bars and lines (library limit line), not between them.
9. **Bar width** ("Custom charts"): no 1 dp minimum on Historia tabs (library bar width is a share
   of the day slot); cards keep it.
10. **Zoom** (new "Chart zoom"): pinch / double-tap zoom and pan on x, library default, added.

## Risks / Trade-offs

- [Pager vs chart gestures: a whole-width chart leaves less room to swipe tabs] → stats, range line
  and list still swipe; tabs are tappable; open question 1 offers a hold-first variant.
- [`AndroidView` inside `LazyColumn` + `HorizontalPager` recomposes and re-creates views] → create
  once per page (`factory`), update data in `update`; key on page.
- [Marker is canvas-drawn: not readable by TalkBack] → hidden live-region text (decision 6).
- [fontScale ignored by the library] → sizes multiplied by fontScale (decision 3); `LargeTextTest`
  keeps the 1.3 no-overlap check on the measured labels.
- [JitPack availability in CI and cloud (Maven Central 429s)] → JitPack is a separate host; if it is
  blocked in the cloud, ask for the host to be allowed; exclusiveContent limits the trust to one
  group.
- [R8 in release] → one local `assembleRelease`, keep rules if needed.
- [Robolectric NATIVE graphics slower] → render tests in their own class; PNGs under
  `app/build/chart-renders/` for review, no pixel assertions beyond non-blank and size.

## Migration Plan

Pure UI change, no stored data changes (`ChartStore` JSON unchanged). Ship as one MINOR bump.
Rollback = revert the merge commit.

## Open Questions

Each has a recommended answer; the specs and tasks are written for the recommendation. A
different answer changes only the named requirement and one task.

**Answered by the user 2026-11-16:** 1 chart owns the gesture, 2 zoom on, 3 "Avaa päivä" dropped,
4 library x-label placement (to be revisited after the change if needed); 5 hint dropped (the
recommendation stands).

1. **Chart gestures vs the Historia tab swipe.** Inside the plot, does a sideways swipe belong to
   the chart (selection scrub, pan when zoomed) or to the pager?
   **Recommended: the chart** (library default; swipe tabs on the stats, list or range line, or tap
   a tab). Alternative: the pager keeps quick swipes and the chart only takes a drag after a
   long-press (our old rule, needs a touch-interceptor wrapper and loses drag-to-scrub at once).
   Affects "One quantity at a time", "Chart crosshair", task 4.4.
2. **Pinch zoom on or off.** **Recommended: on, time axis only**, with double-tap zoom and pan
   (library default; useful on Kaikki and 1 v), reset on a period or tab change. Off would drop the
   "Chart zoom" requirement and task 4.5.
3. **X labels: library placement or our fitted dates.** **Recommended: library placement** with our
   measured count (3–5) and weekday labels for a week. Alternative: a custom `XAxisRenderer`
   (~40 lines) that keeps "first label at the period's start, last at its end, evenly by days".
   Affects "Chart axes" and task 3.4.
4. **"Avaa päivä ›" on Syöty.** **Recommended: a button under the chart while a day is chosen.**
   Alternative: drop it (the Syöty day list already opens Ruoka). Affects "Crosshair on every
   Historia chart" and task 3.6.
5. **Hint "Napauta päivää nähdäksesi arvot".** **Recommended: drop it** with the readout row (the
   marker appears where the user taps; the chart moves up 48 dp). Alternative: keep it as a quiet
   line under the chart while nothing is chosen.
