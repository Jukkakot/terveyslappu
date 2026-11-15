# Spec Delta

## MODIFIED Requirements

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
the sub-screen merely opens. The default SHALL be "Automaattinen". The chosen source SHALL also be the source of Kulutus
(energy-burn), and the sub-screen SHALL say so in one line under "Lähde".

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

### Requirement: Import Garmin step history
Tuo tiedot SHALL also accept a ZIP file (the picker offers ZIP files next to the current types)
and SHALL recognise Garmin's account data export by its entries
`DI_CONNECT/DI-Connect-Aggregator/UDSFile_*.json` (any folder depth before `DI_CONNECT` allowed).
Each such entry is a JSON array of daily summaries; the app SHALL read `calendarDate`
("yyyy-MM-dd"), `totalSteps`, `activeKilocalories` and `bmrKilocalories` (lepo) and SHALL keep a day's steps only when `totalSteps` is at least 1 and its kcal only when both kcal
fields are numbers, skipping a summary that has neither or
whose date does not parse, and SHALL ignore every other entry and field. The ZIP SHALL be read as a
stream, without unpacking it to storage, and a ZIP of 100 MB SHALL be read without running out of
memory. When a day appears twice, the larger step count SHALL be kept. Before saving, the app SHALL
show a preview: "Garminin historia", the first and last day with years, the number of days
with steps and the number of days with kcal, and the buttons Peruuta and Tuo. Tuo SHALL replace any earlier imported history with
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

#### Scenario: Earlier import without kcal
- **GIVEN** a history imported before kcal were read
- **THEN** its steps keep working and Asetukset says to import the ZIP again to get Kulutus

### Requirement: Imported history in Asetukset
Asetukset → Askeleet SHALL show, under the source choice, a row "Garminin historia" with the
imported range and day count (and, when the stored history has no kcal, the line "Tuo ZIP
uudelleen, niin kulutus tulee mukaan") ("18.7.2023 – 8.11.2026 · 1 206 päivää") and a red "Poista" button,
or, with nothing imported, the line "Tuo Garminin tietojen latauksen ZIP kohdasta Tuo tiedot". The
row SHALL show also without the steps permission. Poista SHALL delete the imported history at once
and show "Askelhistoria poistettu · Kumoa"; Kumoa SHALL bring it back unchanged. The steps card and
Askeleet SHALL update at once after an import, a delete and an undo.

#### Scenario: Delete and undo
- **GIVEN** an imported history
- **WHEN** the user taps Poista and then Kumoa
- **THEN** the history is back with the same range and count
