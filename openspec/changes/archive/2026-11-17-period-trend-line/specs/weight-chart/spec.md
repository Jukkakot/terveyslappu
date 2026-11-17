# Delta for weight-chart

## ADDED Requirements

### Requirement: Trend line on long periods
On a period longer than 31 days every Historia chart (the fixed tabs and own chart tabs) that draws the flat average line on a period of 31 days
or less SHALL draw a trend line in its place; Paino and BMI draw only the trend line (never a flat
average). The period's length SHALL be the number of days of the chart's time axis (for Kaikki, its
own axis): an Oma range of 31 days gets the flat line, one of 32 days the trend line, and a Kaikki
whose axis is 31 days or shorter gets the flat line. The trend line SHALL be a smooth curve through
the same daily values the average uses: Syöty the kcal of each logged day, Askeleet the steps of each
day with steps, Kulutus the total of each day with data, each with today left out; Paino each day's
last weight not marked "?", today included; BMI from those weights. It SHALL be computed from the
daily values also when the bars are weekly, and only for a data that is drawn (not while loading,
without permission, or BMI without a height). The trend line SHALL be drawn only in Historia: every front page card keeps its flat average line where it draws one today, at any period. The statistics under a chart SHALL stay as they are:
the period's average stays a number.

Its smoothness SHALL be the same for every part of the line and follow the period: the line shows
changes over a window of the longer of 21 days and a sixth of the axis's days (3 kk 21 days, 1 v 61
days, 2 v 122 days). It SHALL be recomputed when the data or the axis changes; zooming in the chart
SHALL NOT recompute it or change its smoothness.

#### Scenario: Year of steps
- **GIVEN** a year of daily steps that rise from about 6 000 to about 9 000 with daily values
  between 2 000 and 15 000
- **WHEN** Askeleet is shown with 1 v
- **THEN** weekly bars are drawn with a smooth solid line rising from about 6 000 to about 9 000,
  no flat dashed line, and the legend reads askeleet, trendi and viikoittain

#### Scenario: Month keeps the flat line
- **WHEN** Askeleet is shown with 1 kk
- **THEN** the flat dashed average line is drawn and no trend line

#### Scenario: Oma range at 31 and 32 days
- **GIVEN** food logged every day
- **WHEN** Syöty is shown with an Oma range of 31 days, and then of 32 days
- **THEN** the 31 days show the flat dashed line and the 32 days the trend line

#### Scenario: Short Kaikki
- **GIVEN** the first food log is 20 days old
- **WHEN** Syöty is shown with Kaikki
- **THEN** the axis is 31 days or shorter and the flat dashed line is drawn

#### Scenario: Weight trend
- **GIVEN** weights for 90 days falling from about 84 to about 81 kg with ±0,8 kg daily noise
- **WHEN** Paino is shown with 3 kk
- **THEN** the weight line is drawn as today and a smooth line falls from about 84 to about 81 kg
  over it

#### Scenario: Front page cards keep the flat line
- **GIVEN** a big food card, a steps card and an own chart card with Keskiarvo on, 3 kk
- **THEN** each draws the flat dashed average line as before and no trend line

#### Scenario: Zoom keeps the line
- **GIVEN** Paino with 1 v and a trend line
- **WHEN** the user zooms in the chart
- **THEN** the trend line is the same curve, only larger

#### Scenario: Statistics unchanged
- **WHEN** Syöty is shown with 1 v and a trend line
- **THEN** the statistics under the chart show the period's average kcal per logged day as before

### Requirement: Trend line gaps and too few values
The trend line SHALL run from the first to the last day with a value and SHALL break wherever two
consecutive days with values are more than 30 days apart (no line across the gap; 30 days apart
stays joined). A part with fewer than 10 days with values SHALL get no line. When no part has 10 or
more days with values the data has too few values: the flat dashed average line SHALL be drawn
instead (Syöty, Askeleet, Kulutus, own charts) and nothing on Paino and BMI. This SHALL be decided
after the line is split into parts. Syöty, Askeleet and Kulutus trend lines SHALL end at the last day
with a value before today; Paino and BMI trend lines include today.

#### Scenario: Gap in the data
- **GIVEN** Kaikki with food logged in January–February and again in May–June, nothing in between
- **WHEN** Syöty is shown
- **THEN** one trend line covers January–February and another May–June, with no line over
  March–April

