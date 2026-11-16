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
A weight's date, time and value SHALL be shown through the chart's marker card (see "Crosshair on
every Historia chart"): the card's head is the date and the weight's time, the date with the year
when the weight is not from the current year, and its row is the series name and the value.

#### Scenario: Tap a dot
- **WHEN** the user taps the dot of 82.6 kg on Sunday 8.11. this year at 7.05
- **THEN** the marker card reads "Su 8.11. · 7.05" with the row "paino 82,6 kg"

#### Scenario: Tap an old dot
- **WHEN** the user taps the dot of 97.6 kg on Sunday 15.9.2024 at 8.33
- **THEN** the marker card reads "Su 15.9.2024 · 8.33" with the row "paino 97,6 kg"

#### Scenario: Tap a BMI dot
- **GIVEN** the BMI chart is shown and the height is 180 cm
- **WHEN** the user taps the dot of 82.0 kg on Sunday 8.11. this year at 7.05
- **THEN** the marker card reads "Su 8.11. · 7.05" with the row "BMI 25,3"

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
"Uusi kaavio" tab (see Custom chart tabs); tapping a tab, or swiping left or right anywhere on the
page outside a chart's plot area (the range line, statistics, list, messages and empty states),
SHALL switch the tab, and the chosen period SHALL stay. A sideways gesture inside a plot area
belongs to the chart (selection, zoom and pan, see "Chart crosshair" and "Chart zoom"). All four
fixed tabs SHALL always be shown; a quantity that cannot be computed SHALL show, instead of its
chart and statistics, the reason naming what is missing and a button to fix it (Omat tiedot for
BMI, the nutrition permissions for Energia, the steps permission for Askeleet). Historia SHALL
open on Paino. On Paino and BMI the list under the statistics SHALL be the weight list; on Energia
it SHALL be the list of logged days (see Energia list); on Askeleet it SHALL be the list of days
with steps (see Askeleet list); an own chart's tab and the "Uusi kaavio" tab SHALL have no list.

#### Scenario: Swipe to BMI
- **GIVEN** a height is set and Historia shows Paino for 1 kk
- **WHEN** the user swipes the statistics to the left
- **THEN** the BMI tab is chosen and the chart and statistics show the BMI of the same 30 days

#### Scenario: Swipe to Energia
- **GIVEN** Historia shows BMI for 1 vk
- **WHEN** the user swipes the statistics to the left
- **THEN** the Energia tab is chosen for the same 7 days

#### Scenario: Swipe on the plot stays in the chart
- **GIVEN** Historia shows Paino for 1 kk
- **WHEN** the user drags sideways inside the plot
- **THEN** the tab stays Paino and the chosen day follows the finger

#### Scenario: No owner details
- **GIVEN** no height is set
- **WHEN** the user opens the BMI tab
- **THEN** it shows "BMI vaatii pituuden" and a button that opens Asetukset at Omat tiedot

#### Scenario: Swipe to Askeleet
- **GIVEN** Historia shows Energia for 1 kk
- **WHEN** the user swipes the statistics to the left
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
- **WHEN** the user swipes the statistics to the left
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
On the Energia tab the list under the statistics SHALL show the period's logged days, newest first
and grouped as in "Grouped day lists", each with its date, total kcal and treat share; tapping a day
SHALL open the Ruoka screen on that day.

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
first and grouped as in "Grouped day lists", each with its date and steps. A row SHALL NOT open
anything.

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
middle their midpoint, shown with at most one decimal. The labels SHALL sit left of the plot (an
overlaid chart's second data on the right). The x-axis SHALL show 3 to 5 date labels at whole-day
positions chosen by the chart's own label spacing (evenly stepped, not necessarily at the period's
first and last day), formatted as day and month, or month and year when the period spans 365 days
or more (1 v included); a period of 7 days or fewer (1 vk, or a short Oma range) SHALL instead
label every day with its two-letter weekday (Ma, Ti, Ke, To, Pe, La, Su) under that day's bar or
point, with no dates and no special mark for today. Front page cards keep their own date text. The
number of labels (5, 4 or 3) SHALL be the largest that fits without overlap at the measured label
widths, and labels SHALL NOT overlap or be cut at the chart's edges on a 360 dp wide phone at font
scale 1.3. Axis, legend and marker text SHALL follow the system font size. While zoomed in (see
"Chart zoom") the labels SHALL follow the visible days by the same rule.

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
- **THEN** fewer dates are shown rather than overlapping labels, no two label areas intersect and
  no label is cut at an edge

#### Scenario: Month
- **WHEN** the period is 1 kk and five dates fit
- **THEN** five day-and-month labels are shown, evenly stepped by whole days across the 30 days

#### Scenario: Several years
- **GIVEN** weights from 24.9.2022 to today
- **WHEN** Paino is shown with Kaikki
- **THEN** the x-axis shows 3 to 5 labels like "9/2022", "11/2024"

#### Scenario: One year
- **WHEN** the period is 1 v
- **THEN** the date labels read month and year

#### Scenario: Same dates on both chart kinds
- **WHEN** the period is 3 kk on Paino and on Energia
- **THEN** both label whole days in the same day-and-month format, stepped evenly by the same rule

#### Scenario: Large system font
- **GIVEN** font scale 1.3
- **WHEN** a Historia chart is shown
- **THEN** its axis labels, legend and marker text are larger than at font scale 1.0

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
axis). The data SHALL always be drawn and listed in the order Paino, Energia (Syöty), Kulutus, Askeleet. Each chart
SHALL be one definition that is shown as its own Historia tab and, when the user adds it there, as
a front page card; a change to it SHALL show in both, and deleting it SHALL remove both. The charts
SHALL be remembered between app starts and SHALL NOT be part of the data file or the backup file.

