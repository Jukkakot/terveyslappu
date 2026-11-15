# weight-chart Specification

## Purpose
Chart, periods and statistics of the weights this app wrote, shown on the main screen.

## Requirements

### Requirement: Weight chart
Historia SHALL show, above the weight list, a chart of the weights this app wrote (scale, manual and
imported) in the chosen period: each weight as a dot, joined by a straight line from dot to dot in
time order. The line SHALL join every pair of consecutive weights the same way however far apart they
are, also over gaps of more than 30 days; a weight with a gap over 30 days on both sides SHALL show as a dot even when the
chart otherwise leaves the dots out. The x-axis labels SHALL show day and month, or month and year
when the chart spans more than a year. Weights written by other apps SHALL NOT be shown. The plot
SHALL start right at the y-axis and end at the plot's right edge: the period's start (for Kaikki the
first weight) sits on the y-axis and the period's end on the right edge, with no empty margin before
or after; this holds for every chart and quantity.

#### Scenario: A week of weigh-ins
- **GIVEN** the app wrote seven weights in the last seven days
- **WHEN** Historia is shown with period 1 vk
- **THEN** the chart shows seven dots joined by a solid straight line, with day labels

#### Scenario: Long gap
- **GIVEN** weights until 20.5.2024 and the next one on 18.3.2025
- **WHEN** Historia is shown with period Kaikki
- **THEN** the same solid line runs straight from the weight of 20.5.2024 to the weight of 18.3.2025

#### Scenario: Sparse history
- **GIVEN** weights about every 40 days for a year
- **WHEN** Historia is shown with period Kaikki
- **THEN** every weight is a dot and consecutive dots are joined by the solid straight line

#### Scenario: Lone weight between gaps
- **GIVEN** a single weight in October 2024 with no other weight within 30 days, in a chart dense
  enough to leave the dots out
- **WHEN** the chart is shown
- **THEN** that weight shows as a dot, joined to its neighbours by the line

#### Scenario: Several years
- **GIVEN** weights from 24.9.2022 to today
- **WHEN** Historia is shown with period Kaikki
- **THEN** the x-axis labels read like "9/2022", "7/2023"

#### Scenario: Other app's weight
- **GIVEN** another app wrote a weight yesterday
- **WHEN** the chart is shown
- **THEN** that weight is not in the chart

#### Scenario: No weights in the period
- **WHEN** the chosen period has no weights of this app
- **THEN** the chart area says there are no weights in the period

#### Scenario: First weight on the axis
- **GIVEN** weights from 24.9.2022 to today
- **WHEN** Historia is shown with period Kaikki
- **THEN** the line starts right at the y-axis, with no gap between the axis and the first weight

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

### Requirement: Period statistics
Under the chart the app SHALL show for the chosen period: the latest weight, the change from the
first to the latest weight in kg, that change per week, the average, the lowest and highest weight,
and the number of weights. The change and the change per week SHALL each show a small arrow up for
a gain and down for a loss next to the number, and no arrow when the shown value is zero. For BMI the
statistics SHALL show the same values of the BMI except the change per week; the latest BMI SHALL also show its category word and tone.

#### Scenario: Losing
- **GIVEN** a 1 kk period whose first weight is 83.0 kg and latest 82.0 kg, 28 days apart
- **WHEN** the stats are shown
- **THEN** they show latest 82.0, change −1.0 kg and −0.25 kg/vko, both with an arrow down

#### Scenario: No change
- **GIVEN** a period whose first and latest weight are both 82.0 kg
- **WHEN** the stats are shown
- **THEN** the change shows ±0,0 kg without an arrow

#### Scenario: One weight only
- **WHEN** the period has a single weight
- **THEN** the stats show it as latest, average, lowest and highest and no change or rate

#### Scenario: BMI statistics
- **GIVEN** height 180 cm and a period whose first weight is 83.0 kg and latest 82.0 kg
- **WHEN** the BMI statistics are shown
- **THEN** they show latest 25,3 "ylipaino", change −0,3 with an arrow down, and no rate per week

