# Proposal

## Why

With Kaikki the user could not tell how far back the chart reaches (user, 2026-11-15, Askeleet
screenshot): the x-axis showed "15.8." without a year. Every Historia chart should say which days
it shows, whatever the period.

## What Changes

- Every Historia chart (Paino, BMI, Energia, Askeleet, own chart tabs) shows its first and last
  day with years at the top of its chart card: "15.8.2026 – 15.11.2026".

Parts touched: app screens only (Historia charts).

## Capabilities

### New Capabilities

### Modified Capabilities
- `weight-chart`: new requirement Period range line.

## Impact

A pure formatter next to `chart/AxisTicks` (range text from the axis ends), the chart cards in
`ui/HistoryScreen`, `ui/EnergyPage`, `ui/StepsPage`, `ui/ChartPage`/`CustomChartView` (tab mode).
