## MODIFIED Requirements

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

### Requirement: Chart editor
The chart editor SHALL be a dialog with: a name field; one row per data (Paino, Energia (Syöty), Kulutus, Askeleet)
with a switch and a two-way choice Pylväät / Viiva that is enabled only while the data is on
(the Paino row instead shows a plain label "Viiva" in that place, start-aligned, as tall as the
choice so the four colour dots line up, dimmed while Paino is off, read by TalkBack as "Paino:
viiva", not as a button); the
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

#### Scenario: Weight row has no bars
- **WHEN** the user opens the chart editor
- **THEN** the Paino row shows "Viiva" as a label with no Pylväät choice, the other rows keep
  their Pylväät / Viiva choice, and the four colour dots share the same x

### Requirement: Chart editor colours
In the chart editor each data row (Paino, Syöty, Kulutus, Askeleet) SHALL hold, after its switch
and its Pylväät / Viiva choice (Paino: its "Viiva" label), one colour dot (44 dp touch target, named for TalkBack with its
colour) that opens a small picker of nine choices (Oletus and the eight colours, a 3 × 3 grid of
44 dp targets, the chosen one marked with a check and announced "valittu"); picking closes the
picker. The dot SHALL be shown but disabled while the data is off. Peruuta and back discard a
colour change like any other change.

#### Scenario: Pick a colour
- **WHEN** the user taps Askeleet's colour dot and picks vihreä
- **THEN** the picker closes and the dot shows green; Tallenna keeps it
