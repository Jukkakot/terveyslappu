# Tasks

## 1. Project and build

- [x] 1.1 Gradle project (wrapper, version catalog, `app` module, minSdk 34, Compose, Material 3, Finnish strings, light/dark theme) and verify `./gradlew assembleDebug` succeeds in the cloud session
- [x] 1.2 Session-start hook installs the Android SDK and writes `local.properties`; verify a fresh shell can run `./gradlew test` after the hook
- [x] 1.3 Versioning (commit count, `0.1.<count>-<sha>`) and release signing from `RELEASE_*` env vars or `keystore.properties`; verify `./gradlew printVersionName` prints the expected form
- [x] 1.4 `.gitignore` covering build output, `local.properties`, keystores and export file types (`*.csv`, `*.zip`, `*.db`, `*.log`, `*.mp4`, screenshots) per NFR-1; verify `git status` stays clean after a build

## 2. Log

- [x] 2.1 File logger with 1 MB rotation and crash handler; verify unit tests for rotation and line format pass
- [x] 2.2 FileProvider and share intent ("Jaa loki" shares both log files); verify a Robolectric test that the intent carries the files

## 3. Health Connect weights

- [x] 3.1 Weight input parsing and validation (comma/dot, 20–300 kg, no future time); verify unit tests for each spec scenario of "Reject impossible weights"
- [x] 3.2 Health Connect repository: availability check, permission set, write weight (manual entry, client record id), read last 30 days, delete own record, source app label; verify unit tests for record mapping and own-vs-other detection
- [x] 3.3 Main screen: permission explanation and request, unavailable/denied states, weight form with date and time pickers, recent list with source app, long-press delete with confirmation, menu with version and Jaa loki; verify Robolectric smoke test for the no-permission and form-enabled states
- [x] 3.4 Log events for start, availability, permission result, save, read, delete and errors; verify by reading the log lines produced in the unit tests
- [x] 3.5 Durations (`ms`) on Health Connect save, read and delete log lines; a failing intent or missing log file shows a short message (NFR-8, NFR-9); verify `./gradlew test lint` passes

## 4. CI and release

- [x] 4.1 `.github/workflows/ci.yml`: test, lint, assembleRelease on push/PR with docs-only paths ignored; publish job replacing the `latest-build` release with `terveyslappu.apk` when the signing secret exists; verify the workflow passes on the first push
- [x] 4.2 Write the user's one-time steps (signing secret, Obtainium) into `.claude/CLAUDE.md` → Build, release, logs; verify the steps name the exact secret and URL

## 5. Docs

- [x] 5.1 Update `product.md` findings and `roadmap.md` (phase 1 done, Q1 still to check on the phone); verify `openspec validate poc-manual-weight` passes
