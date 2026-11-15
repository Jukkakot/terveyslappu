# Proposal

## Why

On a one-week chart the user thinks in weekdays, not dates (user, 2026-11-15): "Ma Ti Ke …"
under the bars reads faster than 9.11., 11.11. … The user also wants the periods to stay rolling
(the last 7, 30, 90 and 365 days, today included); the code already does this, the spec did not
say it.

## What Changes

- Historia charts (Paino, BMI, Energia, Askeleet and own chart tabs) with a period of 7 days or
  fewer label every day with its two-letter weekday under its bar or point, no dates, no mark for
  today (today is always last).
- The Periods requirement states the rolling lengths explicitly. No behaviour change there.
- Front page cards keep their date text.

Parts touched: app screens only (Historia chart x-axis).

## Capabilities

### New Capabilities

### Modified Capabilities
- `weight-chart`: Periods (rolling lengths), Chart axes (weekday labels for short periods).

## Impact

`chart/AxisTicks` (labels for ≤ 7 days), `ui/ChartAxes` date row (labels centred under each day
via the shared `ChartAxis` day centre); tests in `AxisTicks`' test.
