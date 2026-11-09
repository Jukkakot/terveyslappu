# Proposal

## Why

Picking an own range (Oma) in Historia, the user cannot see which days have weights, so a range is
picked blind. The day's weight under each day with data shows it while choosing (user 2026-11-09;
first a dot, then the weight instead). The
Material 3 date-range picker cannot draw anything on a day (it can only disable days, which the
user does not want), so the picker is replaced.

## What Changes

- Oma opens a new calendar dialog built on the **Calendar** library (kizitonwose, Compose,
  maintained; 2.10.1 from March 2026): one month at a time, swipe or arrows to change month, weeks
  from Monday, the month name and year on top.
- Every day with at least one of this app's weights shows **that day's latest weight** under its
  number ("82,4", no unit, small text); no dot, no count. Every day up to today stays selectable.
- Picking: the first tap sets the start, the second the end (a tap before the start makes that day
  the new start); the days between are highlighted; a third tap starts a new range. OK applies the
  range (a single day when only the start is set), Peruuta closes without change. The dialog opens
  on the month of the chosen range's end, or this month.
- Look from the app theme (Material You, light and dark): the chosen days in the primary colour,
  the range between in a light primary tone, today outlined.

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- `weight-chart`: "Periods" — Oma opens the calendar with the days with weights marked.

## Impact

- Parts touched: Historia's Oma chip and its dialog only.
- Code: new pure range logic in `chart/` (tap → range), new `ui/RangeCalendarDialog`, `PeriodPanel`
  drops the M3 `DateRangePicker`. Dependency `com.kizitonwose.calendar:compose` 2.10.1 (library
  first, as decided in product.md; drawing a month grid by hand is what the library already does).
- No health data leaves the phone; the day weights come from the weights already read.
