# Tasks

## 1. Shared crosshair

- [ ] 1.1 Extract the own charts' crosshair (gesture detector, line, readout slot with placeholder, removal rules, TalkBack actions, haptic) from `ui/CustomChartView` into a reusable piece, without changing own charts' behaviour.
- [ ] 1.2 Marker drawing: a ring on the chosen line/dot value; chosen bar full tone, other bars at 60 % alpha.
- [ ] 1.3 Pure readout texts (in `chart/`): date head (weekday + date, year when not current; week form "Vk N · d.–d.M." for weekly bars, both ends' months when they differ) and per-tab values as specified; unit tests incl. old year and weekly.

## 2. Tabs

- [ ] 2.1 Paino and BMI (`WeightChart`): crosshair by day, value = the day's last non-"?" weight with its time; replaces the old dot text.
- [ ] 2.2 Syöty, Askeleet, Kulutus: crosshair by day or week; Syöty's readout ends with "Avaa päivä ›" (opens Ruoka on that day, back returns to Syöty); replaces the old bar-tap texts and Syöty's direct jump.
- [ ] 2.3 Smoke tests: a tap on Askeleet shows the readout; Syöty's "Avaa päivä" opens Ruoka.

## 3. Context

- [ ] 3.1 Bump MINOR of `appVersion`.
- [ ] 3.2 Run `./gradlew test lint assembleDebug`.
