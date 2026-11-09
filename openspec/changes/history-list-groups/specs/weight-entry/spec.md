## ADDED Requirements

### Requirement: Grouped list
Historia's list SHALL group the period's weights by year, month, week and day, newest first on
every level, with the single weights under their day. Weeks run Monday to Sunday and SHALL be cut
at month borders. A group that would hold only one child SHALL NOT be shown as its own level: its
child takes its place, and a day with one weight is a plain weight row.

#### Scenario: A week, one weight a day
- **GIVEN** the period 1 vk holds one weight on each of seven days
- **WHEN** Historia is shown
- **THEN** the list shows seven weight rows and no group rows

#### Scenario: Two weights in a day
- **GIVEN** a day holds weights at 7.05 and 21.30 among other days' single weights
- **WHEN** Historia is shown
- **THEN** that day is a group row "Ti 4.11." with two weights under it

#### Scenario: Several years
- **GIVEN** the period Kaikki holds weights from 2022 to 2026
- **WHEN** Historia is shown
- **THEN** the top level shows year rows 2026, 2025, 2024, 2023, 2022

#### Scenario: Week across a month border
- **GIVEN** weights on Thu 30.10. and Mon 3.11. and Tue 4.11.
- **THEN** 30.10. sits under Lokakuu and 3.–4.11. under Marraskuu, each in its own part of the week

### Requirement: Group rows
A group row SHALL show the group's name (a year "2024", a month with year "Marraskuu 2025", a week
with its days "Vk 45 · 3.–9.11.", a day "Ti 4.11."; the year shown when not the current year), the
first and the last weight of the group in time ("92,3 → 91,0 kg"), their difference with the list's
arrow and neutral colours, the number of weights, and a sign that it opens. Tapping it SHALL open or
close it.

#### Scenario: Month row
- **GIVEN** November 2025 holds 20 weights, the first 92,3 kg and the last 91,0 kg
- **THEN** its row reads "Marraskuu 2025", "92,3 → 91,0 kg", −1,3 with an arrow down and 20

#### Scenario: Open and close
- **WHEN** the user taps a closed month row
- **THEN** its weeks (or days or weights) appear under it; tapping again hides them

### Requirement: Open by default
When Historia is shown, the newest group on each level SHALL be open, so the newest weights show as
rows, and every other group SHALL be closed. Open and closed groups SHALL NOT be remembered after
leaving Historia; changing the period SHALL reset them.

#### Scenario: Kaikki opened
- **GIVEN** the period Kaikki with weights from 2022 to today
- **WHEN** Historia is shown
- **THEN** this year, its newest month and that month's newest week are open, showing the newest
  weights; 2025 and older years are closed

### Requirement: Gaps in the list
Where more than 30 days pass with no weight between two neighbours on the same level, the list SHALL
show between them a thin row "Ei punnituksia <first day>–<last day>" naming the days without
weights. Empty years, months, weeks or days SHALL NOT be shown.

#### Scenario: Long pause
- **GIVEN** weights until 20.5.2024 and the next on 18.3.2025
- **WHEN** Kaikki is shown
- **THEN** between the 2025 and 2024 rows a thin row reads "Ei punnituksia 21.5.2024–17.3.2025"

#### Scenario: Short pause
- **GIVEN** no weights for 10 days inside a month
- **THEN** no gap row is shown and the month has no empty weeks

## MODIFIED Requirements

### Requirement: Recent weights from Health Connect
The app SHALL show on Historia the weights this app wrote in the period chosen for the chart, read
from Health Connect, newest first, each with its date, time and value; the date SHALL include the
year when the weight is not from the current year. Weights written by other apps SHALL NOT be listed
(user 2026-11-09: they are inaccurate hand entries). Each weight row SHALL also show the change from
the listed weight just before it in time, signed with one decimal (`+0,3`, `−0,5`, `±0,0`); the
oldest row of the period and a weight more than 30 days after the one before it SHALL show no
change. The change SHALL have a small arrow up for a gain and down for a loss, none when it is
zero. Gains and losses SHALL have different colours taken from the phone's palette that are not
red and green, and a zero change SHALL use the neutral text colour.

#### Scenario: Weights from several apps
- **GIVEN** Health Connect holds weights written by this app and by another app
- **WHEN** Historia is shown
- **THEN** only this app's weights appear in the list

#### Scenario: List follows the period
- **WHEN** the user chooses the period 1 kk
- **THEN** the list shows this app's weights of the last 30 days

#### Scenario: Old weight
- **WHEN** the list shows a weight from 15.9.2024 at 8.33
- **THEN** its row reads "Su 15.9.2024 8.33"

#### Scenario: Change from the previous weight
- **GIVEN** the period holds 82,0 kg, then 82,4 kg, then 81,9 kg
- **WHEN** Historia is shown
- **THEN** the rows read 81,9 kg with −0,5, 82,4 kg with +0,4 and 82,0 kg with no change

#### Scenario: No change across a gap
- **GIVEN** 92,0 kg on 20.5.2024 and 97,5 kg on 18.3.2025
- **WHEN** the 18.3.2025 weight is shown
- **THEN** its row shows no change

#### Scenario: Colours without a judgement
- **WHEN** the list shows a gain and a loss
- **THEN** they have two different colours, neither of them red or green, and both show their sign

#### Scenario: Refresh after saving
- **WHEN** a weight is saved
- **THEN** it appears at the top of the list and on the home screen without leaving the screen

#### Scenario: Nothing yet
- **WHEN** this app has no weights in the chosen period
- **THEN** the list area says that there are no weights in the period
