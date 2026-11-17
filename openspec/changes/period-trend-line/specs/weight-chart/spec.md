# Delta for weight-chart

## ADDED Requirements

### Requirement: Trend line on long periods
On a period over 31 days (3 kk, 1 v, Kaikki, an Oma range over 31 days) every chart that draws the
flat average line on shorter periods SHALL draw a trend line in its place, and the Paino and BMI
charts SHALL draw one too (they have no average line on shorter periods). The trend line SHALL be
a smooth curve through the same daily values the average uses: Syöty the kcal of each logged day,
Askeleet the steps of each day with steps, Kulutus the total of each day with data, each with today
left out when it is in the period; Paino each day's last weight not marked "?", today included;
BMI from those weights. It SHALL follow slow changes and iron out the day-to-day zigzag, with a
smoothness that grows with the period: on 3 kk it shows changes over a few weeks, over two years
changes over a few months. It SHALL be computed from the daily values also when the bars are
weekly. It SHALL run from the first to the last day with a value, SHALL break over a stretch of
more than 30 days without values (no line across the gap), and a part with fewer than 10 days with
values SHALL get no line. When the whole period has fewer than 10 days with values the flat average
line SHALL be drawn instead (none on Paino and BMI). The trend line SHALL NOT change the scale rule
or the statistics: the period's average stays a number under the chart.

It SHALL be a solid line, thicker than the data's own line, in the neutral line tone
(onSurfaceVariant) on the fixed tabs and in Allekkain, in the data's tone in Päällekkäin; drawn over
the bars and over the Paino and BMI line. Tapping a day SHALL add the row "trendi" with the trend's
value on that day in the data's unit ("trendi 8 200 askelta/pv", "trendi 2 050 kcal/pv",
"trendi 81,4 kg", "trendi 24,8") to the marker card, only on a day the trend line covers.

The same rule SHALL hold on the front page: a big Syöty, Askeleet, Kulutus or Paino card on 3 kk
draws the trend line where it draws the average line on shorter periods (Paino: none on shorter
periods); small and wide cards draw neither. Own chart cards follow their chart.

#### Scenario: Year of steps
- **GIVEN** a year of daily steps that rise from about 6 000 to about 9 000 with daily values
  between 2 000 and 15 000
- **WHEN** Askeleet is shown with 1 v
- **THEN** weekly bars are drawn with a smooth solid line rising from about 6 000 to about 9 000,
  no flat dashed line, and the legend reads askeleet, trendi and viikoittain

#### Scenario: Month keeps the flat line
- **WHEN** Askeleet is shown with 1 kk
- **THEN** the flat dashed average line is drawn and no trend line

#### Scenario: Weight trend
- **GIVEN** weights for 90 days falling from about 84 to about 81 kg with ±0,8 kg daily noise
- **WHEN** Paino is shown with 3 kk
- **THEN** the weight line is drawn as today and a smooth line falls from about 84 to about 81 kg
  over it

#### Scenario: Gap in the data
- **GIVEN** Kaikki with food logged in January–February and again in May–June, nothing in between
- **WHEN** Syöty is shown
- **THEN** one trend line covers January–February and another May–June, with no line over
  March–April

#### Scenario: Too few days
- **GIVEN** 3 kk with food logged on 6 days
- **WHEN** Syöty is shown
- **THEN** the flat dashed average line is drawn and no trend line

#### Scenario: Trend in the marker card
- **GIVEN** Askeleet with 1 v and a trend line
- **WHEN** the user taps a week
- **THEN** the marker card shows the week's row and "trendi" with the trend's steps per day in that
  week

#### Scenario: Statistics unchanged
- **WHEN** Syöty is shown with 1 v and a trend line
- **THEN** the statistics under the chart show the period's average kcal per logged day as before

## MODIFIED Requirements

