## ADDED Requirements

### Requirement: Custom charts
The user SHALL be able to make any number of own charts. A chart SHALL have a name (1 to 24
characters, not blank), one or more shown data out of Paino, Energia and Askeleet (no BMI), for
each shown data a style, Pylväät (bars) or Viiva (line), and a layout, Allekkain (stacked panels)
or Päällekkäin (overlaid). The data SHALL always be drawn and listed in the order Paino, Energia,
Askeleet. Each chart SHALL be one definition that is shown as its own Historia tab and, when the
user adds it there, as a front page card; a change to it SHALL show in both, and deleting it SHALL
remove both. The charts SHALL be remembered between app starts and SHALL NOT be part of the data
file or the backup file.

What each style draws, over the chart's period:
- Paino, Viiva: the period's weights not marked "?" as a line with the weight scale of the weight
  chart, dots when they fit. Paino, Pylväät: one bar per day with weights (per week over 90 days)
  at the average of its weights not marked "?", drawn up from the bottom of the same weight scale.
- Energia, Pylväät: the Energia tab's bars (kcal per logged day, weekly over 90 days), with the
  treats' share in its tone in the Allekkain layout. Energia, Viiva: the Energia tab's 7-day average
  line; over 90 days a line through the weekly bars' values at the weeks' centres.
- Askeleet, Pylväät: the Askeleet tab's bars. Askeleet, Viiva: the Askeleet tab's 7-day average
  line; over 90 days a line through the weekly values.
- Bars SHALL start at zero (Paino excepted, as above); days without data SHALL be gaps.

#### Scenario: Two charts
- **GIVEN** the user made "Paino ja ruoka" (Paino line, Energia bars) and "Liike" (Askeleet bars)
- **THEN** Historia has the tabs Paino, BMI, Energia, Askeleet, Paino ja ruoka, Liike and Uusi kaavio

#### Scenario: One definition
- **GIVEN** "Liike" is on the front page and in Historia
- **WHEN** the user switches its Askeleet to Viiva in the editor
- **THEN** both the card and the tab draw a line

#### Scenario: Energy line
- **GIVEN** a chart with Energia as Viiva and the period 1 kk
- **THEN** the chart draws the Energia tab's 7-day average line and no kcal bars

#### Scenario: Weight bars
- **GIVEN** a chart with Paino as Pylväät and two weights on 12.11. (82,0 and 82,4 kg)
- **THEN** 12.11. has one bar at 82,2 kg on the weight scale

### Requirement: Custom chart tabs
Historia's tab row SHALL show the fixed tabs Paino, BMI, Energia and Askeleet, then one tab per own
chart named after it (in the order they were made), then a last tab "Uusi kaavio". Historia SHALL
open on Paino. An own chart's tab SHALL show the chart for Historia's chosen period with a
"Muokkaa" button that opens the chart editor, and SHALL show no statistics and no list under it.
The "Uusi kaavio" tab SHALL show one line on what own charts are and a button "Uusi kaavio" that
opens the editor for a new chart. After a new chart is saved Historia SHALL show its tab; after a
chart is deleted Historia SHALL show the tab before it.

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

