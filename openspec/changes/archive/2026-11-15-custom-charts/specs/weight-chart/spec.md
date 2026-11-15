## ADDED Requirements

### Requirement: Custom charts
The user SHALL be able to make any number of own charts. A chart SHALL have a name (1 to 24
characters, trimmed, not blank; duplicates allowed), one or more shown data out of Paino, Energia
and Askeleet (no BMI), for each shown data a style, Pylväät (bars) or Viiva (line), a layout,
Allekkain (stacked panels) or Päällekkäin (overlaid), and its own time period (see Chart time
axis). The data SHALL always be drawn and listed in the order Paino, Energia, Askeleet. Each chart
SHALL be one definition that is shown as its own Historia tab and, when the user adds it there, as
a front page card; a change to it SHALL show in both, and deleting it SHALL remove both. The charts
SHALL be remembered between app starts and SHALL NOT be part of the data file or the backup file.

A custom chart SHALL draw real values only, never averages: for each day its own value, at every
period. It SHALL NOT draw 7-day average lines and SHALL NOT draw weekly bars. What each style draws:
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
- **THEN** the chart draws a line through those three days' own kcal values, no 7-day average and no bars

#### Scenario: Weight bars
- **GIVEN** a chart with Paino as Pylväät and two weights on 12.11. (82,0 in the morning and 82,4 in the evening)
- **THEN** 12.11. has one bar at 82,4 kg on the weight scale, the day's last weight, not 82,2

#### Scenario: Long period stays daily
- **GIVEN** a chart with Energia as Pylväät and the period 1 v
- **THEN** one thin bar is drawn per logged day, not weekly bars

#### Scenario: Steps scale
- **GIVEN** a chart with Askeleet and a highest day of 11 400 steps
- **THEN** the scale's top reads 14 000

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
layout choice Allekkain / Päällekkäin, each with one line saying what it draws; and the buttons
Peruuta and Tallenna. The editor SHALL NOT hold the period (it is chosen with the chips on the
chart's tab; saving keeps it). The last data that is on SHALL NOT be switchable off. Tallenna SHALL
be disabled while the name is blank; the name SHALL accept at most 24 characters (an emoji counts
as one) and SHALL be saved trimmed. A new chart SHALL start with the name "Kaavio <n>" (the lowest
n for which no other chart's name equals "Kaavio <n>", ignoring case and surrounding spaces), all
three data on, Paino as Viiva, Energia and Askeleet as Pylväät, Allekkain, and the period 1 kk. For
an existing chart the editor SHALL also show "Poista kaavio", which asks "Poistetaanko kaavio <nimi>?
Se poistuu myös etusivulta." with Peruuta and Poista. Peruuta, back and tapping outside SHALL close
the editor without saving and without asking. A data switched off SHALL keep its chosen style for
when it is switched on again while the editor is open.

#### Scenario: New chart defaults
- **GIVEN** a chart named "Kaavio 1" exists
- **WHEN** the user taps Uusi kaavio
- **THEN** the editor shows "Kaavio 2", all three data on, Paino Viiva, Energia and Askeleet
  Pylväät, and Allekkain, and the saved chart's period is 1 kk

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
- **WHEN** the user taps Poista kaavio and confirms with Poista
- **THEN** its tab and its card are gone and Historia shows the tab before it

#### Scenario: Cancel
- **GIVEN** the user changed the layout in the editor
- **WHEN** the user presses back
- **THEN** the chart is as before and nothing is asked

### Requirement: Stacked layout
In the Allekkain layout each shown data SHALL have its own panel with its own scale, stacked top to
bottom in the order Paino, Energia, Askeleet on one shared time axis. Axes and legend SHALL follow
the same axis and legend rules as Historia's other charts: each panel a left column of value labels at its bottom, middle and top with the unit, one
row of 4 to 5 dates under the whole stack, and one legend under the dates naming every drawn line
and bar. No panel SHALL have a second value axis and there SHALL be no "viikoittain" or "7 pv
keskiarvo" note. On a Historia tab each panel SHALL be 112 dp high for three panels, 136 dp for two
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
own scale: bars behind lines; with two or three bar data the bars of one day SHALL stand side by
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

## MODIFIED Requirements

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
