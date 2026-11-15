# Tasks

## 1. Item row buttons

- [ ] 1.1 Sheet day list (`ui/FoodSheet`) and Ruoka list (`ui/FoodScreen`): pass `RowActions` as the `FoodRow` trailing (edit = item editor, delete = existing delete with Kumoa); not in Järjestä
- [ ] 1.2 Strings for the edit/delete accessibility names if missing; smoke test: trash in the sheet deletes and Kumoa restores

## 2. Grouped day lists

- [ ] 2.1 Generalise `chart/ListGroups` so the year/month/week tree and the flat ≤14 rule work for day rows (dates) as well as weights; no gap rows for days; keep the weight list's behaviour and tests unchanged
- [ ] 2.2 Group summaries: per-day average of the tab's main value and day count (Energia: kcal + treats; Askeleet: steps; Kulutus: total + active), today counted only when the tab's stats count it
- [ ] 2.3 `ui/HistoryScreen`: render Energia, Askeleet and Kulutus lists through the tree with the weight list's group row look, open/close and reset on period change
- [ ] 2.4 Unit tests for day grouping (flat ≤14, one year → months on top, week cut at month border, averages); one smoke test per tab that group rows show

## 3. Finish

- [ ] 3.1 `./gradlew test lint assembleDebug`
- [ ] 3.2 Bump MINOR `appVersion` in `gradle.properties`