### Requirement: Point details
Tapping a dot SHALL show that weight's date, time and value of the shown quantity; the date SHALL
include the year when the weight is not from the current year.

#### Scenario: Tap a dot
- **WHEN** the user taps the dot of 82.6 kg on 8.11. this year at 7.05
- **THEN** the chart shows "8.11. 7.05 82,6 kg"

#### Scenario: Tap an old dot
- **WHEN** the user taps the dot of 97.6 kg on 15.9.2024 at 8.33
- **THEN** the chart shows "15.9.2024 8.33 97,6 kg"

#### Scenario: Tap a BMI dot
- **GIVEN** the BMI chart is shown and the height is 180 cm
- **WHEN** the user taps the dot of 82.0 kg on 8.11. this year at 7.05
- **THEN** the chart shows "8.11. 7.05 BMI 25,3"

### Requirement: Weight scale
Every weight chart (Historia and the home card) SHALL use one y-scale rule: the lowest and highest
shown weight with 0.5 kg room on each side, widened evenly to at least 4 kg when narrower, and
rounded outwards to whole kilos. The scale SHALL grow when the weights spread more and SHALL never be
narrower than 4 kg, so a small change looks small in every period and on both screens. The BMI chart
SHALL use the same rule with the room and the least span as many BMI units as 0.5 kg and 4 kg give
at the owner's height, rounded outwards to whole BMI units.

#### Scenario: Steady weight
- **GIVEN** the weights in view are 97.76 and 97.84 kg
- **THEN** the y-scale runs from 95 to 100 kg and the line looks flat

#### Scenario: Real change
- **GIVEN** the weights in view run from 92.3 to 98.6 kg
- **THEN** the y-scale runs from 91 to 100 kg

#### Scenario: Steady BMI
- **GIVEN** height 180 cm and BMI values in view of 25,2 and 25,3
- **THEN** the BMI y-scale spans at least 1,2 units and the line looks flat

#### Scenario: Steady basal metabolic rate
- **WHEN** Historia is shown
- **THEN** there is no basal metabolic rate chart, only Paino and BMI

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

### Requirement: One quantity at a time
Historia's fixed tabs SHALL show one quantity at a time: Paino, BMI, Energia or Askeleet. Tabs with
those names SHALL sit above the chart in that order, followed by the user's own charts and the
"Uusi kaavio" tab (see Custom chart tabs); tapping a tab or swiping the chart and statistics left
or right SHALL switch the tab, and the chosen period SHALL stay. All four fixed tabs SHALL always
be shown; a quantity that cannot be computed SHALL show, instead of its chart and statistics, the
reason naming what is missing and a button to fix it (Omat tiedot for BMI, the nutrition
permissions for Energia, the steps permission for Askeleet). Historia SHALL open on Paino. On Paino
and BMI the list under the statistics SHALL be the weight list; on Energia it SHALL be the list of
logged days (see Energia list); on Askeleet it SHALL be the list of days with steps (see Askeleet
list); an own chart's tab and the "Uusi kaavio" tab SHALL have no list.

#### Scenario: Swipe to BMI
- **GIVEN** a height is set and Historia shows Paino for 1 kk
- **WHEN** the user swipes the chart to the left
- **THEN** the BMI tab is chosen and the chart and statistics show the BMI of the same 30 days

#### Scenario: Swipe to Energia
- **GIVEN** Historia shows BMI for 1 vk
- **WHEN** the user swipes the chart to the left
- **THEN** the Energia tab is chosen for the same 7 days

#### Scenario: No owner details
- **GIVEN** no height is set
- **WHEN** the user opens the BMI tab
- **THEN** it shows "BMI vaatii pituuden" and a button that opens Asetukset at Omat tiedot

