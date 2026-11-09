# Proposal

## Why

Comparing Historia's Kaikki chart with OKOK's (user 2026-11-09) showed two ways ours misleads:
a straight line across ten months without weights looks like a steady weight, and the x-axis
labels (24.9., 23.7., 20.5. …) carry no year, so over several years they cannot be placed. The
weight list has the same gap: an old row reads "Ti 15.9. 8.33" with no year.

## What Changes

- The chart line breaks where two consecutive weights are more than 30 days apart; nothing is
  drawn across the gap. A weight alone between two gaps still shows as a dot, also in a dense
  chart that otherwise draws no dots.
- When the chart spans more than a year, the x-axis labels show month and year ("9/2022")
  instead of day and month.
- The year is shown wherever a single weight's time is shown and it is not from the current
  year: the tapped dot's detail ("15.9.2024 8.33 97,6 kg") and Historia's list rows
  ("Su 15.9.2024 8.33").
- Nothing changes in the data, the stats or other screens.

## Capabilities

### New Capabilities

### Modified Capabilities
- `weight-chart`: the line breaks at long gaps; labels and point details carry the year when needed.
- `weight-entry`: Historia's list rows show the year for weights from earlier years.

## Impact

App screens only: Historia's chart and list (`chart/ChartData`, `ui/WeightChart`, `ui/Format`).
No Health Connect, scale, widget or build/CI changes.
