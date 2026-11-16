# Proposal

## Why

An own chart can draw Paino as Pylväät, and those bars start from the bottom of the fitted weight
scale, not from zero. A bar's height then no longer tells its value: 82 kg can look twice as tall
as 81 kg. Bars are read by their length, so a cut bar axis misleads (product principle "Data reads
one way only", user 2026-11-16).

## What Changes

- Own charts draw Paino only as Viiva (a line); the Pylväät choice is removed for Paino.
- The chart editor's Paino row shows a plain "Viiva" label where the other data have their
  Pylväät / Viiva choice, so the rows still line up.
- A chart saved with Paino as Pylväät reads as Viiva; nothing else in it changes.
- Every bar in an own chart now starts at zero (the "Paino excepted" exception goes).

## Capabilities

### New Capabilities

### Modified Capabilities
- `help-page`: "Custom charts in Ohje" says Paino is always a line.
- `weight-chart`: "Custom charts" (Paino styles, bars from zero), "Chart editor" (Paino row without
  a style choice), "Chart editor colours" (the Paino row's colour dot follows the label).

## Impact

- App screens: Historia's own chart tabs, own chart cards on the front page, the chart editor.
  Not Health Connect, scale, widget or build/CI.
- Code: `chart/CustomCharts` (Paino style always line, `setStyle` ignores Paino), `chart/ChartStore`
  (decode Paino as line), `ui/ChartEditor` (label instead of the segmented choice). The drawing code
  needs no change: it never gets Paino bars any more.
