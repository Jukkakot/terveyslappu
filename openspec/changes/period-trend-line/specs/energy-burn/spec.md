# Delta for energy-burn

## MODIFIED Requirements

### Requirement: Kulutus tab
Historia SHALL have a fixed tab "Kulutus" after Askeleet. Its chart SHALL show for the chosen
period one bar per day with data: lepo at the bottom in a light tone and aktiivinen on top in the
full tone of the Kulutus colour (a "yhteensä" day in the light tone, an active-only day in the full
tone), bar height the day's total; the flat dashed average line at the period's average total per day with data (today left out) on periods up to 31 days and the trend line on longer ones (weight-chart "Trend line on long periods"); weekly bars (average per day with data of the week, today left out)
for periods over 90 days; the shared axes, period range line and legend ("lepo", "aktiivinen",
"keskiarvo", "viikoittain" when weekly). Tapping a bar SHALL show, on the chart's marker card, its
day (or week) and the row "kulutus 2 946 kcal · aktiivinen 465 kcal · lepo 2 481 kcal" (a total-only day
"kulutus 2 300 kcal", an active-only day "kulutus 465 kcal · aktiivinen 465 kcal", no data "kulutus ei
tietoa"; a weekly bar its averages per day, "keskiarvo 2 300 kcal/pv · aktiivinen 450 kcal/pv · lepo 1 850 kcal/pv", as food's "keskiarvo 1 900 kcal/pv"). Under the chart: the average total kcal per
day, the average active kcal per
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

#### Scenario: Tap a split day
- **GIVEN** Thursday 12.11. with 2 946 kcal in all and 465 kcal active
- **WHEN** the user taps its bar
- **THEN** the marker card reads "To 12.11." with the row "kulutus 2 946 kcal · aktiivinen 465 kcal · lepo 2 481 kcal"

### Requirement: Kulutus card
The front page SHALL have a card "Kulutus" built like the steps card at every size: title
"Kulutettu tänään", today's total large with "kcal" beside it in a smaller type, as the food card, with "aktiivinen N kcal" under it, small two-tone daily
bars of the card's own period (7, 30 or 90 days, its own menu, remembered, 7 by default), the
period's first and last day and the average total ("keskiarvo 2 300 kcal/pv", "keskiarvo 2 300 kcal/pv" at
small size), the big size's scale top and dashed average line (the trend line on 3 kk, weight-chart "Trend line on long periods"). A tap anywhere but the period label
SHALL open Historia on Kulutus. Without the permissions it SHALL read "Kulutus: anna lupa" and a
tap SHALL ask for them. The card SHALL be hidden by default (see home-screen "Card grid"); its default size is small.

#### Scenario: Card added
- **WHEN** the user adds Kulutus from Lisää kortti and taps Valmis
- **THEN** the card shows today's burned kcal and the last 7 days' bars