### Requirement: Energia chart
The Energia tab SHALL show, for the chosen period, one bar per logged day with its kcal, the
treats' share stacked as its own tone, and one flat dashed line across the whole chart at the period's average kcal per logged
day, the value of the Energia statistics (today left out when it is in the period), also
when the bars are weekly (the per-day average, not the mean of the week bars); no line when
there is no average. The scale's top SHALL cover both the highest bar and the line.
The flat line SHALL be drawn on periods up to 31 days; on longer periods the trend line (see "Trend line on long periods") SHALL replace it. Days with
nothing logged SHALL be gaps, never zero bars. For 1 v, Kaikki and an Oma range over 90 days the
bars SHALL be one per week (average per logged day of the week, today left out). The chart SHALL show energy only:
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

#### Scenario: Average line
- **GIVEN** 1 vk with six earlier days averaging 2 100 kcal and today 400 kcal so far
- **WHEN** the Energia tab is shown
- **THEN** one flat dashed line is drawn at 2 100 kcal, days over it reach above it and today's
  short bar stays under it

### Requirement: Askeleet chart
The Askeleet tab SHALL show, for the chosen period, one bar per day with steps in one tone, and one
flat dashed line across the whole chart at the period's average steps per day with steps, the
value of the Askeleet statistics (today left out when it is in the period), also when the bars
are weekly; no line when there is no average. The scale's top SHALL cover both the highest bar
and the line. The flat line SHALL be drawn on periods up to 31 days; on longer periods the trend line (see "Trend line on long periods") SHALL replace it. Days without steps data SHALL
be gaps, never zero bars. Kaikki SHALL start at the first day with steps or today. The chart SHALL
show steps only: no weights, goal line, target or second y-axis. Tapping a bar SHALL show its day
and steps through the crosshair.

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

#### Scenario: Average line
- **GIVEN** 1 vk where the six earlier days average 8 770 steps and today has 61
- **WHEN** the Askeleet tab is shown
- **THEN** one flat dashed line is drawn at 8 770, the bars above it stand out and the line does
  not reach back to days before the period

### Requirement: Chart legends
Every bar and line in a Historia chart SHALL have a legend entry under its chart saying what it is,
with a swatch in its colour: Paino "paino", BMI "BMI", Energia "syöty", "herkut" and
"keskiarvo", Askeleet "askeleet" and "keskiarvo"; an own chart's average lines share one
"keskiarvo" entry; every "keskiarvo" swatch SHALL be a dashed line in the neutral line tone. An entry SHALL be shown only while its bar or
line is drawn (no average line: no average entry). A drawn trend line SHALL have the entry "trendi" with a solid line swatch in its tone instead of "keskiarvo"; an own chart's trend lines share one "trendi" entry. When the bars are weekly (periods over 90
days), the legend row SHALL also say "viikoittain". The legend SHALL wrap to a second line on a
narrow phone rather than cut text.

#### Scenario: Askeleet legend
- **GIVEN** a month with steps, an average line and weights
- **WHEN** Askeleet is shown
- **THEN** the legend reads askeleet and keskiarvo, each with its colour, and no "paino"

#### Scenario: No weights
- **GIVEN** a period without weights
- **WHEN** Energia is shown
- **THEN** the legend has no "paino" entry

### Requirement: Custom charts
The user SHALL be able to make any number of own charts. A chart SHALL have a name (1 to 24
characters, trimmed, not blank; duplicates allowed), one or more shown data out of Paino, Energia
and Askeleet (no BMI), for each shown data a style, Pylväät (bars) or Viiva (line) (Paino always Viiva: bars of a
weight scale that does not start at zero would mislead), a layout,
Allekkain (stacked panels) or Päällekkäin (overlaid), and its own time period (see Chart time
axis). The data SHALL always be drawn and listed in the order Paino, Energia (Syöty), Kulutus, Askeleet. Each chart
SHALL be one definition that is shown as its own Historia tab and, when the user adds it there, as
a front page card; a change to it SHALL show in both, and deleting it SHALL remove both. The charts
SHALL be remembered between app starts and SHALL NOT be part of the data file or the backup file.

