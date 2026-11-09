# Product

## Goal

Make the morning weigh-in effortless: one tap (widget or Quick Settings tile), step on the
scale, the weight is saved. No ads, no extra screens (the OKOK International app is slow and full
of ads, which is why this app exists). Weigh-ins can happen at any time of day; morning is just
the habit. Long term the app may grow into a small personal health app.

## How the data flows (decided 2026-11-08)

```
scale (BLE broadcast) -> terveyslappu -> Health Connect -> Samsung Health (views, trends)
Garmin watch -> Garmin Connect -> Health Connect (steps, later)
```

- **Health Connect is the interface.** It survives a phone or watch brand change: any new device
  or app writes the same kind of data there.
- **Samsung Health is the viewer** for now; the app does not reinvent charts. If that turns out
  awkward (delays, missing features), the app may get its own weight view later.
- **Moving to a new phone:** still open. Health Connect only stores data on the phone. Options:
  Samsung Health keeps a copy in Samsung Cloud (unverified, see findings), Health Connect's own
  scheduled export to a cloud file, or an app-managed cloud file whose sync status the app shows.
  The user prefers an active, visible sync over a silent backup that could break unnoticed.

## Decisions and reasons

- **Rejected: automation on top of the OKOK app** (accessibility robot): the ad timers cannot be
  skipped.
- **Dropped: sending weights to the OKOK cloud** (imitating its private API): fragile, and the
  user is happy to leave OKOK.
- **Dropped for now: weight and steps in one chart.** Diet matters more for the user's weight and
  will not be tracked, so steps next to weight would not motivate.
- **Own app, not openScale:** openScale supports the scale but has no one-tap weigh-in without
  opening the app, which is the main use case. It is the protocol reference (below).
- **Native Kotlin:** BLE scanning, Quick Settings tile, widget and Health Connect are all Android
  native; a cross-platform stack would need native modules for each.
- **Weigh-in as a dialog over the current screen** (autopilot 2026-11-08, scale-weigh-in
  design): tile, widget and launcher shortcut open a small dialog that scans, saves the stable
  weight and closes; no background service. Stable = the scale's own flag, confirmed by a second
  equal packet or 2 s quiet.
- **Interim phone-move answer: visible backup file** (autopilot 2026-11-08, weight-backup):
  the user picks one CSV file (e.g. Google Drive); the app rewrites it after its own changes
  and daily on opening, and the main screen shows last success or failure. Chosen because the
  user prefers an active, visible sync; no server (NFR-2). Same format as Vie painot and read by
  Tuo historia.
- **No body composition** (user 2026-11-09): weight only; the impedance stays in the log.
- **Weigh-in without any tap is wanted** (user 2026-11-09), together with an easy redo that
  deletes the previous result; see roadmap.
- **Phased** (user 2026-11-08): first a proof of concept with manual weight entry, then the scale
  and the widget. See `roadmap.md`.

## Lessons from rubikki-solveri (2026-11-08)

The user's earlier Android app, read through for mistakes worth not repeating:

- Signing key fixed before the first install (rubikki's later key change would have cost the
  user's history): done, NFR-4.
- Record the outside input and replay failures as tests (NFR-7); measure speed (NFR-8); no
  silent failures (NFR-9).
- Fix the general weakness, never a special case for one situation (rubikki: "no striped-cube
  fix"). For the scale: a rule that holds for every packet, not a check for one odd capture.
- Screens with actions fit on one screen and keep buttons clear of the system bars (rubikki
  needed two fixes for this).
- When unsure what the real-world truth was (what the scale display showed), ask the user instead
  of building on a guess.

## Findings (2026-11-08)

- **Scale:** Prego P814B advertises as `Yoda1` and broadcasts the weight in BLE manufacturer
  data (company id low byte `0xC0`), no connection needed. Decoding matches openScale
  `OkOkHandler` (C0 path): payload bytes 0-1 weight big-endian, byte 6 attributes (bit 0 stable,
  bits 1-2 decimals, bits 3-4 unit), bytes 2-3 impedance /10. Verified against one capture.
- **Samsung Health reads weight from Health Connect** written by another app (Garmin Connect, and
  terveyslappu's own entry within about two minutes of saving, 2026-11-08), shown with its source. Whether it stores such weights in its own data and syncs them to Samsung
  Cloud is unverified: its personal-data CSV export had no Health Connect sourced rows yet.
- **Fitbit (Google Health)** did not write a manually entered weight to Health Connect even with
  the write permission on. **Garmin Connect** does write weight, with some delay.
- **Health Connect** has scheduled export (daily, weekly, monthly) to a cloud storage app and an
  import from that file.
- **History:** the user has an export of about four years of OKOK weights (format to be checked)
  and Garmin step history in Garmin Connect.
