# Tasks

Test data is made up. `everyday-feedback` is merged; the weigh-in dialog and `auto/AutoText` are in scope only for the change text.

## 1. Changes from shown weights

- [x] 1.1 Add the pure helper `derived/ShownChange.kt` (design 3: `shownTenths(value): Long` rounded like `%.1f`, `shownDelta` in whole tenths); make `formatKg`, `formatChange` and `shownDirection` use it; use it in `HomeSummary.sparkChange`, `PeriodStats.change`/`perWeek` and `ListGroups` (row and group change); unit tests: 82.34 → 81.76 gives −0.5, 82.04 → 81.76 gives −0.2, BMI 27.049999999999997 shows and counts as 27.0; existing `HomeSummary`/`PeriodStats`/`ListGroups` tests still pass.
- [x] 1.2 Viikossa: `formatRate` one decimal (half away from zero, "±0,0" and no arrow when it rounds to 0), unit "kg/vk" (string resource), exact span as today and null under one day; update `FormatTest` and add tests: −0,6 kg over 6 days reads −0,7 kg/vk, −0.05 reads −0,1, −0.0049 reads ±0,0 without an arrow.
- [x] 1.3 Use the helper for the weigh-in dialog's change (`scale/WeighIn.kt` `WeightChange`, `WeighInViewModel`) and the notification (`AutoText.change` and "Poikkeaa"); tests: 82.34 → 81.76 reads −0,5 in both; existing weigh-in and `AutoText` tests still pass.

## 2. Front page cards

- [x] 2.1 Non-breaking units: number–unit gaps U+00A0 and a word joiner after the slash in every card string: an explicit list (`kg_value`, `home_spark_range` with no break before "kg" and around "→", `home_food_average*`, `home_steps_average*`, the Kulutus card's average, `steps_value`, `food_kcal` in the big card's scale label, the empty-state strings); one unit test runs over that list and checks no breakable space or slash break sits inside "N unit" or "unit/pv".
- [x] 2.2 Card bottom row as a FlowRow (design 1) on the food, steps and Kulutus cards: small card average two lines end-aligned, no-average text one wrapping line, wide/big one line, same FlowRow fallback; new split string resources; smoke test at 360 dp: small steps card's average value line holds "askelta/pv" on one line, and at 130 % the average sits under the period label; a wide card at 200 % keeps the average whole; `HomeDashboardTest`'s 56 dp plot checks still pass.
- [x] 2.3 Food card one tap target (design 8): one `clickable` on the card ("Avaa päivän annokset"), inner clickables removed, period label menu kept; permission NEEDS asks, DENIED opens Ruoka on today; update `HomeCardsTest`: tapping the bars opens Ruoka on today, not Historia; the period label still opens its menu. Update `help_food_body` (Ohje) to the new tap.
- [x] 2.4 Large text (design 4): `rowUnit()` without the 1.3 cap; `fontScale > 1.3f` small cards render wide, one per row, in order, stored sizes untouched, in edit mode too; tests at `fontScale = 2f`: default layout has three full-width rows, no card text clipped (bounds inside the card), every plot ≥ 56 dp; at 1.3f and 1.0 food and steps share a row; edit mode at 2f draws the same grid and still marks Pieni.

## 3. Historia

- [x] 3.1 "Uusi kaavio" tab as `Tab(icon = …)` "+" (design 5), Icon contentDescription "Uusi kaavio", min 48 dp, tag `tab-NEW` kept; update the tab tests (assert by tag): the tab has no text, its content description is "Uusi kaavio", tapping it shows the Uusi kaavio page; TalkBack shows a tab with selected state.

## 4. Ruoka and the item editor

- [x] 4.1 Ruoka top bar: replace the sort icon with a ⋮ "Lisää toimintoja" menu holding "Järjestä annokset" (design 7), shown under the old sort icon's rule (granted, loaded, more than one annos that day); order mode keeps Valmis; update `FoodScreenTest` (menu item starts Järjestä) and add Ruoka to `LargeTextTest` at 200 % with a fixture of at least two annokset (back, title, Viikko and ⋮ displayed).
- [ ] 4.2 Pure recent-amounts rule in `food/` (design 9) with unit tests: matched by `FoodItem.key`, day-then-time newest first over all days, distinct by shown text, same unit only, edited annos and Yhteensä annokset excluded, at most three, empty for no history.
- [ ] 4.3 `FoodEditor` chips under the amount field from both callers (`MainScreen`, `FoodAddDialog`); hidden for Yhteensä, when empty and while loading; chip equal to the field selected; the field keeps its number on a kpl/g switch; smoke test: tapping "200 g" sets the amount field and the result line.

## 5. Ulkoasu

- [x] 5.1 Teema and Värit as radio rows (design 6): `selectableGroup`, one 48 dp `selectable(role = RadioButton)` row per option, existing test tags kept on the rows; update `SettingsScreenTest` (choosing Vaalea still calls the action) and add Ulkoasu to `LargeTextTest` at 200 % (all five options displayed, none wrapped to two lines).

## 6. Screenshot tour and checks

- [ ] 6.1 Add the 200 % shots to `ScreenshotTour` (design 10: etusivu top and scrolled, Ruoka, Ulkoasu; brand colours; both themes; `-200` in the name); run `./gradlew testDebugUnitTest -Pscreenshots --tests '*ScreenshotTour*'` and look at the PNGs (never commit them).
- [ ] 6.2 Run `./gradlew test lint assembleDebug`; all green.

## 7. Release

- [ ] 7.1 Bump MINOR `appVersion` in `gradle.properties` by one from whatever it is at implementation time (e.g. 1.64 → 1.65); verify `./gradlew assembleDebug` still builds.

## Workflow follow-up

- The master updates `openspec/context/roadmap.md` (Polish line, U7, U9 done) and `product.md`
  decisions (Viikossa one decimal, theme radio rows, food card one target) at merge, then archives.
- Follow-up for `everyday-feedback`'s area: the weigh-in dialog's change line can adopt the
  shown-change helper.
