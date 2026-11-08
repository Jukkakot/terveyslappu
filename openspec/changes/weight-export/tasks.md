# Tasks

## 1. Export core

- [x] 1.1 `history/CsvExport`: rows from weight entries (date, time in the record offset, comma decimal, source label, quoting when needed); JVM test of the row format and that `HistoryParser` reads an export back to the same weights
- [x] 1.2 `HealthConnectWeights.all()`: paged read of every weight (page token), plus history-permission availability; compiles

## 2. UI

- [x] 2.1 Menu "Vie painot" → history permission request when available and not yet asked → `CreateDocument("text/csv")` → write on IO → snackbar with count (and "vain 30 päivää" note); log `export.*` with count, ms and history flag; Robolectric smoke test that the menu item exists
- [x] 2.2 Manifest `READ_HEALTH_DATA_HISTORY`; `./gradlew test lint assembleDebug` passes

## 3. Docs

- [x] 3.1 Update `.claude/CLAUDE.md` map/log events, roadmap backlog (export done), privacy text mentions the export

## Workflow follow-up

- Archive after merged and CI green.
