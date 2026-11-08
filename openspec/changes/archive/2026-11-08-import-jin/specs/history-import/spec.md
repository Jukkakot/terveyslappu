# Spec Delta

## MODIFIED Requirements

### Requirement: Read the table
The app SHALL read text tables separated by comma, semicolon or tab, with or without a header row,
and SHALL find the date column, an optional separate time column and the weight column from the
header names (Finnish, English, Chinese) or, without a header, from the values.

#### Scenario: Header with date, time and weight
- **WHEN** the file starts with `Date;Time;Weight(kg);BMI` and rows like `2024-03-01;07:12;82,4;24,1`
- **THEN** each row is read as 82.4 kg at 1 March 2024 07:12

#### Scenario: No header
- **WHEN** the rows are like `01.03.2024 07:12,82.4`
- **THEN** the date-time and weight columns are found from the values

#### Scenario: Pounds
- **WHEN** the weight column header says lb
- **THEN** the weights are converted to kilograms

#### Scenario: Jin
- **WHEN** the weight column header says jin or 斤 (and not 公斤)
- **THEN** the weights are halved to kilograms
