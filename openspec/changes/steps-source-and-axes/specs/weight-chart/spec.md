## MODIFIED Requirements

### Requirement: Units on the chart
Every Historia chart's y-axis labels SHALL show the unit: `98 kg` on Paino and on the weight line
under the bars, `2 000 kcal` on Energia, `8 000 askelta` on Askeleet; every weight and change in the
period statistics SHALL show `kg`. BMI has no unit and its axis labels SHALL be bare numbers under a
"BMI" chart title. Numbers SHALL use Finnish formatting (thin space for thousands, decimal comma).

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

### Requirement: Askeleet without data
When the chosen period holds no step days at all, the Askeleet tab SHALL show one line instead of
an empty chart: with "Kaikki (Health Connectin järjestys)", or "Automaattinen" with no app found,
it tells the user to connect Garmin Connect (or another app that records steps) to Health Connect;
with one app as the source, whether chosen or picked by "Automaattinen", it says that app has no
steps in the period and offers to change the source, which opens the source setting. The front
page steps card keeps its existing empty text.

#### Scenario: No step days
- **GIVEN** the steps permission is granted and Health Connect holds no steps in the period
- **THEN** the tab shows one line about connecting Garmin Connect to Health Connect and no empty chart

#### Scenario: Chosen app empty
- **GIVEN** the source is Garmin Connect and it wrote no steps in the period, while the phone did
- **THEN** the tab says Garmin Connect has no steps in this period, with a "Vaihda lähde" button
  that opens Asetukset → Askeleet

#### Scenario: Picked app empty
- **GIVEN** "Automaattinen" has picked Garmin Connect and it wrote no steps in the period
- **THEN** the tab shows the same "Garmin Connect: ei askelia tällä jaksolla" and a "Vaihda lähde"
  button

## ADDED Requirements

### Requirement: Chart axes
The Paino, BMI, Energia and Askeleet charts in Historia, and the weight line under the Energia and
Askeleet bars, SHALL share one axis rule. The y-axis SHALL show exactly three labelled values with
a faint guideline each: the bottom, the middle and the top. For bars the bottom SHALL be 0 and the
top a round value above the highest bar or line point (steps in steps of 2 000, kcal in steps of
500), so the middle is a round value too. For a weight or BMI line the bottom and top SHALL be the
ends of the weight scale (which may start above zero) and the middle their midpoint, shown with at
most one decimal. The labels SHALL sit in a column left of the plot, the same width for a bar chart
and the weight line under it, so both plots keep one time axis. The x-axis SHALL show 3 to 5 dates
evenly spread over the period, the first at the period's start and the last at its end (Kaikki:
the first day with data and today), formatted as day and month, or month and year when the period
spans 365 days or more (1 v included); a period of 1 or 2 days SHALL show its 1 or 2 days. The
number of dates (5, 4 or 3) SHALL be the largest that fits without overlap at the measured label
widths, and labels SHALL NOT overlap on a 360 dp wide phone at font scale 1.3. Dates SHALL be
spread evenly by days, also when the bars are weekly. Paino's line chart SHALL use the same dates
as the bar charts. The weight line under the bars SHALL have a strip of about 80 dp with all three
labels; the top and bottom labels SHALL stay inside the plot, and when the three labels with units
do not fit the strip, the unit SHALL stay on the top label only.

#### Scenario: Steps axis
- **GIVEN** the highest steps bar is 9 400
- **WHEN** Askeleet is shown
- **THEN** the y-axis reads `0 askelta`, `6 000 askelta` and `12 000 askelta`

#### Scenario: Weight line axis
- **GIVEN** the weight scale runs from 95 to 100 kg
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
with a swatch in its colour: Paino "paino", BMI "BMI", Energia "ruoka", "herkut", "7 pv
keskiarvo" and "paino", Askeleet "askeleet", "7 pv keskiarvo" and "paino". An entry SHALL be shown
only while its bar or line is drawn (no average line: no average entry; no weights in the period:
no "paino" entry). When the bars are weekly (periods over 90 days), the legend row SHALL also say "viikoittain".
The legend SHALL wrap to a second line on a narrow phone rather than cut text.

#### Scenario: Askeleet legend
- **GIVEN** a month with steps, an average line and weights
- **WHEN** Askeleet is shown
- **THEN** the legend reads askeleet, 7 pv keskiarvo and paino, each with its colour

#### Scenario: No weights
- **GIVEN** a period without weights
- **WHEN** Energia is shown
- **THEN** the legend has no "paino" entry

### Requirement: Askeleet source line
While the steps permission is granted, the Askeleet tab SHALL show one quiet line naming the steps source in use ("Lähde:
Garmin Connect (automaattinen)", "Lähde: Puhelin", "Lähde: kaikki sovellukset", and "Lähde: kaikki
sovellukset (automaattinen)" when the automatic pick found no app), directly under the chart, or
under the empty-period message, and above the statistics, also while the data loads, when the
period is empty and when the data is stale. A chosen app without steps in 30 days SHALL show in the
warning tint once today's source check has run (unknown before it: no tint). Tapping the line SHALL open Asetukset →
Askeleet; back SHALL return to Historia on Askeleet. The front page steps card SHALL NOT show the
source.

#### Scenario: Automatic source
- **GIVEN** the automatic source is Garmin Connect
- **WHEN** Askeleet is shown
- **THEN** the line reads "Lähde: Garmin Connect (automaattinen)"

#### Scenario: Open the setting
- **WHEN** the user taps the source line
- **THEN** Asetukset → Askeleet opens; back returns to Askeleet
