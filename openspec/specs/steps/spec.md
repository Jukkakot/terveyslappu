# steps Specification

## Purpose
TBD - created by archiving change steps. Update Purpose after archive.

## Requirements

### Requirement: Steps come from Health Connect, read only
The app SHALL show the user's daily step totals as Health Connect reports them for the steps
source in use (see "Steps source setting"), filled with the imported Garmin history where "Imported
Garmin days in the totals" allows, and SHALL NOT write, change or delete steps in Health Connect.
With one app as the source, a day's total SHALL be that app's own daily total, so steps another app
wrote for the same day never fill its gaps, and the source SHALL be that one app for the whole
history: no day falls back to another app. With "Kaikki (Health Connectin järjestys)" the total
SHALL be Health Connect's merged daily total over all apps. Steps SHALL be shown only inside the
app (no widget, tile or notification).

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
- **WHEN** the user uses any part of the app, the Garmin import included
- **THEN** no steps are written to or deleted from Health Connect
- Covered by design: a manifest/lint test that the write-steps permission is absent.

### Requirement: Days and gaps
A day SHALL be the local day as Health Connect's daily aggregate reports it. A day for which Health
Connect holds no steps SHALL be a gap, never a zero bar, and SHALL be left out of averages and day
counts. Today SHALL be shown but left out of the averages, the average line and the weekly bar
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
- **THEN** with no imported Garmin history the chart starts 100 days ago, at Garmin Connect's
  first day, and no earlier day shows the phone's steps; with an imported history it starts at the
  first day with steps of either
- Whoever wants the phone's older steps picks Puhelin, or "Kaikki (Health Connectin järjestys)",
  which shows the full history; Ohje says so in one sentence. No notice appears when the source
  is chosen or changed.

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
Steps SHALL NOT be a data kind of the data file (export, import and the automatic backup file):
they are measured by another app and live in Health Connect and Garmin Connect. The imported Garmin
history SHALL travel with Android's own backup and device transfer (as the products do) and SHALL
NOT be in the data file.

#### Scenario: Export with steps in Health Connect
- **GIVEN** Health Connect holds a year of steps and a Garmin history is imported
- **WHEN** the user exports their data
- **THEN** the file holds no steps

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

### Requirement: Import Garmin step history
Tuo tiedot SHALL also accept a ZIP file (the picker offers ZIP files next to the current types)
and SHALL recognise Garmin's account data export by its entries
`DI_CONNECT/DI-Connect-Aggregator/UDSFile_*.json` (any folder depth before `DI_CONNECT` allowed).
Each such entry is a JSON array of daily summaries; the app SHALL read `calendarDate`
("yyyy-MM-dd") and `totalSteps` and SHALL skip summaries whose `totalSteps` is null or below 1 or
whose date does not parse, and SHALL ignore every other entry and field. The ZIP SHALL be read as a
stream, without unpacking it to storage, and a ZIP of 100 MB SHALL be read without running out of
memory. When a day appears twice, the larger step count SHALL be kept. Before saving, the app SHALL
show a preview: "Garminin askelhistoria", the first and last day with years, the number of days
with steps, and the buttons Peruuta and Tuo. Tuo SHALL replace any earlier imported history with
this one in one write and show "Askelhistoria tuotu: N päivää". A ZIP that is not a Garmin export,
or holds no day with steps, SHALL show "Tiedostosta ei löytynyt Garminin askelia" and save nothing.
Garmin's other data (sleep, weight, heart rate …) SHALL NOT be read. Only days before the day of the import SHALL be
stored.

#### Scenario: Import
- **GIVEN** a Garmin export ZIP with daily summaries from 18.7.2023 to 8.11.2026, 1 206 of them
  with steps
- **WHEN** the user picks it in Tuo tiedot
- **THEN** the preview reads Garminin askelhistoria, 18.7.2023 – 8.11.2026, 1 206 päivää, and Tuo
  saves them

#### Scenario: Not a Garmin ZIP
- **WHEN** the user picks a ZIP without UDSFile entries
- **THEN** "Tiedostosta ei löytynyt Garminin askelia" is shown and nothing changes

#### Scenario: Newer export
- **GIVEN** an imported history to 8.11.2026
- **WHEN** the user imports a newer export to 1.2.2027
- **THEN** the stored history is the newer export's days only

### Requirement: Imported Garmin days in the totals
An imported day SHALL be used only when the steps source in use is Garmin Connect (chosen, or the
app Automaattinen uses now) or "Kaikki (Health Connectin järjestys)", for a day Health Connect has no
steps for, and on a day both have, the larger of the two counts SHALL be shown. Today SHALL never come from the import. With any other source the import SHALL be ignored.
Imported days SHALL count like other days with steps everywhere steps are shown: the Askeleet
bars, average line, statistics, day list and Kaikki start (the first day with steps of either),
and the front page steps card. Imported days SHALL be shown without the history permission; the history notice is unchanged.
Without the steps permission, or with Health Connect unavailable, the card and Askeleet SHALL stay
as without an import. Own charts with steps SHALL use the same merged days.

#### Scenario: Before Health Connect
- **GIVEN** Garmin Connect is the source with Health Connect steps from 15.8.2026 and an import
  from 18.7.2023 to 8.11.2026
- **WHEN** the user picks Kaikki on Askeleet
- **THEN** the chart starts 18.7.2023 and days from 15.8.2026 on show Health Connect's totals

#### Scenario: Other source
- **GIVEN** the same import and Puhelin as the source
- **THEN** no imported day is shown

#### Scenario: Gap in Health Connect
- **GIVEN** Garmin Connect is the source and Health Connect has no steps for 3.10.2026 while the
  import has 7 400
- **THEN** 3.10.2026 shows 7 400 steps

#### Scenario: Partial day
- **GIVEN** Kaikki as the source, Health Connect 2 000 steps for 1.3.2025 and the import 9 000
- **THEN** 1.3.2025 shows 9 000 steps

### Requirement: Imported history in Asetukset
Asetukset → Askeleet SHALL show, under the source choice, a row "Garminin askelhistoria" with the
imported range and day count ("18.7.2023 – 8.11.2026 · 1 206 päivää") and a red "Poista" button,
or, with nothing imported, the line "Tuo Garminin tietojen latauksen ZIP kohdasta Tuo tiedot". The
row SHALL show also without the steps permission. Poista SHALL delete the imported history at once
and show "Askelhistoria poistettu · Kumoa"; Kumoa SHALL bring it back unchanged. The steps card and
Askeleet SHALL update at once after an import, a delete and an undo.

#### Scenario: Delete and undo
- **GIVEN** an imported history
- **WHEN** the user taps Poista and then Kumoa
- **THEN** the history is back with the same range and count

### Requirement: Garmin import in the log
The log SHALL record `steps.garmin.read ok days= first= last= entries= ms=` or
`steps.garmin.read failed cause=` when a ZIP is read, `steps.garmin.import ok days=` on Tuo,
`steps.garmin.delete` and `steps.garmin.undo`, never step counts.

#### Scenario: Read logged
- **WHEN** a Garmin ZIP is read
- **THEN** the log has one `steps.garmin.read ok` line with the day count and range and no step counts
