# Tasks

## 1. Range text

- [x] 1.1 A pure function (in `chart/`, next to `AxisTicks`) giving the range text from the chart's first and last day: "d.M.yyyy – d.M.yyyy", one date when equal.
- [x] 1.2 Unit tests: week, a year across the new year, one day.

## 2. Screens

- [x] 2.1 Show the line at the top of the chart card on Paino, BMI, Energia, Askeleet and own chart tabs, using the same first/last day as the chart's x-axis (for Kaikki the first day with data), small secondary label style; not on front page cards.
- [x] 2.2 One smoke test that the Askeleet tab shows the range line.

## 3. Context

- [x] 3.1 Bump MINOR of `appVersion` in `gradle.properties`.
- [x] 3.2 Run `./gradlew test lint assembleDebug`.

## Decisions the spec left open

- The line is labelSmall, onSurfaceVariant, 4 dp below it; on Paino/BMI inset 8 dp like the legend. On own chart tabs it sits above the plot in both layouts; empty charts show none.
- The smoke test checks the line exists (tag stepsRange), not its text; the text is unit-tested in AxisTicksTest.
