# Spec Delta

## ADDED Requirements

### Requirement: Period range line
Every chart in Historia (Paino, BMI, Energia, Askeleet and every own chart tab) SHALL show, at the
top of its chart card above the plot, one line with the first and last day the chart spans, with
the year on both ends: "15.8.2026 – 15.11.2026" (day.month.year, no leading zeros, an en dash with
spaces). The days SHALL be the chart's own x-axis ends: for 1 vk, 1 kk, 3 kk and 1 v the period's
first day and today; for Kaikki the first day with data and today; for Oma the picked range. A
range of one day SHALL show that one date only. The line SHALL use the small secondary text style
of the chart's labels and SHALL change at once with the period. Front page cards keep their own
date text.

#### Scenario: Kaikki
- **GIVEN** steps from 15.8.2026 and today 15.11.2026
- **WHEN** Askeleet is shown with Kaikki
- **THEN** the chart card's top line reads "15.8.2026 – 15.11.2026"

#### Scenario: Week
- **GIVEN** today is 15.11.2026
- **WHEN** Paino is shown with 1 vk
- **THEN** the top line reads "9.11.2026 – 15.11.2026"

#### Scenario: Across a year
- **WHEN** Energia is shown with 1 v ending 15.11.2026
- **THEN** the top line reads "16.11.2025 – 15.11.2026"

#### Scenario: One day
- **WHEN** an Oma range of 3.11.2026 only is picked
- **THEN** the top line reads "3.11.2026"
