# energy-burn Specification

## Purpose
Burned energy (Kulutus) as its own measured data: each day's active and resting kcal as Garmin
(through Health Connect or its export) reports them, shown in Historia, on a front page card and in
own charts, never computed by the app and never written anywhere.

## Requirements

### Requirement: Burned energy is read, never computed or written
The app SHALL read each day's burned energy from Health Connect (daily total calories burned and
daily active calories burned, read-only permissions only) for the steps source in use (Garmin
Connect, another single app, or all apps), and from the imported Garmin history (see steps
"Import Garmin step history"). A day's parts SHALL be: active = the active kcal, lepo = total −
active, when both are known; only total known: the day is one part "yhteensä"; only active known:
the day is active only. The app SHALL NOT estimate any part from height, weight, age or a formula,
and SHALL NOT write, change or delete calories burned in Health Connect. Burned energy SHALL be
shown only inside the app and SHALL NOT be in the data file or the backup file.

#### Scenario: Split from Health Connect
- **GIVEN** Garmin Connect wrote 2 300 kcal total and 450 kcal active for yesterday
- **THEN** yesterday shows lepo 1 850 and aktiivinen 450, 2 300 in all

#### Scenario: Total only
- **GIVEN** Health Connect holds a total of 2 200 kcal for a day and no active kcal
- **THEN** the day is one bar of 2 200 kcal in the "yhteensä" tone, without a split

### Requirement: Imported Garmin days for burned energy
An imported day's active and resting kcal SHALL be used under the same source rule as imported
steps (Garmin Connect or no app filter). On a day both Health Connect and the import have, the one
with the larger total SHALL be shown with its own split. Today SHALL never come from the import.

#### Scenario: Before Health Connect
- **GIVEN** an import from 2023 with active and resting kcal and Health Connect data from 15.8.2026
- **WHEN** Kulutus is shown with Kaikki
- **THEN** the chart starts at the import's first day and each imported day shows its two parts

### Requirement: Burned energy permission
Kulutus SHALL need the two Health Connect read permissions for total and active calories burned,
asked together only from the Kulutus tab or the Kulutus card, in the same way, wording pattern and
states as the steps permission (not asked, denied, Health Connect unavailable, loading, failed).
Older days follow the same history permission rule as steps; imported days need no permission
other than the two read permissions.

#### Scenario: First open
- **GIVEN** the permissions were never asked
- **WHEN** the user opens Kulutus
- **THEN** a line explains why and a button asks for both permissions

### Requirement: Kulutus days
A day without burned energy SHALL be a gap, never a zero bar, and SHALL be left out of averages and
day counts. Today SHALL be shown but left out of the averages, the average line and the weekly bar
averages (it is not over); it SHALL count as a day with data in "x / y päivää" when it has data.

#### Scenario: Today
- **GIVEN** today has 900 kcal so far and the earlier days average 2 300
- **THEN** the average stays 2 300

### Requirement: Kulutus tab
Historia SHALL have a fixed tab "Kulutus" after Askeleet. Its chart SHALL show for the chosen
period one bar per day with data: lepo at the bottom in a light tone and aktiivinen on top in the
full tone of the Kulutus colour (a "yhteensä" day in the light tone, an active-only day in the full
tone), bar height the day's total; the flat dashed average line at the period's average total per
day with data (today left out); weekly bars (average per day with data of the week, today left out)
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
period's first and last day and the average total ("keskiarvo 2 300 kcal/pv", "ka 2 300 kcal/pv" at
small size), the big size's scale top and dashed average line. A tap anywhere but the period label
SHALL open Historia on Kulutus. Without the permissions it SHALL read "Kulutus: anna lupa" and a
tap SHALL ask for them. The card SHALL be hidden by default (see home-screen "Card grid"); its default size is small.

#### Scenario: Card added
- **WHEN** the user adds Kulutus from Lisää kortti and taps Valmis
- **THEN** the card shows today's burned kcal and the last 7 days' bars

### Requirement: Kulutus in the app log
The log SHALL record `burn.read ok days= split= totalOnly= activeOnly= imported= back= ms=` (day
counts, never kcal) or `burn.read failed cause= ms=`, with `source=` as steps do, and
`hc.permission.burn request|granted|denied|check`.

#### Scenario: Read logged
- **WHEN** Kulutus is read
- **THEN** one log line gives the counts of split, total-only and active-only days and no kcal
