## MODIFIED Requirements

### Requirement: Recent weights from Health Connect
The app SHALL show on Historia the weights this app wrote in the period chosen for the chart, read
from Health Connect, newest first, each with its date, time and value; the date SHALL include the
year when the weight is not from the current year. Weights written by other apps SHALL NOT be listed
(user 2026-11-09: they are inaccurate hand entries). Each row SHALL also show the change from the
listed weight just before it in time, signed with one decimal (`+0,3`, `−0,5`, `±0,0`); the oldest
row of the period SHALL show no change. Gains and losses SHALL have different colours taken from the
phone's palette that are not red and green, and a zero change SHALL use the neutral text colour.

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

#### Scenario: Colours without a judgement
- **WHEN** the list shows a gain and a loss
- **THEN** they have two different colours, neither of them red or green, and both show their sign

#### Scenario: Refresh after saving
- **WHEN** a weight is saved
- **THEN** it appears at the top of the list and on the home screen without leaving the screen

#### Scenario: Nothing yet
- **WHEN** this app has no weights in the chosen period
- **THEN** the list area says that there are no weights in the period
