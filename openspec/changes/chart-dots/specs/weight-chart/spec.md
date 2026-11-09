## ADDED Requirements

### Requirement: Dots when they fit
Every weight chart (Historia and the home card's line) SHALL draw each weight as a dot on the line
when the chart has at least 10 dp of width per weight, and only the line when it is denser. A
weight with a gap over 30 days on both sides SHALL keep its dot in Historia, and the latest weight
SHALL keep its larger dot on the home line, however dense the chart is.

#### Scenario: A week
- **GIVEN** seven weights in the period on a phone-wide chart
- **WHEN** the chart is shown
- **THEN** each weight is a dot on the line

#### Scenario: Three months of daily weighing
- **GIVEN** 90 weights in the period on a phone-wide chart
- **WHEN** the chart is shown
- **THEN** only the line is drawn (plus the latest's dot on the home line)
