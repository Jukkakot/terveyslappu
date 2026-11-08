# Design

## Context

Empty repo. The user develops from the phone through cloud sessions and has no cable debugging;
builds reach the phone through GitHub releases and Obtainium. `rubikki-solveri` (same user,
Kotlin + Compose) already solved signing, versioning, CI and log sharing; this project copies
those patterns in a single-module form.

## Goals / Non-Goals

**Goals:** a small app that installs and updates without friction, writes and reads Health
Connect weight records correctly, and leaves a log good enough to debug from a distance.

**Non-Goals:** scale, widget and tile (next change); own storage or cloud copy; charts; English UI.

## Decisions

1. **Single Gradle module `app`, package `fi.jukkakot.terveyslappu`.** Scale decoding will come
   as plain Kotlin in the same module; a separate module is not worth it yet.
2. **Toolchain as in rubikki-solveri** (Gradle wrapper, AGP, Kotlin, Compose BOM from its version
   catalog), so both projects build the same way. Health Connect client `1.1.0` (latest stable).
3. **minSdk 34 (Android 14).** Health Connect is part of the system from Android 14, so no separate
   Health Connect app or install flow is needed; the user's Samsung runs a newer Android. The
   unavailable state is still handled (spec) but needs no store install path beyond a link.
4. **No local database.** The list is read from Health Connect each time the screen opens or after
   a save; Health Connect is the only store (NFR-2).
5. **Records written with metadata "manual entry"** and a client record id, so this app's records
   are recognisable (delete only own records) and Samsung Health sees a plain weight record.
6. **Source app name** comes from the record's data origin package, resolved to the app label
   through the package manager (needs a `<queries>` entry for the known health apps; unknown
   packages show the package name).
7. **Log:** a small file logger (`filesDir/logs/app.log`, rotated to the newest 1 MB, previous
   file kept as `app.log.1` and both shared), uncaught exception handler that writes the stack
   trace, share through a `FileProvider`, copied from rubikki-solveri's `log/` and `ShareLog.kt`.
   Logcat gets the same lines.
8. **Versioning:** versionCode = commit count, versionName `0.1.<count>-<sha>`, as in rubikki.
9. **CI (`.github/workflows/ci.yml`):** on push to `main` and pull requests, skipping
   docs-only pushes (`openspec/**`, `**/*.md`, `.claude/**`): `./gradlew test lint assembleRelease`.
   On green push to `main` a `publish` job replaces the rolling release `latest-build` with
   `terveyslappu.apk`. Obtainium follows the repo's releases. One job builds once; no separate
   debug build.
10. **Signing:** release builds use `RELEASE_KEYSTORE_BASE64` (+ optional password/alias secrets,
    default `android`/`androiddebugkey`) like rubikki. Key: a dedicated key generated in the cloud
    session (the user works from the phone and GitHub secrets cannot be copied between repos),
    with the CI default alias and passwords, so one secret is enough. Without the
    secret, publish is skipped with a notice (a debug-signed APK from CI would change key on every
    runner and break updates).
11. **Cloud builds:** the session-start hook downloads Android command-line tools to
    `~/android-sdk`, installs the platform and build tools that the project's `compileSdk` needs,
    and writes `local.properties`, so `./gradlew test lint assembleDebug` runs in a cloud session.
12. **Tests:** unit tests for weight parsing and validation, record mapping (value, time, own vs
    other app) and log rotation; one Robolectric Compose smoke test for the main screen states
    (no permission, form enabled). Health Connect itself is tested on the phone by the user.

## Risks / Trade-offs

- [Health Connect permission UI differs on Samsung] → the app only uses the standard permission
  contract and logs the granted set; the log shows what happened.
- [Signing secret missing] → publish skips with a clear notice; the build job still checks code.
- [Rubikki toolchain versions too new for Health Connect client] → fall back to the nearest
  working AGP/Kotlin pair and note it in the design when applying.

## Migration Plan

First release. Install: Obtainium → add `https://github.com/Jukkakot/terveyslappu`, it picks the
`latest-build` release APK.
