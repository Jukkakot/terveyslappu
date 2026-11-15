# Tasks

## 1. Series without averages

- [ ] 1.1 `food/EnergySeries`: drop the 7-day `average` points and the weekly bucketing; bars are one per logged day at every period. Stats (average kcal, treats, day count) unchanged.
- [ ] 1.2 `steps/StepsSeries`: same: no 7-day line points, no weekly bars; stats unchanged (today still left out of the average).
- [ ] 1.3 `chart/ChartAxis`: drop the `weekly` flag and the weekly-Monday helper if nothing else uses them.
- [ ] 1.4 Update `EnergySeriesTest` and `StepsSeriesTest`: a 1 v period gives daily bars; no average points exist; stats as before (made-up values).

## 2. Screens

- [ ] 2.1 `ui/EnergyPage` and `ui/StepsPage`: no average line, no "7 pv keskiarvo" or "viikoittain" legend entry; the y-scale top from the bars only; a bar tap picks a day, never a week.
- [ ] 2.2 `ui/HomeScreen`: big food and steps cards draw no dashed line; remove `averageLine` and the `average` parameters of `FoodBars`/`StepsBars`. The "keskim. … /pv" text stays.
- [ ] 2.3 Remove the unused strings `energy_legend_average` and `legend_weekly`; adjust `HomeCardsTest` if it checks the line.

## 3. Context

- [ ] 3.1 `openspec/specs/weight-chart/spec.md` "Date labels": drop the stale "also when the bars are weekly" phrase.
- [ ] 3.2 Roadmap: backlog item done (no-average-lines); bump MINOR in `gradle.properties`.
- [ ] 3.3 Run `./gradlew test lint assembleDebug`.
