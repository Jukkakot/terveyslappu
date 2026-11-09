# Design

## Context

See proposal.md. The main screen today: backup card, "recent weights" list (30 days, all apps, with
source labels), the manual entry form at the bottom. Health Connect reads already exist for a time
range and for all weights (paged); the history permission (older than 30 days) is asked by the
export.

## Goals / Non-Goals

Goals: the period's weights and key numbers at a glance on a phone; pure and tested stats logic.
Non-goals: trend smoothing, goal weight and forecast, zooming or panning, other data types, other
apps' weights anywhere on screen (export and backup keep them).

## Decisions

- **Chart library Vico** (`com.patrykandpatrick.vico:compose-m3`, Apache 2.0, maintained, Compose
  and Material 3 native) rather than hand-drawn Canvas (user 2026-11-09: libraries first). It gives
  line and point layers, axes, markers on tap, scrolling and zoom for later; themed from the app's
  Material colours. Check the latest stable version when adding it.
- **Raw weights, no trend** (user 2026-11-09). Rate per week = (latest − first) / days between them
  × 7; no rate when the period's weights span less than a day.
- **Load own weights once, filter in memory.** Read all of this app's weights (data-origin filter,
  paged) when the screen opens and after every save, delete, undo or import; years of daily weights
  are a few thousand records. The period filters this one list for chart, stats and list.
- **Period kept in preferences** (chip id, or the two dates for Oma).
- **Layout:** chips row, chart ~180 dp high, stats as one compact two-line row, then the list taking
  the rest; the manual entry form stays at the bottom. Light and dark theme from the existing
  Material colours: dots and line in primary, labels in onSurfaceVariant.

## Risks / Trade-offs

- Without the history permission "Kaikki" shows only 30 days: the chart says so and offers the same
  grant flow as the export.
- A very long "Kaikki" with thousands of points: check Vico's rendering speed with a made-up
  four-year series; hide point markers above a density threshold if needed.
