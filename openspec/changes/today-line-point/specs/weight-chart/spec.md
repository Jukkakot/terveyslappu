## ADDED Requirements

### Requirement: Today on running lines
In own charts, on Historia tabs and front page cards in both layouts, a Viiva line of Syöty,
Kulutus or Askeleet SHALL join only the days before today. When today is in the period and has a
value, it SHALL be drawn as a lone dot in the line's tone, at today's value, not joined to the
line. Paino lines SHALL join today as before. Bars, average lines, the legend and the crosshair
readout SHALL be unchanged; tapping today still shows today's running value.

#### Scenario: Partial day
- **GIVEN** an own chart with Kulutus as Viiva, 1 vk, about 2 800 kcal on each earlier day and
  500 kcal so far today
- **THEN** the line runs through the earlier days and stops at yesterday, and today is one dot at
  500 kcal with no segment down to it

#### Scenario: Only today
- **GIVEN** an own chart with Syöty as Viiva and food logged only today
- **THEN** today is drawn as one dot and there is no line

#### Scenario: Weight joins today
- **GIVEN** an own chart with Paino as Viiva and a weight today
- **THEN** the weight line joins today's weight as before
