## MODIFIED Requirements

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
an existing chart the editor SHALL also show a red "Poista kaavio" on the left, which deletes the
chart at once, without a question, and shows "Kaavio poistettu · Kumoa"; Kumoa SHALL bring the
chart back with its name, data, layout, period and its front page card in its place. Peruuta, back and tapping outside SHALL close
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
