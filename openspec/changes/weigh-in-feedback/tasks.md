# Tasks

## 1. Logic

- [ ] 1.1 `ui/Format`: `formatChange(delta)` (−0,3 / +0,3 / ±0,0) and `formatDay(time, today)` (Tänään, Eilen, `ti 7.11.`); JVM tests
- [ ] 1.2 `WeighIn`: after a successful save, look up the previous weight (injected, failures only logged) and put the change in `Saved`; JVM test with a fake lookup
- [ ] 1.3 `HealthConnectWeights.latestBefore(time)` (one record, descending, up to a year back)

## 2. Dialog, widget, tile

- [ ] 2.1 Dialog shows the change line and gives haptic confirmation on Saved; smoke test that the change text is shown
- [ ] 2.2 `LastWeight` cache (prefs) updated from the weigh-in save and from the main screen's read; widget layout with weight and day, refreshed on update; tile subtitle via `requestListeningState`
- [ ] 2.3 `./gradlew test lint assembleDebug` passes

## 3. Docs

- [ ] 3.1 `.claude/CLAUDE.md` map if files move; nothing else changes in context files

## Workflow follow-up

- Archive after merged and CI green.
