# Tasks

## 1. Garmin history with kcal

- [x] 1.1 Reader keeps `activeKilocalories` and `bmrKilocalories` per day next to steps (rules in steps delta); store `garmin-history.json` with migration from `garmin-steps.json`; backup rules + `DataTransferRulesTest`.
- [x] 1.2 Preview "Garminin historia" with both day counts; Asetukset row wording incl. the "Tuo ZIP uudelleen" line.
- [x] 1.3 Tests with made-up ZIPs: kcal kept, steps-only day, kcal-only day, migration of the old file.

## 2. Burned energy data

- [ ] 2.1 `burn/` pure model, merge (larger total wins with its split; today never imported; source rule as steps), `BurnSeries` (daily/weekly, average, stats, list), `HomeBurn`; tests incl. total-only and active-only days, today.
- [ ] 2.2 `HealthConnectBurn` reader (two daily aggregates by the steps source), permissions in manifest/privacy/request; `BurnController` with access states as steps; `burn.read` and `hc.permission.burn` logs; tests on the controller with a fake reader.

## 3. Screens

- [ ] 3.1 Rename the Energia label to Syöty everywhere visible (tab, editor, legends, readouts, Ohje); tab order Paino, BMI, Syöty, Askeleet, Kulutus.
- [ ] 3.2 Kulutus tab (`ui/BurnPage`): chart with two-tone bars, average line, legend, range line, weekday axis; stats; day list; permission/empty states.
- [ ] 3.3 Kulutus card at three sizes, hidden by default; `HomeLayoutCodec` and Lisää kortti; smoke test.
- [ ] 3.4 Own charts: `SeriesKind.BURN`, `SeriesColour` (palette, defaults, codec), editor rows with colour dots, drawing with chosen colours and the lighter tone for treats/lepo; contrast unit test; codec round trip and old JSON.
- [ ] 3.5 Asetukset → Askeleet: the line that the source also applies to Kulutus.

## 4. Context

- [ ] 4.1 `.claude/CLAUDE.md` map (`burn/`, store rename) and log list; `product.md` decision (Kulutus, names, no formula); `knowledge.md` steps/HC section: what Garmin writes for calories once known; Ohje text.
- [ ] 4.2 After archive, sweep `openspec/specs/weight-chart/spec.md` for user-visible "Energia" wording and "three data" so the main spec reads true.
- [ ] 4.3 Bump MINOR of `appVersion`.
- [ ] 4.4 Run `./gradlew test lint assembleDebug`.
