# Tasks

## 1. Series and data

- [ ] 1.1 `food/EnergySeries` and `steps/StepsSeries`: drop the rolling 7-day points; the chart's average line uses the existing stats average (today left out). Weekly bars over 90 days stay; Energia's week bars now leave today out like Askeleet's.
- [ ] 1.2 `chart/CustomCharts` + `ChartCodec`: a `showAverage` flag on the chart and the draft, incl. `ChartDraft.of`/`toChart` (default false; JSON without it reads false; undo of a delete keeps it).
- [ ] 1.3 `chart/CustomChartData`: per data the period average as the spec says (Energia per logged day and Askeleet per day with steps, today out; Paino each day's last non-"?" weight, today in); null when nothing to average.
- [ ] 1.4 Tests (made-up values): Energia/Askeleet series have no rolling points and the average excludes today; codec round trip and old JSON; custom averages per kind incl. today rules and the empty case.

## 2. Drawing

- [ ] 2.1 `ui/ChartPanels`: one shared flat dashed line draw (1 dp, the big cards' dash); `ui/HomeScreen`'s `averageLine` uses it.
- [ ] 2.2 `ui/EnergyPage`, `ui/StepsPage`: draw the flat line at the stats average (onSurfaceVariant, as on the cards); legend "keskiarvo" with a dashed swatch only when drawn; the y-scale top = max(highest bar, average).
- [ ] 2.3 `ui/CustomChartView`: when on, a dashed line per drawn data on its scale (onSurfaceVariant in Allekkain, the data tone in Päällekkäin), over the bars and under value lines and crosshair, in both layouts and in card mode; one "keskiarvo" legend entry.
- [ ] 2.4 `ui/ChartEditor`: the Keskiarvo switch (always enabled) with its line, after the layout choice.
- [ ] 2.5 Smoke test: the editor's switch saves the flag.

## 3. Context

- [ ] 3.1 Reword the string `energy_legend_average` to "keskiarvo" (kept, now shared); `LegendEntry` gets a dashed swatch.
- [ ] 3.1b Log `average=on|off` on `chart.custom create|edit` and add it to the log list in `.claude/CLAUDE.md`. The crosshair readout stays without averages. Own-chart scale steps (250/1 000 in code vs 500/2 000 in the spec) are out of scope: leave them.
- [ ] 3.2 `product.md` keeps the decisions; roadmap item done; bump MINOR in `gradle.properties`.
- [ ] 3.3 Run `./gradlew test lint assembleDebug`.
