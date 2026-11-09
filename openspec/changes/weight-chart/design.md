# Design

## Context

See proposal.md. The main screen today: backup card, "recent weights" list (30 days, all apps), the
manual entry form at the bottom. Health Connect reads already exist for a time range and for all
weights (paged); the history permission (older than 30 days) is asked by the export.

## Goals / Non-Goals

Goals: a readable trend at a glance on a phone, pure and tested trend and stats logic.
Non-goals: goal weight and forecast, zooming or panning, other data types, other apps' weights in
the chart.

## Decisions

- **Draw the chart with Compose Canvas, no chart library.** One line, dots, a few axis labels and a
  tap target are little code; a library (e.g. Vico) adds a dependency and its own look to theme.
- **Trend = Hacker's Diet exponential smoothing on daily values.** Daily value = the day's first
  weight (the morning one, before eating); after a gap of n days the trend moves
  `1 - 0.9^n` of the way to the new value. Computed over all own weights from the first one, so a
  short period starts from the real trend. Alternatives: 7-day moving average (jumps when an old
  value drops out, needs 7 days to start), lowest-of-day (Happy Scale) - first-of-day matches the
  user's morning habit.
- **Load own weights once, filter in memory.** Read all of this app's weights (data-origin filter,
  paged) when the screen opens and after every save, delete, undo or import; years of daily
  weights are a few thousand records. The period only filters this list. The list under the chart
  still reads all apps for the period (it shows sources).
- **Period kept in preferences** (chip id, or the two dates for Oma).
- **Stats rate** = trend change / days in the period * 7; no rate for periods under two days of data.
- **Layout:** chips row, chart ~180 dp high, stats as one compact two-line row, then the list
  taking the rest; the manual entry form stays at the bottom. Light and dark theme from the existing
  Material colours: dots in onSurfaceVariant, trend line in primary.

## Risks / Trade-offs

- Without the history permission "Kaikki" shows only 30 days: the chart says so and offers the
  same grant flow as the export.
- A very long "Kaikki" with thousands of dots: dots are drawn only when they are at least a few
  pixels apart; the trend line always.