A custom chart SHALL draw real values only: for each day its own value, at every period. It SHALL
NOT draw moving average lines and SHALL NOT draw weekly bars. When the chart's Keskiarvo is on,
each drawn data SHALL also get one flat dashed line (1 dp, the front page cards' dash); on a
Historia tab it is drawn over the bars and lines, on a front page card over the bars and under the
value lines; in the neutral line tone (onSurfaceVariant) in Allekkain
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
- Bars SHALL start at zero (Paino excepted, as above) and take the same share of every day's slot,
  so over long periods they are thin (on a front page card at least 1 dp wide); days without data
  SHALL be gaps in bars, and a line SHALL join the days that have a value.
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
The chart editor SHALL be a dialog with: a name field; one row per data (Paino, Energia (Syöty), Kulutus, Askeleet)
with a switch and a two-way choice Pylväät / Viiva that is enabled only while the data is on; the
layout choice Allekkain / Päällekkäin, each with one line saying what it draws; a switch "Keskiarvo" with one
line "Vaakaviiva jakson keskiarvon kohdalla"; and the buttons
Peruuta and Tallenna. The editor SHALL NOT hold the period (it is chosen with the chips on the
chart's tab; saving keeps it). The last data that is on SHALL NOT be switchable off. Tallenna SHALL
be disabled while the name is blank; the name SHALL accept at most 24 characters (an emoji counts
as one) and SHALL be saved trimmed. A new chart SHALL start with the name "Kaavio <n>" (the lowest
n for which no other chart's name equals "Kaavio <n>", ignoring case and surrounding spaces), all
Paino, Energia and Askeleet on (Kulutus off), Paino as Viiva, Energia and Askeleet as Pylväät, Allekkain, Keskiarvo off, and the period 1 kk.
A chart saved before Keskiarvo existed SHALL read as Keskiarvo off. For
an existing chart the editor SHALL also show a red "Poista kaavio" on the left, which deletes the
chart at once, without a question, and shows "Kaavio poistettu · Kumoa"; Kumoa SHALL bring the
chart back with its name, data, layout, Keskiarvo, period and its front page card in its place. Peruuta, back and tapping outside SHALL close
the editor without saving and without asking. A data switched off SHALL keep its chosen style for
when it is switched on again while the editor is open.

#### Scenario: New chart defaults
- **GIVEN** a chart named "Kaavio 1" exists
- **WHEN** the user taps Uusi kaavio
- **THEN** the editor shows "Kaavio 2", Paino, Energia and Askeleet on and Kulutus off, Paino Viiva, Energia and Askeleet
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
bottom in the order Paino, Energia, Kulutus, Askeleet on one shared time axis. Axes and legend SHALL follow
the same axis and legend rules as Historia's other charts: each panel value labels at its bottom,
middle and top with the unit on the left, the panels' plots starting and ending at the same x so
the same day lines up in every panel, one row of dates (see "Chart axes") under the whole stack,
and one legend under the dates naming every drawn line and bar. No panel SHALL have a second value
axis and there SHALL be no "viikoittain" note. On a Historia tab each panel SHALL be 112 dp high for
three panels, 136 dp for two and 160 dp for one.

#### Scenario: Three panels
- **GIVEN** weights, food and steps in the last 30 days and a chart with all three data, Allekkain
- **WHEN** the user opens its tab for 1 kk
- **THEN** three panels are stacked, weight, energy, steps, over the same 30 days, each with its value labels, one date row and one legend under the stack

#### Scenario: Two panels
- **GIVEN** a chart with Paino and Askeleet, Allekkain
- **THEN** two panels of 136 dp are shown on the same axis

#### Scenario: Days line up
- **GIVEN** a chart with Paino and Askeleet, Allekkain, whose value labels differ in width
- **THEN** the same day sits at the same x in both panels

### Requirement: Overlaid layout
In the Päällekkäin layout all shown data SHALL be drawn in one plot on one time axis, each with its
own scale: bars behind lines, average lines over the bars; with two to four bar data the bars of one day SHALL stand side by
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
On the plot areas of an own chart's tab (and, by "Crosshair on every Historia chart", of every
Historia chart), a tap or a press-and-hold SHALL choose the day of the nearest drawn value and a sideways drag inside
the plot SHALL move the choice with the finger (scrub) while the chart is not zoomed in; the chosen
day SHALL be marked on every panel that draws data, and one marker card SHALL show over the plot at
that day. The card SHALL hold a head line with the date and one row per shown data with access: a
dot in the data's colour, its name and that day's own value with its unit (weight in kg, the day's
last weight not marked "?", or "ei painoa"; kcal or "ei kirjauksia"; steps or "ei tietoa"). There
SHALL be no week form and no averages. The card SHALL be placed beside the chosen day and SHALL
stay fully inside the chart's area, also at the first and last day. Buttons inside a panel (such as
a permission button) SHALL stay buttons and a tap outside the drawn plot areas SHALL choose nothing.
Today SHALL show its running values without a mark. Tapping the chosen value again, or a tap on the
plot far from any value, SHALL remove the choice; leaving the tab, a period change, saving the
chart in the editor and leaving Historia SHALL remove it; a data reload SHALL keep it, and it SHALL
be removed only when its day is no longer on the axis. For TalkBack the chart SHALL offer
"Edellinen päivä" and "Seuraava päivä", the first use starting from today, and the card's text SHALL
be announced as a polite live region only on a tap, the end of a drag or one of those actions.

#### Scenario: Tap a day
- **GIVEN** 1 kk with all three data shown
- **WHEN** the user taps a day with all three
- **THEN** the day is marked and the card shows that date and the rows "paino 82,4 kg", "ruoka
  1 900 kcal" and "askeleet 6 420 askelta", each with its colour dot

#### Scenario: Day without data
- **WHEN** the user taps a day with weight but no food and no steps
- **THEN** the card shows the weight row, "ei kirjauksia" in the food row and "ei tietoa" in the
  steps row

#### Scenario: Last weight of the day
- **GIVEN** two weights on a day, 82,0 and later 82,4, and a third marked "?"
- **WHEN** the user taps that day
- **THEN** the card shows 82,4 kg

#### Scenario: Hold in place
- **WHEN** the user presses and holds on a day and releases without moving
- **THEN** the nearest value's day is chosen and the card shows its values

#### Scenario: Swipe still switches tabs
- **WHEN** the user flings the statistics under the chart sideways
- **THEN** Historia switches tab and no day is chosen

#### Scenario: Scrub
- **WHEN** the user drags sideways inside the plot
- **THEN** the chosen day and the card follow the finger and Historia does not switch tab

#### Scenario: Card at the edge
- **WHEN** the user taps the last day of the period
- **THEN** the card is drawn left of the day, fully inside the chart

#### Scenario: Long period is still daily
- **GIVEN** the period 1 v
- **WHEN** the user taps a day
- **THEN** the card shows that day's own values, not a week's

#### Scenario: Gated panel
- **GIVEN** the steps permission is missing, Allekkain
- **WHEN** the user taps a day
- **THEN** the day is marked in the weight and energy panels only, and the grant button still asks for the permission when tapped

#### Scenario: Clearing
- **GIVEN** a chosen day
- **WHEN** the user taps the chosen value again, or changes the period or the tab
- **THEN** the choice and the card are removed

#### Scenario: Kept on reload
- **GIVEN** a chosen day inside the axis
- **WHEN** the data reloads
- **THEN** the choice and the card stay

#### Scenario: Step through days
- **GIVEN** TalkBack on the chart
- **WHEN** the user runs "Edellinen päivä" with no day chosen
- **THEN** today is chosen and the card's text is announced

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

### Requirement: Eaten and burned energy names
The eaten-energy tab and data, called Energia in this spec, SHALL read "Syöty" where the user sees
it as a tab or data name (Historia tab, own chart editor, own chart legends and readouts, Ohje); a
product's energy field and the food texts ("Ei ruokakirjauksia…", legend "ruoka", "herkut") stay.
The burned-energy tab and data SHALL read "Kulutus". Both are in kcal. Neither SHALL use plus or
minus signs or show a difference between them. Historia's fixed tabs SHALL be Paino, BMI, Syöty,
Askeleet, Kulutus, then the own charts and Uusi kaavio.

#### Scenario: Tab row
- **WHEN** Historia is opened
- **THEN** the tabs read Paino, BMI, Syöty, Askeleet, Kulutus, the own charts and Uusi kaavio

### Requirement: Kulutus in own charts
Own charts SHALL offer Kulutus as a fourth data, drawn and listed in the order Paino, Syöty,
Kulutus, Askeleet. Kulutus, Pylväät: one bar per day with data, its height the day's total; in the
Allekkain layout split into lepo (light tone) and aktiivinen (full tone) as on the Kulutus tab, in
Päällekkäin one tone. Kulutus, Viiva: a line through each day's total. Its scale SHALL follow the
Syöty rule (`ceil(highest × 1,1 / 500) × 500` kcal, at least 500). With Keskiarvo on, its line is
the average total per day with data, today left out. An own chart SHALL hold one to four data; in
Päällekkäin up to four bar data stand side by side in a day's slot and only the first two drawn
data have a scale. The crosshair readout SHALL show Kulutus as "2 300 kcal · aktiivinen 450" (a
day without a split: "2 300 kcal"), "ei tietoa" without a value. Its Allekkain panel without values
SHALL read "Ei kulutustietoja tällä jaksolla". A new chart SHALL start with Kulutus off.

#### Scenario: Eaten and burned together
- **GIVEN** a chart with Syöty (Pylväät) and Kulutus (Viiva), Allekkain
- **THEN** a Syöty panel and a Kulutus panel are stacked on one time axis, each with its kcal scale

### Requirement: Data colours in own charts
Each data of an own chart SHALL have a colour: "Oletus" (the tone it has today, from the app's
theme; Kulutus's Oletus is oranssi) or one of eight fixed colours: sininen, vihreä, oranssi,
punainen, violetti, turkoosi, keltainen, harmaa, each with a light-theme and a dark-theme shade. A
chart saved before colours existed SHALL read as Oletus for every data, so it looks unchanged. The
colour SHALL be used for the data's bars or line, its value axis labels in Päällekkäin, its legend
swatch and its average line in Päällekkäin; Syöty's treat share and Kulutus's lepo part SHALL use a
lighter tone of the same colour. Two data MAY share a colour. The fixed Historia tabs keep their own
tones.

#### Scenario: Own colour
- **GIVEN** a chart with Askeleet as Pylväät
- **WHEN** the user picks punainen for Askeleet in the editor and saves
- **THEN** the steps bars, the legend swatch and (in Päällekkäin) the steps axis labels are red

#### Scenario: Old chart
- **GIVEN** a chart saved before colours existed
- **THEN** it looks as before and the editor shows Oletus for every data

### Requirement: Chart editor colours
In the chart editor each data row (Paino, Syöty, Kulutus, Askeleet) SHALL hold, after its switch
and its Pylväät / Viiva choice, one colour dot (44 dp touch target, named for TalkBack with its
colour) that opens a small picker of nine choices (Oletus and the eight colours, a 3 × 3 grid of
44 dp targets, the chosen one marked with a check and announced "valittu"); picking closes the
picker. The dot SHALL be shown but disabled while the data is off. Peruuta and back discard a
colour change like any other change.

#### Scenario: Pick a colour
- **WHEN** the user taps Askeleet's colour dot and picks vihreä
- **THEN** the picker closes and the dot shows green; Tallenna keeps it

### Requirement: Crosshair on every Historia chart
Every chart in Historia (Paino, BMI, Syöty, Askeleet, Kulutus and the own charts) SHALL answer a
tap and a drag on its plot the way "Chart crosshair" describes for own charts, choosing a day (or a
week, when the bars are weekly). The chosen value SHALL be marked: a line's point with a ring and a
vertical line through the plot, a bar in the highlight tint. The marker card's head SHALL be the
weekday and the date, with the year when not the current year ("Ma 9.11.", "La 15.9.2024"), or for
a weekly bar the week ("Vk 46 · 9.–15.11."), and its rows SHALL be:
- Paino: the head adds the time of that day's last weight not marked "?" ("Su 8.11. · 7.05"), row
  "paino 82,6 kg", or "paino ei painoa"; BMI the same with "BMI 25,3".
- Syöty: "ruoka 1 760 kcal" and "herkut 230 kcal", or "ruoka ei kirjauksia"; a weekly bar "ruoka
  keskim. 1 900 kcal/pv".
- Askeleet: "askeleet 9 202 askelta" or "askeleet ei tietoa"; weekly "askeleet keskim. 8 200
  askelta/pv".
- Kulutus: as in energy-burn "Kulutus tab".
The removal rules and TalkBack actions SHALL be those of "Chart crosshair". The chart SHALL have no
"Avaa päivä" link (user 2026-11-16); a day opens on Ruoka from the Syöty day list. Front page cards
keep their tap (open Historia) and have no marker.

#### Scenario: Tap a steps bar
- **GIVEN** Askeleet with 1 vk
- **WHEN** the user taps Thursday's bar
- **THEN** Thursday's bar takes the highlight tint and the card reads "To 12.11." with the row
  "askeleet 9 202 askelta"

#### Scenario: Old weight
- **GIVEN** Paino with Kaikki
- **WHEN** the user taps 15.9.2024, whose last weight was 97,6 kg at 8.33
- **THEN** the card reads "Su 15.9.2024 · 8.33" with "paino 97,6 kg" and a ring marks the dot

#### Scenario: Tap between weights
- **GIVEN** Paino with weights on 10.11. and 13.11. only
- **WHEN** the user taps 11.11.
- **THEN** the nearest weight, 10.11., is chosen

#### Scenario: Open a day from Syöty
- **WHEN** the user taps a day in the Syöty day list (the chart has no "Avaa päivä" link)
- **THEN** Ruoka opens on that day; back returns to Historia on Syöty

#### Scenario: Weekly bar
- **GIVEN** Syöty with 1 v
- **WHEN** the user taps a week's bar
- **THEN** the card reads "Vk 46 · 9.–15.11." with "ruoka keskim. 1 900 kcal/pv"

### Requirement: Grouped day lists
The day lists of Energia, Askeleet and Kulutus SHALL be grouped by the same rule as the weight list
(weight-entry "Grouped list" and "Open by default"): year, month and week (Monday to Sunday, cut at
month borders), newest first on every level, the days as plain rows under their week, every year,
month and week holding a listed day its own row, the year level left out when all listed days fall
in one year, and a flat list without groups when the period lists at most 14 days. Every group SHALL
be closed when the tab is shown and after a period change; open groups SHALL NOT be remembered after
leaving Historia. There SHALL be no gap rows. Day rows SHALL look and act as before (an Energia day
opens Ruoka; an Askeleet or Kulutus day opens nothing).

A group row SHALL show the group's name as a weight group does ("2024", "Marraskuu 2025", "Vk 45 ·
3.–9.11."), the per-day average of the tab's main value over the group's listed days, the number of
those days ("12 pv"), and the open sign; tapping it SHALL open or close it:
- Energia: "ka 1 850 kcal/pv" and "herkkuja ka 230" (left out when zero).
- Askeleet: "ka 8 400 askelta/pv".
- Kulutus: "ka 2 300 kcal/pv" and "aktiivinen ka 450" (left out when no day of the group has an
  active value).
Details:
- Listed days are the days each list already shows (Energia: days with kcal; Askeleet: days with
  steps; Kulutus: days with burned energy); the 14 counts those.
- Today counts in "N pv" but not in the averages, as in the tab's statistics; a group whose only
  day is today shows today's value (the week bars' fallback).
- Secondary averages use the stats' denominators (treats over all listed days, active over the days
  with an active value), rounded as the stats (whole numbers, grouped thousands); a part that
  rounds to 0 or is missing is left out.
