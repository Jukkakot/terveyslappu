# Tasks

## 0. Baseline

- [x] 0.1 First check that a Robolectric NATIVE capture includes `AndroidView` content (Compose `captureToImage`; else draw the root view to a bitmap). Then, before any change, screenshot with made-up data into the session scratchpad (never the repo): weight, food, steps, Kulutus and own chart cards (Allekkain and Päällekkäin) at small, wide and big, light and dark; plus 90 days on a small card, a single weight, the latest weight at the right edge, Kulutus with a kokonais day, Päällekkäin with three bar data, Keskiarvo on over lines, edit mode dimmed.

## 1. Card chart definitions

- [x] 1.1 `chart/CardCharts` (design 1, 2) and the `ChartSpec` x-range override; card line widths, dot radii and the last-dot style in the spec model.
- [x] 1.2 `CustomChartSpec.of(..., card = true)`: daily bars, today's sub-slot geometry, average between bars and lines, no-op readout/access.
- [x] 1.3 `CardChartsTest`: weight x by time with two weighings a day and a single weight; dots fit and the last dot; daily bars at 90 days; small/wide scale = highest bar, big = the tab's top with the average; weight tone primary; Päällekkäin sub-slot offsets.

## 2. Card mode and host

- [x] 2.1 Card branch of `bind` (design 3, 4), card-mode `dispatchTouchEvent`.
- [x] 2.2 `ui/CardChart` host (design 5, 6).
- [x] 2.3 Compose tests: a tap on each card's chart opens its target (weight → Historia, food bars → Historia Energia while food's title still opens Ruoka, steps → Askeleet, Kulutus → its tab, own chart → its tab); a long-press on a chart enters edit mode; a long-press-then-drag on a chart moves the card; a recomposition does not rebind; content descriptions and test tags present.

## 3. Switch the cards

- [x] 3.1 Weight, food, steps and Kulutus cards in `ui/HomeScreen` draw through `CardChart` at every size; big-card scale tops and dashed average as today.
- [x] 3.2 Own chart cards (`ui/CustomChartView` card mode, `ui/ChartCard`) draw through `CardChart`, one per Allekkain panel; layout, messages, labels and legend unchanged.
- [x] 3.3 Remove the Canvas path (design 7); fix tests that referenced it.
- [x] 3.4 After screenshots of the same cases as 0.1; compare by eye; fix any difference against design 1–4; note accepted pixel-level differences here.
  - Weight, food, steps and Kulutus cards: identical to the pixel at every size, light and dark (food, steps, Kulutus) or within line anti-aliasing (weight; the title's clock differs between runs).
  - Own chart cards: grid lines straddle the plot's edge by half a pixel (the library centres a 0.5 dp line on the edge, the Canvas drew it inside), and line joins and dot edges anti-alias differently; accepted. The bottom guideline is a 0.5 dp x-axis line in the grid colour, not 1 dp (design 3): the Canvas clipped its 1 dp line to the upper half, so 0.5 dp keeps today's look.
  - A card squeezed to no chart height (a small weight card whose title and change chip wrap) draws nothing; the Canvas drew a flattened, upside-down line over the dates there. The library view of no height skips drawing.

## 4. Finish

- [x] 4.1 `./gradlew test lint assembleDebug`
- [x] 4.2 Update the CLAUDE.md map (`ui/ChartPanels` gone, `chart/CardCharts`, `ui/CardChart`, card mode of `HistoryChartView`); mark the roadmap item "front page cards on the chart library" done; bump `appVersion` to 1.52.
