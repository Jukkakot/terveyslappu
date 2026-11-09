# Proposal

## Why

Derived values (BMI first, maybe more later) need the phone owner's height, age and gender, and the
app keeps only raw data. The phone has one user, so one set of details in Asetukset is enough.

## What Changes

- Asetukset gets an "Omat tiedot" section: syntymävuosi (shown with the age it gives), pituus (cm)
  and sukupuoli (Mies / Nainen / Ei valittu). All optional; nothing else in the app requires them.
- The details are kept in the app's own storage on the phone, one set per phone, no profiles.
- The export and the automatic backup (same format) carry the details in one `#profile;` line
  before the header; Tuo painot fills the phone's empty details from it, so a new phone gets them
  back with the weights (user 2026-11-09).
- A saved height is also written to Health Connect as a height record (own permission, asked
  when the height is first saved); Health Connect has no record for birth year or gender
  (user 2026-11-09).
- Logged as `profile.set` with which field changed, never the value (NFR-1).
- Decided not to do now (user 2026-11-09): BMI or other derived values, filling the details from an OKOK file's title rows.

## Capabilities

### New Capabilities
- `owner-profile`: the phone owner's details (birth year, height, gender), how they are entered,
  validated and kept.

### Modified Capabilities
- `weight-export`: "File content" gains the optional `#profile;` line (the backup uses the same
  format, so `weight-backup` needs no own delta).
- `history-import`: new requirement to restore owner details from that line.

## Impact

App screens: Asetukset and the import preview. Health Connect: new height write permission and record. No scale, widget or CI change. New
pure validation logic, a small preferences store next to `AppearancePrefs` (singleton in
`TerveysApp`), the CSV export writer and the history parser.
