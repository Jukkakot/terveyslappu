# Proposal

## Why

In an own chart Syöty and Kulutus are both kcal, but each gets its own scale, so a 1 800 kcal
eaten bar and a 2 600 kcal burned bar can reach the same height. Eaten vs burned only compares
truly when both are drawn on one kcal scale (user 2026-11-16, roadmap backlog).

## What Changes

- Own charts: data with the same unit (today only Syöty and Kulutus, kcal) SHALL share one scale,
  fitted to the higher of them by the kcal rule; data with different units keep their own scales.
- Päällekkäin: the shared data take one value axis between them; the two axes go to the first two
  drawn *units* (so Paino + Syöty + Kulutus shows kg left and kcal right, nothing unscaled). A shared
  axis's labels are in the neutral tone, not one data's colour.
- Allekkain: the Syöty and Kulutus panels use the same scale (same top), each with its own labels.
- Average lines and the front page card (big: scale tops) follow the shared scale.
- No editor change; choosing an axis per series stays a later idea.

## Capabilities

### New Capabilities

### Modified Capabilities
- `weight-chart`: own charts' scales and axes ("Custom charts", "Stacked layout", "Overlaid layout",
  "Chart axes", "Kulutus in own charts", "Data colours in own charts") gain the shared-unit rule.
- `home-screen`: a big chart card's scale tops follow the shared axis.

## Impact

- App screens: Historia's own chart tabs and own chart cards on the front page. Not Health Connect,
  scale, widget or build/CI.
- Code: `chart/CustomChartData` (shared ticks per unit), `chart/CustomChartSpec` (axis sides per
  unit), `ui/CustomChartView` (card scale tops); tests in `CustomChartDataTest`,
  `CustomChartSpecTest`.
