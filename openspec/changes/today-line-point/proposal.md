## Why

In an own chart, a Syöty, Kulutus or Askeleet line joins today's running total, which is only a
part of the day. The line dives at the right end every day (seen on a Kalori chart: Kulutus from
about 2 800 to 500 kcal), which looks like a real drop.

## What Changes

- In own charts (Historia tabs and front page cards), a line of Syöty, Kulutus or Askeleet ends at
  the last day before today; today's value, when there is one, is a lone dot in the line's tone.
- Paino lines are unchanged (a weight is final).
- Bars, averages, the readout and the legend are unchanged.

## Capabilities

### New Capabilities

### Modified Capabilities

- `weight-chart`: own charts' lines leave today's running value out of the line.

## Impact

`chart/CustomChartSpec` (line building, both layouts) and its tests. No data or settings change.
