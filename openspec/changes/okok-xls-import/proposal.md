# Proposal

## Why

The user's OKOK International history is exported only as an old binary Excel file (`.xls`, BIFF8),
not CSV (Q2, user 2026-11-09), so Tuo historia cannot read it and the file picker does not even offer
it. Converting it by hand in a spreadsheet app is an extra step the app should save.

## What Changes

- Tuo historia also offers and reads `.xls` files (old binary Excel), first sheet.
- The OKOK layout is recognised: title rows before the header (name; gender, age, height; the
  covered range `YYYY-MM-DD ~ YYYY-MM-DD(nDay)`), a header row `Date;Time;Weight(Kg);BMI;...`, rows
  newest first with dates `MM-DD` without a year and all values as text.
- The year of each row is worked out from the range line: the newest row is in the range's end year,
  and going down the file the year drops by one whenever the month-day goes up.
- Only date, time and weight are imported, as today; OKOK's computed columns (BMI, body fat, muscle
  and so on) are ignored (body composition is out, Q4).
- Everything after reading is the existing flow: preview, write without duplicates, log.
- Not now: `.xlsx`, other sheets.

## Capabilities

### New Capabilities

### Modified Capabilities
- `history-import`: pick and read `.xls` files; the OKOK layout with year-less dates.

## Impact

History import only: the file picker types and a small reader for the binary Excel format
(compound-document container and the cell records), written in the app without a library. No new
permission, no UI change beyond the picker accepting the file.
