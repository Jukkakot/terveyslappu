# Proposal

## Why

Tapping a chart should always show a clear pointer and the data at that spot, the date included
(user, 2026-11-15). Today only own charts have the crosshair; Paino/BMI show a small dot text,
Askeleet and Kulutus a bar text, and Syöty jumps to Ruoka.

## What Changes

- Every Historia chart (Paino, BMI, Syöty, Askeleet, Kulutus, own charts) gets the own charts'
  crosshair: vertical line, a marker on the chosen value (ring on a dot, chosen bar full, others
  dimmed), and a readout above the plot that starts with weekday and date (year when not this
  year), or the week for weekly bars.
- Syöty's bar tap no longer jumps straight to Ruoka: the readout offers "Avaa päivä ›".
- Front page cards unchanged (tap opens Historia; long-press edits).

Parts touched: app screens (Historia charts).

## Capabilities

### New Capabilities

### Modified Capabilities
- `weight-chart`: new requirement Crosshair on every Historia chart; Point details, Askeleet chart
  and Chart crosshair point to it.
- `food-log`: Ruoka is opened from Syöty through the readout.
- `energy-burn`: the Kulutus tab's tap uses the crosshair.

## Impact

`ui/CustomChartView` crosshair gestures and readout made reusable (`ui/ChartPanels`), adopted by
`HistoryScreen` (Paino/BMI `WeightChart`), `EnergyPage`, `StepsPage`, `BurnPage`; a pure readout
text builder per tab in `chart/`; marker drawing; tests for the readout texts.
