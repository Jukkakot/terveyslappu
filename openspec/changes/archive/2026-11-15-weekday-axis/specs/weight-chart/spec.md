# Spec Delta

## MODIFIED Requirements

### Requirement: Periods
The chart SHALL offer the periods 1 vk, 1 kk, 3 kk, 1 v, Kaikki and Oma, where Oma opens a calendar
for picking a date range. 1 vk, 1 kk, 3 kk and 1 v SHALL be the last 7, 30, 90 and 365 days, today
included and last on the chart (rolling, not calendar weeks, months or years). The first time 1 vk SHALL be chosen; after that the app SHALL open with the
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

#### Scenario: Rolling week
- **GIVEN** today is Sunday 15.11.
- **WHEN** the user picks 1 vk
- **THEN** the chart shows Monday 9.11. to Sunday 15.11., today at the right end

### Requirement: Chart axes
The Paino, BMI, Energia and Askeleet charts and the own charts in Historia SHALL share one axis
rule. The y-axis SHALL
show exactly three labelled values with a faint guideline each: the bottom, the middle and the top.
For bars the bottom SHALL be 0 and the top a round value above the highest bar or line point (steps
in steps of 2 000, kcal in steps of 500), so the middle is a round value too. For a weight or BMI
line the bottom and top SHALL be the ends of the weight scale (which may start above zero) and the
middle their midpoint, shown with at most one decimal. The labels SHALL sit in a column left of the
plot. The x-axis SHALL show 3 to 5 dates evenly spread over the period, the first at the period's
start and the last at its end (Kaikki: the first day with data and today), formatted as day and
month, or month and year when the period spans 365 days or more (1 v included); a period of 7 days or
fewer (1 vk, or a short Oma range) SHALL instead label every day with its two-letter weekday
(Ma, Ti, Ke, To, Pe, La, Su) under that day's bar or point, with no dates and no special mark for
today. Front page cards keep their own date text. The number of dates (5, 4 or 3) SHALL be the largest that fits
without overlap at the measured label widths, and labels SHALL NOT overlap on a 360 dp wide phone
at font scale 1.3. Dates SHALL be spread evenly by days, also when the bars are weekly. Paino's
line chart SHALL use the same dates as the bar charts.

#### Scenario: Steps axis
- **GIVEN** the highest steps bar is 9 400
- **WHEN** Askeleet is shown
- **THEN** the y-axis reads `0 askelta`, `6 000 askelta` and `12 000 askelta`

#### Scenario: Weight line axis
- **GIVEN** the weight scale runs from 95 to 100 kg
- **WHEN** Paino is shown
- **THEN** the y-axis reads `95 kg`, `97,5 kg`, `100 kg`, with no zero

#### Scenario: Week
- **WHEN** the period is 1 vk ending Sunday 15.11.
- **THEN** the x-axis reads Ma Ti Ke To Pe La Su, one under each day, Su at the right end

#### Scenario: Week ending midweek
- **WHEN** the period is 1 vk ending Wednesday 18.11.
- **THEN** the x-axis reads To Pe La Su Ma Ti Ke

#### Scenario: Narrow and large text
- **GIVEN** a 360 dp wide phone and font scale 1.3
- **THEN** fewer dates are shown rather than overlapping labels, and no two label areas intersect

#### Scenario: Month
- **WHEN** the period is 1 kk ending 15.11. and five dates fit
- **THEN** five dates are shown, from 17.10. to 15.11., about a week apart

#### Scenario: Several years
- **GIVEN** weights from 24.9.2022 to today
- **WHEN** Paino is shown with Kaikki
- **THEN** the x-axis shows 3 to 5 labels like "9/2022", "11/2026", the last at today

#### Scenario: One year
- **WHEN** the period is 1 v
- **THEN** the date labels read month and year

#### Scenario: Same dates on both chart kinds
- **WHEN** the period is 3 kk on Paino and on Energia
- **THEN** both show the same label dates
