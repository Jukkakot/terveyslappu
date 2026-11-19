## MODIFIED Requirements

### Requirement: Period statistics
Under the chart the app SHALL show for the chosen period: the latest weight, the change from the
first to the latest weight in kg, that change per week, the average, the lowest and highest weight,
and the number of weights. The change per week ("Viikossa") SHALL use the same one decimal as the
change and carry the unit "kg/vk"; it SHALL be computed from the change as shown (see "Changes from
shown weights"). The change and the change per week SHALL each show a small arrow up for
a gain and down for a loss next to the number, and no arrow when the shown value is zero. For BMI the
statistics SHALL show the same values of the BMI except the change per week; the latest BMI SHALL also show its category word and tone.

#### Scenario: Losing
- **GIVEN** a 1 kk period whose first weight is 84.0 kg and latest 82.0 kg, 28 days apart
- **WHEN** the stats are shown
- **THEN** they show change −2,0 kg and Viikossa −0,5 kg/vk, both with an arrow down

#### Scenario: Same decimals
- **GIVEN** a 1 vk period whose change is −0,6 kg over 6 days
- **WHEN** the stats are shown
- **THEN** Muutos reads −0,6 kg and Viikossa −0,7 kg/vk, both with one decimal

#### Scenario: Slow change
- **GIVEN** a 1 v period whose change is −0,3 kg
- **WHEN** the stats are shown
- **THEN** Viikossa reads ±0,0 kg/vk without an arrow

#### Scenario: No change
- **GIVEN** a period whose first and latest weight are both 82.0 kg
- **WHEN** the stats are shown
- **THEN** the change shows ±0,0 kg without an arrow

#### Scenario: One weight only
- **WHEN** the period has a single weight
- **THEN** the stats show it as latest, average, lowest and highest and no change or rate

#### Scenario: BMI statistics
- **GIVEN** height 180 cm and a period whose first weight is 83.0 kg and latest 82.0 kg
- **WHEN** the BMI statistics are shown
- **THEN** they show latest 25,3 "ylipaino", change −0,3 with an arrow down, and no rate per week

### Requirement: Custom chart tabs
Historia's tab row SHALL show the fixed tabs Paino, BMI, Energia and Askeleet, then one tab per own
chart named after it (in the order they were made), then a last tab "Uusi kaavio". Historia SHALL
open on Paino. An own chart's tab SHALL show the chart for the chart's own period with a "Muokkaa"
button that opens the chart editor, and SHALL show no statistics and no list under it. On an own
chart's tab the period chips (1 vk, 1 kk, 3 kk, 1 v, Kaikki, Oma with its calendar) SHALL be the
chart's own: they SHALL show and change only that chart's period, and Historia's shared period
chips SHALL NOT affect the chart; on the other tabs the shared chips work as before and a chart's
period SHALL NOT change them. The "Uusi kaavio" tab SHALL show one line on what own charts are and a
button "Uusi kaavio" that opens the editor for a new chart. After a new chart is saved Historia
SHALL show its tab; after a chart is deleted Historia SHALL show the tab before it. While the
weights are loading or failed an own chart's tab SHALL show the same spinner or failure notice as
the other tabs. The "Uusi kaavio" tab itself SHALL show only a "+" icon, at least 48 dp wide, with
the accessible name "Uusi kaavio", so it is never shortened and reads as adding.

#### Scenario: No charts yet
- **GIVEN** the user has made no charts
- **WHEN** the user swipes past Askeleet
- **THEN** the "Uusi kaavio" tab shows its line and the Uusi kaavio button

#### Scenario: Plus tab
- **WHEN** Historia's tab row is scrolled to its end
- **THEN** the last tab shows a "+" icon and no text, TalkBack reads it as "Uusi kaavio", and
  tapping it shows the Uusi kaavio page

#### Scenario: Opens on weight
- **GIVEN** the user left Historia on an own chart's tab
- **WHEN** Historia is opened again
- **THEN** it shows Paino

#### Scenario: Long name
- **GIVEN** a chart named "Paino, ruoka ja askeleet"
- **THEN** its tab shows the name on one line, shortened with an ellipsis when it does not fit

#### Scenario: Own period
- **GIVEN** Historia's shared period is 1 kk and the chart "Liike" has 1 kk
- **WHEN** the user opens the Liike tab and taps 3 kk
- **THEN** the chart shows 90 days, Paino's period is still 1 kk, and the Liike card on the front page shows 3 kk too

#### Scenario: Shared chips do not reach a chart
- **GIVEN** the Liike tab with 1 kk
- **WHEN** the user switches to Paino and picks 1 v, then returns to the Liike tab
- **THEN** the Liike tab still shows 1 kk

## ADDED Requirements

### Requirement: Changes from shown weights
Every change between two weights or BMI values that the app shows next to those values SHALL be the
difference of the values as shown (each rounded to its one decimal first): the front page weight
card's period change, Historia's Muutos and Viikossa, the change on each weight row and on each
group row. A shown change SHALL never disagree with the two numbers it is computed from.

#### Scenario: Card range and change agree
- **GIVEN** a week whose first weight is 82.34 kg and latest 81.76 kg
- **WHEN** the front page weight card is shown
- **THEN** it reads "82,3 → 81,8 kg" and −0,5 kg, never −0,6 kg

#### Scenario: Rows agree
- **GIVEN** weights 82.04 kg yesterday and 81.76 kg today
- **WHEN** Historia's list is shown
- **THEN** today's row reads 81,8 kg with −0,2 kg
