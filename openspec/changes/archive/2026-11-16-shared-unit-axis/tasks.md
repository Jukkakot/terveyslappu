# Tasks

## 1. Shared scale

- [x] 1.1 `chart/CustomChartData.of`: after the per-kind data, give every drawn kind whose unit (`CustomChartSpec.unit`) is shared with another drawn kind the same ticks, computed by the kcal rule from the union of their values; kinds without values keep empty ticks and do not count. Fractions and averages then follow the shared ticks.
- [x] 1.2 `CustomChartDataTest`: the spec scenarios "Eaten and burned compare" (0–3 500 kcal for both), "Burned without values" (Syöty alone 0–2 500), "Different units stay apart".

## 2. Axes per scale

- [x] 2.1 `chart/CustomChartSpec.overlaid`: group the drawn kinds by unit in data order; the first group gets `Side.LEFT`, the second `Side.RIGHT`, the rest `Side.HIDDEN`; the panel's left/right `AxisSpec` come from those groups (right null when there is one group); a group of two or more gets axis tone `null` (neutral), a single kind its own tone.
- [x] 2.2 `ui/CustomChartView` Overlaid card: big card scale tops from the same groups (one top per axis, neutral tone for a shared one; no right top with one group). Check the right axis is absent in Historia when `right` is null (`ui/HistoryChartView`).
- [x] 2.3 `CustomChartSpecTest`: "Eaten and burned share the right axis", "Only eaten and burned", "Shared scale frees the second axis", and the existing three-data scenario unchanged. Also "Eaten, burned and steps", "Burned without permission" (right axis in Syöty's tone). One card smoke test: big Syöty+Kulutus Päällekkäin card has one `scaleTop-*` tag.

## 3. Finish

- [x] 3.1 `./gradlew test lint assembleDebug`
- [x] 3.2 Mark the roadmap item "Shared y-axis for the same unit" done (keep the later per-series axis choice as its own backlog line); bump `appVersion` to the next MINOR.
