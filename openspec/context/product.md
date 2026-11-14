# Product

## Goal

Make the morning weigh-in effortless: one tap (widget or Quick Settings tile), step on the
scale, the weight is saved. No ads, no extra screens (the OKOK International app is slow and full
of ads, which is why this app exists). Weigh-ins can happen at any time of day; morning is just
the habit. Long term the app may grow into a small personal health app.

## How the data flows (decided 2026-11-08)

```
scale (BLE broadcast) -> terveyslappu -> Health Connect -> terveyslappu chart (own weights), Samsung Health
Garmin watch -> Garmin Connect -> Health Connect (steps, read only since v1.28)
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

- **Moving to a new phone** (user 2026-11-09, Q3): the backup file in the user's own cloud
  carries the weights (Tuo tiedot restores it); Android's own backup (encrypted Google cloud
  backup and Smart Switch transfer) carries the app's own data: owner details, theme, periods,
  raw-data archive. The backup file setting, the log and the weigh-in state stay behind
  (app-data-transfer). Samsung Cloud is not relied on.

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
- **Background listening: PendingIntent scan, listen-only trial first** (weigh-in-listen-trial,
  2026-11-14): a hardware-filtered (address, plus name) low-power scan whose results the Bluetooth
  stack delivers to a manifest receiver, so the app process may die; an inexact Doze-safe alarm
  starts, checks hourly and ends the user's time window. No foreground service (it would need a
  permanent notification). Phase 1 saves nothing and only logs what it heard, to measure range,
  scan survival on Samsung and battery cost before the feature.
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
  keeps a change the same size across periods. The home line spans its whole period. kg is shown
  on every number and axis; no unit choice (user: kg only).
- **Home chart period: 1 vk first, own choice** (user 2026-11-09, home-period): the home card's
  line shows 1 vk, 1 kk or 3 kk, picked from a small label under it (its tap does not open
  Historia), remembered and separate from Historia's period. A week was the user's wish from the
  start; the main-screen refresh had given the card a fixed 30 days.
- **Dots when they fit** (user 2026-11-09, chart-dots): both charts draw each weight as a dot
  when there is at least 10 dp of width per weight, else only the line (lone weights between gaps
  and the home line's latest keep theirs). Chart guides agree on markers for sparse points and none
  on dense daily series but give no number; the rule is by room, so on a phone 1 vk and a month of
  daily weighing get dots, 3 kk and longer do not.
- **Changes without judgement** (user 2026-11-09, history-list-delta): every change number has a
  small arrow up/down (none at ±0) because the direction is read before the digits; the Historia
  list shows each weight's change from the one before it, gains and losses in two palette tones
  (tertiary / primary), never red and green, since neither direction is "good"; zero is neutral.
- **Theme** (user 2026-11-09): the phone's own palette (Material You) in light and dark, following
  the system by default; Asetukset → Teema forces Vaalea or Tumma. Choices are filter chips like
  the period chips (a segmented button broke "Järjestelmä" mid-word on a 360 dp phone).
- **Derived values** (user 2026-11-09, derived-values, simpler-derived): BMI and the normal-weight
  range for the height, in a row under the home card and as Historia's BMI tab (swipe or tap; opens
  on Paino; faint class bands). On the home screen the BMI number shows only in its traffic-light
  tone (green normal, yellow 17–18.5 and 25–30, red beyond) without the class word, which read as a
  judgement at first glance; the word stays in the screen reader text and in Historia's BMI stats.
  Old weights use today's height (adult height is stable). A missing height shows the reason, which
  opens Omat tiedot; under 18 the class and range are left out. Nothing derived goes to Health
  Connect or the export: it is calculated, not measured. The basal metabolic rate and a total
  energy expenditure were tried and dropped (user: no use for them, keep the app simple); gender
  stays in Omat tiedot though nothing uses it now.
- **Grouped Historia list** (user 2026-11-09, history-list-groups, simpler-derived): Kaikki holds
  300+ weights, too many rows to scan, so the list groups by year → month → week (cut at months) →
  weight rows, each group with first → last weight, change and count, tapping opens it. One
  systematic rule (user): every year, month and week with a weight is its own row, even with one
  weight; no day level (a weight row shows its day); the year level is left out when the period is
  within one year; a list of up to 14 weights stays flat. Every group starts closed and nothing is
  remembered. A pause over 30 days shows as a thin "Ei punnituksia" row, and no change is computed
  across it (the chart's line-break rule). A level picker as in other weight apps was rejected.
- **Oma calendar** (user 2026-11-09, custom-range-calendar): picking an own range shows each
  weighed day's latest weight under its number ("82,4", no unit), so a range is not picked blind.
  Every past day stays selectable (greying out empty days was rejected); future days are not. The
  Material 3 range picker cannot draw on a day, so it was replaced by the Calendar library
  (kizitonwose) with our own day cells and selection (earlier tap after the start = new start, as
  Android's own picker). Without the history permission the calendar says why old days are empty.
- **Libraries first** (user 2026-11-09): use a maintained library (chart, Excel) instead of
  writing the feature by hand; size is not a reason against one.
- **Weigh-in without any tap: reopened** (user 2026-11-14, after dropping it 2026-11-09 for
  battery): planned in phases in `roadmap.md`. The redo that deletes the previous result was done
  in weigh-in-discard.
- **Automatic weigh-in saves in the scan receiver** (background-weigh-in-save, 2026-11-14): the
  decided weight is written from the manifest receiver with `goAsync()` (no WorkManager, no
  foreground service), within an 8 s budget; a failed or slow write waits in a pending queue in
  app storage and is retried at the next weigh-in, the hourly check and the first foreground read.
  The background never reads Health Connect (reads are foreground-only): the reference for the
  "?" rule comes from our own list of the latest ten accepted weights, and the backup of a change
  made while the app was closed runs at the next app start. The "?" mark lives in our own storage
  (`marks.json`, keyed by `clientRecordId`), since Health Connect has no field for it, and travels
  in the data file's `check` field.
- **Phased** (user 2026-11-08): first a proof of concept with manual weight entry, then the scale
  and the widget. See `roadmap.md`.
- **Step on first, tap later is fine** (user 2026-11-09, Q5): a stable weight the scale is
  already showing when the weigh-in starts is saved; only the previous weigh-in's lingering
  broadcast (same company id, two minutes) is ignored.
- **Food log: why** (user 2026-11-14, explore session). When meals vary from day to day, the
  calories in everyday portions are unknown, and the user wants to learn them and see whether
  everyday eating matches the goal (weight going down). Requirements:
  - BR1 Learn: see what the usual meals contain; logging is for a learning period, not for life.
  - BR2 See the whole: eaten, moved (steps) and weight side by side, at week level, as curves.
    The app draws no conclusions: no "calories left", no daily budget, no maintenance or energy
    expenditure inferred from intake and weight change (too many unknowns; user rejected it).
  - BR3 Effortless: meal slots breakfast, lunch, dinner, evening snack plus extra items in
    between; fixed meals in one tap; a household's handful of usual dishes saved once, so only
    the portion varies. Portions in grams as eaten (cooked, not dry weight); a kitchen scale is
    used a few times per dish to learn the usual portion, not every time. A meal can combine
    items (a dish plus bread).
  - BR4 Treats visible: small frequent treats add up unnoticed, so they stand apart from meals.
  - BR5 No pressure: a missed day is fine; no streaks, red numbers or judgement.
  - BR6 Data in Health Connect (`NutritionRecord`, meal type breakfast/lunch/dinner/snack), as
    weight.
  - No photo AI in the app and no other food app to maintain: the user asks an AI chat for the
    calories of a new dish and saves the number. Free photo-AI apps were looked at (most limit
    the free tier); Samsung Health logs food for free and writes nutrition to Health Connect.
  - Steps come back from the backlog for BR2 (Garmin → Health Connect).
  - The daily morning weigh-in is the main motivator; the drift began when it stopped, so the
    weigh-in stays the core and the food log must not make it heavier.
  - Do not reinvent: follow common food-log patterns (saved meals per slot, quick add of a
    number, servings or grams when logging a saved item, frequent items first).
- **Food log: how it is used** (user 2026-11-14, while proposing food-log). Logged mostly right
  after eating ("Lisää annos" from the home screen adds to today, after the previous item), but
  catching up later is normal: in bed in the evening, or the next day for yesterday. Therefore:
  - No clock times are ever asked (the logging time is only shown small); a day is its items in eating order (breakfast first,
    something, lunch, ...). Meal names are at most a loose template, not data that matters.
  - A repeated thing is a saved **product** with a fixed kcal per piece or per 100 g ("suklaapala
    25 kcal", 5 kpl today), like products in a shopping-list app; repeats must be one or two taps.
  - An item can be logged as plain text first and get its kcal later.
  - Categories only where they have a reason. **Herkku** has one: in the kcal bars, base food and
    treats in their own colours show whether the excess comes from ordinary meals or from treats.
    No other categories until one has a reason (user: "everything needs a reason").
  - No food widget for now; the app's front page is enough.
  - Food, kcal and weight only inside the app, never on the phone's home screen (widget, tile,
    notification): the user looks at them only when they choose to open the app.
  - Order kept (helps recall and spot what was forgotten); the logging time is saved for free and
    shown small; it also feeds "usually eaten at this time" suggestions, which replace day
    templates. No "eat now or later" switch: Järjestä fixes the rare advance entry.
  - As built (food-log, v1.24): no meal slots after all (every eaten thing is its own item, meal
    type unknown in Health Connect); every typed name is learnt as a product; about 370 generic
    base products (Open Food Facts style averages and generic estimates, `scripts/base_products.py`)
    so nothing starts from zero; a whole day can be one bare number (Pikalisäys).
  - As built (food-ai-import, v1.25): still no AI inside the app; the AI chat's estimate comes in
    as JSON by sharing or pasting (the app copies the prompt for the chat), shown as a preview
    to fine-tune, then appended to the chosen day (today by default); never replaces items.
  - As built (home-overview, v1.26): the front page is one card per feature (Paino, Ruoka with
    its own period, Automaattinen punnitus while it is on); every feature gets the same parts on
    the front page and in its details unless a part means nothing for it.
  - As built (steps, v1.28): steps are read only, from Health Connect's merged daily aggregate
    (no origin filter: two writers are merged by the user's app priority, never summed); a day
    without data is a gap, never zero; the front page row is the food card and the steps card
    side by side at half width; Historia's Askeleet tab after Energia. Decided: no step goal or
    target line; steps are not in the data file or the backup (they are Garmin's and live in
    Health Connect and Garmin Connect); the permission is asked only from the steps card or tab.
  - Products are learnt from what is typed (as in kauppalappu); about 300 base products (dishes and raw
    ingredients) from open sources and generic estimates, so nothing starts from zero. Storage: Health Connect, app storage, the data
    file and its backup; no server database.
  - Simple and intuitive: never wonder what to do next; every field says what goes in it; one
    modern look across the app. Not doing: flags for unrealistically low days.

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

- **Samsung Health follows Health Connect with a delay** (user test 2026-11-09, Q1): a weight
  written by terveyslappu shows in Samsung Health, and deleting it in terveyslappu removes it
  there too after a short delay. Whether Samsung Health also stores the weights in Samsung Cloud
  is not known and not relied on (Q3: the backup file carries the weights).
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
