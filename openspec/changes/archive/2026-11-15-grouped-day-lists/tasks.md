# Tasks

## 1. Item row buttons

- [x] 1.1 Sheet day list (`ui/FoodSheet`) and Ruoka list (`ui/FoodScreen`): pass `RowActions` as the `FoodRow` trailing (edit = item editor, delete = existing delete with Kumoa); not in Järjestä
- [x] 1.2 Strings for the edit/delete accessibility names if missing; smoke test: trash in the sheet deletes and Kumoa restores

## 2. Grouped day lists

- [x] 2.1 Generalise `chart/ListGroups` so the year/month/week tree and the flat ≤14 rule work for day rows (dates) as well as weights; no gap rows for days; keep the weight list's behaviour and tests unchanged
- [x] 2.2 Group summaries: per-day average of the tab's main value and day count (Energia: kcal + treats; Askeleet: steps; Kulutus: total + active), today counted only when the tab's stats count it
- [x] 2.3 `ui/HistoryScreen`: render Energia, Askeleet and Kulutus lists through the tree with the weight list's group row look, open/close and reset on period change
- [x] 2.4 Unit tests for day grouping (flat ≤14, one year → months on top, week cut at month border, averages); one smoke test per tab that group rows show

## 3. Finish

- [x] 3.1 `./gradlew test lint assembleDebug`
- [x] 3.2 Bump MINOR `appVersion` in `gradle.properties`

## Decisions taken

- Ruoka screen already had RowActions; only the sheet day list got the buttons. In the sheet the trash uses the same `FoodViewModel.delete` (sheet open => Kumoa line) and clears the highlight.
- Day grouping is a separate generic `chart/DayGroups` (weight `ListGroups` unchanged apart from three helpers made internal); summaries in `GroupSummaries`.
- Open sets: one per day list, all reset on period change and on any tab switch (the weight list too).