- Group row layout reuses the weight group row: name left, "ka … /pv" right on the first line; the
  secondary part and "N pv" on the second; the open sign; rows indented by depth. A month shows its
  year only when not the current year.
- Each list keeps its own open groups; every list closes all groups on a period change and on a
  tab switch (this also applies to the weight list).
- List weeks are cut at month borders and may differ from the weekly chart bars.
- The crosshair opens no group; empty and permission states stay as they are.

#### Scenario: Numbers in a week group
- **GIVEN** today is Wednesday and Askeleet lists Monday 8 000, Tuesday 10 000 and today 3 000 in a
  period of over 14 listed days
- **THEN** the week's row reads "ka 9 000 askelta/pv" and "3 pv"

#### Scenario: Only today in the week
- **GIVEN** today is Monday with 2 000 kcal logged and the period lists over 14 days
- **THEN** this week's Energia row reads "ka 2 000 kcal/pv" and "1 pv"

#### Scenario: A week stays flat
- **GIVEN** the period 1 vk with seven logged days on Energia
- **THEN** the list shows seven day rows and no group rows

#### Scenario: Months of steps
- **GIVEN** the period 1 v with steps on 200 days, all in 2026
- **WHEN** Askeleet is shown
- **THEN** the top level shows closed month rows, newest first, each with its average steps per day
  and its number of days, and no year row