#### Scenario: Gap of 30 and 31 days
- **GIVEN** 3 kk of steps with a stretch without values between two logged days
- **WHEN** the two days are 30 days apart, and then 31 days apart
- **THEN** one joined trend line is drawn at 30 days and two separate lines at 31 days

#### Scenario: Too few days
- **GIVEN** 3 kk with food logged on 6 days
- **WHEN** Syöty is shown
- **THEN** the flat dashed average line is drawn and no trend line

#### Scenario: No part is long enough
- **GIVEN** Kaikki with food logged on 6 days in March and 8 days in September
- **WHEN** Syöty is shown
- **THEN** the flat dashed average line is drawn and no trend line, though both parts have values

#### Scenario: Too few weights
- **GIVEN** 3 kk with weights on 6 days
- **WHEN** Paino is shown
- **THEN** no trend line and no flat line is drawn

#### Scenario: Calculation fails
- **GIVEN** the smoothing of a part gives no usable curve
- **WHEN** the tab is shown
- **THEN** that part is treated as having too few values and the tab still shows, without an error

### Requirement: Trend line look and scale
A trend line SHALL be solid and drawn over the bars. On the fixed tabs it SHALL be 2.5 dp wide, in
the neutral line tone (onSurfaceVariant), under the Paino and BMI line's dots and under the
crosshair ring. Paino and BMI trend points SHALL sit at the time of each day's last weight. In an own
chart tab it SHALL be drawn where the average line is drawn, in both layouts; in
Päällekkäin it is in the data's tone at 60 % opacity with the widths above, and mapped through the
data's scale (shared by data of the same unit). The scale of a bar chart's top SHALL be the highest
of the highest bar, the average and the trend line's highest value; the Paino and BMI scale rule and
the own charts' scale rules SHALL stay as they are, with trend values clamped into the scale.

#### Scenario: Trend above the bars
- **GIVEN** Syöty with 1 v and a trend line
- **THEN** the trend is a solid 2.5 dp line over the bars and the scale's top covers its highest value

#### Scenario: Trend under the weight dots
- **GIVEN** Paino with 3 kk
- **THEN** the weight dots and the crosshair ring are drawn over the trend line

#### Scenario: Tapping still picks the weight
- **GIVEN** Paino with 3 kk and a trend line
- **WHEN** the user taps between two weights
- **THEN** the nearest weight's day is chosen, never the trend line's point

### Requirement: Trend line in the marker card
Tapping a day SHALL add a trend row right after the data's own row, with a dot in the trend tone,
only on a day the trend line covers, formatted like the keskiarvo rows: on the fixed tabs "trendi
8 200 askelta/pv", "trendi 2 050 kcal/pv", "trendi 81,4 kg", "trendi 24,8"; in an own chart
"<data> trendi" ("paino trendi 81,4 kg", "askeleet trendi 8 200 askelta/pv"). Weight and BMI SHALL
show one decimal. Today's tap on Syöty, Askeleet and Kulutus SHALL have no trend row. A weekly bar's
row SHALL use the trend at the week's middle day when that day is inside the axis, else at the
nearest day of that week the line covers, else no row; kcal and steps rows are always "/pv".

#### Scenario: Trend in the marker card
- **GIVEN** Askeleet with 1 v and a trend line
- **WHEN** the user taps a week
- **THEN** the marker card shows the week's row and "trendi 8 200 askelta/pv" for that week

#### Scenario: Week at the axis edge
- **GIVEN** a weekly bar whose middle day is before the axis start
- **THEN** its trend row uses the nearest covered day of that week, or none when none is covered

#### Scenario: Tap today
- **GIVEN** Syöty with 1 v and food logged today
- **WHEN** the user taps today
- **THEN** the card shows today's running kcal and no trend row

#### Scenario: Weight trend row
- **GIVEN** Paino with 3 kk
- **WHEN** the user taps a day with a weight
- **THEN** the card shows the weight and "trendi 81,4 kg"

## MODIFIED Requirements

