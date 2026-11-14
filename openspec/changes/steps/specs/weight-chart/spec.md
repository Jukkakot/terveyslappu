## MODIFIED Requirements

### Requirement: One quantity at a time
Historia's chart and period statistics SHALL show one quantity at a time: Paino, BMI, Energia or Askeleet.
Tabs with those names SHALL sit above the chart, Askeleet last; tapping a tab or swiping the chart and statistics
left or right SHALL switch the quantity, and the chosen period SHALL stay. All four tabs SHALL
always be shown; a quantity that cannot be computed SHALL show, instead of its chart and
statistics, the reason naming what is missing and a button to fix it (Omat tiedot for BMI, the
nutrition permissions for Energia, the steps permission for Askeleet). Historia SHALL open on Paino. On Paino and BMI the list under
the statistics SHALL be the weight list; on Energia it SHALL be the list of logged days (see
Energia list); on Askeleet it SHALL be the list of days with steps (see Askeleet list).

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

#### Scenario: Swipe to Askeleet
- **GIVEN** Historia shows Energia for 1 kk
- **WHEN** the user swipes the chart to the left
- **THEN** the Askeleet tab is chosen for the same 30 days

#### Scenario: No steps permission
- **GIVEN** the steps permission is not granted
- **WHEN** the user opens the Askeleet tab
- **THEN** it shows the reason and a button that asks for the permission

#### Scenario: Opens on weight
- **GIVEN** the user left Historia on Energia
- **WHEN** Historia is opened again
- **THEN** it shows Paino

#### Scenario: Tap a tab
- **WHEN** the user taps BMI
- **THEN** the chart and statistics show the BMI of the chosen period

## ADDED Requirements

### Requirement: Askeleet chart
The Askeleet tab SHALL show, for the chosen period, one bar per day with steps, in one tone, and a
line of the 7-day average over the days with steps of the 7 days ending on each day (drawn only
where those 7 days hold at least 3 days with steps). Days without steps data SHALL be gaps, never
zero bars. For 1 v, Kaikki and an Oma range over 90 days the bars SHALL be one per week (average
per day with steps of the week). Under it, on the same time axis, a small chart SHALL show the
period's weights with the weight scale, as on Energia. There SHALL be no goal line, target or second
y-axis on the same plot. Tapping a bar SHALL show its day and steps, as the point details of the
other charts.

#### Scenario: Week with a gap
- **GIVEN** 1 vk with five days with steps and Wednesday and Thursday without data
- **WHEN** the Askeleet tab is shown
- **THEN** five bars are drawn, Wednesday and Thursday are empty, and the weight line runs under them on the same days

#### Scenario: Year view
- **WHEN** the period is 1 v
- **THEN** one bar per week is drawn

#### Scenario: Day details
- **WHEN** the user taps a bar
- **THEN** its date and step count are shown

### Requirement: Askeleet statistics
Under the Askeleet chart the app SHALL show for the period: the average steps per day with steps
(today left out, since it is not over), and the number of days with steps of the period's days
("26 / 30 päivää"). It SHALL NOT show a goal, a difference to a goal, distance or calories.

#### Scenario: Partial month
- **GIVEN** 1 kk with 26 days with steps averaging 7 800
- **THEN** the statistics show 7 800 askelta/pv and 26 / 30 päivää

### Requirement: Askeleet list
On the Askeleet tab the list under the statistics SHALL show the period's days with steps, newest
first, each with its date and steps. A row SHALL NOT open anything.

#### Scenario: Newest first
- **GIVEN** steps for 12.11., 13.11. and 14.11.
- **THEN** the list reads 14.11., 13.11., 12.11. with their steps
