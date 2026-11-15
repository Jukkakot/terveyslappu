# Tasks

## 1. Labels

- [x] 1.1 `chart/AxisTicks`: for a period of 7 days or fewer return one label per day, the two-letter Finnish weekday (Ma Ti Ke To Pe La Su), at each day's centre (the same `ChartAxis` day centre bars and points use); longer periods unchanged.
- [x] 1.2 Unit tests: 1 vk ending Sunday gives Ma…Su; ending Wednesday gives To…Ke; a 3-day Oma range gives three weekdays; 8 days still gives dates.

## 2. Screens

- [x] 2.1 `ui/ChartAxes` (and any chart that builds its own date row: Paino/BMI line in `HistoryScreen`, `EnergyPage`, `StepsPage`, `CustomChartView` tab mode) show the weekday labels centred under each day; front page cards unchanged.
- [x] 2.2 One smoke check that the Paino and Askeleet tabs at 1 vk render seven labels without overlap at 360 dp and font scale 1.3 (existing axis test style).

## 3. Context

- [x] 3.1 Bump MINOR of `appVersion` in `gradle.properties` (the user notices the change).
- [x] 3.2 Run `./gradlew test lint assembleDebug`.

## Notes (implementer)

- 2.2: only the Askeleet tab has label tags; its existing 360 dp / 1.3 overlap test now also asserts seven weekday labels at 1 vk. Paino (Vico axis) is covered by the pure AxisTicks tests, no Compose check.
- Weekday names are hard-coded Finnish in `AxisTicks.weekday`, as the app formats everything in Finnish.