#### Scenario: Swipe to Askeleet
- **GIVEN** Historia shows Energia for 1 kk
- **WHEN** the user swipes the chart to the left
- **THEN** the Askeleet tab is chosen for the same 30 days

#### Scenario: No steps permission
- **GIVEN** the steps permission is not granted
- **WHEN** the user opens the Askeleet tab
- **THEN** it shows the reason and a button that asks for the permission

#### Scenario: Opens on weight
- **GIVEN** the user left Historia on Energia
- **WHEN** Historia is opened again
- **THEN** it shows Paino

#### Scenario: Tap a tab
- **WHEN** the user taps BMI
- **THEN** the chart and statistics show the BMI of the chosen period

#### Scenario: Swipe past Askeleet
- **GIVEN** an own chart "Liike" and Historia on Askeleet
- **WHEN** the user swipes the chart to the left
- **THEN** the Liike tab is chosen for the same period

### Requirement: BMI bands
The BMI chart SHALL show, behind the line, faint background bands in the category tones (green,
yellow, red) for the BMI ranges that fall inside its y-scale.

#### Scenario: Around the border
- **GIVEN** the BMI values in view run from 24,6 to 25,4
- **WHEN** the BMI chart is shown
- **THEN** the area under 25 has a faint green band and the area from 25 a faint yellow band

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

### Requirement: Questioned weights left out
A weight marked "?" SHALL NOT be drawn on any chart, SHALL NOT count in the period statistics and
SHALL NOT affect the weight scale or the calendar's days with weights, until it is accepted.

#### Scenario: Questioned weight
- **GIVEN** the period holds 82,0 kg and 82,4 kg and a 61,3 kg marked "?"
- **WHEN** the chart is shown
- **THEN** two dots are drawn, the scale fits 82,0-82,4 kg and the lowest weight is 82,0 kg

#### Scenario: Accepted
- **WHEN** the user accepts 61,3 kg
- **THEN** it is drawn and counted like any weight

### Requirement: Energia chart
The Energia tab SHALL show, for the chosen period, one bar per logged day with its kcal, the
treats' share stacked as its own tone, and one flat dashed line across the whole chart at the period's average kcal per logged
day, the value of the Energia statistics (today left out when it is in the period), also
when the bars are weekly (the per-day average, not the mean of the week bars); no line when
there is no average. The scale's top SHALL cover both the highest bar and the line.
There SHALL be no moving average line. Days with
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

### Requirement: Energia statistics
Under the Energia chart the app SHALL show for the period: the average kcal per logged day, the
average treat kcal per logged day, and the number of logged days of the period's days ("12 / 30
päivää"). It SHALL NOT show a target, a difference to a target or any energy balance.

#### Scenario: Partial month
- **GIVEN** 1 kk with 12 logged days averaging 1 900 kcal, treats 210 kcal per day
- **THEN** the statistics show 1 900 kcal/pv, herkut 210 kcal/pv and 12 / 30 päivää

### Requirement: Energia list
On the Energia tab the list under the statistics SHALL show the period's logged days, newest first,
each with its date, total kcal and treat share; tapping a day SHALL open the Ruoka screen on that
day.

#### Scenario: Open a day
- **WHEN** the user taps 12.11. in the Energia list
- **THEN** the Ruoka screen opens on 12.11.; back returns to Historia on Energia

### Requirement: Askeleet chart
The Askeleet tab SHALL show, for the chosen period, one bar per day with steps in one tone, and one
flat dashed line across the whole chart at the period's average steps per day with steps, the
value of the Askeleet statistics (today left out when it is in the period), also when the bars
are weekly; no line when there is no average. The scale's top SHALL cover both the highest bar
and the line. There SHALL
be no moving average line. Days without steps data SHALL
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

#### Scenario: Average line
- **GIVEN** 1 vk where the six earlier days average 8 770 steps and today has 61
- **WHEN** the Askeleet tab is shown
- **THEN** one flat dashed line is drawn at 8 770, the bars above it stand out and the line does
  not reach back to days before the period

