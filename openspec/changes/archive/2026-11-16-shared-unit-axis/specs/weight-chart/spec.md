## ADDED Requirements

### Requirement: Shared scale for the same unit
In an own chart, drawn data with the same unit (today only Syöty and Kulutus, kcal) SHALL share
one scale in both layouts, on the Historia tab and the front page card, fitted by the kcal rule of
"Custom charts" to the highest day of any of them in the period, today included. Data with other
units SHALL keep their own scales. Only drawn data count, so the scale follows what is drawn at the
moment. Average lines SHALL sit on the shared scale in their own data's tone.

#### Scenario: Eaten and burned compare
- **GIVEN** a chart with Syöty (Pylväät) and Kulutus (Viiva), highest Syöty day 2 100 kcal and
  highest Kulutus day 2 800 kcal
- **THEN** both use the scale 0–3 500 kcal, so a 1 750 kcal Syöty bar reaches half way, as does a
  1 750 kcal Kulutus point

#### Scenario: Burned without values
- **GIVEN** the same chart and no Kulutus in the period
- **THEN** Syöty's scale is fitted to Syöty alone (0–2 500 kcal)

#### Scenario: Burned without permission
- **GIVEN** a chart with Paino, Syöty and Kulutus, Päällekkäin, and no permission for Kulutus
- **THEN** the right axis is Syöty's own, in Syöty's tone, fitted to Syöty alone

#### Scenario: Average lines on the shared scale
- **GIVEN** a chart with Syöty and Kulutus, Päällekkäin, Keskiarvo on
- **THEN** two dashed lines are drawn on the one kcal scale, each in its data's tone

#### Scenario: Different units stay apart
- **GIVEN** a chart with Syöty and Askeleet
- **THEN** each keeps its own scale, as before

## MODIFIED Requirements

### Requirement: Stacked layout
In the Allekkain layout each shown data SHALL have its own panel with its own scale (data with the
same unit share one scale, see "Shared scale for the same unit"), stacked top to bottom in the
order Paino, Energia, Kulutus, Askeleet on one shared time axis. Axes and legend SHALL follow
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

#### Scenario: Same unit, same scale
- **GIVEN** a chart with Syöty and Kulutus, Allekkain, highest Syöty day 2 100 kcal and highest
  Kulutus day 2 800 kcal
- **THEN** both panels read `0 kcal`, `1 750 kcal`, `3 500 kcal`

