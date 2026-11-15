# Proposal

## Why

The Askeleet chart's 7-day line sat under every bar of the week (user, 2026-11-15, screenshot):
each point averaged the 7 days ending on that day, so it reached back to days left of the chart
and confused more than it told. The user does not want averages drawn in any chart; charts show
the real daily values. Number averages (the stat tiles, the cards' "keskim." text) stay.

## What Changes

- Historia's Energia and Askeleet charts draw no 7-day average line and no legend entry for it.
- Energia and Askeleet bars are one per day at every period; the weekly bars (an average per week)
  for periods over 90 days go, and with them the legend's "viikoittain". Same rule as own charts.
- The big food and steps cards on the front page draw no dashed average line.
- Unchanged: the Keskimäärin and herkut stat tiles, the cards' "keskim. … /pv" text, today left
  out of those averages.
- Not now: an optional average line in own charts (user: "korkeintaan … voi haluta"); noted in the
  roadmap backlog with its rule (average of the days under the line, today left out).

Parts touched: app screens only (Historia Energia/Askeleet, front page cards). No Health Connect,
scale, widget or build change.

## Capabilities

### New Capabilities

### Modified Capabilities
- `weight-chart`: Energia chart, Askeleet chart, Askeleet weekly bars (removed), Chart legends.
- `home-screen`: Card sizes (big food and steps cards lose the dashed line).
- `steps`: Days and gaps (no 7-day line or weekly bar averages to leave today out of).

## Impact

`food/EnergySeries` and `steps/StepsSeries` lose the average line and weekly bucketing;
`ui/EnergyPage`, `ui/StepsPage` (line, legend), `ui/HomeScreen` (`averageLine`); their tests.
