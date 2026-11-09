# Tasks

## 1. Trend and statistics (pure)

- [ ] 1.1 Daily values (first weight of each day, phone time zone) and the exponential trend with
  gap handling. Verify: unit tests for steady weight, one heavy day (82.0 -> 82.1), a gap of
  several days, two weigh-ins in a day.
- [ ] 1.2 Period model (1 vk, 1 kk, 3 kk, 1 v, Kaikki, Oma with dates) and stats: trend at end,
  change, kg/week, min, max, count, single-weight case. Verify: unit tests.

## 2. Data

- [ ] 2.1 Read all of this app's weights (data-origin filter, paged) and reload after save, delete,
  undo and import; the list reads all apps for the chosen period; the period is kept in
  preferences. Verify: `./gradlew test` passes; existing main screen tests still pass.

## 3. Screen

- [ ] 3.1 Chips row with Oma date-range picker, Canvas chart (dots, trend line, day/value labels,
  thinned dots), tap shows point details, stats row, empty-period text, history-access notice with
  the export's grant flow. Verify: main screen smoke test (chart and stats shown with made-up
  weights; tapping 1 kk changes the period); LargeTextTest still passes at 200 % text.

## 4. Context

- [ ] 4.1 product.md: own chart replaces "Samsung Health is the viewer"; roadmap: own weight view
  done, goal and forecast in backlog; bump `appVersion` to 1.1. Verify: files read correctly.