### Requirement: Askeleet weekly bars
When the period is over 90 days the Askeleet bars SHALL be one per week (average per day with steps
of the week, today left out), as on Energia; 3 kk and shorter stay daily.

#### Scenario: Year view
- **WHEN** the period is 1 v
- **THEN** one bar per week is drawn

#### Scenario: Three months
- **WHEN** the period is 3 kk
- **THEN** one bar per day is drawn

### Requirement: Askeleet statistics
Under the Askeleet chart the app SHALL show for the period: the average steps per day with steps
(today left out, since it is not over), and the number of days with steps of the period's days
("26 / 30 päivää"; today counts as a day with steps when it has steps, as on Energia).
It SHALL NOT show a goal, a difference to a goal, distance or calories.

#### Scenario: Partial month
- **GIVEN** 1 kk with 26 days with steps averaging 7 800
- **THEN** the statistics show 7 800 askelta/pv and 26 / 30 päivää

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

### Requirement: Askeleet list
On the Askeleet tab the list under the statistics SHALL show the period's days with steps, newest
first, each with its date and steps. A row SHALL NOT open anything.

#### Scenario: Newest first
- **GIVEN** steps for 12.11., 13.11. and 14.11.
- **THEN** the list reads 14.11., 13.11., 12.11. with their steps

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