#### Scenario: Open a week on Energia
- **GIVEN** the period 3 kk with 60 logged days
- **WHEN** the user opens Marraskuu and then "Vk 46 · 9.–15.11."
- **THEN** the week's days show under it, and tapping 12.11. opens Ruoka on that day

#### Scenario: Period change closes groups
- **GIVEN** a month row is open on Kulutus
- **WHEN** the user picks another period
- **THEN** every group is closed

### Requirement: Chart zoom
Every Historia chart SHALL zoom on its time axis with a two-finger pinch or a double tap and SHALL
pan sideways with one finger while zoomed in; the value axes SHALL keep their scale. The stacked
panels of an own chart SHALL zoom and pan together. A period change or leaving the tab SHALL reset
the zoom; zooming SHALL NOT change the period, the statistics or the list.

#### Scenario: Pinch a month
- **GIVEN** Paino for 1 kk
- **WHEN** the user pinches the plot open
- **THEN** fewer days fill the plot, the kg labels stay as they were and the date labels follow the
  visible days

#### Scenario: Panels together
- **GIVEN** an own chart with Paino and Askeleet, Allekkain
- **WHEN** the user zooms the steps panel
- **THEN** the weight panel shows the same days

#### Scenario: Period resets zoom
- **GIVEN** a zoomed Paino chart
- **WHEN** the user picks 3 kk
- **THEN** the chart shows the whole 90 days
