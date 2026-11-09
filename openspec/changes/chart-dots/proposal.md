# Proposal

## Why

The user wants to see each weight as a dot on the line when there are few of them, and only the
line when the chart is crowded (user 2026-11-09). Today the home line has no dots (only the
latest), and Historia drops all dots above a fixed 120 weights, which still crowds a phone-wide
chart long before that.

## What Changes

- One rule for both charts: each weight is a dot on the line when the chart has room, at least
  10 dp of width per weight (a dot is 5–7 dp, so dots never touch); when it is denser only the line
  is drawn. Chart guides agree on the idea (markers for sparse, discrete points; none on dense daily
  series) but give no number, so the rule is by space, not by period: on a phone 1 vk and a month of
  daily weighing get dots, 3 kk and longer do not.
- Kept as today: a lone weight between two gaps over 30 days always has its dot (Historia); the
  latest weight on the home line always has its larger dot.
- Home line dots are small (5 dp) next to the latest's 8 dp.

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- `weight-chart`: dots shown by available width instead of a fixed count, on both charts.

## Impact

- App screens: Historia chart, home card line. Code: a pure dot rule in `chart/`, `ui/WeightChart`
  (chart width), `ui/HomeScreen` sparkline. Health Connect, scale, widget, CI: untouched.
