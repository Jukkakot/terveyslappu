# Tasks

## 1. Shared crosshair

- [x] 1.1 Extract the own charts' crosshair (gesture detector, line, readout slot with placeholder, removal rules, TalkBack actions, haptic) from `ui/CustomChartView` into a reusable piece, without changing own charts' behaviour.
- [x] 1.2 Marker drawing: a ring on the chosen line/dot value; chosen bar full tone, other bars at 60 % alpha.
- [x] 1.3 Pure readout texts (in `chart/`): date head (weekday + date, year when not current; week form "Vk N · d.–d.M." for weekly bars, both ends' months when they differ) and per-tab values as specified; unit tests incl. old year and weekly.

## 2. Tabs

- [x] 2.1 Paino and BMI (`WeightChart`): crosshair by day, value = the day's last non-"?" weight with its time; replaces the old dot text.
- [x] 2.2 Syöty, Askeleet, Kulutus: crosshair by day or week; Syöty's readout ends with "Avaa päivä ›" (opens Ruoka on that day, back returns to Syöty); replaces the old bar-tap texts and Syöty's direct jump.
- [x] 2.3 Smoke tests: a tap on Askeleet shows the readout; Syöty's "Avaa päivä" opens Ruoka.

## 3. Context

- [x] 3.1 Bump MINOR of `appVersion`.
- [x] 3.2 Run `./gradlew test lint assembleDebug`.

## Implementation notes (choices the spec left open)

- Marker: a 2 dp ring (6 dp radius) in the data's tone, as tasks 1.2 and the "Old weight" scenario say (the requirement's "filled circle" read as the ring).
- Own charts get the same weekday date head and markers (the new requirement lists them); gestures, removal rules and TalkBack are unchanged.
- Paino/BMI are drawn on the shared Canvas axes (`YAxisLabels`, `DateAxis`, 160 dp plot) instead of Vico, so the crosshair maps to the same day slots; the Vico dependency is now unused but left in the build.
- Week head: the ISO week Monday–Sunday; the year after the Sunday when it is not this year ("Vk 1 · 30.12.–5.1.2025").
- Syöty readout always shows "herkut N kcal" for a logged day (also 0); "Avaa päivä ›" is shown on empty days too.
- Kulutus weekly readout: the week bar's values in the day format, no "keskim." (spec: "as in energy-burn").
- TalkBack on weekly bars steps one week at a time.
