# Tasks

## 1. Grouping (pure)

- [ ] 1.1 `chart/ListGroups`: year → month → week (cut at months) → day → weight tree, newest first,
  single-child collapse, first/last/count per group, gap nodes over 30 days; `changesFromPrevious`
  gives no change after a gap over 30 days. Verify: unit tests with made-up weights for every
  scenario of the delta spec (one-a-day week flat, two in a day, years, week across a month, month
  row values, long and short pause, no change across a gap); existing `RowChangesTest` green.

## 2. List UI

- [ ] 2.1 Historia's `LazyColumn` flattens the open part of the tree: group rows (name, first → last
  kg, change with the list's arrow and colours, count, chevron, tap toggles), gap rows, weight rows
  as today with delete and undo; newest path open by default, reset on period change. Verify:
  MainScreen smoke test with made-up multi-year weights: a year row shows and tapping a closed one
  reveals its rows; LargeTextTest still passes at 200 %.

## 3. Context

- [ ] 3.1 product.md: grouped list decision (levels, auto-collapse, default open, gap rows, why);
  roadmap backlog entry done; bump `appVersion` minor. Verify: `./gradlew test lint assembleDebug`
  passes.
