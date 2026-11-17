# Proposal

## Why

On long periods (3 kk, 1 v, Kaikki) the daily bars and weights zigzag so much that the user has
to judge the direction by eye. The user wants to see at a glance how a value has developed on
average over months or years (user 2026-11-17, reversing "never smoothed values" of 2026-11-15).
A flat period average says nothing about the direction; a smoothed trend line does.

## What Changes

- On periods longer than 31 days (period length = the chart axis's day count; Kaikki its own axis)
  the Historia charts draw a **trend line**: a smooth curve through the daily values that irons out
  the day-to-day zigzag. It replaces the flat dashed average line there.
- Shorter periods keep the flat dashed average line. Oma 31 days is flat, 32 days trend.
- Applies in Historia only: Syöty, Askeleet, Kulutus, Paino and BMI tabs and own chart tabs (the
  Keskiarvo switch). Paino and BMI have no flat average today, so they get the trend only.
- Smoothness: window = the longer of 21 days and a sixth of the axis's days; no setting.
- A line breaks over a gap of more than 30 days; fewer than 10 values gives no line (flat average
  line instead, nothing on Paino/BMI).
- Front page cards (every size, own chart cards) are unchanged: they keep the flat average line
  at any period (user decision 2026-11-17).
- Legend gets "trendi" (solid swatch); the marker card gets a trend row after the data's row
  ("paino trendi 81,4 kg" in own charts).
- Statistics tiles stay as they are (the period average stays a number).
- Product decision "never smoothed values" is replaced by this rule.

Parts touched: Historia charts and own chart tabs. No front page, Health Connect, scale, widget or
CI changes. New build dependency: a maths library for the smoothing.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `weight-chart`: trend line (new requirements), Energia, Askeleet, legend, own chart, crosshair,
  editor, overlaid layout, shared scale and running-line rules.
- `energy-burn`: the Kulutus tab follows the trend rule.

## Impact

- `chart/` pure rules (a new trend rule next to the average) and the chart definitions
  (`TabCharts`, `CustomChartSpec`), legend and marker card rows (`TabReadouts`).
- `ui/HistoryChartView` draws one more line kind.
- `gradle/libs.versions.toml`, `app/build.gradle.kts`: the maths library.
