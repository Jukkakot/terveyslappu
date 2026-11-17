# Tasks

## 1. Remove the chip

- [ ] 1.1 In `ui/HomeScreen.kt` `LatestValues`, drop the `ChangeChip` call and the `ChangeChip`
  composable; the number row no longer needs the FlowRow (a plain row with the number and `kg`).
- [ ] 1.2 Remove the string `home_change` and any test that looks for the chip.
- [ ] 1.3 Remove `HomeSummary.change` and its assertions in `HomeSummaryTest` and
  `QuestionedDataTest` if nothing else reads it (the period change `sparkChange` stays).

## 2. Finish

- [ ] 2.1 Bump `appVersion` MINOR in `gradle.properties` (1.58 → 1.59).
- [ ] 2.2 `./gradlew test lint assembleDebug` passes.
