## MODIFIED Requirements

### Requirement: Periods
The chart SHALL offer the periods 1 vk, 1 kk, 3 kk, 1 v, Kaikki and Oma, where Oma opens a calendar
for picking a date range. 1 vk, 1 kk, 3 kk and 1 v SHALL be the last 7, 30, 90 and 365 days, today
included and last on the chart (rolling, not calendar weeks, months or years). The first time 1 vk SHALL be chosen; after that the app SHALL open with the
last chosen period. The periods SHALL be one row of six equal segments directly under the tabs, fully visible at 360 dp
and 130 % text size without scrolling; the chosen segment SHALL be filled in the raised surface
tone with its label in the brand colour. Oma's segment SHALL read "Oma" also when chosen; the chosen
dates show in the range line above the chart ("Period range line"). Tapping Oma SHALL open the
calendar every time, also when Oma is already chosen. For TalkBack the segments SHALL act as one
group of choices with the chosen one announced as selected.

#### Scenario: Change period
- **WHEN** the user taps 1 kk
- **THEN** the chart, stats and list show the last 30 days

#### Scenario: Own range
- **WHEN** the user taps Oma and picks 1.9.–15.10.
- **THEN** the chart, stats and list show that range, the Oma segment is chosen and the range line shows 1.9.–15.10.

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

### Requirement: One quantity at a time
Historia's fixed tabs SHALL show one quantity at a time: Paino, BMI, Syöty, Askeleet or Kulutus.
Tabs with those names SHALL be the topmost row under the top bar, in that order, above the periods
row ("Periods") and the chart, followed by the user's own charts and the "Uusi kaavio" tab (see
Custom chart tabs), reached by scrolling the tab row; at 360 dp and the default text size the five
fixed tabs SHALL be fully visible without scrolling (at bigger text the row may scroll). Tapping a
tab, or swiping left or right anywhere on the page outside a chart's plot area (the range line,
statistics, list, messages and empty states), SHALL switch the tab, and the chosen period SHALL
stay. A sideways gesture inside a plot area belongs to the chart (selection, zoom and pan, see
"Chart crosshair" and "Chart zoom"). All five fixed tabs SHALL always be shown; a quantity that
cannot be computed SHALL show, instead of its chart and statistics, the reason naming what is
missing and a button to fix it (Omat tiedot for BMI, the nutrition permissions for Syöty, the
steps permission for Askeleet, the burned energy permissions for Kulutus). Historia SHALL open on
Paino. On Paino and BMI the list under the statistics SHALL be the weight list; on Syöty it SHALL
be the list of logged days (see Energia list); on Askeleet it SHALL be the list of days with steps
(see Askeleet list); on Kulutus its list of days; an own chart's tab and the "Uusi kaavio" tab
SHALL have no list.

#### Scenario: Swipe to BMI
- **GIVEN** a height is set and Historia shows Paino for 1 kk
- **WHEN** the user swipes the statistics to the left
- **THEN** the BMI tab is chosen and the chart and statistics show the BMI of the same 30 days

#### Scenario: Swipe to Energia
- **GIVEN** Historia shows BMI for 1 vk
- **WHEN** the user swipes the statistics to the left
- **THEN** the Syöty tab is chosen for the same 7 days

#### Scenario: Swipe on the plot stays in the chart
- **GIVEN** Historia shows Paino for 1 kk
- **WHEN** the user drags sideways inside the plot
- **THEN** the tab stays Paino and the chosen day follows the finger

#### Scenario: No owner details
- **GIVEN** no height is set
- **WHEN** the user opens the BMI tab
- **THEN** it shows "BMI vaatii pituuden" and a button that opens Asetukset at Omat tiedot

#### Scenario: Swipe to Askeleet
- **GIVEN** Historia shows Syöty for 1 kk
- **WHEN** the user swipes the statistics to the left
- **THEN** the Askeleet tab is chosen for the same 30 days

#### Scenario: No steps permission
- **GIVEN** the steps permission is not granted
- **WHEN** the user opens the Askeleet tab
- **THEN** it shows the reason and a button that asks for the permission

#### Scenario: Opens on weight
- **GIVEN** the user left Historia on Syöty
- **WHEN** Historia is opened again
- **THEN** it shows Paino

#### Scenario: Tap a tab
- **WHEN** the user taps BMI
- **THEN** the chart and statistics show the BMI of the chosen period

#### Scenario: Swipe past Askeleet
- **GIVEN** an own chart "Liike" and Historia on Askeleet
- **WHEN** the user swipes the statistics to the left twice
- **THEN** the Kulutus tab and then the Liike tab is chosen for the same period

