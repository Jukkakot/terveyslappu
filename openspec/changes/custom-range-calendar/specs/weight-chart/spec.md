## MODIFIED Requirements

### Requirement: Periods
The chart SHALL offer the periods 1 vk, 1 kk, 3 kk, 1 v, Kaikki and Oma, where Oma opens a calendar
for picking a date range. The first time 1 vk SHALL be chosen; after that the app SHALL open with the
last chosen period.

#### Scenario: Change period
- **WHEN** the user taps 1 kk
- **THEN** the chart, stats and list show the last 30 days

#### Scenario: Own range
- **WHEN** the user taps Oma and picks 1.9.–15.10.
- **THEN** the chart, stats and list show that range and the chip shows the dates

#### Scenario: Remembered
- **GIVEN** the user chose 3 kk
- **WHEN** the app is opened again
- **THEN** 3 kk is chosen

#### Scenario: Long period without history access
- **GIVEN** the history permission is not granted
- **WHEN** the user picks a period reaching further back than 30 days
- **THEN** the chart shows what it can read and offers to grant access to older weights

## ADDED Requirements

### Requirement: Days with weights in the calendar
The Oma calendar SHALL show one month at a time with weeks starting on Monday, the month and year on
top, and change month by swiping or by arrows. Every day with at least one weight of this app SHALL
show that day's latest weight under its number with one decimal and no unit ("82,4"); a day without
weights shows only its number. Every day up to today SHALL be selectable, with or without a weight;
days after today SHALL NOT be. The first tap SHALL set the start,
the second the end (a day before the start becomes the new start instead), the days between SHALL be
highlighted, and a further tap SHALL start a new range. OK SHALL apply the range, or the single
start day when no end is set; Peruuta SHALL close without changes. The calendar SHALL open on the
month of the current Oma range's end, or on this month.

#### Scenario: Weights on the days
- **GIVEN** weights 82,9 kg at 7.05 and 82,4 kg at 21.30 on 3.11., 82,1 kg on 5.11., none on 4.11.
- **WHEN** the user opens Oma in November
- **THEN** 3.11. shows 82,4, 5.11. shows 82,1 and 4.11. shows only its number

#### Scenario: Empty day can be picked
- **WHEN** the user taps 4.11. (no weight) and then 9.11.
- **THEN** 4.–9.11. is highlighted and OK shows that range

#### Scenario: End before start
- **GIVEN** the start is 9.11.
- **WHEN** the user taps 2.11.
- **THEN** 2.11. becomes the start and no end is set

#### Scenario: Single day
- **WHEN** the user taps 5.11. and then OK
- **THEN** the chart shows 5.11. only

#### Scenario: Older month
- **WHEN** the user swipes to October 2024
- **THEN** that month's days with weights show them, as far as the app may read them

### Requirement: Calendar explains missing old weights
When the app may not read weights older than 30 days (Health Connect's history permission not
granted), the Oma calendar SHALL say so under the month ("Yli 30 päivää vanhat painot näkyvät vasta
luvalla") with a button that asks for the permission, so empty older days are not mistaken for days
without weighing. With the permission granted, nothing about it SHALL be shown.

#### Scenario: Without history access
- **GIVEN** the history permission is not granted
- **WHEN** the user opens Oma
- **THEN** the calendar shows the text and an Anna lupa button that asks Health Connect for access

#### Scenario: With history access
- **GIVEN** the history permission is granted
- **WHEN** the user opens Oma
- **THEN** no text about access is shown
