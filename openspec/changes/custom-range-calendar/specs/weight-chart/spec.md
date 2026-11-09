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
have a small dot under its number; how many weights is not shown. Every day up to today SHALL be
selectable, with or without a dot; days after today SHALL NOT be. The first tap SHALL set the start,
the second the end (a day before the start becomes the new start instead), the days between SHALL be
highlighted, and a further tap SHALL start a new range. OK SHALL apply the range, or the single
start day when no end is set; Peruuta SHALL close without changes. The calendar SHALL open on the
month of the current Oma range's end, or on this month.

#### Scenario: Dots on days with weights
- **GIVEN** weights on 3.11. (two of them) and 5.11., none on 4.11.
- **WHEN** the user opens Oma in November
- **THEN** 3.11. and 5.11. have one dot each and 4.11. has none

#### Scenario: Empty day can be picked
- **WHEN** the user taps 4.11. (no dot) and then 9.11.
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
- **THEN** that month's days with weights have dots, as far as the app may read them
