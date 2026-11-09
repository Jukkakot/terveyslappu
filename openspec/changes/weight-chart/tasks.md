# Tasks

## 1. Periods and statistics (pure)

- [ ] 1.1 Period model (1 vk, 1 kk, 3 kk, 1 v, Kaikki, Oma with dates) and its time range; stats for
  a list of weights: latest, change, kg/week, average, min, max, count, single-weight case.
  Verify: unit tests with made-up weights.

## 2. Data

- [ ] 2.1 Read all of this app's weights (data-origin filter, paged), reload after save, delete,
  undo and import; chart, stats and list all filter this one list by the period; the period is kept
  in preferences; source labels and the other-app branch leave the list. Verify: `./gradlew test`
  passes; main screen tests updated for own-only rows.

## 3. Screen

- [ ] 3.1 Chips row with Oma date-range picker, Vico chart (points, line, day/value axes), tap
  marker shows point details, stats row, empty-period text, history-access notice with the
  export's grant flow. Verify: main screen smoke test (chart and stats shown with made-up weights;
  tapping 1 kk changes the period); LargeTextTest still passes at 200 % text.

## 4. Context

- [ ] 4.1 product.md: own chart replaces "Samsung Health is the viewer", other apps' weights are
  not shown (why); roadmap: own weight view done, goal and forecast in backlog; bump `appVersion`
  to 1.1. Verify: `./gradlew test lint assembleDebug` passes and the files read correctly.