### Requirement: Chart legends
Every bar and line in a Historia chart SHALL have a legend entry under its chart saying what it is,
with a swatch in its colour: Paino "paino", BMI "BMI", Energia "ruoka", "herkut" and
"keskiarvo", Askeleet "askeleet" and "keskiarvo"; an own chart's average lines share one
"keskiarvo" entry; every "keskiarvo" swatch SHALL be a dashed line in the neutral line tone. An entry SHALL be shown only while its bar or
line is drawn (no average line: no average entry). When the bars are weekly (periods over 90
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

### Requirement: Custom charts
The user SHALL be able to make any number of own charts. A chart SHALL have a name (1 to 24
characters, trimmed, not blank; duplicates allowed), one or more shown data out of Paino, Energia
and Askeleet (no BMI), for each shown data a style, Pylväät (bars) or Viiva (line), a layout,
Allekkain (stacked panels) or Päällekkäin (overlaid), and its own time period (see Chart time
axis). The data SHALL always be drawn and listed in the order Paino, Energia, Askeleet. Each chart
SHALL be one definition that is shown as its own Historia tab and, when the user adds it there, as
a front page card; a change to it SHALL show in both, and deleting it SHALL remove both. The charts
SHALL be remembered between app starts and SHALL NOT be part of the data file or the backup file.

A custom chart SHALL draw real values only: for each day its own value, at every period. It SHALL
NOT draw moving average lines and SHALL NOT draw weekly bars. When the chart's Keskiarvo is on,
each drawn data SHALL also get one flat dashed line (1 dp, the front page cards' dash) drawn over the bars and
under the value lines and the crosshair, in the neutral line tone (onSurfaceVariant) in Allekkain
and in the data's tone in Päällekkäin, across the period at its average on its own scale: Energia the average kcal per
logged day and Askeleet the average steps per day with steps, both with today left out when it is in the period; Paino the
average of each day's last weight not marked "?", today included (a weight is final). A line SHALL be drawn only for a data that is drawn
(not loading, not without permission) and has a value to average; a third Päällekkäin data
without a scale SHALL still get its line. The line SHALL be drawn on the Historia tab and on the
front page card at every size. The Paino average can differ from the Paino tab's statistic, which
counts every weight. What each style draws:
- Paino, Viiva: each day's last weight not marked "?" as a line with the weight scale of the weight
  chart, dots when they fit. Paino, Pylväät: one bar per day with such a weight, drawn up from the
  bottom of the same weight scale.
- Energia, Pylväät: one bar per logged day (kcal), with the treats' share in its tone in the
  Allekkain layout. Energia, Viiva: a line joining each logged day's own kcal.
- Askeleet, Pylväät: one bar per day with steps. Askeleet, Viiva: a line joining each such day's
  own steps.
- Bars SHALL start at zero (Paino excepted, as above) and SHALL be at least 1 dp wide, so over long
  periods they are thin; days without data SHALL be gaps in bars, and a line SHALL join the days
  that have a value.
- Scales: Paino the weight chart's scale; Energia `ceil(highest day × 1,1 / 500) × 500` kcal
  (at least 500); Askeleet `ceil(highest day × 1,1 / 2 000) × 2 000` steps (at least 2 000), as on
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
- **GIVEN** a chart with Paino as Pylväät and two weights on 12.11. (82,0 in the morning and 82,4 in the evening)
- **THEN** 12.11. has one bar at 82,4 kg on the weight scale, the day's last weight, not 82,2

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

### Requirement: Custom chart tabs
Historia's tab row SHALL show the fixed tabs Paino, BMI, Energia and Askeleet, then one tab per own
chart named after it (in the order they were made), then a last tab "Uusi kaavio". Historia SHALL
open on Paino. An own chart's tab SHALL show the chart for the chart's own period with a "Muokkaa"
button that opens the chart editor, and SHALL show no statistics and no list under it. On an own
chart's tab the period chips (1 vk, 1 kk, 3 kk, 1 v, Kaikki, Oma with its calendar) SHALL be the
chart's own: they SHALL show and change only that chart's period, and Historia's shared period
chips SHALL NOT affect the chart; on the other tabs the shared chips work as before and a chart's
period SHALL NOT change them. The "Uusi kaavio" tab SHALL show one line on what own charts are and a
button "Uusi kaavio" that opens the editor for a new chart. After a new chart is saved Historia
SHALL show its tab; after a chart is deleted Historia SHALL show the tab before it. While the
weights are loading or failed an own chart's tab SHALL show the same spinner or failure notice as
the other tabs.

#### Scenario: No charts yet
- **GIVEN** the user has made no charts
- **WHEN** the user swipes past Askeleet
- **THEN** the "Uusi kaavio" tab shows its line and the Uusi kaavio button

#### Scenario: Opens on weight
- **GIVEN** the user left Historia on an own chart's tab
- **WHEN** Historia is opened again
- **THEN** it shows Paino

#### Scenario: Long name
- **GIVEN** a chart named "Paino, ruoka ja askeleet"
- **THEN** its tab shows the name on one line, shortened with an ellipsis when it does not fit

#### Scenario: Own period
- **GIVEN** Historia's shared period is 1 kk and the chart "Liike" has 1 kk
- **WHEN** the user opens the Liike tab and taps 3 kk
- **THEN** the chart shows 90 days, Paino's period is still 1 kk, and the Liike card on the front page shows 3 kk too

#### Scenario: Shared chips do not reach a chart
- **GIVEN** the Liike tab with 1 kk
- **WHEN** the user switches to Paino and picks 1 v, then returns to the Liike tab
- **THEN** the Liike tab still shows 1 kk

### Requirement: Chart editor
The chart editor SHALL be a dialog with: a name field; one row per data (Paino, Energia, Askeleet)
with a switch and a two-way choice Pylväät / Viiva that is enabled only while the data is on; the
layout choice Allekkain / Päällekkäin, each with one line saying what it draws; a switch "Keskiarvo" with one
line "Vaakaviiva jakson keskiarvon kohdalla"; and the buttons
Peruuta and Tallenna. The editor SHALL NOT hold the period (it is chosen with the chips on the
chart's tab; saving keeps it). The last data that is on SHALL NOT be switchable off. Tallenna SHALL
be disabled while the name is blank; the name SHALL accept at most 24 characters (an emoji counts
as one) and SHALL be saved trimmed. A new chart SHALL start with the name "Kaavio <n>" (the lowest
n for which no other chart's name equals "Kaavio <n>", ignoring case and surrounding spaces), all
three data on, Paino as Viiva, Energia and Askeleet as Pylväät, Allekkain, Keskiarvo off, and the period 1 kk.
A chart saved before Keskiarvo existed SHALL read as Keskiarvo off. For
an existing chart the editor SHALL also show a red "Poista kaavio" on the left, which deletes the
chart at once, without a question, and shows "Kaavio poistettu · Kumoa"; Kumoa SHALL bring the
chart back with its name, data, layout, Keskiarvo, period and its front page card in its place. Peruuta, back and tapping outside SHALL close
the editor without saving and without asking. A data switched off SHALL keep its chosen style for
when it is switched on again while the editor is open.

#### Scenario: New chart defaults
- **GIVEN** a chart named "Kaavio 1" exists
- **WHEN** the user taps Uusi kaavio
- **THEN** the editor shows "Kaavio 2", all three data on, Paino Viiva, Energia and Askeleet
  Pylväät, Allekkain and Keskiarvo off, and the saved chart's period is 1 kk

#### Scenario: Rename
- **WHEN** the user changes the name to "Ilta" and taps Tallenna
- **THEN** the tab and the front page card read "Ilta"

#### Scenario: Same name allowed
- **GIVEN** a chart named "Ilta"
- **WHEN** the user names another chart "Ilta" and taps Tallenna
- **THEN** both charts exist

#### Scenario: Last data stays on
- **GIVEN** only Askeleet is on
- **WHEN** the user taps its switch
- **THEN** it stays on

#### Scenario: Blank name
- **WHEN** the user clears the name
- **THEN** Tallenna is disabled

#### Scenario: Delete
- **GIVEN** the chart "Liike" is on the front page
- **WHEN** the user taps Poista kaavio
- **THEN** its tab and its card are gone at once and Historia shows the tab before it

#### Scenario: Undo a chart delete
- **GIVEN** the user deleted "Liike", which was on the front page
- **WHEN** they tap Kumoa
- **THEN** its tab and its front page card are back as before

#### Scenario: Cancel
- **GIVEN** the user changed the layout in the editor
- **WHEN** the user presses back
- **THEN** the chart is as before and nothing is asked

### Requirement: Stacked layout
In the Allekkain layout each shown data SHALL have its own panel with its own scale, stacked top to
bottom in the order Paino, Energia, Askeleet on one shared time axis. Axes and legend SHALL follow
the same axis and legend rules as Historia's other charts: each panel a left column of value labels at its bottom, middle and top with the unit, one
row of 4 to 5 dates under the whole stack, and one legend under the dates naming every drawn line
and bar. No panel SHALL have a second value axis and there SHALL be no "viikoittain" note. On a Historia tab each panel SHALL be 112 dp high for three panels, 136 dp for two
and 160 dp for one.

#### Scenario: Three panels
- **GIVEN** weights, food and steps in the last 30 days and a chart with all three data, Allekkain
- **WHEN** the user opens its tab for 1 kk
- **THEN** three panels are stacked, weight, energy, steps, over the same 30 days, each with its value labels, one date row and one legend under the stack

#### Scenario: Two panels
- **GIVEN** a chart with Paino and Askeleet, Allekkain
- **THEN** two panels of 136 dp are shown on the same axis

### Requirement: Overlaid layout
In the Päällekkäin layout all shown data SHALL be drawn in one plot on one time axis, each with its
own scale: bars behind lines, average lines over the bars; with two or three bar data the bars of one day SHALL stand side by
side in its slot. Each data SHALL have its own tone (Paino secondary, Energia primary, Askeleet
tertiary; Energia without the treats' split) and a legend under the plot SHALL name every drawn
data with its tone and its shape. The first two data that are drawn SHALL have their value axis,
the first on the left and the second on the right, each labelled at bottom, middle and top with the
unit in that data's tone; a third drawn data SHALL have no scale at all and SHALL be recognised by
its colour and its legend name only. With one data drawn the plot SHALL look exactly like the
Allekkain layout's single panel. On a Historia tab the plot SHALL be 200 dp high.

#### Scenario: Weight and energy overlaid
- **GIVEN** a chart with Paino (Viiva) and Energia (Pylväät), Päällekkäin
- **THEN** one plot shows the kcal bars with the weight line over them, a kg axis on the left, a kcal
  axis on the right, and a legend naming both

#### Scenario: Three overlaid
- **GIVEN** a chart with all three data, Päällekkäin, Energia and Askeleet as Pylväät
- **THEN** each day has an energy bar and a steps bar side by side, the weight line is over them,
  kg is on the left axis, kcal on the right, and steps have no scale, only their colour and the legend name

#### Scenario: Axes go to what is drawn
- **GIVEN** a chart with all three data, Päällekkäin, and no weights in the period
- **THEN** the kcal axis is on the left and the steps axis on the right

#### Scenario: One data drawn
- **GIVEN** a chart with all three data, Päällekkäin, and only steps have values
- **THEN** the plot looks like a stacked single panel with the steps axis on the left

#### Scenario: Only today logged
- **GIVEN** 1 vk with food logged only today
- **WHEN** the Energia tab is shown
- **THEN** today's bar is drawn and there is no average line and no "keskiarvo" entry

#### Scenario: One weight
- **GIVEN** an own chart with Paino (Viiva), Keskiarvo on, and one weight of 82,0 kg in the period
- **THEN** the dashed line runs at 82,0 kg through the dot

### Requirement: Chart crosshair
On the plot areas of an own chart's tab, tapping, or pressing and holding (released in place or
followed by a sideways drag), SHALL place one vertical line at that day through the chart and SHALL
show above it one readout: the date and, for each shown data with access, that day's own value
(weight in kg, the day's last weight not marked "?", or "ei painoa"; kcal or "ei kirjauksia"; steps
or "ei tietoa"). There SHALL be no week form and no averages. A press-and-hold released without
moving SHALL place the line with a haptic tick; a hold followed by a drag SHALL scrub and own both
directions until release. Buttons inside a panel (such as a permission button) SHALL stay buttons
and a tap outside the drawn plot areas SHALL place nothing. The line SHALL be drawn only over
panels that draw data. Today SHALL show its running values without a mark. Tapping the readout
SHALL remove the crosshair; leaving the tab, a period change, saving the chart in the editor and
leaving Historia SHALL remove it; a data reload SHALL keep it, and it SHALL be removed only when its
day is no longer on the axis. While no day is chosen the readout's place SHALL keep its height and
read "Napauta päivää nähdäksesi arvot". A plain sideways swipe or fling SHALL switch the tab as on
the other tabs and SHALL NOT place a crosshair. For TalkBack the chart SHALL offer "Edellinen päivä"
and "Seuraava päivä", the first use starting from today, and the readout SHALL be a polite live
region updated only on a tap, a drop or one of those actions.

#### Scenario: Tap a day
- **GIVEN** 1 kk with all three data shown
- **WHEN** the user taps a day with all three
- **THEN** a line crosses the chart and the readout shows that date, the weight, the kcal and the steps

#### Scenario: Day without data
- **WHEN** the user taps a day with weight but no food and no steps
- **THEN** the readout shows the weight, "ei kirjauksia" for kcal and "ei tietoa" for the steps

#### Scenario: Last weight of the day
- **GIVEN** two weights on a day, 82,0 and later 82,4, and a third marked "?"
- **WHEN** the user taps that day
- **THEN** the readout shows 82,4 kg

#### Scenario: Hold in place
- **WHEN** the user presses and holds on a day and releases without moving
- **THEN** the line is at that day and the readout shows its values

#### Scenario: Scrub
- **WHEN** the user presses and holds on the chart and drags sideways and up or down
- **THEN** the line and the readout follow the finger day by day and the page does not scroll until release

#### Scenario: Long period is still daily
- **GIVEN** the period 1 v
- **WHEN** the user taps a day
- **THEN** the readout shows that day's own values, not a week's

#### Scenario: Gated panel
- **GIVEN** the steps permission is missing, Allekkain
- **WHEN** the user taps a day
- **THEN** the line crosses the weight and energy panels only, and the grant button still asks for the permission when tapped

#### Scenario: Swipe still switches tabs
- **WHEN** the user flings the chart sideways without holding
- **THEN** Historia switches tab and no crosshair appears

#### Scenario: Clearing
- **GIVEN** a crosshair
- **WHEN** the user taps the readout, or changes the period or the tab
- **THEN** the crosshair is removed

#### Scenario: Kept on reload
- **GIVEN** a crosshair on a day inside the axis
- **WHEN** the data reloads
- **THEN** the crosshair stays

#### Scenario: Step through days
- **GIVEN** TalkBack on the chart
- **WHEN** the user runs "Edellinen päivä" with no crosshair
- **THEN** today is chosen and the readout is announced

### Requirement: Chart time axis
An own chart SHALL have its own period, chosen with the same choices as Historia's period chips (1
vk, 1 kk, 3 kk, 1 v, Kaikki, Oma with its calendar), stored in the chart's definition, 1 kk for a
new chart, and the same on its Historia tab and its front page card. The chart SHALL span that
period on one time axis shared by all its data and SHALL draw daily values at every period (no
weekly bars). With Kaikki the axis SHALL start at the first day with data among the shown data
whose data has loaded, and MAY move earlier when slower data finishes loading; with Kaikki and no
data in any shown data the axis SHALL be only today and the chart SHALL read "Ei tietoja tällä
jaksolla". A chart tab SHALL load the food and steps it shows the same way the Energia and
Askeleet tabs do, and SHALL show the same history notice.

#### Scenario: Kaikki start
- **GIVEN** Kaikki and steps that load later than the weights
- **THEN** the axis starts at the first day with data of the loaded data and may move earlier when
  the steps load

#### Scenario: Kaikki with nothing
- **GIVEN** Kaikki and no weights, food or steps
- **THEN** the axis is just today and the chart reads "Ei tietoja tällä jaksolla"

#### Scenario: Oma period
- **WHEN** the user taps Oma on the chart's tab and picks 1.9.–15.10.
- **THEN** the chart spans those days and the chart's front page card shows them as its period

### Requirement: Chart data without values
A chart SHALL NOT ask for any permission by itself. In the Allekkain layout a panel whose data has
no values in the period SHALL keep its place and height and show one centred line instead of the
drawing ("Ei painoja tällä jaksolla", "Ei ruokakirjauksia tällä jaksolla", "Ei askeleita tällä
jaksolla"); a panel without its permission SHALL show the reason of its own tab with its grant
button; a panel still loading SHALL show a progress indicator; the other panels SHALL be
unaffected. In the Päällekkäin layout such data SHALL be left out of the plot and named in a line
under it (the same texts, with the grant button for a missing permission); when no shown data has
values the plot SHALL be replaced by "Ei tietoja tällä jaksolla". Data without permission SHALL be
left out of the readout.

#### Scenario: No steps permission, stacked
- **GIVEN** the steps permission is not granted and a chart with all three data, Allekkain
- **WHEN** its tab is shown
- **THEN** the weight and energy panels are drawn and the steps panel, at the same height, shows the
  reason and a grant button

#### Scenario: No food, overlaid
- **GIVEN** nothing is logged in the period and a chart with Paino and Energia, Päällekkäin
- **THEN** the plot draws the weights and a line under it reads "Ei ruokakirjauksia tällä jaksolla"

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
