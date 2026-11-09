# weight-entry Specification

## Purpose
Lets the user save a body weight to Health Connect by hand and see the latest weights stored
there, so other health apps on the phone can show them.

## Requirements

### Requirement: Enter and save a weight
The app SHALL let the user enter a weight in kilograms with one decimal, plus a date and time that
default to the current moment, and save it to Health Connect as a weight record. The form SHALL be
a dialog opened from the add button ("Lisää paino käsin") in the Historia top bar; no other screen
shows the form.

#### Scenario: Open the form
- **WHEN** the user taps the add button on Historia
- **THEN** a dialog shows the weight field, date, time and Tallenna, the date and time set to now

#### Scenario: Save a weight for now
- **WHEN** the user types 82,4 in the dialog and taps Tallenna without touching the date and time
- **THEN** a weight of 82.4 kg with the current date and time is written to Health Connect
- **AND** the dialog closes and the app confirms the save

#### Scenario: Save a weight for an earlier moment
- **WHEN** the user picks yesterday 07:30 in the dialog and saves 82,4
- **THEN** the weight is written with yesterday 07:30 as its time

#### Scenario: Cancel
- **WHEN** the user closes the dialog without saving
- **THEN** nothing is written

#### Scenario: Save fails
- **WHEN** writing to Health Connect fails
- **THEN** the dialog stays open with the typed weight and says the save failed

### Requirement: Reject impossible weights
The app SHALL accept weights from 20,0 to 300,0 kg, with a comma or a dot as the decimal mark, and
SHALL NOT save anything else.

#### Scenario: Out of range
- **WHEN** the user enters 7 or 450
- **THEN** the save button stays disabled and the field tells the allowed range

#### Scenario: Dot or comma
- **WHEN** the user enters 82.4 or 82,4
- **THEN** both are read as 82.4 kg

#### Scenario: Future time
- **WHEN** the chosen date and time are later than now
- **THEN** the app does not save and says the time cannot be in the future

### Requirement: Recent weights from Health Connect
The app SHALL show on Historia the weights this app wrote in the period chosen for the chart, read
from Health Connect, newest first, each with its date, time and value; the date SHALL include the
year when the weight is not from the current year. Weights written by other apps SHALL NOT be listed
(user 2026-11-09: they are inaccurate hand entries). Each weight row SHALL also show the change from
the listed weight just before it in time, signed with one decimal (`+0,3`, `−0,5`, `±0,0`); the
oldest row of the period and a weight more than 30 days after the one before it SHALL show no
change. The change SHALL have a small arrow up for a gain and down for a loss, none when it is
zero. Gains and losses SHALL have different colours taken from the phone's palette that are not
red and green, and a zero change SHALL use the neutral text colour.

#### Scenario: Weights from several apps
- **GIVEN** Health Connect holds weights written by this app and by another app
- **WHEN** Historia is shown
- **THEN** only this app's weights appear in the list

#### Scenario: List follows the period
- **WHEN** the user chooses the period 1 kk
- **THEN** the list shows this app's weights of the last 30 days

#### Scenario: Old weight
- **WHEN** the list shows a weight from 15.9.2024 at 8.33
- **THEN** its row reads "Su 15.9.2024 8.33"

#### Scenario: Change from the previous weight
- **GIVEN** the period holds 82,0 kg, then 82,4 kg, then 81,9 kg
- **WHEN** Historia is shown
- **THEN** the rows read 81,9 kg with −0,5, 82,4 kg with +0,4 and 82,0 kg with no change

#### Scenario: No change across a gap
- **GIVEN** 92,0 kg on 20.5.2024 and 97,5 kg on 18.3.2025
- **WHEN** the 18.3.2025 weight is shown
- **THEN** its row shows no change

#### Scenario: Colours without a judgement
- **WHEN** the list shows a gain and a loss
- **THEN** they have two different colours, neither of them red or green, and both show their sign

#### Scenario: Refresh after saving
- **WHEN** a weight is saved
- **THEN** it appears at the top of the list and on the home screen without leaving the screen

#### Scenario: Nothing yet
- **WHEN** this app has no weights in the chosen period
- **THEN** the list area says that there are no weights in the period

### Requirement: Delete own entry
The app SHALL let the user delete a listed weight with a long-press or the row's delete button,
without a confirmation, and SHALL offer to undo the deletion.

#### Scenario: Delete a test weight
- **WHEN** the user long-presses a weight or taps its delete button
- **THEN** the weight is removed from Health Connect and from the list
- **AND** a message "Paino poistettu" with Kumoa is shown

#### Scenario: Undo
- **WHEN** the user taps Kumoa
- **THEN** the same weight is written back at the same time, marked as from the scale when it was
  a scale weight, and appears in the list again

#### Scenario: Other app's weight
- **GIVEN** another app wrote a weight
- **THEN** it is not listed, so the app offers no way to delete it

