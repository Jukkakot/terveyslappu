## MODIFIED Requirements

### Requirement: Steps come from Health Connect, read only
The app SHALL show the user's daily step totals as Health Connect reports them for the steps
source in use (see "Steps source setting") and SHALL NOT write, change or delete steps. With one
app as the source, a day's total SHALL be that app's own daily total, so steps another app wrote
for the same day never fill its gaps. With "Kaikki (Health Connectin järjestys)" the total SHALL be
Health Connect's merged daily total over all apps. Steps SHALL be shown only inside the app (no
widget, tile or notification).

#### Scenario: Two sources, one total
- **GIVEN** Garmin Connect wrote 9 000 steps for yesterday and the phone's own counter wrote 2 000
  steps for hours in which Garmin wrote nothing
- **WHEN** the source is Garmin Connect and yesterday's steps are shown
- **THEN** yesterday reads 9 000 steps, not more

#### Scenario: All apps
- **GIVEN** the same steps
- **WHEN** the source is "Kaikki (Health Connectin järjestys)"
- **THEN** yesterday reads Health Connect's merged total for the day, as before this change

#### Scenario: Never written
- **WHEN** the user uses any part of the app
- **THEN** no steps are written to or deleted from Health Connect
- Covered by design: a manifest/lint test that the write-steps permission is absent.

### Requirement: Old steps need the history permission
Steps older than 30 days before the app's first permission grant SHALL be read only when the
history-read permission the app already asks for weights is granted. The daily read SHALL start at
the later of the period's start and the first readable steps day of the source in use (found by an
ascending read limited to that source, which returns only readable records). The app SHALL NOT ask
for the history permission from the steps card or the source setting.

#### Scenario: Read start
- **GIVEN** the first readable steps day of the chosen app is 20 days ago
- **WHEN** the user picks 3 kk
- **THEN** steps are read from that day on and earlier days are gaps

#### Scenario: Kaikki starts at the source's first day
- **GIVEN** the phone has steps from 300 days ago and Garmin Connect, the source in use, from 100
  days ago, with the history permission granted
- **WHEN** the user picks Kaikki on Askeleet
- **THEN** the chart starts 100 days ago

### Requirement: Steps in the app log
The log SHALL record steps reads, source discovery, source choices and permission results with
counts, timings, outcomes and the source's package name only, never the number of steps of any day
or any app's step total. The phone's own steps SHALL be logged as `phone`, never by their
device-specific package name. A failing read or discovery SHALL leave a log line with its cause.

#### Scenario: Read logged
- **WHEN** the steps of a period are read
- **THEN** the log gets one line with the outcome, the source, the number of days and the duration,
  and no step counts

#### Scenario: Failed read logged
- **GIVEN** Health Connect refuses the read
- **THEN** the log holds the cause

#### Scenario: Automatic switch logged
- **GIVEN** the automatic source changes from the phone to Garmin Connect
- **THEN** the log gets a line naming `phone` and Garmin Connect's package name, with no step totals

## ADDED Requirements

### Requirement: Steps source setting
Asetukset SHALL have a row "Askeleet", after Ruoka and before Ulkoasu, whose state line names the
source in use ("Automaattinen: Garmin Connect", "Garmin Connect", "Kaikki (Health Connectin
järjestys)"; "Ei lupaa" without the steps permission; "Ei saatavilla" when Health Connect is
unavailable). Its sub-screen SHALL hold "Lähde", a single choice of: "Automaattinen" (with a line
naming the app it uses now), every app that wrote steps in the last 30 days, and "Kaikki (Health
Connectin järjestys)". Each app SHALL show a readable name (the phone's own steps as "Puhelin";
otherwise the installed app's name, or its package name when the name cannot be found) and its
average steps per day with steps over those 30 days. The list SHALL be read again each time the
sub-screen opens. A change SHALL apply at once: the front page steps card and Askeleet re-read
their whole range from the new source. Without the steps permission the sub-screen SHALL show the
same reason and buttons as Askeleet and no list. The default SHALL be "Automaattinen".

