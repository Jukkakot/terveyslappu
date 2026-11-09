# Proposal

## Why

Over a long period (Kaikki holds 300+ weights) Historia's list is one row per weight, too long to
scan. The user wants it grouped by time with each group's span and change, opening down to the
single weights (user 2026-11-09).

## What Changes

- Historia's list groups the period's weights by year → month → week → day → weight. A level that
  would hold a single child is skipped, so short periods look as today (a week of one weight a day
  shows seven plain rows).
- A group row shows the period's name ("2024", "Marraskuu 2025", "Vk 45 · 3.–9.11.",
  "Ti 4.11."), the first → last weight of the group, the change with an arrow in the list's neutral
  colours, the number of weights and a chevron; tapping opens or closes it.
- The newest group on each level opens by default, the rest are closed; the state is not
  remembered.
- No empty groups. A time over 30 days with no weights between neighbouring groups or rows shows a
  thin "Ei punnituksia 21.5.2024–17.3.2025" row (the chart's 30-day line-break rule), and no change
  is ever computed across such a gap.
- Weight rows keep their date, change from the previous weight, delete and undo.

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- `weight-entry`: "Recent weights from Health Connect" — the list is grouped by time with group
  rows, default open groups and gap rows; the per-weight change no longer crosses a gap over 30
  days.

## Impact

- Parts touched: app screens (Historia list only). Not touched: Health Connect, scale, widget,
  export, chart, home screen.
- Code: new pure grouping in `chart/` beside `RowChanges` (tree of groups, single-child collapse,
  gap rows), `ui/HistoryScreen` list. No library: a few lines of `groupBy` on `java.time`; Compose's
  `LazyColumn` already draws the list.
