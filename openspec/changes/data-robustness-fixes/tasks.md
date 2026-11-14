# Tasks

## 1. CSV offset
- [x] 1.1 `CsvData` writes the offset; `DateParsing` reads a time column with or without one; tests (zones, DST hour, old files)

## 2. Backup shrink
- [ ] 2.1 `WeightBackup` guard, `BackupStatus.recentOnly` and refusal flag, Asetukset notes; test with a fake write

## 3. Stale now
- [ ] 3.1 `MainUiState.loadedAt` set on every load; Historia and Home key `now` on it; test

## 4. Pending action
- [ ] 4.1 Pending history action as an enum in `MainViewModel`; Activity runs it after the permission screen

## 5. Docs
- [ ] 5.1 CLAUDE.md log events; `./gradlew test lint assembleDebug` green
