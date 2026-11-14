## ADDED Requirements

### Requirement: Questioned weights left out
A weight marked "?" SHALL NOT be drawn on the chart, SHALL NOT count in the period statistics and
SHALL NOT affect the weight scale or the calendar's days with weights, until it is confirmed.

#### Scenario: Questioned weight
- **GIVEN** the period holds 82,0 kg and 82,4 kg and a 61,3 kg marked "?"
- **WHEN** the chart is shown
- **THEN** two dots are drawn, the scale fits 82,0-82,4 kg and the lowest weight is 82,0 kg

#### Scenario: Confirmed
- **WHEN** the user confirms 61,3 kg
- **THEN** it is drawn and counted like any weight
