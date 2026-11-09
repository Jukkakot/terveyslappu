# Design

## Context

`ui/PeriodPanel.PeriodChips` opens Material 3 `DatePickerDialog` + `DateRangePicker` for Oma and
builds `Period.Custom(from, to)`. M3 1.4 has no per-day content slot; `SelectableDates` can only
disable days, which the user rejected (2026-11-09). Historia already holds every own weight in
`MainUiState.weights`.

## Goals / Non-Goals

**Goals:** a dot on days with weights; all past days selectable; the same Period.Custom result.
**Non-Goals:** counts or values per day, multi-month view, picking from the home screen.

## Decisions

- **Library `com.kizitonwose.calendar:compose` 2.10.1** (Maven Central, Compose-only, updated
  March 2026): `HorizontalCalendar` with `rememberCalendarState(startMonth, endMonth, firstVisibleMonth,
  firstDayOfWeek = MONDAY)` and our own `dayContent`. Alternatives: own grid (more code to keep),
  M3 picker (cannot mark days). The library leaves selection to us, which keeps our rules.
- **Pure range logic `chart/RangePick`**: `tap(state, day): state` with start/end; `contains(day)`;
  `result(): Pair<LocalDate, LocalDate>?`. Unit-tested for the spec scenarios.
- **Days with data** = `weights.map { local date in the system zone }.toSet()`, computed once per
  open of the dialog; passed in, so the dialog stays testable with made-up weights.
- **Month range**: from the month of the oldest loaded weight (at least 12 months back) to this
  month; opens on the current Custom end's month or this month.
- **Look** (theme, both modes): day cell 40–48 dp; start/end a filled primary circle with onPrimary
  text, days between a primaryContainer band, today an outline, future days in disabled alpha; dot
  4 dp under the number in primary (onPrimary on a selected day). Header row: month name + year
  (Finnish, "Marraskuu 2026") with previous/next icon buttons (accessible names); weekday initials
  ma–su. Buttons Peruuta / OK as in other dialogs.
- **Accessibility**: each day's semantics read the date, "punnittu" when it has a dot, and the
  selection state; buttons ≥ 44 dp.

## Risks / Trade-offs

- [New dependency] → small, Compose-only, Apache 2.0; pinned version; Robolectric smoke test.
- [Weights older than 30 days need the history permission] → without it old months show no dots,
  as the chart; the existing history notice already explains this.
