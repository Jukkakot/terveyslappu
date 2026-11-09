# Proposal

## Why

The user's OKOK International history is exported only as an old binary Excel file (`.xls`, BIFF8),
not CSV (Q2, user 2026-11-09), so Tuo historia cannot read it and the file picker does not even offer
it. The user wants everything in the file kept, as it is, while the app shows only the weight
(user 2026-11-09).

## What Changes

- Tuo historia also offers and reads `.xls` files with a library (Apache POI's `poi` module, the
  HSSF reader for `.xls`) instead of a hand-written reader (user 2026-11-09: libraries first).
- The OKOK layout is recognised: title rows before the header (name; gender, age, height; the
  covered range `YYYY-MM-DD ~ YYYY-MM-DD(nDay)`), a header row `Date;Time;Weight(Kg);BMI;...`, rows
  newest first with dates `MM-DD` without a year and all values as text.
- The year of each row is worked out from the range line: the newest row is in the range's end year,
  and going down the file the year drops by one whenever the month-day goes up.
- The weight goes to Health Connect as today. Every other column of the row (BMI, body fat, muscle,
  water, bone mass, body age ... 19 columns) and the profile from the title rows (gender, age,
  height) go to the raw-data archive (scale-reading-archive) exactly as written in the file, under
  their original column names, marked as source `okok`. Note: OKOK's file holds no impedance; these
  columns are OKOK's own calculations, kept as found, never recalculated.
- Everything after reading is the existing flow: preview, write without duplicates, log.
- Not now: `.xlsx`, other sheets.

## Capabilities

### New Capabilities

### Modified Capabilities
- `history-import`: pick and read `.xls` files; the OKOK layout with year-less dates; all columns
  kept in the archive.

## Impact

History import only, plus the archive from scale-reading-archive (do that change first). New
dependency: Apache POI (`org.apache.poi:poi`, HSSF only, not `poi-ooxml`). No new permission, no UI
change beyond the picker accepting the file.