### Requirement: Health Connect access
The app SHALL ask for permission to read and write weight the first time it needs Health Connect,
and SHALL explain what is missing when access is denied or Health Connect is not available.

#### Scenario: First start
- **WHEN** the app starts without weight permissions
- **THEN** it shows a short explanation and a button that opens the Health Connect permission
  request for reading and writing weight

#### Scenario: Permission denied
- **WHEN** the user has denied the permissions
- **THEN** the screen says saving needs the permission and offers a button to Health Connect
  settings, and the weight form is disabled

#### Scenario: Health Connect unavailable
- **WHEN** Health Connect is not available on the phone or needs an update
- **THEN** the screen says so and offers to open the store page or settings where it is fixed

### Requirement: Version visible
The app SHALL show its version in Asetukset as `v<major>.<minor>.<build>`, where the build number
grows by one with every commit that changes the app and never resets, so a shared log and a report
can be matched to a build.

#### Scenario: Check the version
- **WHEN** the user opens Asetukset
- **THEN** the version is shown, e.g. "v1.0.24"

#### Scenario: Docs-only commit
- **WHEN** a commit changes only specs or documentation
- **THEN** the next build's number is the same as without that commit

### Requirement: Grouped list
Historia's list SHALL group the period's weights by year, month and week, newest first on every
level, with the weights as plain rows under their week (no day level: a weight row already shows its
day). Weeks run Monday to Sunday and SHALL be cut at month borders. Every year, month and week that
holds a weight SHALL be its own row, even with a single weight (user 2026-11-09: one systematic
rule). When all the period's weights fall in one year, the year row SHALL be left out and the list
SHALL start with the months. A period with at most 14 weights SHALL stay a flat list of weight rows
without groups.

#### Scenario: A week, one weight a day
- **GIVEN** the period 1 vk holds one weight on each of seven days
- **WHEN** Historia is shown
- **THEN** the list shows seven weight rows and no group rows

#### Scenario: Two weights in a day
- **GIVEN** a period of over 14 weights where a day holds weights at 7.05 and 21.30
- **WHEN** that day's week is open
- **THEN** both weights show as weight rows under the week, with no day row

#### Scenario: Several years
- **GIVEN** the period Kaikki holds weights from 2022 to 2026
- **WHEN** Historia is shown
- **THEN** the top level shows year rows 2026, 2025, 2024, 2023, 2022

#### Scenario: Week across a month border
- **GIVEN** weights on Thu 30.10. and Mon 3.11. and Tue 4.11.
- **THEN** 30.10. sits under Lokakuu and 3.–4.11. under Marraskuu, each in its own part of the week

#### Scenario: Month with one weight
- **GIVEN** a period of over 14 weights where March 2024 holds a single weight
- **THEN** March 2024 is a month row with a week row under it, and the weight under that week

#### Scenario: One year
- **GIVEN** the period 1 v holds 200 weights all from 2026
- **THEN** the top level shows month rows and no year row

### Requirement: Group rows
A group row SHALL show the group's name (a year "2024", a month with year "Marraskuu 2025", a week
with its days "Vk 45 · 3.–9.11."; the year shown when not the current year), the first and the last
weight of the group in time ("92,3 → 91,0 kg"), their difference with the list's arrow and neutral
colours, the number of weights, and a sign that it opens. A group with a single weight SHALL show
that weight and no difference. Tapping it SHALL open or close it.

#### Scenario: Month row
- **GIVEN** November 2025 holds 20 weights, the first 92,3 kg and the last 91,0 kg
- **THEN** its row reads "Marraskuu 2025", "92,3 → 91,0 kg", −1,3 with an arrow down and 20

#### Scenario: Open and close
- **WHEN** the user taps a closed month row
- **THEN** its weeks appear under it; tapping again hides them

### Requirement: Open by default
When Historia is shown, every group SHALL be closed, so the list starts as an overview of its top
level; tapping a group opens it. Open and closed groups SHALL NOT be remembered after leaving
Historia; changing the period SHALL reset them to closed. A list short enough to stay flat (no
groups) shows its weights as before.

#### Scenario: Kaikki opened
- **GIVEN** the period Kaikki with weights from 2022 to today
- **WHEN** Historia is shown
- **THEN** the list shows only the closed year rows (and gap rows between them); no weight row shows
  until a group is opened

### Requirement: Gaps in the list
Where more than 30 days pass with no weight between two neighbours on the same level, the list SHALL
show between them a thin row "Ei punnituksia <first day>–<last day>" naming the days without
weights. Empty years, months, weeks or days SHALL NOT be shown.

#### Scenario: Long pause
- **GIVEN** weights until 20.5.2024 and the next on 18.3.2025
- **WHEN** Kaikki is shown
- **THEN** between the 2025 and 2024 rows a thin row reads "Ei punnituksia 21.5.2024–17.3.2025"

#### Scenario: Short pause
- **GIVEN** no weights for 10 days inside a month
- **THEN** no gap row is shown and the month has no empty weeks
