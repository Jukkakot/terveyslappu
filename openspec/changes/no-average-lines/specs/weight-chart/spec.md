# Spec Delta

## MODIFIED Requirements

### Requirement: Energia chart
The Energia tab SHALL show, for the chosen period, one bar per logged day with its kcal and the
treats' share stacked as its own tone, at every period length. Days with nothing logged SHALL be
gaps, never zero bars. The chart SHALL draw real daily values only: no average line and no weekly
bars. The chart SHALL show energy only: no weights, no target line, budget or second y-axis.
Combining energy with other data is done in own charts.

#### Scenario: Week with a gap
- **GIVEN** 1 vk with five logged days and Wednesday and Thursday not logged
- **WHEN** the Energia tab is shown
- **THEN** five bars are drawn and Wednesday and Thursday are empty

#### Scenario: Energy only
- **GIVEN** weights in the period
- **WHEN** the Energia tab is shown
- **THEN** no weight line or kg axis is shown

#### Scenario: Treat share
- **GIVEN** a day of 2 000 kcal with 300 kcal of Herkku products
- **THEN** its bar shows a 300 kcal part in the treat tone on top of 1 700

#### Scenario: Year view
- **WHEN** the period is 1 v
- **THEN** one bar per logged day is drawn and no line

### Requirement: Askeleet chart
The Askeleet tab SHALL show, for the chosen period, one bar per day with steps in one tone, at
every period length. Days without steps data SHALL be gaps, never zero bars. The chart SHALL draw
real daily values only: no average line and no weekly bars. Kaikki SHALL start at the first day
with steps or today. The chart SHALL show steps only: no weights, goal line, target or second
y-axis. Tapping a bar SHALL show its day and steps.

#### Scenario: Week with a gap
- **GIVEN** 1 vk with five days with steps and Wednesday and Thursday without data
- **WHEN** the Askeleet tab is shown
- **THEN** five bars are drawn and Wednesday and Thursday are empty

#### Scenario: No average line
- **GIVEN** 1 vk where every finished day has about 9 000 steps and today 61
- **WHEN** the Askeleet tab is shown
- **THEN** seven bars are drawn, today's tiny, and no line crosses them

#### Scenario: Steps only
- **GIVEN** weights in the period
- **WHEN** the Askeleet tab is shown
- **THEN** no weight line or kg axis is shown

#### Scenario: Day details
- **WHEN** the user taps a bar
- **THEN** its date and step count are shown

### Requirement: Chart legends
Every bar and line in a Historia chart SHALL have a legend entry under its chart saying what it is,
with a swatch in its colour: Paino "paino", BMI "BMI", Energia "ruoka" and "herkut", Askeleet
"askeleet". An entry SHALL be shown only while its bar or line is drawn. The legend SHALL wrap to a
second line on a narrow phone rather than cut text.

#### Scenario: Askeleet legend
- **GIVEN** a month with steps and weights
- **WHEN** Askeleet is shown
- **THEN** the legend reads askeleet only, with its colour

#### Scenario: No weights
- **GIVEN** a period without weights
- **WHEN** Energia is shown
- **THEN** the legend has no "paino" entry

## REMOVED Requirements

### Requirement: Askeleet weekly bars
**Reason**: weekly bars are averages; the user wants real daily values in every chart.
**Migration**: Askeleet draws one bar per day at every period, as own charts do.
