## MODIFIED Requirements

### Requirement: Steps come from Health Connect, read only
The app SHALL show the user's daily step totals as Health Connect reports them for the steps
source in use (see "Steps source setting") and SHALL NOT write, change or delete steps. With one
app as the source, a day's total SHALL be that app's own daily total, so steps another app wrote
for the same day never fill its gaps, and the source SHALL be that one app for the whole history:
no day falls back to another app. With "Kaikki (Health Connectin järjestys)" the total SHALL be
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
- **GIVEN** the phone has steps from 2 years ago and Garmin Connect, the source in use, from 100
  days ago, with the history permission granted
- **WHEN** the user picks Kaikki on Askeleet
- **THEN** the chart starts 100 days ago, at Garmin Connect's first day, and no earlier day shows
  the phone's steps
- Whoever wants the phone's older steps picks Puhelin, or "Kaikki (Health Connectin järjestys)",
  which shows the full history; Ohje says so in one sentence. No notice appears when the source
  is chosen or changed.

### Requirement: Steps in the app log
The log SHALL record steps reads, source checks, source choices and permission results with
counts, timings, outcomes and the source's package name only, never the number of steps of any day
or any app's step total. The phone's own steps SHALL be logged as `phone`, never by their
device-specific package name. A failing read or check SHALL leave a log line with its cause.

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
Asetukset SHALL have a row "Askeleet", after Ruokakirjaus and before Ulkoasu, whose state line
names the source in use ("Automaattinen: Garmin Connect", "Garmin Connect", "Kaikki (Health
Connectin järjestys)"; "Ei lupaa" without the steps permission, whether it is missing or denied;
"Ei saatavilla" when Health Connect is unavailable; blank while the permission is still being
checked). Before any source check has run in this app start, the row SHALL read from the stored
automatic pick. Its sub-screen SHALL hold "Lähde", a single choice of: "Automaattinen" (with a line
naming the app it uses now; when a check fails the stored pick is still named), every app that wrote
steps in the last 30 days, and "Kaikki (Health Connectin järjestys)". Each app SHALL show a readable
name (the phone's own steps as "Puhelin"; otherwise the installed app's name, or its package name
when the name cannot be found) and its average steps per day with steps over those 30 days, today
left out (it is not over). Apps SHALL be listed by their 30-day total steps, today included, most
first, so the order can differ from the order of the averages shown; a chosen app without steps in
those 30 days SHALL be listed after them. The list SHALL be checked again each time the sub-screen
is entered (not on a resume) and when the steps permission becomes granted while it is shown, except
that no check SHALL start when the last one finished under a minute ago. A change SHALL apply at
once: the front page steps card and Askeleet re-read their whole range from the new source. Without
the steps permission the sub-screen SHALL show the same reason and buttons as Askeleet (asking or
opening Health Connect's settings from there) and no list; the permission SHALL NOT be asked when
the sub-screen merely opens. The default SHALL be "Automaattinen".

#### Scenario: First visit
- **GIVEN** Garmin Connect and the phone wrote steps in the last 30 days and the user never chose
- **WHEN** the user opens Asetukset → Askeleet
- **THEN** "Automaattinen" is selected with the line "Nyt: Garmin Connect", followed by Garmin
  Connect and Puhelin with their averages per day, and "Kaikki (Health Connectin järjestys)"

#### Scenario: Order and averages
- **GIVEN** Garmin Connect has 150 000 steps in 30 days on 30 days with steps (5 000 a day) and the
  phone has 120 000 steps on 12 days with steps (10 000 a day)
- **THEN** Garmin Connect is listed first with "keskim. 5 000 askelta/pv", then Puhelin with
  "keskim. 10 000 askelta/pv"

#### Scenario: Pick an app
- **WHEN** the user picks Puhelin
- **THEN** the row reads "Puhelin" and the steps card and Askeleet show the phone's steps only

#### Scenario: Name not found
- **GIVEN** an app wrote steps but its name cannot be read
- **THEN** it is listed by its package name

#### Scenario: Row before any check
- **GIVEN** the automatic pick stored yesterday is Garmin Connect and no check has run since the
  app started
- **WHEN** Asetukset is shown
- **THEN** the row reads "Automaattinen: Garmin Connect"

#### Scenario: Check failed under Automaattinen
- **GIVEN** the stored pick is Garmin Connect and the check fails (for example Health Connect's
  rate limit)
- **WHEN** the sub-screen is shown
- **THEN** "Nyt: Garmin Connect" is still shown and the list falls back to the failure line

#### Scenario: Back
- **WHEN** the user presses back on the sub-screen
- **THEN** Asetukset's list is shown

### Requirement: Automatic steps source
With "Automaattinen", the source SHALL be the app with the most steps over the last 30 days
(today included). The phone's own step records (old and new attribution) SHALL count as one app,
"Puhelin". The 30-day totals SHALL come from a source check, which SHALL run under every choice
(Automaattinen, one app or Kaikki), so the pick and each app's recent steps stay known: at most
once per local day, on foreground reads only, and whenever the source sub-screen is entered. A
check that succeeds on entering the sub-screen counts as the day's check. A check that fails
(any Health Connect error, including its rate limit) SHALL NOT count: it is tried again at the next
resume at least an hour later. The first read with no stored pick under "Automaattinen" SHALL wait
for the check; on later days the read SHALL use the stored pick at once and the check SHALL run
after it, re-reading only when the pick changed. The app in use SHALL be kept unless another app's
30-day total exceeds it by more than 10 %. A change of the pick SHALL be silent (no notice) apart
from the log line and the source shown on Askeleet and in Asetukset, and SHALL re-read the shown
range. When no app wrote steps in the last 30 days, or the check fails and no earlier pick is
known, steps SHALL be read as with "Kaikki (Health Connectin järjestys)". When the check fails and
an earlier pick is known, that pick SHALL stay in use. The apps checked SHALL be the phone, Samsung
Health, Garmin Connect, Fitbit, Google Fit, the stored choice and the stored pick, each with one
filtered read, plus any other app found in the origins of the latest step records or of the
unfiltered aggregate. "Wrote steps in the last 30 days" SHALL mean that app's filtered 30-day total
is above 0.

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

#### Scenario: Checked once a day
- **GIVEN** the check succeeded this morning
- **WHEN** the app returns to the foreground three times
- **THEN** no new check runs; the next local day's first foreground read runs one

#### Scenario: Failed check retried later
- **GIVEN** today's check failed at 10.00
- **WHEN** the app resumes at 10.30 and again at 11.15
- **THEN** no check runs at 10.30 and one runs at 11.15

#### Scenario: Check under a chosen app
- **GIVEN** the user chose Garmin Connect
- **WHEN** the day's check runs
- **THEN** the source stays Garmin Connect; the check only updates whether Garmin Connect wrote
  steps in the last 30 days and the stored pick

#### Scenario: Phone's package names unknown
- **GIVEN** the system cannot tell the phone's current steps package, but an earlier check stored
  the phone's packages
- **THEN** the stored packages are used for the phone; with none stored, `android` alone

### Requirement: Chosen app without recent steps
An app the user picked SHALL stay the source even when it wrote no steps in the last 30 days or is
no longer installed; it SHALL then stay in the source list with "ei askelia 30 päivään", and the
Asetukset row and the Askeleet source line SHALL say so in the warning tint, once today's source
check has run. Until then, whether the app has recent steps is unknown and nothing SHALL show the
warning tint. The last day each app was seen with steps SHALL be kept on the phone. The app SHALL
NOT switch to another source by itself.

#### Scenario: Watch app stopped syncing
- **GIVEN** the user picked Garmin Connect and it wrote nothing for 30 days
- **WHEN** today's check has run and Asetukset is shown
- **THEN** the Askeleet row reads "Garmin Connect: ei askelia 30 päivään" in the warning tint and
  steps are still read from Garmin Connect only

#### Scenario: Not yet checked
- **GIVEN** the user picked Garmin Connect and today's check has not run yet
- **THEN** the row reads "Garmin Connect" with no warning tint

#### Scenario: Uninstalled app
- **GIVEN** the user picked an app that has since been uninstalled
- **THEN** it is listed by its package name with "ei askelia 30 päivään" until the user picks
  another source

### Requirement: Source changes and late results
At most one source check SHALL run at a time. A result of a read or a check whose source is no
longer the current one SHALL be dropped. Choosing a source SHALL cancel a running read and start
the read of the new source. After a change of source, a read that fails SHALL show the read-failure
state, never the previous source's numbers.

#### Scenario: Quick double change
- **GIVEN** the user picks Puhelin and a second later Garmin Connect, while the first read runs
- **THEN** only Garmin Connect's read is shown, whatever order the answers come in

#### Scenario: Failed read after a change
- **GIVEN** the source was Garmin Connect with numbers shown
- **WHEN** the user picks Puhelin and its read fails
- **THEN** the failure state is shown, not Garmin Connect's numbers

### Requirement: Source choice stays on the phone
The source choice, the automatic pick, the days apps were last seen and the phone's package names
SHALL be kept only on this phone: not in the data file, the backup file or the phone's own cloud
backup and transfer. After a reinstall or on a new phone the choice SHALL read as "Automaattinen".

#### Scenario: New phone
- **GIVEN** the user picked Puhelin and moves to a new phone with the app's data transferred
- **THEN** the new phone uses "Automaattinen"
