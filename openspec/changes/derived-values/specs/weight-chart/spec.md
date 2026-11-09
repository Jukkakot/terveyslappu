## ADDED Requirements

### Requirement: One quantity at a time
Historia's chart and period statistics SHALL show one quantity at a time: Paino, BMI or
Perusaineenvaihdunta. Tabs with those names SHALL sit above the chart; tapping a tab or swiping the
chart and statistics left or right SHALL switch the quantity, and the chosen period SHALL stay. A
quantity whose value cannot be computed from the owner details SHALL have no tab; with only Paino
left no tabs SHALL be shown. Historia SHALL open on Paino. The weight list under the statistics
SHALL stay the same whatever quantity is shown.

#### Scenario: Swipe to BMI
- **GIVEN** a height is set and Historia shows Paino for 1 kk
- **WHEN** the user swipes the chart to the left
- **THEN** the BMI tab is chosen and the chart and statistics show the BMI of the same 30 days

#### Scenario: Tap a tab
- **WHEN** the user taps Perusaineenvaihdunta
- **THEN** the chart and statistics show the basal metabolic rate of the chosen period

#### Scenario: No owner details
- **GIVEN** no height is set
- **WHEN** Historia is shown
- **THEN** no tabs are shown and the chart shows Paino

#### Scenario: Opens on weight
- **GIVEN** the user left Historia on BMI
- **WHEN** Historia is opened again
- **THEN** it shows Paino

### Requirement: BMI bands
The BMI chart SHALL show, behind the line, faint background bands in the category tones (green,
yellow, red) for the BMI ranges that fall inside its y-scale.

#### Scenario: Around the border
- **GIVEN** the BMI values in view run from 24,6 to 25,4
- **WHEN** the BMI chart is shown
- **THEN** the area under 25 has a faint green band and the area from 25 a faint yellow band

## MODIFIED Requirements

### Requirement: Period statistics
Under the chart the app SHALL show for the chosen period: the latest weight, the change from the
first to the latest weight in kg, that change per week, the average, the lowest and highest weight,
and the number of weights. The change and the change per week SHALL each show a small arrow up for
a gain and down for a loss next to the number, and no arrow when the shown value is zero. For BMI
and Perusaineenvaihdunta the statistics SHALL show the same values of that quantity except the
change per week; the latest BMI SHALL also show its category word and tone.

#### Scenario: Losing
- **GIVEN** a 1 kk period whose first weight is 83.0 kg and latest 82.0 kg, 28 days apart
- **WHEN** the stats are shown
- **THEN** they show latest 82.0, change −1.0 kg and −0.25 kg/vko, both with an arrow down

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

### Requirement: Point details
Tapping a dot SHALL show that weight's date, time and value of the shown quantity; the date SHALL
include the year when the weight is not from the current year.

#### Scenario: Tap a dot
- **WHEN** the user taps the dot of 82.6 kg on 8.11. this year at 7.05
- **THEN** the chart shows "8.11. 7.05 82,6 kg"

#### Scenario: Tap an old dot
- **WHEN** the user taps the dot of 97.6 kg on 15.9.2024 at 8.33
- **THEN** the chart shows "15.9.2024 8.33 97,6 kg"

#### Scenario: Tap a BMI dot
- **GIVEN** the BMI chart is shown and the height is 180 cm
- **WHEN** the user taps the dot of 82.0 kg on 8.11. this year at 7.05
- **THEN** the chart shows "8.11. 7.05 BMI 25,3"

### Requirement: Weight scale
Every weight chart (Historia and the home card) SHALL use one y-scale rule: the lowest and highest
shown weight with 0.5 kg room on each side, widened evenly to at least 4 kg when narrower, and
rounded outwards to whole kilos. The scale SHALL grow when the weights spread more and SHALL never be
narrower than 4 kg, so a small change looks small in every period and on both screens. The BMI and
basal metabolic rate charts SHALL use the same rule with the room and the least span as many units
as 0.5 kg and 4 kg give at the owner's height (BMI) or by the equation (10 kcal per kg), rounded
outwards to whole BMI units and to tens of kcal.

#### Scenario: Steady weight
- **GIVEN** the weights in view are 97.76 and 97.84 kg
- **THEN** the y-scale runs from 95 to 100 kg and the line looks flat

#### Scenario: Real change
- **GIVEN** the weights in view run from 92.3 to 98.6 kg
- **THEN** the y-scale runs from 91 to 100 kg

#### Scenario: Steady BMI
- **GIVEN** height 180 cm and BMI values in view of 25,2 and 25,3
- **THEN** the BMI y-scale spans at least 1,2 units and the line looks flat

#### Scenario: Steady basal metabolic rate
- **GIVEN** basal metabolic rates in view of 1 770 and 1 780 kcal
- **THEN** the y-scale spans at least 40 kcal and the line looks flat

### Requirement: Units on the chart
The Historia chart's y-axis labels SHALL show the unit (`98 kg`), and every weight and change in
the period statistics SHALL show `kg`. Basal metabolic rate labels and statistics SHALL show
`kcal`; BMI has no unit and its axis labels SHALL be bare numbers under a "BMI" chart title.

#### Scenario: Axis labels
- **WHEN** Historia shows a chart
- **THEN** its y-axis labels read like `96 kg`, `98 kg`

#### Scenario: Statistics
- **WHEN** the period statistics are shown
- **THEN** the latest, average, lowest and highest weight read like `82,0 kg`

#### Scenario: Basal metabolic rate axis
- **WHEN** Historia shows Perusaineenvaihdunta
- **THEN** its y-axis labels read like `1 760 kcal`, `1 800 kcal`
