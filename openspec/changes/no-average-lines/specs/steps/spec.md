# Spec Delta

## MODIFIED Requirements

### Requirement: Days and gaps
A day SHALL be the local day as Health Connect's daily aggregate reports it. A day for which Health
Connect holds no steps SHALL be a gap, never a zero bar, and SHALL be left out of averages and day
counts. Today SHALL be shown but left out of the averages (it is not over); it SHALL count as a day with steps in "x / y päivää" when it has steps.

#### Scenario: Day without data
- **GIVEN** Health Connect holds no steps for Tuesday
- **THEN** Tuesday is a gap in the bars and is not listed

#### Scenario: Today
- **GIVEN** today holds 3 000 steps and the earlier days average 8 000
- **THEN** the average stays 8 000 and today counts as a day with steps in the day count
