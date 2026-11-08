# Proposal

## Why

Before building the scale and widget, we need a real app on the phone that writes weight to
Health Connect, to see what Samsung Health does with it (shows it, how fast, does it keep it in its
own data and so in Samsung Cloud, Q1). Fitbit could not do the test for us, and Garmin only with a
delay. The same app is the base every later phase builds on.

## What Changes

- New Android app `terveyslappu` (Kotlin, Compose) with one screen: enter a weight with date and
  time (default now), save it to Health Connect, see the latest weights read back from Health
  Connect with the app that wrote each one.
- Health Connect permission flow: asks to read and write weight on first use; explains and offers
  the settings when access is missing or Health Connect is unavailable.
- Shareable log: the app writes a log file and shares it from the screen's menu (NFR-3).
- Build and release: Gradle project, GitHub Actions builds, tests and lints on every code push to
  `main` and replaces one rolling GitHub release with a signed APK, so Obtainium updates the app
  in place (NFR-4).
- Cloud-session setup: the session-start hook installs the Android SDK so builds and tests also
  run in cloud sessions.

## Capabilities

### New Capabilities
- `weight-entry`: entering a weight by hand, saving it to Health Connect and showing recent weights
  from Health Connect, including the permission handling.
- `app-log`: the app's own log file and sharing it from the app.

### Modified Capabilities
- (none)

## Impact

- Parts: app screen, Health Connect, build/CI. No scale, widget or tile yet.
- New code: the whole Android project (`app/`, Gradle files, `.github/workflows/`).
- Dependencies: AndroidX, Compose Material 3, Health Connect client 1.1.0.
- User steps (cannot be done from the cloud): add the signing secret to the GitHub repo once,
  install Obtainium and add the repo there.
