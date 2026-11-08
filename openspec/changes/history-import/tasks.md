# Tasks

## 1. Parser (pure Kotlin)

- [x] 1.1 `history/Table`: separator detection and quote-aware split; JVM tests for `;`, tab, `,` with quoted decimals
- [x] 1.2 `history/DateParsing`: ISO, `d.M.yyyy`, `yyyy/M/d`, slash day/month order for the whole file, epoch s/ms, optional separate time; JVM tests
- [x] 1.3 `history/HistoryParser`: header keywords and value-based column detection, kg/lb, comma decimals, skip reasons (unreadable, range, future, duplicate moment); JVM tests for the spec scenarios with made-up data

## 2. Writing

- [x] 2.1 `HealthConnectWeights.writeImported`: batches of 200, deterministic ids, progress callback, resume from a batch; unit test for the ids and batch split

## 3. UI

- [x] 3.1 `MainViewModel` import state (reading, preview, writing with progress, failed with saved count, done) and logging; JVM test of the state flow with a fake writer where practical
- [x] 3.2 Menu entry "Tuo historia", file picker, preview/progress dialog; Robolectric smoke test that the preview shows the count and a Tuo button
- [x] 3.3 `./gradlew test lint assembleDebug` passes

## 4. Docs

- [x] 4.1 Update `.claude/CLAUDE.md` map and log events, roadmap (phase 3), questions (Q2 note)

## Workflow follow-up

- Archive after merged and CI green.