#### Scenario: First visit
- **GIVEN** Garmin Connect and the phone wrote steps in the last 30 days and the user never chose
- **WHEN** the user opens Asetukset → Askeleet
- **THEN** "Automaattinen" is selected with the line "Nyt: Garmin Connect", followed by Garmin
  Connect and Puhelin with their averages per day, and "Kaikki (Health Connectin järjestys)"

#### Scenario: Pick an app
- **WHEN** the user picks Puhelin
- **THEN** the row reads "Puhelin" and the steps card and Askeleet show the phone's steps only

#### Scenario: Name not found
- **GIVEN** an app wrote steps but its name cannot be read
- **THEN** it is listed by its package name

#### Scenario: Back
- **WHEN** the user presses back on the sub-screen
- **THEN** Asetukset's list is shown

### Requirement: Automatic steps source
With "Automaattinen", the source SHALL be the app with the most steps over the last 30 days. The
phone's own step records (old and new attribution) SHALL count as one app, "Puhelin". The pick
SHALL be re-checked at the first steps read of each day and whenever the source sub-screen opens.
The app in use SHALL be kept unless another app's 30-day total exceeds it by more than 10 %. A
change of the pick SHALL be silent (no notice) apart from the log line and the source shown on
Askeleet and in Asetukset, and SHALL re-read the shown range. When no app wrote steps in the last
30 days, or the check fails and no earlier pick is known, steps SHALL be read as with "Kaikki
(Health Connectin järjestys)". When the check fails and an earlier pick is known, that pick SHALL
stay in use.

#### Scenario: Most steps wins
- **GIVEN** Garmin Connect wrote 240 000 and the phone 90 000 steps in the last 30 days
- **WHEN** the automatic pick is checked
- **THEN** Garmin Connect is used

#### Scenario: Near tie keeps the current app
- **GIVEN** the phone is in use with 200 000 steps in 30 days and Garmin Connect has 210 000
- **WHEN** the next day's check runs
- **THEN** the phone stays in use

#### Scenario: Clear overtake
- **GIVEN** the phone is in use with 100 000 steps in 30 days and Garmin Connect has 150 000
- **WHEN** the next day's check runs
- **THEN** Garmin Connect is used, the shown range is re-read and no notice appears

#### Scenario: Nothing in 30 days
- **GIVEN** no app wrote steps in the last 30 days
- **THEN** steps are read from all apps as Health Connect merges them, and the row reads
  "Automaattinen: ei askelia 30 päivään"

### Requirement: Chosen app without recent steps
An app the user picked SHALL stay the source even when it wrote no steps in the last 30 days or is
no longer installed; it SHALL then stay in the source list with "ei askelia 30 päivään", and the
Asetukset row and the Askeleet source line SHALL say so in the warning tint. The app SHALL NOT
switch to another source by itself.

#### Scenario: Watch app stopped syncing
- **GIVEN** the user picked Garmin Connect and it wrote nothing for 30 days
- **WHEN** Asetukset is shown
- **THEN** the Askeleet row reads "Garmin Connect: ei askelia 30 päivään" in the warning tint and
  steps are still read from Garmin Connect only

#### Scenario: Uninstalled app
- **GIVEN** the user picked an app that has since been uninstalled
- **THEN** it is listed by its package name with "ei askelia 30 päivään" until the user picks
  another source

### Requirement: Source choice stays on the phone
The source choice and the automatic pick SHALL be kept only on this phone: not in the data file,
the backup file or the phone's own cloud backup and transfer. After a reinstall or on a new phone
the choice SHALL read as "Automaattinen".

#### Scenario: New phone
- **GIVEN** the user picked Puhelin and moves to a new phone with the app's data transferred
- **THEN** the new phone uses "Automaattinen"
