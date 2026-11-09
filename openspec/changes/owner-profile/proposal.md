# Proposal

## Why

Derived values (BMI first, maybe more later) need the phone owner's height, age and gender, and the
app keeps only raw data. The phone has one user, so one set of details in Asetukset is enough.

## What Changes

- Asetukset gets an "Omat tiedot" section: syntymävuosi (shown with the age it gives), pituus (cm)
  and sukupuoli (Mies / Nainen / Ei valittu). All optional; nothing else in the app requires them.
- The details are kept in the app's own storage on the phone, one set per phone, no profiles.
- Logged as `profile.set` with which field changed, never the value (NFR-1).
- Nothing uses the details yet; BMI or other derived values are later changes.

## Capabilities

### New Capabilities
- `owner-profile`: the phone owner's details (birth year, height, gender), how they are entered,
  validated and kept.

### Modified Capabilities

## Impact

App screens: Asetukset only. No Health Connect, scale, widget or CI change. New pure validation
logic and a small preferences store next to `AppearancePrefs`; singleton in `TerveysApp`.
