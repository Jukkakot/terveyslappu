# Tasks

## 1. Series and data

- [ ] 1.1 `food/EnergySeries` and `steps/StepsSeries`: drop the rolling 7-day points; the chart's average line uses the existing stats average (today left out). Weekly bars over 90 days stay.
- [ ] 1.2 `chart/CustomCharts` + `ChartCodec`: a `showAverage` flag on the chart and the draft (default false; JSON without it reads false; undo of a delete keeps it).
- [ ] 1.3 `chart/CustomChartData`: per data the period average as the spec says (Energia per logged day and Askeleet per day with steps, today out; Paino each day's last non-"?" weight, today in); null when nothing to average.
- [ ] 1.4 Tests (made-up values): Energia/Askeleet series have no rolling points and the average excludes today; codec round trip and old JSON; custom averages per kind incl. today rules and the empty case.

## 2. Drawing

- [ ] 2.1 `ui/ChartPanels`: one shared flat dashed line draw (1 dp, the big cards' dash); `ui/HomeScreen`'s `averageLine` uses it.
- [ ] 2.2 `ui/EnergyPage`, `ui/StepsPage`: draw the flat line at the stats average (onSurfaceVariant, as on the cards); legend "keskiarvo" with a dashed swatch only when drawn; the y-scale top from the bars.
- [ ] 2.3 `ui/CustomChartView`: when on, a dashed line per drawn data in its tone on its scale, over the bars, in both layouts and in card mode; one "keskiarvo" legend entry.
- [ ] 2.4 `ui/ChartEditor`: the Keskiarvo switch with its line, after the layout choice.
- [ ] 2.5 Smoke test: the editor's switch saves the flag.

## 3. Context

- [ ] 3.1 Remove the string `energy_legend_average` ("7 pv keskiarvo") or reword it to "keskiarvo".
- [ ] 3.2 `product.md` keeps the decisions; roadmap item done; bump MINOR in `gradle.properties`.
- [ ] 3.3 Run `./gradlew test lint assembleDebug`.
