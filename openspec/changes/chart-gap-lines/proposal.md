# Proposal

## Why

Historia's chart breaks its line wherever two weights are more than 30 days apart. With sparse
history (imported years, weighing once a month or less) much of the chart is loose dots with no
line, which hides the trend. The user wants lines between the dots more aggressively, straight
from dot to dot (no smoothing).

## What Changes

- Historia's chart no longer breaks the line at a gap over 30 days: the two weights on either side
  are joined by a straight dashed line in the same colour, so the trend reads through the gap while
  the dashes still tell that there were no weighings in between.
- Within a run the line stays solid and straight (Vico's sharp interpolation, as today).
- A weight with a gap over 30 days on both sides keeps its dot in a dense chart (unchanged).
- The home card's small line already joins every weight with a straight line; unchanged.

Parts touched: app screens (Historia chart only).

## Capabilities

### New Capabilities

### Modified Capabilities
- `weight-chart`: "Weight chart" requirement: a long gap is drawn as a dashed line instead of a
  break; the line is straight from dot to dot.

## Impact

`ui/WeightChart.kt` (an extra dashed series between segments). `chart/ChartData.segments` stays.
