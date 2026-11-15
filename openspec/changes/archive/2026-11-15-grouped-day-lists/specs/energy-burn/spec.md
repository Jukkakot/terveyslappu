## MODIFIED Requirements

### Requirement: Kulutus tab
Historia SHALL have a fixed tab "Kulutus" after Askeleet. Its chart SHALL show for the chosen
period one bar per day with data: lepo at the bottom in a light tone and aktiivinen on top in the
full tone of the Kulutus colour (a "kokonais" day in the light tone, an active-only day in the full
tone), bar height the day's total; the flat dashed average line at the period's average total per
day with data (today left out); weekly bars (average per day with data of the week, today left out)
for periods over 90 days; the shared axes, period range line and legend ("lepo", "aktiivinen",
"keskiarvo", "viikoittain" when weekly). Tapping a bar SHALL show, through the crosshair, its day (or week) with total, active and lepo
kcal. Under the chart: the average total kcal per day, the average active kcal per
day, and "x / y päivää"; then the days newest first and grouped as in weight-chart "Grouped day lists", each with its date ("Tänään", "Eilen",
weekday and date as on Askeleet), total kcal and "aktiivinen N kcal". The tab SHALL show energy
burned only: no eaten energy, no difference, no target.

#### Scenario: A week
- **GIVEN** 1 vk with six days of about 2 300 kcal and today 900
- **WHEN** Kulutus is shown
- **THEN** seven two-tone bars, the average line at the six days' average and Ma–Su under them

#### Scenario: No data
- **GIVEN** no burned energy in the period
- **THEN** the tab reads "Tällä jaksolla ei ole kulutustietoja." instead of an empty chart
