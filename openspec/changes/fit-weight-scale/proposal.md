# Proposal

## Why

The weight charts' y-scale is never narrower than 4 kg, so a week's 1 kg of moves draws an almost
flat line, worst on the short home card chart. The user wants every chart fitted to the data it
shows (user 2026-11-16), on the home card and Historia alike.

## What Changes

- `WeightScale`: room 0.5 → 0.2 kg, least span 4 → 1 kg, rounding to whole kilos → half kilos,
  plus one widening step so the middle axis value is on a half kilo. BMI the same in BMI units.
- The rule stays shared: Historia, the home weight card and own charts' weight series all use it,
  so a change still looks the same size everywhere.
- Accepted trade-off: daily water swings (0.5–2 kg) now look bigger than before; the user chose
  fit over calm.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `weight-chart`: "Weight scale" fits the data (1 kg least span, half-kilo rounding).

## Impact

`chart/WeightScale.kt` (the one rule; its doc comment explains the old 4 kg reason and must be
rewritten), `chart/AxisTicks.kt` only if needed for half-kilo labels, tests `WeightScaleTest`,
`TabChartsTest`, `ChartFixtures` expectations. Axis label formatting (`formatAxisKg`) already
prints one decimal when needed.
