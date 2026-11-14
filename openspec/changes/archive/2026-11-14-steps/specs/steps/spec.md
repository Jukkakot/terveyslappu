## ADDED Requirements

### Requirement: Steps come from Health Connect, read only
The app SHALL show the user's daily step totals as Health Connect reports them and SHALL NOT write,
change or delete steps. Steps SHALL be shown only inside the app (no widget, tile or notification).

#### Scenario: Two sources, one total
- **GIVEN** Garmin Connect and Samsung Health both wrote steps for yesterday and the user ranks
  Garmin first in Health Connect
- **WHEN** yesterday's steps are shown
- **THEN** the number is the one Health Connect shows for yesterday, not the sum of both apps
- Covered by design: only Health Connect's daily aggregate is used, with no origin filter; no test.

#### Scenario: Never written
- **WHEN** the user uses any part of the app
- **THEN** no steps are written to or deleted from Health Connect
- Covered by design: a manifest/lint test that the write-steps permission is absent.

### Requirement: Days and gaps
A day SHALL be the local day as Health Connect's daily aggregate reports it. A day for which Health
Connect holds no steps SHALL be a gap, never a zero bar, and SHALL be left out of averages and day
counts. Today SHALL be shown but left out of the averages, the 7-day line and the weekly bar
averages (it is not over); it SHALL count as a day with steps in "x / y päivää" when it has steps.

#### Scenario: Day without data
- **GIVEN** Health Connect holds no steps for Tuesday
- **THEN** Tuesday is a gap in the bars and is not listed

#### Scenario: Today
- **GIVEN** today holds 3 000 steps and the earlier days average 8 000
- **THEN** the average stays 8 000 and today counts as a day with steps in the day count

### Requirement: Steps permission
The steps read permission SHALL be asked only when the user taps the steps card on the front page
or the Askeleet tab in Historia, and never at start-up or from a weigh-in. Without it both SHALL
show the reason ("Askeleet: anna lupa" on the card) and the rest of the app SHALL work as before.
The permission SHALL be read again whenever the app is resumed, since the user may change it in
Health Connect's settings.

#### Scenario: First time
- **GIVEN** the steps permission has not been asked
- **WHEN** the user taps the steps card
- **THEN** Health Connect's permission dialog for steps opens

#### Scenario: Not at start
- **WHEN** the app is started and the user weighs in
- **THEN** no steps permission dialog appears

#### Scenario: Granted in Health Connect's settings
- **GIVEN** the permission was denied
- **WHEN** the user grants it in Health Connect and returns to the app
- **THEN** the steps card and tab show the steps without asking again

### Requirement: Denied steps permission
After the user has denied the steps permission, a tap on the steps card SHALL open the Askeleet
tab, which SHALL show the reason and two buttons: ask again and open Health Connect's permission
settings. Whether the permission was asked before is remembered only while the app runs; after a
restart the state reads as not yet asked and a tap asks again.

#### Scenario: Denied
- **GIVEN** the user denied the steps permission
- **WHEN** the user taps the steps card
- **THEN** Askeleet opens with the reason, "ask again" and "open Health Connect settings", and the
  weight and food cards work as before

#### Scenario: Health Connect unavailable
- **GIVEN** Health Connect is not available on the phone
- **THEN** the card reads "Askeleet: ei saatavilla" with no tap action and the tab shows the same
  text without a button

### Requirement: Old steps need the history permission
Steps older than 30 days before the app's first permission grant SHALL be read only when the
history-read permission the app already asks for weights is granted. The daily read SHALL start at
the later of the period's start and the first readable steps day (found by an ascending read, which
returns only readable records). The app SHALL NOT ask for the history permission from the steps
card.

#### Scenario: Read start
- **GIVEN** the first readable steps day is 20 days ago
- **WHEN** the user picks 3 kk
- **THEN** steps are read from that day on and earlier days are gaps

### Requirement: History notice on Askeleet
When the history permission is not granted and the period starts more than 30 days back, the
Askeleet tab SHALL show the same notice that offers the history permission as for weights; when
history reading is unsupported it SHALL show nothing.

#### Scenario: Long period without history access
- **GIVEN** the history permission is not granted
- **WHEN** the user picks a period starting more than 30 days back
- **THEN** the chart shows the readable days and the notice offers to grant access to older data

#### Scenario: Short period
- **GIVEN** the history permission is not granted
- **WHEN** the user picks 1 kk
- **THEN** no history notice is shown

### Requirement: Refresh and failed reads
Steps SHALL be re-read, for the whole shown range, whenever the app is resumed and the home screen
or the Askeleet tab is shown, so late watch data appears; numbers SHALL NOT update live while the
screen stays open. When a read fails and nothing has been read yet, a short message SHALL replace
the numbers. When numbers were read earlier, they SHALL stay with a quiet "ei päivitetty" line.

#### Scenario: Late watch data
- **GIVEN** the watch syncs yesterday's steps after the app was left open
- **WHEN** the user returns to the app
- **THEN** the shown range is read again and yesterday's bar updates

#### Scenario: First read fails
- **GIVEN** nothing has been read yet and Health Connect refuses the read
- **THEN** the card and tab show that steps could not be read, not zero

#### Scenario: Later read fails
- **GIVEN** steps were shown and the next read fails
- **THEN** the last numbers stay with a quiet "ei päivitetty" line

### Requirement: Steps stay out of the data file and the backup
Steps SHALL NOT be a data kind of the data file (export, import and the automatic backup): they
are measured by another app and live in Health Connect and Garmin Connect, not in this app.

#### Scenario: Export with steps in Health Connect
- **GIVEN** Health Connect holds a year of steps
- **WHEN** the user exports their data
- **THEN** the file holds no steps

### Requirement: Steps in the app log
The log SHALL record steps reads and permission results with counts and timings only (days read,
milliseconds, outcome and cause of a failure), never the number of steps of any day. A failing read
SHALL leave a log line with its cause.

#### Scenario: Read logged
- **WHEN** the steps of a period are read
- **THEN** the log gets one line with the outcome, the number of days and the duration, and no
  step counts

#### Scenario: Failed read logged
- **GIVEN** Health Connect refuses the read
- **THEN** the log holds the cause