### Requirement: Chart editor
The chart editor SHALL be a dialog with: a name field; one row per data (Paino, Energia, Askeleet)
with a switch and a two-way choice Pylväät / Viiva that is enabled only while the data is on; the
layout choice Allekkain / Päällekkäin, each with one line saying what it draws; and the buttons
Peruuta and Tallenna. The last data that is on SHALL NOT be switchable off. Tallenna SHALL be
disabled while the name is blank. A new chart SHALL start with the name "Kaavio <n>" (the lowest n
not used by another chart's name), all three data on, Paino as Viiva, Energia and Askeleet as
Pylväät, and Allekkain. For an existing chart the editor SHALL also show "Poista kaavio", which
asks "Poistetaanko kaavio <nimi>? Se poistuu myös etusivulta." with Peruuta and Poista. Peruuta,
back and tapping outside SHALL close the editor without saving and without asking. A data switched
off SHALL keep its chosen style for when it is switched on again while the editor is open.

#### Scenario: New chart defaults
- **GIVEN** a chart named "Kaavio 1" exists
- **WHEN** the user taps Uusi kaavio
- **THEN** the editor shows "Kaavio 2", all three data on, Paino Viiva, Energia and Askeleet
  Pylväät, and Allekkain

#### Scenario: Rename
- **WHEN** the user changes the name to "Ilta" and taps Tallenna
- **THEN** the tab and the front page card read "Ilta"

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
In the Allekkain layout each shown data SHALL have its own panel with its own scale and unit,
stacked top to bottom in the order Paino, Energia, Askeleet on one shared time axis, with one date
row under the whole stack. No panel SHALL have a second value axis. On a Historia tab each panel
SHALL be 112 dp high for three panels, 136 dp for two and 160 dp for one; each panel SHALL show the
top of its scale with its unit. Over 90 days one "viikoittain" note SHALL sit above the panels, and
Energia's legend (food, treats, average line as drawn) SHALL be one line under its panel.

#### Scenario: Three panels
- **GIVEN** weights, food and steps in the last 30 days and a chart with all three data, Allekkain
- **WHEN** the user opens its tab for 1 kk
- **THEN** three panels are stacked, weight, energy, steps, over the same 30 days, with one date row under the stack

#### Scenario: Two panels
- **GIVEN** a chart with Paino and Askeleet, Allekkain
- **THEN** two panels of 136 dp are shown on the same axis

### Requirement: Overlaid layout
In the Päällekkäin layout all shown data SHALL be drawn in one plot on one time axis, each with its
own scale: bars behind lines; with two or three bar data the bars of one day (or week) SHALL stand
side by side in its slot. Each data SHALL have its own tone (Paino secondary, Energia primary,
Askeleet tertiary; Energia without the treats' split) and a legend under the plot SHALL name every
data with its tone and its shape. The first shown data SHALL have its value axis on the left and the
second on the right, each axis labelled with numbers and unit in that data's tone; a third data SHALL
have no axis and its legend entry SHALL name its scale ("Askeleet 0–12 000"). With one data the
plot SHALL look like the Allekkain layout's single panel. On a Historia tab the plot SHALL be 200 dp
high.

#### Scenario: Weight and energy overlaid
- **GIVEN** a chart with Paino (Viiva) and Energia (Pylväät), Päällekkäin
- **THEN** one plot shows the kcal bars with the weight line over them, a kg axis on the left, a kcal
  axis on the right, and a legend naming both

#### Scenario: Three overlaid
- **GIVEN** a chart with all three data, Päällekkäin, Energia and Askeleet as Pylväät
- **THEN** each day has an energy bar and a steps bar side by side, the weight line is over them,
  kg is on the left axis, kcal on the right, and the legend reads the steps scale

### Requirement: Chart crosshair
On an own chart's tab, tapping the chart, or pressing and holding and then dragging sideways, SHALL
place one vertical line at that day through the whole chart and SHALL show above it one readout: the
date and, for each shown data with access, its value (weight in kg or "ei painoa"; kcal or "ei
kirjauksia"; steps or "ei tietoa"). The weight of a day SHALL be its last weight not marked "?".
Over 90 days the line SHALL snap to the week and sit at its centre, and the readout SHALL read "vk 46
(10.–16.11.)" with the week's average weight (without "?" weights), the average kcal per logged
day and the average steps per day with steps, today left out, and "ei tietoa" for a part with no
days. Today SHALL show its running values without a mark. After a press-and-hold the drag SHALL own
both directions until release. Tapping the chart SHALL never open anything. Tapping the readout
SHALL remove the crosshair; leaving the tab, a period change, saving the chart in the editor and
leaving Historia SHALL remove it; a data reload SHALL keep it. A plain sideways swipe or fling
SHALL switch the tab as on the other tabs and SHALL NOT place a crosshair. For TalkBack the chart
SHALL offer "Edellinen päivä" and "Seuraava päivä", starting from the newest day, and the readout
SHALL be a polite live region updated only on a tap, a drop or one of those actions.

#### Scenario: Tap a day
- **GIVEN** 1 kk with all three data shown
- **WHEN** the user taps a day with all three
- **THEN** a line crosses the chart and the readout shows that date, the weight, the kcal and the steps

#### Scenario: Day without data
- **WHEN** the user taps a day with weight but no food and no steps
- **THEN** the readout shows the weight, "ei kirjauksia" for kcal and "ei tietoa" for the steps

#### Scenario: Scrub
- **WHEN** the user presses and holds on the chart and drags sideways and up or down
- **THEN** the line and the readout follow the finger day by day and the page does not scroll until release

#### Scenario: Week readout
- **GIVEN** a period over 90 days
- **WHEN** the user taps a week
- **THEN** the line is at the week's centre and the readout reads "vk 46 (10.–16.11.)" with the
  week's averages

#### Scenario: Swipe still switches tabs
- **WHEN** the user flings the chart sideways without holding
- **THEN** Historia switches tab and no crosshair appears

#### Scenario: Clearing
- **GIVEN** a crosshair
- **WHEN** the user taps the readout, or changes the period or the tab
- **THEN** the crosshair is removed

#### Scenario: Step through days
- **GIVEN** TalkBack on the chart
- **WHEN** the user runs "Edellinen päivä" with no crosshair
- **THEN** the newest day is chosen and the readout is announced

### Requirement: Chart time axis
An own chart SHALL span the chosen period on one time axis shared by all its data; over 90 days
bars SHALL be weekly and partial first and last weeks SHALL be clipped to the period. With Kaikki
the axis SHALL start at the first day with data among the shown data whose data has loaded, and
MAY move earlier when slower data finishes loading. A chart tab SHALL load the food and steps it
shows the same way the Energia and Askeleet tabs do, and SHALL show the same history notice.

#### Scenario: Kaikki start
- **GIVEN** Kaikki and steps that load later than the weights
- **THEN** the axis starts at the first day with data of the loaded data and may move earlier when
  the steps load

#### Scenario: Weekly bars
- **WHEN** the period is over 90 days
- **THEN** kcal and steps bars are weekly, one "viikoittain" note is shown, and the partial first
  and last weeks are clipped to the period

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
