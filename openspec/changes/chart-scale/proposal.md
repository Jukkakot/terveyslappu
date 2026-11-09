# Proposal

## Why

The home card's 30-day line looked like a steep drop while the change read ±0,0 kg (user
2026-11-09, screenshot). Two causes in the code: the y-axis stretches from the lowest to the highest
weight with no minimum, so a 0.08 kg difference (both shown as 97,8) fills the whole height; and
the x-axis spans only the first to the last weight, so two days look like the whole "30 päivää".
Historia's chart has the same kind of rule (1 kg minimum), so its scale jumps between periods.

## What Changes

- One y-scale rule for every weight chart (home card and Historia): the weights with 0.5 kg room,
  at least **4 kg** high (±2 kg, a normal month of daily weighing: water and glycogen move the
  weight 0.5–2 kg from day to day), centred on the weights and rounded out to whole kilos. The scale
  grows with the data when the weights spread more; it never zooms below 4 kg, so the same change
  looks about the same in every period and on both screens.
- Home card: the line spans the whole 30 days (short history sits at the right end). Under it the
  start and end dates of the 30 days, and the first and latest weight of the period with the unit
  next to the 30-day change: `98,6 → 97,8 kg` … `−0,8 kg`.
- Units everywhere: Historia's y-axis labels read `98 kg`; every weight and change on the screens
  shows `kg`.
- No unit choice (the user wants kg only).

Not now: a smoothed trend line (moving average) on the chart; noted in the backlog.

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- `home-screen`: the card's 30-day chart: full 30 days, scale rule, dates and start/end weights.
- `weight-chart`: the shared y-scale rule and kg on the axis labels.

## Impact

- App screens: home card, Historia chart. Code: new pure scale rule in `chart/`, `HomeSummary`,
  `HomeScreen` sparkline, `ChartData`, `WeightChart` axis labels; strings.
- Health Connect, scale, widget, build/CI: untouched.
