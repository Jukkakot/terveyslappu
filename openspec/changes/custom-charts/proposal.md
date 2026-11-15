# Proposal: custom-charts

## Why

Weight, food and steps are in Historia one quantity per tab; Energia and Askeleet only show the
weight line under their bars. The user wants to see them together, in charts of their own (roadmap
"combined chart", user 2026-11-14; answers 2026-11-15 replace the single fixed "Yhdistelmä" tab of
the first draft): as many charts as they like, each with its own data, bar or line per data, and
either stacked panels or one overlaid plot, shown in Historia and on the front page.

## What Changes

- **User-made charts, any number.** Each chart has a name, the shown data (any of Paino, Energia,
  Askeleet; at least one; no BMI, Q13), a style per data (Pylväät or Viiva), a layout:
  **Allekkain** (one panel per data, stacked on one shared time axis) or **Päällekkäin** (one plot
  with all data overlaid and two value axes; a third data has no scale, only its colour and legend
  name), and **its own time range** (the same choices as Historia's period chips, Oma calendar
  included; 1 kk by default), stored in the chart definition.
- **Real values, never averages.** A custom chart draws each day's own value: weight = the day's
  last weight not marked "?" (line or bars), Energia and Askeleet as a line join each day's own
  value (no 7-day average). There are NO weekly average bars: at every period, long ones too, daily
  values are drawn (thin bars with a minimum width). The 7-day average lines are gone from custom
  charts. (The fixed Energia and Askeleet tabs keep their averages and weekly bars unchanged.)
- **One chart definition, shown in two places:** always as its own Historia tab after Askeleet
  (Historia still opens on Paino), and optionally as a front page card (a card type of
  `home-card-layout`, added from "Lisää kortti", sized small, wide or big like the other cards).
  Editing a chart changes both; deleting it removes both. The chart's period is the same in both
  places: on its Historia tab the chart's own period chips replace the shared ones (the shared
  chips do not affect it), and changing the period there changes the card too. The card shows the
  period as a plain label (no separate card period).
- **Create, edit, rename, delete in one small editor** (a dialog): name, the three data rows with
  a switch and Pylväät/Viiva, the layout, Tallenna/Peruuta, and "Poista kaavio" for an existing
  chart. Created from Historia's last tab "Uusi kaavio" or from the front page's "Lisää kortti".
  A new chart starts with all three data on (Q12 a): Paino as a line, Energia and Askeleet as
  bars, Allekkain, 1 kk.
- **Reading a chart:** axes and legend from the shared helper of `steps-source-and-axes`; a tap, or
  a press-and-hold (released in place or dragged), puts one crosshair through the chart and a
  readout above it shows that day's own values; a plain swipe still switches tabs.
- Data without permission or without values keeps its panel (Allekkain) or is named under the plot
  (Päällekkäin); the chart never asks for a permission by itself.
- Remembered in app preferences (moves with Android's phone transfer like the periods), not in the
  data file or the backup file. Names are never logged.
- Front page layout rules for charts: "Piilota" on a chart card removes it from both lists (it
  returns from "Lisää kortti", wide); "Palauta oletus" also clears chart ids; deleting the chart
  that is the only shown card saves the default layout.
- Not in this change: BMI as data (Q13), reordering the data inside a chart, reordering the chart
  tabs, statistics or a day list on chart tabs, exporting a chart, weekly bars.
- Parts touched: Historia (tabs, a chart page with its own period chips, the editor), the
  Energia/Askeleet drawing (extracted into shared panel drawing), the front page grid (chart card,
  "Lisää kortti"), app preferences, Ohje.

## Capabilities

- weight-chart (modified: One quantity at a time; added: Custom charts, Custom chart tabs, Chart
  editor, Stacked layout, Overlaid layout, Chart crosshair, Chart time axis, Chart data without
  values)
- home-screen (added: Chart cards)
- help-page (added: Custom charts in Ohje)
- app-data-transfer (added: Carry custom charts)

## Impact

**Depends on `steps-source-and-axes`** (its pure axis helper `AxisTicks` and shared `ChartLegend`
are reused by every panel and plot) **and on `home-card-layout`** (chart cards are cards of its
grid, its `HomeLayout` ids and "Lisää kortti"). Implementation order: `steps-source-and-axes`,
`home-card-layout`, then this change; archive each first. Uses the existing series (`EnergySeries`,
`StepsSeries`, `WeightScale`) in daily mode; no new Health Connect reads.
