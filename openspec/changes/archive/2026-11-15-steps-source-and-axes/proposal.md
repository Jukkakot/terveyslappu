# Proposal: steps-source-and-axes

## Why

1. **Steps read too high.** The user has steps from the phone (Health Connect's own on-device
   steps, Samsung Health) and from Garmin Connect. The app shows more steps than Garmin for the
   same day: Health Connect's no-origin aggregate takes, for each stretch of time, the
   highest-priority app that has data there, so the phone fills the hours the watch left empty and
   the day's total grows past either app's own count. The user wants one source's steps only.
2. **Charts are hard to read.** Askeleet and Energia show only the top value of the y-axis and only
   the first and last date; Paino's labels come from the chart library and its line has no legend;
   the weight line under the bars has no legend entry either (user 2026-11-15, choice 2A: all three
   Historia charts get one shared axis rule).

## What Changes

- **One steps source.** Steps are read from one app only. New setting Asetukset → **Askeleet** →
  **Lähde**: *Automaattinen* (default), each app that wrote steps in the last 30 days (readable
  name, the phone's own steps as "Puhelin"), and *Kaikki (Health Connectin järjestys)* (today's
  behaviour).
- **One source for the whole history:** no day falls back to another app. Picking Garmin on a phone
  with 2 years of phone steps makes Kaikki start at Garmin's first day; Ohje says in one sentence
  that "Kaikki (Health Connectin järjestys)" shows the full history. No first-run notice.
- **Automatic pick (user choice 1B):** until the user picks, the app uses the app with the most
  steps in the last 30 days. A source check (one filtered 30-day read per candidate app) runs once
  a local day on foreground reads, **under every choice**, and when the setting is entered; a
  failed check is retried after an hour. A switch is silent but logged and visible in the source
  line. A small margin keeps two near-equal apps from flipping back and forth. Until the check has
  run today, "no recent steps" is unknown and draws no warning tint.
- Askeleet tab and the front page steps card both follow the chosen source. Askeleet shows one line
  "Lähde: Garmin Connect (automaattinen)" whenever access is granted, under the chart; tapping it
  opens the setting. The front page card keeps its empty text.
- The phone's package names are remembered on the phone for when the system cannot tell them.
- An explicitly chosen app that stops writing stays chosen; its row and the source line say it has
  no steps in 30 days (warning tint, once today's check has run). Never switched behind the user's back.
- The source choice stays on this phone: not in the data file, the backup file or Android backup.
- **Chart axes, one rule for Paino, Energia and Askeleet (and the weight line under the bars):**
  y-axis labels at the bottom (0 for bars; the weight scale's low end for the weight line), the
  middle and the top, each with its unit ("askelta", "kcal", "kg"); x-axis 3–5 dates evenly across
  the period, first and last included; every bar and line has a legend entry ("paino" included).
  The date count (5, 4 or 3) is chosen by measured fit and Paino's Vico axis uses the same dates;
  month/year labels from a span of 365 days. The rule is a pure shared helper that `custom-charts`
  reuses.
- Ohje's steps text no longer tells the user to reorder Health Connect's priority list; it explains
  the source setting.
- Parts touched: Health Connect (steps reads: the source check and filtered reads; no new
  permission), Historia (Paino, BMI, Energia, Askeleet charts), Asetukset (new Askeleet row, between
  Ruokakirjaus and Ulkoasu, and sub-screen), front page steps card (data only), Ohje, app log. Not touched: scale, widget,
  build/CI (besides the version bump).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `steps`: steps come from one chosen source (setting, automatic pick, filtered reads, source check,
  log lines, empty-source state) instead of Health Connect's merged total.
- `home-screen`: the Asetukset list gets the Askeleet row (seven rows with Ruokakirjaus, which the
  old scenario forgot).
- `weight-chart`: one axis and legend rule for the Historia charts; units on Energia's axis (the old
  "no kcal on any axis" scenario goes); Askeleet's source line and empty-source message.

## Impact

- Code: `steps/` (new pure source rules, Health Connect source check and filtered reads, prefs file
  `steps`), `ui/StepsViewModel`, `ui/StepsPage`, `ui/EnergyPage` (`PeriodWeights`),
  `ui/WeightChart` (Vico axis placers), new pure `chart/AxisTicks`, `ui/SettingsScreen`,
  `ui/SettingsSummary`, `MainViewModel` (`SettingsPage.STEPS`), manifest `<queries>` for app names,
  strings, Ohje.
- No new permission, no new dependency. `READ_STEPS` returns records with their data origin.
- **Order:** this change first, then `home-card-layout`, then `custom-charts`. `custom-charts`'
  design 5–8 use `AxisTicks`, `ChartLegend` and the shared label column; the drawers are extracted
  from the new look.
