## 1. Change per row

- [x] 1.1 Pure function: for a newest-first list, the change of each weight from the one before it in time (oldest none); unit tests with made-up weights, incl. rounding to ±0,0
- [x] 1.2 Historia row shows the change (existing `formatChange`, small text left of the weight); gain and loss colours from the theme's palette (e.g. tertiary vs primary), zero in onSurfaceVariant; the row's spoken label includes the change
- [x] 1.3 Smoke test: a list of three weights shows two changes

## 2. Arrows on every change

- [x] 2.1 One shared composable: arrow up/down (existing `ic_arrow_up` / `ic_arrow_down`, none when the shown value rounds to zero) + signed number; arrow decided from the rounded value (unit test for the rounding rule)
- [x] 2.2 Use it in the list rows, the stats' Muutos and Tahti tiles (Tahti rounds to two decimals) and the home card's 30-day change; home `ChangeChip` keeps its chip but uses the same rule
- [x] 2.3 Smoke test: the stats show an arrow for a loss

## 3. Docs and release

- [x] 3.1 product.md decision (arrows on every change, neutral colours, no red/green), roadmap entry; bump MINOR in `gradle.properties`
- [x] 3.2 `./gradlew test lint assembleDebug`
