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
The Historia chart's y-axis labels SHALL show the unit (`98 kg`), and every weight and change in
the period statistics SHALL show `kg`. BMI has no unit and its axis labels SHALL be bare numbers under a "BMI" chart title.

#### Scenario: Axis labels
- **WHEN** Historia shows a chart
- **THEN** its y-axis labels read like `96 kg`, `98 kg`

#### Scenario: Statistics
- **WHEN** the period statistics are shown
- **THEN** the latest, average, lowest and highest weight read like `82,0 kg`

#### Scenario: Basal metabolic rate axis
- **WHEN** Historia shows any tab
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
Historia's chart and period statistics SHALL show one quantity at a time: Paino or BMI. Tabs with those names SHALL sit above the chart; tapping a tab or swiping the
chart and statistics left or right SHALL switch the quantity, and the chosen period SHALL stay. Both
tabs SHALL always be shown; a quantity that cannot be computed SHALL show, instead of its
chart and statistics, the reason naming the missing owner details and a button to Omat tiedot.
Historia SHALL open on Paino. The weight list under the statistics
SHALL stay the same whatever quantity is shown.

#### Scenario: Swipe to BMI
- **GIVEN** a height is set and Historia shows Paino for 1 kk
- **WHEN** the user swipes the chart to the left
- **THEN** the BMI tab is chosen and the chart and statistics show the BMI of the same 30 days

#### Scenario: No owner details
- **GIVEN** no height is set
- **WHEN** the user opens the BMI tab
- **THEN** it shows "BMI vaatii pituuden" and a button that opens Asetukset at Omat tiedot

#### Scenario: Opens on weight
- **GIVEN** the user left Historia on BMI
- **WHEN** Historia is opened again
- **THEN** it shows Paino

#### Scenario: Tap a tab
- **WHEN** the user taps BMI
- **THEN** the chart and statistics show the BMI of the chosen period

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
