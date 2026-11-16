## 1. Units in texts

- [ ] 1.1 Food chips (`ui/FoodSheet.kt`): "+ <name> <kcal> kcal" through `food_kcal`; both chip texts `labelMedium` instead of `labelLarge`
- [ ] 1.2 `food_day_treats` → "herkkuja %1$s kcal" (Ruoka summary, sheet, home food card, Energia list)
- [ ] 1.3 `group_avg_treats` → "herkkuja ka %1$s kcal/pv", `group_avg_active` → "aktiivinen ka %1$s kcal/pv"
- [ ] 1.4 Home cards: steps and burn big numbers get "askelta" / "kcal" beside them as the food and weight cards (`ui/HomeScreen.kt`)
- [ ] 1.5 Home small-size averages carry the unit with the short word "ka": `home_food_average` → "ka %1$s kcal/pv", `home_steps_average` → "ka %1$s askelta/pv", `home_burn_average` → "ka %1$s kcal/pv" (may wrap to two lines); wide and big keep the `_long` "keskim." forms
- [ ] 1.6 Historia list change labels with kg: weight rows (`ui/HistoryScreen.kt`) and group rows (`ui/ListGroupRows.kt`) via `kg_value`, also in their TalkBack text
- [ ] 1.7 Kulutus readouts (`chart/TabReadouts.kt`): "aktiivinen N kcal", "lepo N kcal" in the Historia marker card and own charts
- [ ] 1.8 Sweep `strings.xml` and `formatKcal`/`formatKg`/`formatSteps`/`formatChange` call sites once more for any bare number left; fix in the same way

## 2. Finish

- [ ] 2.1 Update tests asserting the changed texts; `./gradlew test lint assembleDebug`
- [ ] 2.2 Bump `appVersion` to 1.47
