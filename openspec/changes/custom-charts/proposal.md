# Proposal: custom-charts

## Why

Weight, food and steps are in Historia one quantity per tab; Energia and Askeleet only show the
weight line under their bars. The user wants to see them together, in charts of their own (roadmap
"combined chart", user 2026-11-14; answers 2026-11-15 replace the single fixed "Yhdistelmä" tab of
the first draft): as many charts as they like, each with its own data, bar or line per data, and
either stacked panels or one overlaid plot, shown in Historia and on the front page.

## What Changes

- **User-made charts, any number.** Each chart has a name, the shown data (any of Paino, Energia,
  Askeleet; at least one; no BMI, Q13), a style per data (Pylväät or Viiva) and a layout:
  **Allekkain** (one panel per data, stacked on one shared time axis) or **Päällekkäin** (one plot
  with all data overlaid and two value axes).
- **One chart definition, shown in two places:** always as its own Historia tab after Askeleet
  (Historia still opens on Paino), and optionally as a front page card (a card type of
  `home-card-layout`, added from "Lisää kortti", sized small, wide or big like the other cards).
  Editing a chart changes both; deleting it removes both.
- **Create, edit, rename, delete in one small editor** (a dialog): name, the three data rows with
  a switch and Pylväät/Viiva, the layout, Tallenna/Peruuta, and "Poista kaavio" for an existing
  chart. Created from Historia's last tab "Uusi kaavio" or from the front page's "Lisää kortti".
  A new chart starts with all three data on (Q12 a): Paino as a line, Energia and Askeleet as bars,
  Allekkain.
- **Reading a chart:** Historia's period chips; weekly bars over 90 days as on the single tabs; a
  tap or a press-and-hold drag puts one crosshair through the chart and a readout above it shows
  the day's (or week's) values; a plain swipe still switches tabs.
- Data without permission or without values keeps its panel (Allekkain) or is named under the plot
  (Päällekkäin); the chart never asks for a permission by itself.
- Remembered in app preferences (moves with Android's phone transfer like the periods), not in the
  data file or the backup file. Names are never logged.
- Not in this change: BMI as data (Q13), reordering the data inside a chart, reordering the chart
  tabs, statistics or a day list on chart tabs, exporting a chart.
- Parts touched: Historia (tabs, a chart page, the editor), the Energia/Askeleet drawing (extracted
  into shared panel drawing), the front page grid (chart card, "Lisää kortti"), app preferences,
  Ohje.

## Capabilities

- weight-chart (modified: One quantity at a time; added: Custom charts, Custom chart tabs, Chart
  editor, Stacked layout, Overlaid layout, Chart crosshair, Chart time axis, Chart data without
  values)
- home-screen (added: Chart cards)
- help-page (added: Custom charts in Ohje)
- app-data-transfer (added: Carry custom charts)

## Impact

**Depends on `home-card-layout`** (chart cards are cards of its grid, its `HomeLayout` ids and
"Lisää kortti"): implement and archive `home-card-layout` first, then this change. Uses the
existing series (`EnergySeries`, `StepsSeries`, `WeightScale`); no new Health Connect reads.
