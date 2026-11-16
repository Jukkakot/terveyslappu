## MODIFIED Requirements

### Requirement: Recent weights from Health Connect
The app SHALL show on Historia the weights this app wrote in the period chosen for the chart, read
from Health Connect, newest first, each with its date, time and value; the date SHALL include the
year when the weight is not from the current year. Weights written by other apps SHALL NOT be listed
(user 2026-11-09: they are inaccurate hand entries). Each weight row SHALL also show the change from
the listed weight just before it in time, signed with one decimal and `kg` (`+0,3 kg`, `−0,5 kg`, `±0,0 kg`); the
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
- **THEN** the rows read 81,9 kg with −0,5 kg, 82,4 kg with +0,4 kg and 82,0 kg with no change

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

### Requirement: Group rows
A group row SHALL show the group's name (a year "2024", a month with year "Marraskuu 2025", a week
with its days "Vk 45 · 3.–9.11."; the year shown when not the current year), the first and the last
weight of the group in time ("92,3 → 91,0 kg"), their difference with the list's arrow and neutral
colours, the number of weights, and a sign that it opens. A group with a single weight SHALL show
that weight and no difference. Tapping it SHALL open or close it.

#### Scenario: Month row
- **GIVEN** November 2025 holds 20 weights, the first 92,3 kg and the last 91,0 kg
- **THEN** its row reads "Marraskuu 2025", "92,3 → 91,0 kg", −1,3 kg with an arrow down and 20

#### Scenario: Open and close
- **WHEN** the user taps a closed month row
- **THEN** its weeks appear under it; tapping again hides them
