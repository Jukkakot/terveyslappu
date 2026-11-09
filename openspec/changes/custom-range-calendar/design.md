# Design

## Context

`ui/PeriodPanel.PeriodChips` opens Material 3 `DatePickerDialog` + `DateRangePicker` for Oma and
builds `Period.Custom(from, to)`. M3 1.4 has no per-day content slot; `SelectableDates` can only
disable days, which the user rejected (2026-11-09). Historia already holds every own weight in
`MainUiState.weights`.

## Goals / Non-Goals

**Goals:** the day's latest weight on days with weights; all past days selectable; the same Period.Custom result.
**Non-Goals:** the number of weights per day, multi-month view, picking from the home screen.

## Decisions

- **Library `com.kizitonwose.calendar:compose` 2.10.1** (Maven Central, Compose-only, updated
  March 2026): `HorizontalCalendar` with `rememberCalendarState(startMonth, endMonth, firstVisibleMonth,
  firstDayOfWeek = MONDAY)` and our own `dayContent`. Alternatives: own grid (more code to keep),
  M3 picker (cannot mark days). The library leaves selection to us, which keeps our rules. An earlier tap after the start becomes the new start, as
  in the Material date-range picker (the familiar Android behaviour; user: industry standard).
- **Pure range logic `chart/RangePick`**: `tap(state, day): state` with start/end; `contains(day)`;
  `result(): Pair<LocalDate, LocalDate>?`. Unit-tested for the spec scenarios.
- **Day weights** = the latest weight of each local day (system zone) as `Map<LocalDate, Double>`,
  computed once per open of the dialog; passed in, so the dialog stays testable with made-up
  weights. Latest, because it is the value the list and home card lead with.
- **Month range**: from the month of the oldest loaded weight (at least 12 months back) to this
  month (user left the default to us); opens on the current Custom end's month or this month.
- **Look** (theme, both modes): day cell 40–48 dp; start/end a filled primary circle with onPrimary
  text, days between a primaryContainer band, today an outline, future days in disabled alpha; the
  weight under the number in a small fixed size (10 sp, not scaled further at 200 % text so
  "100,0" fits a 1/7-width cell) in onSurfaceVariant (onPrimary on a selected day). Header row: month name + year
  (Finnish, "Marraskuu 2026") with previous/next icon buttons (accessible names); weekday initials
  ma–su. Buttons Peruuta / OK as in other dialogs.
- **Accessibility**: each day's semantics read the date and the
  selection state; a day with a weight reads it ("3. marraskuuta, 82,4 kg"); buttons ≥ 44 dp.

## Risks / Trade-offs

- [New dependency] → small, Compose-only, Apache 2.0; pinned version; Robolectric smoke test.
- [Weights older than 30 days need the history permission] → without it old months show no weights;
  the calendar then shows the history notice with Anna lupa (user 2026-11-09), as Historia does.