### Requirement: Energia chart
The Energia tab SHALL show, for the chosen period, one bar per logged day with its kcal, the
treats' share stacked as its own tone, and one flat dashed line across the whole chart at the period's average kcal per logged
day, the value of the Energia statistics (today left out when it is in the period), also
when the bars are weekly (the per-day average, not the mean of the week bars); no line when
there is no average. The scale's top SHALL cover the highest bar, the average line and the trend line's highest value.
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
are weekly; no line when there is no average. The scale's top SHALL cover the highest bar, the average line and the trend line's highest value. The flat line SHALL be drawn on periods up to 31 days; on longer periods the trend line (see "Trend line on long periods") SHALL replace it. Days without steps data SHALL
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
line is drawn (no average line: no average entry). A drawn trend line SHALL have the entry "trendi" instead of "keskiarvo"; an own chart's trend lines share one "trendi" entry, and when it draws both kinds (one data with a trend, another with a flat average) it has both "keskiarvo" (dashed) and "trendi" (solid). Every "trendi" swatch SHALL be a solid line in the neutral line tone. When the bars are weekly (periods over 90
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

#### Scenario: Both kinds in one own chart
- **GIVEN** an own chart with Paino and Askeleet, Keskiarvo on, 3 kk, and Askeleet with only 6 days
  with steps
- **THEN** the legend has "trendi" (solid) for the weight and "keskiarvo" (dashed) for the steps

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

A custom chart SHALL draw real values only: for each day its own value, at every period. It SHALL NOT draw weekly bars. When the chart's Keskiarvo is on, each drawn data SHALL also get one flat dashed line (1 dp, the front page cards' dash); on a Historia tab a period over 31 days draws a trend line instead (see "Trend line on long periods"), on a front page card the flat line stays at every period; on a
Historia tab the line is drawn over the bars and lines, in both layouts, on a front page card over the bars and under the
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
- **THEN** the chart draws a line through those three days' own kcal values and no bars, and with Keskiarvo off no other line

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

#### Scenario: Average on a long period
- **GIVEN** a chart with Askeleet (Pylväät) and Paino (Viiva), Keskiarvo on, period 1 v, each with
  at least 10 days of values
- **THEN** a solid trend line in each data's way (see "Trend line look and scale") is drawn instead
  of the dashed lines, and the marker card has the rows "paino trendi" and "askeleet trendi"

### Requirement: Chart crosshair
On the plot areas of an own chart's tab (and, by "Crosshair on every Historia chart", of every
Historia chart), a tap or a press-and-hold SHALL choose the day of the nearest drawn value and a sideways drag inside
the plot SHALL move the choice with the finger (scrub) while the chart is not zoomed in; the chosen
day SHALL be marked on every panel that draws data, and one marker card SHALL show over the plot at
that day. The card SHALL hold a head line with the date and one row per shown data with access: a
dot in the data's colour, its name and that day's own value with its unit (weight in kg, the day's
last weight not marked "?", or "ei painoa"; kcal or "ei kirjauksia"; steps or "ei tietoa"). There
SHALL be no week form and no averages; a data that has a trend line also gets the row "<data> trendi" right after its own row (see "Trend line in the marker card"). The card SHALL be placed beside the chosen day and SHALL
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

#### Scenario: Trend row
- **GIVEN** an own chart with Paino and Askeleet, Keskiarvo on, 1 v
- **WHEN** the user taps a day inside both trend lines
- **THEN** the card shows "paino 82,4 kg", "paino trendi 81,4 kg", "askeleet 6 420 askelta" and
  "askeleet trendi 8 200 askelta/pv", each trend row with its dot in the trend tone

