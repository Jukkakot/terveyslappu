## MODIFIED Requirements

### Requirement: Weight scale
Every weight chart (Historia, the home card, own charts) SHALL use one y-scale rule fitted to the
weights in view: the lowest and highest shown weight with 0.2 kg room on each side, widened evenly
to at least 1 kg when narrower, rounded outwards to half kilos, and then, when the middle value
would not fall on a half kilo, widened by 0.5 kg at the top, so the three axis values (bottom,
middle, top) all read in half kilos. The scale SHALL grow when the weights spread more. The BMI
chart SHALL use the same rule with the room and the least span as many BMI units as 0.2 kg and
1 kg give at the owner's height, rounded outwards to half BMI units in the same way.

#### Scenario: Steady weight
- **GIVEN** the weights in view are 97.76 and 97.84 kg
- **THEN** the y-scale runs from 97.5 to 98.5 kg (axis values 97,5, 98 and 98,5 kg; 98.04 with room rounds up to 98.5)

#### Scenario: A week of small moves
- **GIVEN** the weights in view run from 97.0 to 98.0 kg
- **THEN** the y-scale runs from 96.5 to 98.5 kg (axis values 96,5, 97,5 and 98,5 kg) and the
  1 kg move fills half the chart's height, on the home card as on Historia

#### Scenario: Real change
- **GIVEN** the weights in view run from 92.3 to 98.6 kg
- **THEN** the y-scale runs from 92.0 to 99.0 kg

#### Scenario: Steady BMI
- **GIVEN** height 180 cm and BMI values in view of 25,2 and 25,3
- **THEN** the BMI y-scale spans at least 0,5 units and its axis values read in half units

#### Scenario: Steady basal metabolic rate
- **WHEN** Historia is shown
- **THEN** there is no basal metabolic rate chart, only Paino and BMI
