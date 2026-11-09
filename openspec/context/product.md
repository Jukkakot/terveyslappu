# Product

## Goal

Make the morning weigh-in effortless: one tap (widget or Quick Settings tile), step on the
scale, the weight is saved. No ads, no extra screens (the OKOK International app is slow and full
of ads, which is why this app exists). Weigh-ins can happen at any time of day; morning is just
the habit. Long term the app may grow into a small personal health app.

## How the data flows (decided 2026-11-08)

```
scale (BLE broadcast) -> terveyslappu -> Health Connect -> terveyslappu chart (own weights), Samsung Health
Garmin watch -> Garmin Connect -> Health Connect (steps, later)
```

- **Health Connect is the interface.** It survives a phone or watch brand change: any new device
  or app writes the same kind of data there.
- **The app has its own weight chart** (weight-chart, user 2026-11-09), replacing the earlier
  "Samsung Health is the viewer": periods, dots joined by a line, period stats. It shows only this
  app's own weights (scale, manual, imported); other apps' weights are the user's inaccurate hand
  entries. Export and backup still hold every app's weights. Goal weight and forecast: backlog.
  The x-axis stays a time axis (chart-gaps-and-years, user 2026-11-09): the line breaks where
  weights are over 30 days apart, labels read "9/2022" over a year, and a single weight's time
  carries the year when not from this year. OKOK's one-step-per-measurement axis was rejected: it
  hides long gaps and makes the trend misleading.
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
  and on the first opening of each day, and Asetukset shows last success or failure (home only a notice on trouble). Chosen because the
  user prefers an active, visible sync; no server (NFR-2). Same CSV as Vie tiedot and read by
  Tuo tiedot. No background job: the write is a cheap local file write, and Samsung's battery
  rules make background jobs unreliable (user 2026-11-09, generic-data-transfer).
- **No weight before weighing; Poista or Valmis on the result** (user 2026-11-09,
  weigh-in-button-redo, weigh-in-discard): the widget is a plain 1x1 Punnitse button and the tile
  has no subtitle. The dialog shows the result ~10 s (a tap keeps it open) with only Poista and
  Valmis: saving is the default, Poista deletes a test weigh-in at once and closes, no Kumoa.
  Weighing again = Poista and open the widget again ("Punnitse uudelleen" dropped: two buttons are
  clearer). The scale repeats its last stable packet for 10+ s and its company id is a
  per-weighing counter (5fc0, 60c0, 61c0 seen), so a weigh-in ignores the previous weigh-in's id
  for two minutes.
- **Version `MAJOR.MINOR.BUILD`** (user 2026-11-09, app-version-numbers): SemVer-style name with a
  never-resetting build number instead of PATCH, because every green push is a release; the
  Android version code stays the total commit count so updates always install.
- **Keep all raw data, show only the weight** (user 2026-11-09, replaces "no body composition"):
  everything the scale sends and every column of imported history is kept as received, without
  calculations; derived values (BMI, body fat ...) may be computed later in the app from the raw
  data. Kept in app storage (`raw/RawArchive`, JSON lines keyed by the weight's second) and in
  the export/backup's `raw` column, since Health Connect has no record for it. Changes
  scale-reading-archive and okok-xls-import.
- **Simple home, Historia and Asetukset** (user 2026-11-09, main-screen-refresh, mockup B): the
  home screen shows only the latest weight card (change, 30-day sparkline), the three latest
  weights and Punnitse vaa'alla; the card opens Historia (chart, stats, list, + for manual entry,
  which is nowhere else), the gear opens Asetukset (theme, import/export, backup, log). No ⋮ menu;
  the backup shows on home only as a notice when it failed or is stale. No bottom navigation.
