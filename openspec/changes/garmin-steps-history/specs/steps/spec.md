# Spec Delta

## MODIFIED Requirements

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

### Requirement: Steps stay out of the data file and the backup
Steps SHALL NOT be a data kind of the data file (export, import and the automatic backup file):
they are measured by another app and live in Health Connect and Garmin Connect. The imported Garmin
history SHALL travel with Android's own backup and device transfer (as the products do) and SHALL
NOT be in the data file.

#### Scenario: Export with steps in Health Connect
- **GIVEN** Health Connect holds a year of steps and a Garmin history is imported
- **WHEN** the user exports their data
- **THEN** the file holds no steps

## ADDED Requirements

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
Garmin's other data (sleep, weight, heart rate …) SHALL NOT be read. Days after today SHALL be
skipped.

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
app Automaattinen uses now) or "Kaikki (Health Connectin järjestys)", and only for a day for which
that source has no steps in Health Connect; a day Health Connect has SHALL keep Health Connect's
total. Today SHALL never come from the import. With any other source the import SHALL be ignored.
Imported days SHALL count like other days with steps everywhere steps are shown: the Askeleet
bars, average line, statistics, day list and Kaikki start (the first day with steps of either),
and the front page steps card. Imported days SHALL be shown without the history permission, and
the history notice SHALL appear only for days the import does not cover.

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