### Requirement: Chart editor
The chart editor SHALL be a dialog with: a name field; one row per data (Paino, Energia (Syöty), Kulutus, Askeleet)
with a switch and a two-way choice Pylväät / Viiva that is enabled only while the data is on
(the Paino row instead shows a plain label "Viiva" in that place, start-aligned, as tall as the
choice so the four colour dots line up, dimmed while Paino is off, read by TalkBack as "Paino:
viiva", not as a button); the
layout choice Allekkain / Päällekkäin, each with one line saying what it draws; a switch "Keskiarvo" with one
line "Vaakaviiva jakson keskiarvon kohdalla, yli kuukauden jaksoilla trendiviiva"; and the buttons
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

#### Scenario: Weight row has no bars
- **WHEN** the user opens the chart editor
- **THEN** the Paino row shows "Viiva" as a label with no Pylväät choice, the other rows keep
  their Pylväät / Viiva choice, and the four colour dots share the same x

#### Scenario: Keskiarvo text
- **WHEN** the user opens the chart editor
- **THEN** the Keskiarvo switch's supporting text reads "Vaakaviiva jakson keskiarvon kohdalla, yli
  kuukauden jaksoilla trendiviiva"

### Requirement: Overlaid layout
In the Päällekkäin layout all shown data SHALL be drawn in one plot on one time axis, each with its
own scale except that data with the same unit share one (see "Shared scale for the same unit"):
bars behind lines, average and trend lines over the bars; with two to four bar data the bars of one day SHALL
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

#### Scenario: Trend overlaid
- **GIVEN** a chart with Paino (Viiva) and Askeleet (Pylväät), Päällekkäin, Keskiarvo on, 1 v
- **THEN** one trend line per data is drawn over the bars, each in its data's tone at 60 % opacity
  and mapped through that data's scale

### Requirement: Shared scale for the same unit
In an own chart, drawn data with the same unit (today only Syöty and Kulutus, kcal) SHALL share
one scale in both layouts, on the Historia tab and the front page card, fitted by the kcal rule of
"Custom charts" to the highest day of any of them in the period, today included. Data with other
units SHALL keep their own scales. Only drawn data count, so the scale follows what is drawn at the
moment. Average lines and trend lines SHALL sit on the shared scale (their values clamped into it) in their own data's tone.

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

#### Scenario: Trend lines on the shared scale
- **GIVEN** a chart with Syöty and Kulutus, Päällekkäin, Keskiarvo on, 1 v
- **THEN** two trend lines are drawn on the one kcal scale, each in its data's tone

### Requirement: Today on running lines
In own charts, on Historia tabs and front page cards in both layouts, a Viiva line of Syöty,
Kulutus or Askeleet SHALL join only the days before today. When today is in the period and has a
value, it SHALL be drawn as a lone dot in the line's tone, at today's value, not joined to the
line. Paino lines SHALL join today as before. A trend line of these data SHALL end at the last day with a value before today. Bars, average lines, the legend and the crosshair
readout SHALL be unchanged; tapping today still shows today's running value.

#### Scenario: Partial day
- **GIVEN** an own chart with Kulutus as Viiva, 1 vk, about 2 800 kcal on each earlier day and
  500 kcal so far today
- **THEN** the line runs through the earlier days and stops at yesterday, and today is one dot at
  500 kcal with no segment down to it

#### Scenario: Only today
- **GIVEN** an own chart with Syöty as Viiva and food logged only today
- **THEN** today is drawn as one dot and there is no line

#### Scenario: Weight joins today
- **GIVEN** an own chart with Paino as Viiva and a weight today
- **THEN** the weight line joins today's weight as before

#### Scenario: Trend stops yesterday
- **GIVEN** an own chart with Askeleet as Viiva, Keskiarvo on, 1 v, steps every day including today
- **THEN** the trend line ends at yesterday and today is a lone dot

### Requirement: Kulutus in own charts
Own charts SHALL offer Kulutus as a fourth data, drawn and listed in the order Paino, Syöty,
Kulutus, Askeleet. Kulutus, Pylväät: one bar per day with data, its height the day's total; in the
Allekkain layout split into lepo (light tone) and aktiivinen (full tone) as on the Kulutus tab, in
Päällekkäin one tone. Kulutus, Viiva: a line through each day's total. Its scale SHALL follow the
Syöty rule (`ceil(highest × 1,1 / 500) × 500` kcal, at least 500) and SHALL be shared with Syöty
when both are drawn (see "Shared scale for the same unit"). With Keskiarvo on, its line is
the average total per day with data, today left out, or on a period over 31 days the trend line of the same values (see "Trend line on long periods"). An own chart SHALL hold one to four data; in
Päällekkäin up to four bar data stand side by side in a day's slot and only the first two scales
have an axis (see "Overlaid layout"). The crosshair readout SHALL show Kulutus as "2 300 kcal · aktiivinen 450 kcal" (a
day without a split: "2 300 kcal"), "ei tietoa" without a value. Its Allekkain panel without values
SHALL read "Ei kulutustietoja tällä jaksolla". A new chart SHALL start with Kulutus off.

#### Scenario: Eaten and burned together
- **GIVEN** a chart with Syöty (Pylväät) and Kulutus (Viiva), Allekkain
- **THEN** a Syöty panel and a Kulutus panel are stacked on one time axis, both on the same kcal scale
