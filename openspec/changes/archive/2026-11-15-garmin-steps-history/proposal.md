# Proposal

## Why

Askeleet starts at 15.8.2026 because Garmin Connect wrote to Health Connect only from when the link
was enabled (plus about a month back). The user has years of steps in Garmin and shared Garmin's
account data export (user, 2026-11-15): a ZIP whose `DI_CONNECT/DI-Connect-Aggregator/
UDSFile_<from>_<to>.json` files hold one daily summary per day (`calendarDate` "yyyy-MM-dd",
`totalSteps` int or null), here from 2023 on. The user wants the easiest way to see that history
in the app now.

## What Changes

- Tuo tiedot also accepts Garmin's export ZIP. The app reads only the UDSFile daily summaries and
  keeps day → steps in its own storage ("Garminin askelhistoria"), replacing any earlier import.
  Nothing is written to Health Connect (steps stay read-only there).
- Askeleet, the steps card and their stats use the imported days for days Health Connect has no
  steps for, when the steps source in use is Garmin Connect (chosen, or picked by Automaattinen)
  or "Kaikki". Health Connect wins on days it has. Today never comes from the import.
- Asetukset → Askeleet shows the imported range with "Poista" (Kumoa as everywhere).
- The import travels with Android's backup/transfer like products; it stays out of the data file
  and the automatic backup file.

Parts touched: app screens (Tuo tiedot, Askeleet, steps card, Asetukset → Askeleet), app storage,
Android backup rules. No Health Connect write.

## Capabilities

### New Capabilities

### Modified Capabilities
- `steps`: read only (plus the imported history), stay out of the data file (but in Android
  transfer), new requirements for the Garmin import, how imported days are used, removing it.

## Impact

New pure reader for the export (ZIP entries → day/steps), a store file `garmin-steps.json`,
`steps/StepsController`/`StepsSeries` merge, Tuo tiedot's picker MIME types and format detection,
`ui/StepsSourcePage`, `res/xml/data_extraction_rules.xml` (and the legacy backup rules), log events.
