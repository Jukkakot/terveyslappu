# Spec Delta

## ADDED Requirements

### Requirement: Crosshair on every Historia chart
Every chart in Historia (Paino, BMI, Syöty, Askeleet, Kulutus and the own charts) SHALL answer a
tap, a press-and-hold and a hold-and-drag on its plot the way "Chart crosshair" describes for own
charts: one vertical line at the chosen day (or week, when the bars are weekly) through the plot,
a marker on the chosen value (a filled circle on a line or dot, the chosen bar in full tone while
the other bars dim to 60 %), and one readout above the plot that always starts with the date: the
weekday and the date, with the year when it is not the current year ("Ma 9.11.", "La 15.9.2024"),
or for a weekly bar the week ("Vk 46 · 9.–15.11."). The readout SHALL then show:
- Paino: the time and value of that day's last weight not marked "?" ("7.05 · 82,6 kg"), or "ei
  painoa"; BMI the same with "BMI 25,3".
- Syöty: total kcal and "herkut N kcal", or "ei kirjauksia"; a weekly bar its average per logged
  day ("keskim. 1 900 kcal/pv").
- Askeleet: steps or "ei tietoa"; weekly: "keskim. 8 200 askelta/pv".
- Kulutus: as in energy-burn "Kulutus tab".
The removal rules, the "Napauta päivää nähdäksesi arvot" placeholder, swipe-to-switch-tab, haptic
tick and TalkBack actions SHALL be those of "Chart crosshair". On Syöty the readout SHALL end with
"Avaa päivä ›", which opens Ruoka on that day (not for a weekly bar); tapping elsewhere on the
readout removes the crosshair. Front page cards keep their tap (open Historia) and have no
crosshair.

#### Scenario: Tap a steps bar
- **GIVEN** Askeleet with 1 vk
- **WHEN** the user taps Thursday's bar
- **THEN** a line crosses Thursday, its bar stays full while the others dim, and the readout reads
  "To 12.11. · 9 202 askelta"

#### Scenario: Old weight
- **GIVEN** Paino with Kaikki
- **WHEN** the user taps 15.9.2024, whose last weight was 97,6 kg at 8.33
- **THEN** the readout reads "Su 15.9.2024 · 8.33 · 97,6 kg" and a ring marks the dot

#### Scenario: Open a day from Syöty
- **WHEN** the user taps a day on Syöty and then "Avaa päivä ›"
- **THEN** Ruoka opens on that day; back returns to Historia on Syöty

#### Scenario: Weekly bar
- **GIVEN** Syöty with 1 v
- **WHEN** the user taps a week's bar
- **THEN** the readout reads "Vk 46 · 9.–15.11. · keskim. 1 900 kcal/pv" and no "Avaa päivä"

## MODIFIED Requirements

### Requirement: Point details
A weight's date, time and value SHALL be shown through the chart's crosshair (see "Crosshair on
every Historia chart"); the date SHALL include the year when the weight is not from the current
year.

#### Scenario: Tap a dot
- **WHEN** the user taps the dot of 82.6 kg on Sunday 8.11. this year at 7.05
- **THEN** the readout shows "Su 8.11. · 7.05 · 82,6 kg"

#### Scenario: Tap a BMI dot
- **GIVEN** the BMI chart is shown and the height is 180 cm
- **WHEN** the user taps the dot of 82.0 kg on Sunday 8.11. this year at 7.05
- **THEN** the readout shows "Su 8.11. · 7.05 · BMI 25,3"

### Requirement: Askeleet chart
The Askeleet tab SHALL show, for the chosen period, one bar per day with steps in one tone, and one
flat dashed line across the whole chart at the period's average steps per day with steps, the
value of the Askeleet statistics (today left out when it is in the period), also when the bars
are weekly; no line when there is no average. The scale's top SHALL cover both the highest bar
and the line. There SHALL
be no moving average line. Days without steps data SHALL
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

### Requirement: Chart crosshair
On the plot areas of an own chart's tab (and, by "Crosshair on every Historia chart", of every
Historia chart), tapping, or pressing and holding (released in place or
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