A custom chart SHALL draw real values only: for each day its own value, at every period. It SHALL NOT draw weekly bars. When the chart's Keskiarvo is on, each drawn data SHALL also get, on a period up to 31 days, one flat dashed line (on a longer period the trend line (see "Trend line on long periods") instead) (1 dp, the front page cards' dash); on a
Historia tab it is drawn over the bars and lines, on a front page card over the bars and under the
value lines; in the neutral line tone (onSurfaceVariant) in Allekkain
and in the data's tone in Päällekkäin, across the period at its average on its scale (shared with a data of the same unit, see "Shared scale for the same unit"): Energia the average kcal per
logged day and Askeleet the average steps per day with steps, both with today left out when it is in the period; Paino the
average of each day's last weight not marked "?", today included (a weight is final). A line SHALL be drawn only for a data that is drawn
(not loading, not without permission) and has a value to average; a Päällekkäin data
without a value axis SHALL still get its line. The line SHALL be drawn on the Historia tab and on the
front page card at every size. The Paino average can differ from the Paino tab's statistic, which
counts every weight. What each style draws:
- Paino, Viiva: each day's last weight not marked "?" as a line with the weight scale of the weight
  chart, dots when they fit. Paino has no Pylväät.
- Energia, Pylväät: one bar per logged day (kcal), with the treats' share in its tone in the
  Allekkain layout. Energia, Viiva: a line joining each logged day's own kcal.
- Askeleet, Pylväät: one bar per day with steps. Askeleet, Viiva: a line joining each such day's
  own steps.
- Bars SHALL start at zero and take the same share of every day's slot,
  so over long periods they are thin (on a front page card at least 1 dp wide); days without data
  SHALL be gaps in bars, and a line SHALL join the days that have a value.
- Scales: Paino the weight chart's scale; Energia `ceil(highest day × 1,1 / 500) × 500` kcal
  (at least 500; shared with Kulutus when both are drawn, see "Shared scale for the same unit"); Askeleet `ceil(highest day × 1,1 / 2 000) × 2 000` steps (at least 2 000), as on
  the Energia and Askeleet tabs.

#### Scenario: Two charts
- **GIVEN** the user made "Paino ja ruoka" (Paino line, Energia bars) and "Liike" (Askeleet bars)
- **THEN** Historia has the tabs Paino, BMI, Energia, Askeleet, Paino ja ruoka, Liike and Uusi kaavio

#### Scenario: One definition
- **GIVEN** "Liike" is on the front page and in Historia
- **WHEN** the user switches its Askeleet to Viiva in the editor
- **THEN** both the card and the tab draw a line

#### Scenario: Energy line is real
- **GIVEN** a chart with Energia as Viiva and food logged on three days of the last 30
- **THEN** the chart draws a line through those three days' own kcal values, no moving average and no bars

#### Scenario: Weight bars
- **GIVEN** a chart with Paino and two weights on 12.11. (82,0 in the morning and 82,4 in the evening)
- **THEN** Paino is drawn as a line, never bars, with 12.11. at 82,4 kg, the day's last weight, not 82,2

#### Scenario: Old chart with weight bars
- **GIVEN** a chart saved with Paino as Pylväät
- **THEN** it draws Paino as a line; its name, data, layout, period, colours and Keskiarvo are
  unchanged, and in Päällekkäin its other bars share the day's slot as in any chart with that
  many bar data

#### Scenario: Long period stays daily
- **GIVEN** a chart with Energia as Pylväät and the period 1 v
- **THEN** one thin bar is drawn per logged day, not weekly bars

#### Scenario: Steps scale
- **GIVEN** a chart with Askeleet and a highest day of 11 400 steps
- **THEN** the scale's top reads 14 000

#### Scenario: Average on
- **GIVEN** a chart with Askeleet (Pylväät) and Paino (Viiva), Keskiarvo on, period 1 kk
- **THEN** a dashed line in the steps tone marks the month's steps per day without today and a
  dashed line in the weight tone marks the month's average weight, each on its own scale