- **Owner details** (user 2026-11-09, owner-profile): one user per phone, no profiles. Birth year
  (not a full date) gives the age; height in whole cm; gender Mies/Nainen/Ei valittu; all optional.
  Kept in app preferences; the height also goes to Health Connect (its only record of the three, own
  permission asked on the first save, the app keeps the height when denied). Not carried in the
  export/backup since generic-data-transfer: static, quickly typed again. Not now: BMI, filling
  from an OKOK file's title rows.
- **One data file, CSV or JSON** (user 2026-11-09, generic-data-transfer): Vie tiedot / Tuo tiedot
  move the measured raw data between apps and phones; the user picks CSV or JSON on export, import
  detects the format. One part per data kind (`#table;<kind>` in CSV, a key in JSON), today only
  weights; a new kind gets its own part and travels through export, import and backup with no
  new buttons. Settings and owner details are not carried. File name
  `terveyslappu-export-<yyyyMMdd-HHmmss-SSS>` so names never collide.
- **One weight scale, at least 4 kg** (user 2026-11-09, chart-scale): every weight chart (home
  card, Historia) uses the weights with 0.5 kg room, widened to at least 4 kg and rounded to whole
  kilos. Daily weight moves 0.5–2 kg with water and glycogen, so a narrower scale makes noise look
  like a trend (the home line once dropped steeply for a 0.08 kg difference); one rule everywhere
  keeps a change the same size across periods. The home line spans the whole 30 days. kg is shown
  on every number and axis; no unit choice (user: kg only).
- **Changes without judgement** (user 2026-11-09, history-list-delta): every change number has a
  small arrow up/down (none at ±0) because the direction is read before the digits; the Historia
  list shows each weight's change from the one before it, gains and losses in two palette tones
  (tertiary / primary), never red and green, since neither direction is "good"; zero is neutral.
- **Theme** (user 2026-11-09): the phone's own palette (Material You) in light and dark, following
  the system by default; Asetukset → Teema forces Vaalea or Tumma. Choices are filter chips like
  the period chips (a segmented button broke "Järjestelmä" mid-word on a 360 dp phone).
- **Libraries first** (user 2026-11-09): use a maintained library (chart, Excel) instead of
  writing the feature by hand; size is not a reason against one.
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
  The rest of the 13 bytes (openScale's older `BluetoothOKOK2`, 2026-11-09): bytes 4-5 product id,
  bytes 7-12 the scale's own Bluetooth address (openScale filtered on it). The company id's high
  byte is not fixed: openScale registered a scan filter for every `00C0`-`FFC0`, so a background
  scan filter should match the low byte `C0` and the address, not a fixed `key`.
  Log 2026-11-09: this scale sends zeros in bytes 4-12 (no product id, no address in the data);
  the address is only in `addr=`, and the high byte counts weighings (5f, 60, 61).
- **Samsung Health reads weight from Health Connect** written by another app (Garmin Connect, and
  terveyslappu's own entry within about two minutes of saving, 2026-11-08), shown with its source. Whether it stores such weights in its own data and syncs them to Samsung
  Cloud is unverified: its personal-data CSV export had no Health Connect sourced rows yet.
- **Fitbit (Google Health)** did not write a manually entered weight to Health Connect even with
  the write permission on. **Garmin Connect** does write weight, with some delay.
- **Health Connect** has scheduled export (daily, weekly, monthly) to a cloud storage app and an
  import from that file.
- **History:** the user has an export of about four years of OKOK weights and Garmin step history
  in Garmin Connect. OKOK International exports a binary Excel file (`.xls`, BIFF8; Q2 closed
  2026-11-09): title rows (name; profile; the covered range `YYYY-MM-DD ~ YYYY-MM-DD(nDay)`), then a
  header `Date;Time;Weight(Kg);BMI;...` (about 19 more columns of OKOK's own calculations, no
  impedance) and rows newest first with year-less `MM-DD` dates, all values as text. Read by
  `history/XlsTable` (Apache POI HSSF) and `history/OkokLayout` (years from the range line, other
  columns and title cells to the raw archive as source `okok`).
