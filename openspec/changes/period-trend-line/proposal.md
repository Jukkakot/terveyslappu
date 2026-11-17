# Proposal

## Why

On long periods (3 kk, 1 v, Kaikki) the daily bars and weights zigzag so much that the user has
to judge the direction by eye. The user wants to see at a glance how a value has developed on
average over months or years (user 2026-11-17, reversing "never smoothed values" of 2026-11-15).
A flat period average says nothing about the direction; a smoothed trend line does.

## What Changes

- On long periods (over 31 days: 3 kk, 1 v, Kaikki, an Oma range over 31 days) the Historia
  charts draw a **trend line**: a smooth curve through the daily values that follows the data
  but irons out the day-to-day zigzag. It replaces the flat dashed average line there.
- Short periods (1 vk, 1 kk, Oma up to 31 days) keep the flat dashed average line as today.
- Applies to Syöty, Askeleet and Kulutus, and (pending the user's answer) to Paino and BMI,
  which today have no average line at all.
- Own charts: the Keskiarvo switch draws the flat line on short periods and the trend line on
  long ones, per drawn data.
- Front page: the big food, steps and Kulutus cards draw the trend line on their 3 kk period;
  own chart cards follow their chart.
- The legend gets a "trendi" entry (solid line swatch); the marker card gets a "trendi" row with
  the trend's value on the chosen day.
- Statistics tiles stay as they are (the period average stays a number).
- Product decision "never smoothed values" is replaced by this rule.

Parts touched: app screens only (Historia charts, own charts, front page cards). No Health
Connect, scale, widget or CI changes. New build dependency: a maths library for the smoothing.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `weight-chart`: trend line on long periods (new requirement), Energia, Askeleet, legend and
  own chart rules no longer forbid a moving line.
- `energy-burn`: the Kulutus tab follows the trend rule.
- `home-screen`: big cards draw the trend line on 3 kk.

## Impact

- `chart/` pure rules (a new trend rule next to the average) and the chart definitions
  (`TabCharts`, `CardCharts`, `CustomChartSpec`), legend and marker card rows (`TabReadouts`).
- `ui/HistoryChartView` draws one more line kind.
- `gradle/libs.versions.toml`, `app/build.gradle.kts`: the maths library.
