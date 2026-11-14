## ADDED Requirements

### Requirement: Steps come from Health Connect, read only
The app SHALL show the user's daily step totals as Health Connect reports them. It SHALL NOT write,
change or delete steps. A day's total SHALL be Health Connect's merged total of all apps writing
steps (such as Garmin Connect and Samsung Health), following the user's data-source priority in
Health Connect, so the same steps from two apps are never counted twice. A day for which Health
Connect holds no steps SHALL be a gap, never a zero bar. A day SHALL be the calendar day in the
phone's time zone. Steps SHALL be shown only inside the app (no widget, tile or notification).

#### Scenario: Two sources, one total
- **GIVEN** Garmin Connect and Samsung Health both wrote steps for yesterday and the user ranks
  Garmin first in Health Connect
- **WHEN** yesterday's steps are shown
- **THEN** the number is the one Health Connect shows for yesterday, not the sum of both apps

#### Scenario: Day without data
- **GIVEN** Health Connect holds no steps for Tuesday
- **THEN** Tuesday is a gap in the bars and is not listed

#### Scenario: Never written
- **WHEN** the user uses any part of the app
- **THEN** no steps are written to or deleted from Health Connect

### Requirement: Steps permission
The steps read permission SHALL be asked only from the steps card on the front page or the Askeleet
tab in Historia, and never at start-up or from a weigh-in. Without it both SHALL show the reason
("Askeleet: anna lupa" on the card) and a way to ask for it, and the rest of the app SHALL work as
before. After a denial the same places SHALL stay as they are, so the user can try again. When
Health Connect is unavailable the steps card and tab SHALL say so and not offer the permission.
The permission SHALL be read again whenever the app is resumed, since the user may change it in
Health Connect's settings.

#### Scenario: First time
- **GIVEN** the steps permission has not been asked
- **WHEN** the user taps the steps card
- **THEN** Health Connect's permission dialog for steps opens

#### Scenario: Not at start
- **WHEN** the app is started and the user weighs in
- **THEN** no steps permission dialog appears

#### Scenario: Denied
- **GIVEN** the user denied the steps permission
- **THEN** the card still reads "Askeleet: anna lupa", the Askeleet tab shows the reason and a
  button, and the weight and food cards work as before

#### Scenario: Granted in Health Connect's settings
- **GIVEN** the permission was denied
- **WHEN** the user grants it in Health Connect and returns to the app
- **THEN** the steps card and tab show the steps without asking again

### Requirement: Old steps need the history permission
Steps older than 30 days before the app's first permission grant SHALL be read only when the
history-read permission the app already asks for weights is granted. Without it a period reaching
further back SHALL show the steps it can read and the same notice that offers the history
permission as for weights. The app SHALL NOT ask for the history permission from the steps card.

#### Scenario: Long period without history access
- **GIVEN** the history permission is not granted
- **WHEN** the user picks a period reaching further back than Health Connect lets the app read
- **THEN** the chart shows the readable days and the notice offers to grant access to older data

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
SHALL leave a log line with its cause and the screen SHALL show a short message instead of old
numbers.

#### Scenario: Read logged
- **WHEN** the steps of a period are read
- **THEN** the log gets one line with the outcome, the number of days and the duration, and no
  step counts

#### Scenario: Failed read
- **GIVEN** Health Connect refuses the read
- **THEN** the log holds the cause and the card and tab show that steps could not be read
