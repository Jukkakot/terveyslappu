# Tasks

## 1. Backup core

- [x] 1.1 `backup/BackupStatus` (off/ok/failed with times and count, `isDue` 24 h, `isStale` 3 d); JVM tests
- [x] 1.2 `backup/WeightBackup`: prefs persistence, enable (persist URI permission) / disable (release), `run(reason)` with mutex and coalescing, writes the export; created in `TerveysApp`; JVM test of coalescing with a fake writer

## 2. Triggers and UI

- [x] 2.1 Trigger after manual save, delete, import (MainViewModel) and scale save (WeighInViewModel, app scope), and on main-screen resume when due
- [x] 2.2 Status card on the main screen and menu entries (back up now, change file, turn off); enable flow with picker and history permission; Robolectric smoke test for the off and failed cards
- [x] 2.3 `./gradlew test lint assembleDebug` passes

## 3. Docs

- [x] 3.1 Update `.claude/CLAUDE.md` map/log events, product.md (phase 4 interim decision), roadmap

## Workflow follow-up

- Archive after merged and CI green.
