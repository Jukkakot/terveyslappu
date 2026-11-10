# Proposal

## Why

Historia's chart breaks its line wherever two weights are more than 30 days apart. With sparse
history (imported years, weighing once a month or less) much of the chart is loose dots with no
line, which hides the trend. The x-axis is continuous time with no skipped steps and the dots show
where the weighings are, so the line need not break (user 2026-11-10: same normal line whatever the
interval, no dashes).

## What Changes

- Historia's chart joins every pair of consecutive weights with the same solid straight line,
  however far apart they are; the 30-day break is dropped.
- A weight with a gap over 30 days on both sides keeps its dot in a dense chart (unchanged), so a
  lone weighing is still visible as a measurement.
- The home card's small line already joins every weight; unchanged. The Historia list's gap rows
  and the "no change across a gap" rule stay.

Parts touched: app screens (Historia chart only).

## Capabilities

### New Capabilities

### Modified Capabilities
- `weight-chart`: "Weight chart" requirement: the line no longer breaks at a gap over 30 days.

## Impact

`ui/WeightChart.kt` (one series, per-point dots), `chart/ChartData` (segments replaced by the
indices of lone weights).
