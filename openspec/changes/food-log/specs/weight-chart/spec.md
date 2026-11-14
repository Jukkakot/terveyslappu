## MODIFIED Requirements

### Requirement: One quantity at a time
Historia's chart and period statistics SHALL show one quantity at a time: Paino, BMI or Energia.
Tabs with those names SHALL sit above the chart; tapping a tab or swiping the chart and statistics
left or right SHALL switch the quantity, and the chosen period SHALL stay. All three tabs SHALL
always be shown; a quantity that cannot be computed SHALL show, instead of its chart and
statistics, the reason naming what is missing and a button to fix it (Omat tiedot for BMI, the
nutrition permissions for Energia). Historia SHALL open on Paino. On Paino and BMI the list under
the statistics SHALL be the weight list; on Energia it SHALL be the list of logged days (see
Energia list).

#### Scenario: Swipe to BMI
- **GIVEN** a height is set and Historia shows Paino for 1 kk
- **WHEN** the user swipes the chart to the left
- **THEN** the BMI tab is chosen and the chart and statistics show the BMI of the same 30 days

#### Scenario: Swipe to Energia
- **GIVEN** Historia shows BMI for 1 vk
- **WHEN** the user swipes the chart to the left
- **THEN** the Energia tab is chosen for the same 7 days

#### Scenario: No owner details
- **GIVEN** no height is set
- **WHEN** the user opens the BMI tab
- **THEN** it shows "BMI vaatii pituuden" and a button that opens Asetukset at Omat tiedot

#### Scenario: Opens on weight
- **GIVEN** the user left Historia on Energia
- **WHEN** Historia is opened again
- **THEN** it shows Paino

#### Scenario: Tap a tab
- **WHEN** the user taps BMI
- **THEN** the chart and statistics show the BMI of the chosen period

## ADDED Requirements

### Requirement: Energia chart
The Energia tab SHALL show, for the chosen period, one bar per logged day with its kcal, the
treats' share stacked as its own tone, and a line of the 7-day average over the logged days of the
7 days ending on each day (drawn only where those 7 days hold at least 3 logged days). Days with
nothing logged SHALL be gaps, never zero bars. For 1 v, Kaikki and an Oma range over 90 days the
bars SHALL be one per week (average per logged day of the week). Under it, on the same time axis,
a small chart SHALL show the period's weights with the weight scale. There SHALL be no target
line, budget or second y-axis on the same plot.

#### Scenario: Week with a gap
- **GIVEN** 1 vk with five logged days and Wednesday and Thursday not logged
- **WHEN** the Energia tab is shown
- **THEN** five bars are drawn, Wednesday and Thursday are empty, and the weight line runs under
  them on the same days

#### Scenario: Treat share
- **GIVEN** a day of 2 000 kcal with 300 kcal marked herkku
- **THEN** its bar shows a 300 kcal part in the treat tone on top of 1 700

#### Scenario: Year view
- **WHEN** the period is 1 v
- **THEN** one bar per week is drawn

### Requirement: Energia statistics
Under the Energia chart the app SHALL show for the period: the average kcal per logged day, the
average treat kcal per logged day, and the number of logged days of the period's days ("12 / 30
päivää"). It SHALL NOT show a target, a difference to a target or any energy balance.

#### Scenario: Partial month
- **GIVEN** 1 kk with 12 logged days averaging 1 900 kcal, treats 210 kcal per day
- **THEN** the statistics show 1 900 kcal/pv, herkut 210 kcal/pv and 12 / 30 päivää

### Requirement: Energia list
On the Energia tab the list under the statistics SHALL show the period's logged days, newest first,
each with its date, total kcal and treat share; tapping a day SHALL open the Ruoka screen on that
day.

#### Scenario: Open a day
- **WHEN** the user taps 12.11. in the Energia list
- **THEN** the Ruoka screen opens on 12.11.; back returns to Historia on Energia
