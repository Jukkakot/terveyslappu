# Proposal

## Why

The app only lists the last 30 days, so the user cannot see where the weight is heading or how it
went over a chosen period without opening Samsung Health. Daily weights swing by up to a kilo or
two with water and salt, so a smoothed trend tells more than single readings (the approach of
weight apps such as Happy Scale, Libra and The Hacker's Diet). This replaces the earlier decision
that Samsung Health is the only viewer (user 2026-11-09).

## What Changes

- A chart at the top of the main screen: each weigh-in as a dot, a smoothed trend line through them.
- Period chips: 1 vk (first default), 1 kk, 3 kk, 1 v, Kaikki and Oma (a date-range picker); the
  last choice is remembered.
- A stats row for the period: trend now, change over the period, rate per week, lowest, highest and
  number of weigh-ins.
- Tapping a dot shows its date, time, weight and trend value.
- Only weights this app wrote (scale, manual, imported) are in the chart and stats.
- The weight list under the chart follows the chosen period instead of a fixed 30 days.
- Periods longer than 30 days need the history permission; without it the chart offers to ask.
- Not now: goal weight and forecast (later), BMI, steps, body composition.

## Capabilities

### New Capabilities
- `weight-chart`: chart, trend line, periods and statistics of the user's own weights.

### Modified Capabilities
- `weight-entry`: "Recent weights from Health Connect" lists the chosen period instead of 30 days.

## Impact

Main screen (new chart and stats above the list) and Health Connect reads (the app's own weights
over the chosen period, history permission for long periods). No new dependency: the chart is
drawn with Compose. No build/CI change; the widget and weigh-in are untouched.
