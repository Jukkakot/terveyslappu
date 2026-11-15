# Tasks

## 1. Reader and store

- [ ] 1.1 Pure reader: from a ZIP `InputStream` (streamed with `ZipInputStream`), parse every `.../DI_CONNECT/DI-Connect-Aggregator/UDSFile_*.json` entry (JSON array; read `calendarDate`, `totalSteps`); skip null/<1/bad dates and days from the import day on; duplicates keep the larger; result = sorted day → steps, plus entry count. Parse each entry with a streaming JSON reader so memory stays bounded.
- [ ] 1.2 Store `garmin-steps.json` in app storage (one atomic write, replace whole), with delete and restore for Kumoa; singleton in `TerveysApp`; add to `data_extraction_rules.xml` (both sections) like `products.json`; update `DataTransferRulesTest`.
- [ ] 1.3 Unit tests with a made-up ZIP built in the test: nested folder prefix, two UDS files with an overlapping day, null and zero steps, bad date, a non-Garmin ZIP, a future day.

## 2. Totals

- [ ] 2.1 Merge rule (pure, in `steps/`): given the source in use (Garmin Connect package `com.garmin.android.apps.connectmobile`, or Kaikki) fill days without Health Connect steps from the import, never today; other sources ignore it. Kaikki start = first day with steps of either.
- [ ] 2.2 Wire it into `StepsController` so `StepsSeries` (Askeleet) and `HomeSteps` (card) see merged days; imported days need no history permission; follow design.md 2-6.
- [ ] 2.3 Tests: the scenarios of "Imported Garmin days in the totals" (before HC, other source, gap in HC, today never imported).

## 3. Screens

- [ ] 3.1 Tuo tiedot: picker MIME types include `application/zip` (and `application/x-zip-compressed`); detect a ZIP by its magic bytes `PK\x03\x04` before the other formats; show the Garmin preview (range with years, day count, Peruuta/Tuo); messages as specified.
- [ ] 3.2 Asetukset → Askeleet: the "Garminin askelhistoria" row with range, count and red Poista + Kumoa bar (`AppSnackbar.showTimed`); the hint line when empty; visible without the steps permission.
- [ ] 3.3 Log events as specified; add them to the log list in `.claude/CLAUDE.md`, and the new store to the map there.
- [ ] 3.4 Smoke test: the Asetukset row shows the range after an import.

## 4. Context

- [ ] 4.0 Ohje: one sentence that the imported history shows with Garmin Connect or Kaikki as the source.

- [ ] 4.1 `product.md`: steps decision updated (Garmin history imported into app storage, HC still read-only); roadmap backlog entry done.
- [ ] 4.2 Bump MINOR of `appVersion` in `gradle.properties`.
- [ ] 4.3 Run `./gradlew test lint assembleDebug`.
