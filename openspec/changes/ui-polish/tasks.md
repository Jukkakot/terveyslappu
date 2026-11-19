# Tasks

Do not edit `ui/FoodSearch.kt`, `ui/AppSnackbar.kt`, `ui/WeighInDialog.kt`, `ui/WeighInClose.kt`,
the notification or the widgets (owned by `everyday-feedback`). Test data is made up.

## 1. Changes from shown weights

- [ ] 1.1 Add the pure shown-change helper (design 3) and use it in `HomeSummary.sparkChange`, `PeriodStats.change`/`perWeek` and `ListGroups` (row and group change); unit tests: 82.34 → 81.76 gives −0.5, 82.04 → 81.76 gives −0.2, BMI values the same way; existing `HomeSummary`/`PeriodStats`/`ListGroups` tests still pass.
- [ ] 1.2 Viikossa: `formatRate` one decimal, unit "kg/vk" (string resource), arrow by `shownDirection(it, 1)`; update `FormatTest` and add a stats smoke test that −0,6 kg over 6 days reads −0,7 kg/vk.

## 2. Front page cards

- [ ] 2.1 Non-breaking units: number–unit gaps U+00A0 and a word joiner after the slash in the home average strings (`home_food_average*`, `home_steps_average*`, the Kulutus card's average, `steps_value`, `food_kcal` where used on cards); unit test over these strings that no breakable space or slash break sits inside "N unit/pv".
- [ ] 2.2 Card bottom row as a FlowRow (design 1) on the food, steps and Kulutus cards; smoke test at 360 dp: small steps card's average value line holds "askelta/pv" on one line, and at 130 % the average sits under the period label; `HomeDashboardTest`'s 56 dp plot checks still pass.
- [ ] 2.3 Food card one tap target (design 8): one `clickable` on the card ("Avaa päivän annokset"), inner clickables removed, period label menu kept; update `HomeCardsTest`: tapping the bars opens Ruoka on today, not Historia; the period label still opens its menu; NEEDS asks for the permission.
- [ ] 2.4 Large text (design 4): `rowUnit()` without the 1.3 cap; above 1.3 small cards render wide, one per row, stored sizes untouched; tests at `fontScale = 2f`: default layout has three full-width rows, no card text clipped (bounds inside the card), every plot ≥ 56 dp; at 1.0 food and steps share a row again; edit mode at 2f still marks Pieni.

## 3. Historia

- [ ] 3.1 "Uusi kaavio" tab as a "+" icon tab (design 5), contentDescription "Uusi kaavio", min 48 dp; update the tab tests: the tab has no text, its content description is "Uusi kaavio", tapping it shows the Uusi kaavio page.

## 4. Ruoka and the item editor

- [ ] 4.1 Ruoka top bar: replace the sort icon with a ⋮ "Lisää toimintoja" menu holding "Järjestä annokset" (design 7); order mode keeps Valmis; update `FoodScreenTest` (menu item starts Järjestä) and add Ruoka to `LargeTextTest` at 200 % (back, title, Viikko and ⋮ displayed).
- [ ] 4.2 Pure recent-amounts rule in `food/` (design 9) with unit tests: newest first, distinct, same unit only, edited annos excluded, at most three, empty for no history.
- [ ] 4.3 `FoodEditor` chips under the amount field from both callers (`MainScreen`, `FoodAddDialog`); hidden for Yhteensä and when empty; chip equal to the field selected; smoke test: tapping "200 g" sets the amount field and the result line.

## 5. Ulkoasu

- [ ] 5.1 Teema and Värit as radio rows (design 6): `selectableGroup`, one 48 dp `selectable(role = RadioButton)` row per option, existing test tags kept on the rows; update `SettingsScreenTest` (choosing Vaalea still calls the action) and add Ulkoasu to `LargeTextTest` at 200 % (all five options displayed, none wrapped to two lines).

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
