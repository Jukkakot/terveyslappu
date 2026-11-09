# Proposal

## Why

The app only lists the last 30 days, so the user cannot see how the weight went over a chosen period
without opening Samsung Health. This replaces the earlier decision that Samsung Health is the only
viewer (user 2026-11-09). Weights from other apps are the user's inaccurate hand entries, so the app
shows only its own (user 2026-11-09).

## What Changes

- A chart at the top of the main screen: each weight as a dot, joined by a line. No smoothed trend
  line (user 2026-11-09: one morning weigh-in a day, a trend is not needed).
- Period chips: 1 vk (first default), 1 kk, 3 kk, 1 v, Kaikki and Oma (a date-range picker); the
  last choice is remembered.
- A stats row for the period: latest, change from first to latest, change per week, average, lowest,
  highest and number of weights.
- Tapping a dot shows its date, time and weight.
- Chart, stats and list show only weights this app wrote (scale, manual, imported); the list follows
  the chosen period instead of a fixed 30 days and drops the source-app label.
- Periods longer than 30 days need the history permission; without it the chart offers to ask.
- Unchanged: export and backup still hold every app's weights.
- Not now: goal weight and forecast (later), trend smoothing, BMI, steps, body composition.

## Capabilities

### New Capabilities
- `weight-chart`: chart, periods and statistics of the app's own weights.

### Modified Capabilities
- `weight-entry`: the list shows only this app's weights for the chosen period; deleting no longer
  needs the own-or-other distinction in the list.

## Impact

Main screen (new chart and stats above the list, list filter) and Health Connect reads (the app's
own weights, history permission for long periods). New dependency: the Vico chart library. No build/CI change; the widget and weigh-in are untouched.
