# Tasks

## 1. Reader and store

- [x] 1.1 Pure reader: from a ZIP `InputStream` (streamed with `ZipInputStream`), parse every `.../DI_CONNECT/DI-Connect-Aggregator/UDSFile_*.json` entry (JSON array; read `calendarDate`, `totalSteps`); skip null/<1/bad dates and days from the import day on; duplicates keep the larger; result = sorted day → steps, plus entry count. Parse each entry with a streaming JSON reader so memory stays bounded.
- [x] 1.2 Store `garmin-steps.json` in app storage (one atomic write, replace whole), with delete and restore for Kumoa; singleton in `TerveysApp`; add to `data_extraction_rules.xml` (both sections) like `products.json`; update `DataTransferRulesTest`.
- [x] 1.3 Unit tests with a made-up ZIP built in the test: nested folder prefix, two UDS files with an overlapping day, null and zero steps, bad date, a non-Garmin ZIP, a future day.

## 2. Totals

- [x] 2.1 Merge rule (pure, in `steps/`): given the source in use (Garmin Connect package `com.garmin.android.apps.connectmobile`, or no origin filter: Kaikki / Automaattinen without a pick) take per day the larger of Health Connect and the import (import alone where HC has none), never today; other sources ignore it. Kaikki start = first day with steps of either.
- [x] 2.2 Wire it into `StepsController` so `StepsSeries` (Askeleet) and `HomeSteps` (card) see merged days; imported days need no history permission; follow design.md 2-6.
- [x] 2.3 Tests: the scenarios of "Imported Garmin days in the totals" (before HC, other source, gap in HC, today never imported).

## 3. Screens

- [x] 3.1 Tuo tiedot: picker MIME types include `application/zip` (and `application/x-zip-compressed`); detect a ZIP by its magic bytes `PK\x03\x04` before the other formats; show the Garmin preview (range with years, day count, Peruuta/Tuo); messages as specified.
- [x] 3.2 Asetukset → Askeleet: the "Garminin askelhistoria" row with range, count and red Poista + Kumoa bar (`AppSnackbar.showTimed`); the hint line when empty; visible without the steps permission.
- [x] 3.3 Log events as specified; add them to the log list in `.claude/CLAUDE.md`, and the new store to the map there.
- [x] 3.4 Smoke test: the Asetukset row shows the range after an import.

## 4. Context

- [x] 4.0 Ohje: one sentence that the imported history shows with Garmin Connect or Kaikki as the source.

- [x] 4.1 `product.md`: steps decision updated (Garmin history imported into app storage, HC still read-only); roadmap backlog entry done.
- [x] 4.2 Bump MINOR of `appVersion` in `gradle.properties`.
- [x] 4.3 Run `./gradlew test lint assembleDebug`.

## Implementer's notes (decisions the spec left open)

- Not-a-Garmin ZIP / no steps: the text is shown inside the import dialog with Sulje (like the weight import's "nothing found").
- A failed save on Tuo shows "Askelhistoriaa ei voitu tallentaa. Syy on lokissa." and logs `steps.garmin.import failed cause=`.
- The preview's text: "18.7.2023 – 8.11.2026, 1 206 päivää" under the dialog title "Garminin askelhistoria"; the count uses the steps number format (narrow no-break space).
- The "näkyy, kun lähde on …" suffix is decided from the stored choice and pick (no source check runs on Tuo).
- Asetukset → Askeleet: the row is its own group below Lähde; Poista is a red text button.
- Peruuta while reading drops the result; the stream read itself runs to its end in the background.
