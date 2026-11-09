# Tasks

## 1. Range logic (pure)

- [ ] 1.1 `chart/RangePick`: start/end state, tap rules (first start, second end, earlier day →
  new start, third tap → new range), `contains`, `result` (single day when no end); the
  latest weight per day from a weight list and zone. Verify: unit tests for each spec scenario with made-up
  weights.

## 2. Calendar dialog

- [ ] 2.1 Add `com.kizitonwose.calendar:compose:2.10.1` (version catalog). `ui/RangeCalendarDialog`:
  `HorizontalCalendar` from Monday, header with Finnish month and arrows, weekday initials, day
  cells with the day's weight, selection look and semantics per design, future days disabled, Peruuta/OK;
  `PeriodChips` opens it instead of the M3 picker and gets the day weights. Verify: Robolectric
  smoke test with made-up weights: a day with weights shows its latest weight and one without shows none;
  tapping two days and OK gives the `Period.Custom`; LargeTextTest at 200 % keeps OK visible; one
  screenshot in light and dark judged, kept out of the repo.

- [ ] 2.2 History notice in the calendar when history access is not granted (reuse `history_needed`
  text and the grant action). Verify: smoke test that the notice shows without access and not with it.

## 3. Context

- [ ] 3.1 product.md: calendar decision (day's latest weight, all days selectable, library, why not M3); CLAUDE.md
  map gains `RangeCalendarDialog`; bump `appVersion` minor. Verify: `./gradlew test lint
  assembleDebug` passes.
