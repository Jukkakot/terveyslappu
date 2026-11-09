# Proposal

## Why

The user wanted the home chart to show a week by default (said when the chart was planned); the
main-screen refresh gave the home card a fixed 30-day line instead. The user wants a week first and
a way to change it (user 2026-11-09).

## What Changes

- The home card's small chart shows a chosen period: **1 vk** (default), **1 kk** or **3 kk**.
- Under the chart a small period label (`1 vk ▾`) opens a menu with the three choices; tapping it
  does not open Historia. The rest of the card still opens Historia.
- The choice is the home card's own (Historia's period stays separate) and is remembered between
  app starts.
- Everything under the chart follows the period: the start and end dates, `first → latest kg` and
  the change. The scale rule from chart-scale stays.

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- `home-screen`: the card's chart period: default 1 vk, choice of 1 vk / 1 kk / 3 kk, remembered.

## Impact

- App screens: home card only. Code: `chart/HomeSummary` (period instead of fixed 30 days), a
  remembered home period in preferences, `ui/HomeScreen` label and menu, `MainViewModel`; strings;
  log `home.period`.
- Health Connect, scale, widget, build/CI: untouched.
