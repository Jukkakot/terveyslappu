## MODIFIED Requirements

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

## ADDED Requirements

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
