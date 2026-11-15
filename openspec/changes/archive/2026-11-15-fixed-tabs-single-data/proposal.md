# Proposal

## Why

The user wants each fixed Historia tab to show one kind of data only (2026-11-15): Energia only
food, Askeleet only steps. The weight line under their bars mixes data; combinations belong in own
charts (Uusi kaavio), where the user chooses them.

## What Changes

- Energia and Askeleet drop the weight strip under the bars and its "paino" legend entry.
- Their chart descriptions (accessibility) and the Ohje food text stop mentioning weight.
- Units and axis rules no longer mention the weight line under bars.
- Own charts are unchanged; they remain the place to combine weight, energy and steps.

## Capabilities

### Modified Capabilities
- `weight-chart`: Energia chart, Askeleet chart, Weight line under the Askeleet bars (removed),
  Units on the chart, Chart axes, Chart legends.

## Impact

Historia screen only (`ui/EnergyPage.kt`, `ui/StepsPage.kt`, strings, their UI tests). No data,
Health Connect, widget or build change. The shared `PeriodWeights` helper goes away if nothing else
uses it.