#### Scenario: Tabs first
- **GIVEN** a 360 dp wide phone at the default text size
- **WHEN** Historia opens
- **THEN** Paino, BMI, Syöty, Askeleet and Kulutus are fully visible in the top row, and the periods
  row is under them

### Requirement: Data colours in own charts
Each data of an own chart SHALL have a colour: "Oletus" (its quantity colour, app-appearance
"Quantity colours": Paino mint, Syöty amber, Askeleet blue, Kulutus coral) or one of eight fixed colours: sininen, vihreä, oranssi,
punainen, violetti, turkoosi, keltainen, harmaa, each with a light-theme and a dark-theme shade. A chart saved before colours existed, or with Oletus, SHALL show the quantity colours. The
colour SHALL be used for the data's bars or line, its value axis labels in Päällekkäin when the scale is its own (a shared scale's labels are neutral, see "Overlaid layout"), its legend
swatch and its average line in Päällekkäin; Syöty's treat share and Kulutus's lepo part SHALL use a
lighter tone of the same colour. Two data MAY share a colour. The fixed Historia tabs use the quantity colours.

#### Scenario: Own colour
- **GIVEN** a chart with Askeleet as Pylväät
- **WHEN** the user picks punainen for Askeleet in the editor and saves
- **THEN** the steps bars, the legend swatch and (in Päällekkäin) the steps axis labels are red

#### Scenario: Old chart
- **GIVEN** a chart saved before colours existed
- **THEN** it shows the quantity colours and the editor shows Oletus for every data

### Requirement: Overlaid layout
In the Päällekkäin layout all shown data SHALL be drawn in one plot on one time axis, each with its
own scale except that data with the same unit share one (see "Shared scale for the same unit"):
bars behind lines, average and trend lines over the bars; with two to four bar data the bars of one day SHALL
stand side by side in its slot. Each data SHALL have its own tone (its chosen colour, or with Oletus its quantity colour:
app-appearance "Quantity colours"; Syöty without the treats' split) and a legend under the plot SHALL name every
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

#### Scenario: Trend overlaid
- **GIVEN** a chart with Paino (Viiva) and Askeleet (Pylväät), Päällekkäin, Keskiarvo on, 1 v
- **THEN** one trend line per data is drawn over the bars, each in its data's tone at 60 % opacity
  and mapped through that data's scale

## ADDED Requirements

### Requirement: Statistics layout
Under each fixed tab's chart the statistics SHALL be one hero figure and one row. The hero is a
card tinted with the tab's quantity colour (14 % dark, 10 % light, never by the value) with a
muted name over a big number (app-appearance "Big numbers") and, on Paino and BMI, a second figure
at its right. The row holds the other values in equal columns, each a muted name over its value,
separated by thin dividers, wrapping to a second row only past four values; a single value is still
drawn as a row. Per tab:
- Paino: hero Muutos, right Viikossa; row Keskiarvo, Alin, Ylin, Painoja.
- BMI: hero Viimeisin with its class word in its tone, right Muutos; row Keskiarvo, Alin, Ylin,
  Painoja (for a minor: Viimeisin without the word).
- Syöty: hero Keskiarvo; row Herkut, Päiviä.
- Askeleet: hero Keskiarvo; row Päiviä.
- Kulutus: hero Keskiarvo; row Aktiivinen keskiarvo, Päiviä.
A value that cannot be shown reads "—" (one weight only: Muutos and Viikossa "—"). The values and
their rules stay as in "Period statistics", "Energia statistics", "Askeleet statistics" and
energy-burn "Kulutus tab"; only their layout changes (the BMI's latest moves from a tile to the
hero, Paino shows no latest weight, as today).

#### Scenario: Paino
- **GIVEN** Paino 1 v with a change of −3,7 kg
- **THEN** the hero reads "Muutos ↓ −3,7 kg" in the main text tone with "Viikossa" at its right on a
  mint tint, and the row holds Keskiarvo, Alin, Ylin and Painoja

#### Scenario: Syöty
- **GIVEN** Syöty 1 v
- **THEN** the hero reads "Keskiarvo 890 kcal/pv" on an amber tint and the row holds Herkut and Päiviä

#### Scenario: One weight
- **GIVEN** Paino with a single weight in the period
- **THEN** the hero reads "—" with "—" at its right, and the row shows the weight as Keskiarvo, Alin
  and Ylin, and Painoja 1

