# Tasks

## 0. Baseline

- [ ] 0.1 Before any change: Robolectric NATIVE screenshots of the weight, food, steps, Kulutus and an own chart card (Allekkain and Päällekkäin) at small, wide and big size, light and dark, with made-up data, into the session scratchpad (not the repo) for the before/after comparison.

## 1. Card chart definitions

- [ ] 1.1 `chart/CardCharts` (pure): `weight`, `food`, `steps`, `burn` one-panel `ChartSpec`s from the card data (design 1, 2): daily bars at every period, food treats and Kulutus lepo/aktiivinen tones as the cards today, average series only for big size, scales as the cards use today (weight scale, Energia/Askeleet/Kulutus tops).
- [ ] 1.2 Own chart cards: `CustomChartSpec.of(..., card = true)` daily bars, the cards' guidelines, the shared-unit axes unchanged.
- [ ] 1.3 Unit tests (`CardChartsTest`): weight x by time with two weighings a day, dots fit / last dot, daily bars at 90 days, average only at big, scales equal to the Historia tab's for the same data.

## 2. Card mode and host

- [ ] 2.1 `PanelStyle.card` and the card branch of `bind` (design 3), bars at least 1 dp (design 5).
- [ ] 2.2 `ui/CardChart` host (design 4), not accessible, not clickable, no animation.
- [ ] 2.3 Compose tests: tap on a card's chart opens its target (weight → Historia, food bars → Historia Energia, steps → Askeleet, own chart → its tab); long-press on the chart enters edit mode (`HomeEditTest`).

## 3. Switch the cards

- [ ] 3.1 Weight, food, steps and Kulutus cards in `ui/HomeScreen` draw through `CardChart` at every size; big-card scale tops and dashed average stay as specified.
- [ ] 3.2 Own chart cards (`ui/CustomChartView` card mode, `ui/ChartCard`) draw through `CardChart`; card layout, labels and legend unchanged.
- [ ] 3.3 Remove `ui/ChartPanels`, `Sparkline`, `FoodBars`, `StepsBars`, `BurnBars` and helpers used only by them; fix tests that referenced them.
- [ ] 3.4 After screenshots as in 0.1; compare by eye; fix any difference against the specified look; note accepted pixel-level differences in this file.

## 4. Finish

- [ ] 4.1 `./gradlew test lint assembleDebug`
- [ ] 4.2 Update the CLAUDE.md map (`ui/ChartPanels` gone, `chart/CardCharts`, `ui/CardChart`); mark the roadmap item "front page cards on the chart library" done; bump `appVersion` to 1.52 (the user notices nothing on purpose, but it is a whole drawing path).
