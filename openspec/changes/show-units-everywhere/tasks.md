Rule for every task: the formatters (`formatKcal`, `formatKg`, `formatSteps`, `formatChange`, `formatRate` in `ui/Format.kt`, `ui/FoodScreen.kt`, `ui/StepsPage.kt`) stay bare numbers; the unit is added at the call site through a string resource (`food_kcal`, `kg_value`, `steps_value` …), otherwise texts that already add the unit read "kg kg". The visible text is the TalkBack text; add no new contentDescription.

## 1. Units in texts

- [ ] 1.1 `ProductChip` (`ui/FoodSheet.kt`): its kcal Text reads "145 kcal" through `food_kcal`; both its Texts (name and kcal) `labelMedium` instead of `labelLarge`. The heading "Lisää – usein tähän aikaan" stays `labelLarge`. The 60 % `chipMax` still applies to the whole chip, so a long name ellipsizes a little sooner.
- [ ] 1.2 `food_day_treats` → "herkkuja %1$s kcal" (Ruoka summary, sheet, home food card, Energia list)
- [ ] 1.3 `group_avg_treats` → "herkkuja ka %1$s kcal/pv", `group_avg_active` → "aktiivinen ka %1$s kcal/pv"
- [ ] 1.4 Steps and burn home cards (`ui/HomeScreen.kt:345`, `:452`): copy the food card's structure: a `FlowRow` with the number (`stepsTodayValue` / `burnTodayValue`, number only) and a separate baseline-aligned unit Text ("askelta" / "kcal", titleMedium, onSurfaceVariant). At small size the unit may wrap under the number; on the burn card "aktiivinen N kcal" still drops first (`activeFits`).
- [ ] 1.5 Home small-size averages carry the unit with the short word "ka": `home_food_average` → "ka %1$s kcal/pv", `home_steps_average` → "ka %1$s askelta/pv", `home_burn_average` → "ka %1$s kcal/pv", `maxLines = 2`; when space is tight the bars shrink first (down to `MIN_CHART`). Wide and big keep the `_long` "keskim." forms.
- [ ] 1.6 Historia list change labels with kg: weight rows (`ui/HistoryScreen.kt:643`) and group rows (`ui/ListGroupRows.kt:124`) via `kg_value`. The change keeps `maxLines = 1`; a weight row's time may wrap to two lines in deep groups.
- [ ] 1.7 Kulutus readouts (`chart/TabReadouts.kt`): Historia marker card day: "aktiivinen N kcal · lepo N kcal"; a weekly bar: "keskim. N kcal/pv · aktiivinen N kcal/pv · lepo N kcal/pv"; own charts (no lepo part): "aktiivinen N kcal".
- [ ] 1.8 Delete the unused string `burn_value_split`.
- [ ] 1.9 Historia calendar (`ui/RangeCalendarDialog.kt`): the day cells stay bare ("82,4"); one line "Painot kg" (bodySmall, onSurfaceVariant) under the month grid, shown only when the month has weights.

- [ ] 1.10 Historia's statistics tiles (`ui/HistoryScreen.kt`, e.g. Viikossa "−0,47 kg"): the value with its unit stays on one line with the arrow (shrink to fit or a smaller style), never three lines as now.
- [ ] 1.11 weight-entry "Questioned weight in the list": the change after a questioned weight also reads with kg ("+0,4 kg").
## 2. Finish

- [ ] 2.1 Update tests asserting the changed texts: `HomeCardsTest` (short `home_burn_average` text at line 168; add a small-size check at font scale 1.3 that the steps and burn averages stay inside their slots), `TabReadoutsTest` (lines 91, 93, 110), `MainScreenTest` (`rowChange`), `FormatTest` stays as is. Run `./gradlew test lint assembleDebug`.
- [ ] 2.2 Bump `appVersion` to 1.47
