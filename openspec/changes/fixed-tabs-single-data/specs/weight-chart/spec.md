## MODIFIED Requirements

### Requirement: Energia chart
The Energia tab SHALL show, for the chosen period, one bar per logged day with its kcal, the
treats' share stacked as its own tone, and a line of the 7-day average over the logged days of the
7 days ending on each day (drawn only where those 7 days hold at least 3 logged days). Days with
nothing logged SHALL be gaps, never zero bars. For 1 v, Kaikki and an Oma range over 90 days the
bars SHALL be one per week (average per logged day of the week). The chart SHALL show energy only:
no weights, no target line, budget or second y-axis. Combining energy with other data is done in
own charts.

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
- **THEN** one bar per week is drawn

### Requirement: Askeleet chart
The Askeleet tab SHALL show, for the chosen period, one bar per day with steps in one tone, and a
line of the 7-day average over the days with steps of the 7 days ending on each day (drawn only
where those 7 days hold at least 3 days with steps; today left out). Days without steps data SHALL
be gaps, never zero bars. Kaikki SHALL start at the first day with steps or today. The chart SHALL
show steps only: no weights, goal line, target or second y-axis. Tapping a bar SHALL show its day
and steps.

#### Scenario: Week with a gap
- **GIVEN** 1 vk with five days with steps and Wednesday and Thursday without data
- **WHEN** the Askeleet tab is shown
- **THEN** five bars are drawn and Wednesday and Thursday are empty

#### Scenario: Steps only
- **GIVEN** weights in the period
- **WHEN** the Askeleet tab is shown
- **THEN** no weight line or kg axis is shown

#### Scenario: Day details
- **WHEN** the user taps a bar
- **THEN** its date and step count are shown

### Requirement: Units on the chart
Every Historia chart's y-axis labels SHALL show the unit: `98 kg` on Paino, `2 000 kcal` on
Energia, `8 000 askelta` on Askeleet; every weight and change in the period statistics SHALL show
`kg`. BMI has no unit and its axis labels SHALL be bare numbers under a "BMI" chart title. Numbers
SHALL use Finnish formatting (thin space for thousands, decimal comma).

#### Scenario: Axis labels
- **WHEN** Historia shows the Paino chart
- **THEN** its y-axis labels read like `95 kg`, `97,5 kg`, `100 kg`

#### Scenario: Energia axis
- **WHEN** Historia shows Energia with a top of 2 500 kcal
- **THEN** its y-axis labels read `0 kcal`, `1 250 kcal`, `2 500 kcal`

#### Scenario: Statistics
- **WHEN** the period statistics are shown
- **THEN** the latest, average, lowest and highest weight read like `82,0 kg`

#### Scenario: Basal metabolic rate axis
- **WHEN** Historia shows Paino or BMI
- **THEN** no axis label or statistic reads `kcal`

### Requirement: Chart axes
The Paino, BMI, Energia and Askeleet charts in Historia SHALL share one axis rule. The y-axis SHALL
show exactly three labelled values with a faint guideline each: the bottom, the middle and the top.
For bars the bottom SHALL be 0 and the top a round value above the highest bar or line point (steps
in steps of 2 000, kcal in steps of 500), so the middle is a round value too. For a weight or BMI
line the bottom and top SHALL be the ends of the weight scale (which may start above zero) and the
middle their midpoint, shown with at most one decimal. The labels SHALL sit in a column left of the
plot. The x-axis SHALL show 3 to 5 dates evenly spread over the period, the first at the period's
start and the last at its end (Kaikki: the first day with data and today), formatted as day and
month, or month and year when the period spans 365 days or more (1 v included); a period of 1 or 2
days SHALL show its 1 or 2 days. The number of dates (5, 4 or 3) SHALL be the largest that fits
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
- **WHEN** the period is 1 vk ending 15.11. and four dates fit
- **THEN** the x-axis reads 9.11., 11.11., 13.11. and 15.11.

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

### Requirement: Chart legends
Every bar and line in a Historia chart SHALL have a legend entry under its chart saying what it is,
with a swatch in its colour: Paino "paino", BMI "BMI", Energia "ruoka", "herkut" and "7 pv
keskiarvo", Askeleet "askeleet" and "7 pv keskiarvo". An entry SHALL be shown only while its bar or
line is drawn (no average line: no average entry). When the bars are weekly (periods over 90
days), the legend row SHALL also say "viikoittain". The legend SHALL wrap to a second line on a
narrow phone rather than cut text.

#### Scenario: Askeleet legend
- **GIVEN** a month with steps, an average line and weights
- **WHEN** Askeleet is shown
- **THEN** the legend reads askeleet and 7 pv keskiarvo, each with its colour, and no "paino"

#### Scenario: No weights
- **GIVEN** a period without weights
- **WHEN** Energia is shown
- **THEN** the legend has no "paino" entry

## REMOVED Requirements

### Requirement: Weight line under the Askeleet bars
**Reason**: Fixed tabs show one data kind (user, 2026-11-15).
**Migration**: Combine steps and weight in an own chart (Uusi kaavio).
