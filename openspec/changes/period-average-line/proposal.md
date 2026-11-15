# Proposal

## Why

The Askeleet chart's 7-day line sat under every bar of the week (user, 2026-11-15, screenshot):
each point averaged the 7 days ending on that day, so it reached back to days left of the chart.
The user wants a flat line at the period's average instead, to see at a glance which days went
over and which stayed under it (steps, food). The big front page cards already draw that line;
Historia should match. The user also wants it as an option in own charts.

## What Changes

- Energia and Askeleet: the rolling 7-day line is replaced by one flat dashed line at the
  period's average per day, the same number as the Keskimäärin tile (today left out). Legend
  "keskiarvo".
- Own charts: the editor gets a "Keskiarvo" switch (off by default); when on, each drawn data gets
  a flat dashed line at its period average in its own tone and scale (kcal and steps without
  today, weight with every day's last weight not marked "?", today included since a weight is
  final).
- Unchanged: the big cards' line, the stat tiles, the bars (daily up to 3 kk, weekly over 90 days
  on Energia and Askeleet; own charts stay daily), the weight line's real daily values.

Parts touched: app screens only (Historia Energia, Askeleet, own charts and their editor). No
Health Connect, scale, widget or build change.

## Capabilities

### New Capabilities

### Modified Capabilities
- `weight-chart`: Energia chart, Askeleet chart, Chart legends, Custom charts, Chart editor,
  Stacked layout, Overlaid layout.
- `steps`: Days and gaps (the 7-day line is gone; the average line leaves today out).

## Impact

`food/EnergySeries`, `steps/StepsSeries` (rolling points out), `chart/CustomCharts` +
`ChartCodec` (new flag, old JSON reads as off), `chart/CustomChartData` (period averages),
`ui/EnergyPage`, `ui/StepsPage`, `ui/CustomChartView`, `ui/ChartEditor`, `ui/ChartPanels` (one
shared dashed-line draw, also used by `ui/HomeScreen`); their tests.