### Requirement: Overlaid layout
In the Päällekkäin layout all shown data SHALL be drawn in one plot on one time axis, each with its
own scale except that data with the same unit share one (see "Shared scale for the same unit"):
bars behind lines, average lines over the bars; with two to four bar data the bars of one day SHALL
stand side by side in its slot. Each data SHALL have its own tone (Paino secondary, Energia primary,
Askeleet tertiary; Energia without the treats' split) and a legend under the plot SHALL name every
drawn data with its tone and its shape. Value axes SHALL go to scales, not to data: the first two
scales of the drawn data, in the data order, SHALL have their value axis, the first on the left
and the second on the right, each labelled at bottom, middle and top with the unit; a scale used
by one data SHALL be labelled in that data's tone, a shared scale always in the neutral axis tone
(onSurfaceVariant), also when its data have the same chosen colour. Data on a third scale SHALL have no axis at all and SHALL be recognised by
their colour and legend name only. With one data drawn the plot SHALL look exactly like the
Allekkain layout's single panel; with two drawn data on one shared scale the plot SHALL have that
one axis on the left, none on the right, and the plot reaching the right edge as with one data. On a Historia tab the plot SHALL be 200 dp high. On a
big front page card the scale tops SHALL follow the same axes (one top for a shared scale).

#### Scenario: Weight and energy overlaid
- **GIVEN** a chart with Paino (Viiva) and Energia (Pylväät), Päällekkäin
- **THEN** one plot shows the kcal bars with the weight line over them, a kg axis on the left, a kcal
  axis on the right, and a legend naming both

#### Scenario: Three overlaid
- **GIVEN** a chart with Paino, Energia and Askeleet, Päällekkäin, Energia and Askeleet as Pylväät
- **THEN** each day has an energy bar and a steps bar side by side, the weight line is over them,
  kg is on the left axis, kcal on the right, and steps have no scale, only their colour and the legend name

#### Scenario: Eaten and burned share the right axis
- **GIVEN** a chart with Paino, Syöty and Kulutus, Päällekkäin
- **THEN** kg is on the left axis, and Syöty and Kulutus are both drawn on one kcal axis on the
  right, labelled in the neutral tone

#### Scenario: Only eaten and burned
- **GIVEN** a chart with Syöty and Kulutus, Päällekkäin
- **THEN** one kcal axis is on the left in the neutral tone, there is no right axis and the plot
  reaches the right edge

#### Scenario: Eaten, burned and steps
- **GIVEN** a chart with Syöty, Kulutus and Askeleet, Päällekkäin
- **THEN** the shared kcal axis is on the left in the neutral tone and the steps axis on the right
  in the steps tone

#### Scenario: Shared scale frees the second axis
- **GIVEN** a chart with all four data, Päällekkäin
- **THEN** kg is on the left, the shared kcal axis on the right, and steps have no scale

#### Scenario: Axes go to what is drawn
- **GIVEN** a chart with Paino, Energia and Askeleet, Päällekkäin, and no weights in the period
- **THEN** the kcal axis is on the left and the steps axis on the right

#### Scenario: One data drawn
- **GIVEN** a chart with Paino, Syöty and Askeleet, Päällekkäin, and only steps have values
- **THEN** the plot looks like a stacked single panel with the steps axis on the left

#### Scenario: Only today logged
- **GIVEN** 1 vk with food logged only today
- **WHEN** the Energia tab is shown
- **THEN** today's bar is drawn and there is no average line and no "keskiarvo" entry

#### Scenario: One weight
- **GIVEN** an own chart with Paino (Viiva), Keskiarvo on, and one weight of 82,0 kg in the period
- **THEN** the dashed line runs at 82,0 kg through the dot

### Requirement: Kulutus in own charts
Own charts SHALL offer Kulutus as a fourth data, drawn and listed in the order Paino, Syöty,
Kulutus, Askeleet. Kulutus, Pylväät: one bar per day with data, its height the day's total; in the
Allekkain layout split into lepo (light tone) and aktiivinen (full tone) as on the Kulutus tab, in
Päällekkäin one tone. Kulutus, Viiva: a line through each day's total. Its scale SHALL follow the
Syöty rule (`ceil(highest × 1,1 / 500) × 500` kcal, at least 500) and SHALL be shared with Syöty
when both are drawn (see "Shared scale for the same unit"). With Keskiarvo on, its line is
the average total per day with data, today left out. An own chart SHALL hold one to four data; in
Päällekkäin up to four bar data stand side by side in a day's slot and only the first two scales
have an axis (see "Overlaid layout"). The crosshair readout SHALL show Kulutus as "2 300 kcal · aktiivinen 450 kcal" (a
day without a split: "2 300 kcal"), "ei tietoa" without a value. Its Allekkain panel without values
SHALL read "Ei kulutustietoja tällä jaksolla". A new chart SHALL start with Kulutus off.

#### Scenario: Eaten and burned together
- **GIVEN** a chart with Syöty (Pylväät) and Kulutus (Viiva), Allekkain
- **THEN** a Syöty panel and a Kulutus panel are stacked on one time axis, both on the same kcal scale

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
and in the data's tone in Päällekkäin, across the period at its average on its scale (shared with a data of the same unit, see "Shared scale for the same unit"): Energia the average kcal per
logged day and Askeleet the average steps per day with steps, both with today left out when it is in the period; Paino the
average of each day's last weight not marked "?", today included (a weight is final). A line SHALL be drawn only for a data that is drawn
(not loading, not without permission) and has a value to average; a Päällekkäin data
without a value axis SHALL still get its line. The line SHALL be drawn on the Historia tab and on the
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

### Requirement: Chart axes
The Paino, BMI, Energia and Askeleet charts and the own charts in Historia SHALL share one axis
rule. The y-axis SHALL
show exactly three labelled values with a faint guideline each: the bottom, the middle and the top.
For bars the bottom SHALL be 0 and the top a round value above the highest bar or line point (steps
in steps of 2 000, kcal in steps of 500), so the middle is a round value too. For a weight or BMI
line the bottom and top SHALL be the ends of the weight scale (which may start above zero) and the
middle their midpoint, shown with at most one decimal. The labels SHALL sit left of the plot (an
overlaid chart's second scale on the right, see "Overlaid layout"). The x-axis SHALL show 3 to 5 date labels at whole-day
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

### Requirement: Data colours in own charts
Each data of an own chart SHALL have a colour: "Oletus" (the tone it has today, from the app's
theme; Kulutus's Oletus is oranssi) or one of eight fixed colours: sininen, vihreä, oranssi,
punainen, violetti, turkoosi, keltainen, harmaa, each with a light-theme and a dark-theme shade. A
chart saved before colours existed SHALL read as Oletus for every data, so it looks unchanged. The
colour SHALL be used for the data's bars or line, its value axis labels in Päällekkäin when the scale is its own (a shared scale's labels are neutral, see "Overlaid layout"), its legend
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
