# Tasks

## 1. Info lines (pure)

- [ ] 1.1 A pure function (e.g. `food/FoodInfo.kt`) that builds the ⓘ bubble's lines from a
  `FoodItem` and the matching own `Product?` (by `FoodNames.key`): amount, kcal or "ilman kcal",
  Kalorit per 100 g / kpl, Herkku, logging time only when `timed`; each line only when known, in
  that order. Returns typed parts so the UI picks strings and formats (`formatAmount`,
  `formatKcal`, `food_per_100g`, `food_per_kpl`).
- [ ] 1.2 Unit tests with made-up items: all lines, no amount and no kcal, product missing or
  without Kalorit, treat, untimed item.

## 2. One-line rows with ⓘ

- [ ] 2.1 `FoodRow` (ui/FoodScreen.kt): drop the amount line; name + herkku sign + kcal on one
  line, min height 56 dp; name shortens first, kcal and buttons never shrink.
- [ ] 2.2 An `ItemInfoButton` (ⓘ, 48 dp target, content description "Tiedot: <name>") built on
  Material 3 `TooltipBox` + `RichTooltip` (title = name, lines from 1.1), opened on tap
  (`rememberTooltipState(isPersistent = true)`), closed by outside tap, back or a second tap.
  Placed before the ⋮ button on the Ruoka screen's rows and the sheet's day list rows; not in
  Järjestä. Row tap elsewhere still opens the editor. The own product comes from the view
  model's product list (no new reads).
  Implementation notes from the review: `enableUserInput = false` on `TooltipBox`; the ⓘ click
  toggles (`if (visible) dismiss() else show()`) so a second tap closes instead of reopening;
  `TooltipDefaults.rememberRichTooltipPositionProvider()` without a caret; if the popup is
  focusable, back must not reach the `ModalBottomSheet` and focus/keyboard are restored (else a
  `BackHandler` while open); outside taps are consumed (a full-screen transparent catcher while
  open, or the Popup's own dismiss consuming); close on list scroll (`isScrollInProgress`) and
  sheet drag; announce via `liveRegion` or accessibility focus. Icon: Material Symbols "info"
  outlined as a new drawable, tinted like ⋮; ⓘ and ⋮ the same size (match `RowMenu`). Name keeps
  at least ~24 dp; the herkku sign never shrinks; the row may grow taller at big fonts but its
  text stays one line. Time from `item.time.atOffset(item.zoneOffset)` with `formatTime`; drop
  `FoodRow`'s unused `zone` parameter and update its KDoc. Herkku from `item.treat`.
- [ ] 2.3 Strings: `food_info` ("Tiedot: %1$s"), `food_info_treat` ("Herkku"),
  `food_info_time` ("kirjattu klo %1$s"); reuse existing kcal/amount/per strings.
- [ ] 2.4 Smoke tests (Robolectric/Compose): the row has no node with the amount text; after a
  click on "Tiedot: Rahka" `onNodeWithText("200 g", useUnmergedTree = true)` exists and the item
  is unchanged; a second click closes it. Unit tests in 1.2 also cover a removed ruoka and an
  untimed item.

## 3. Docs and version

- [ ] 3.1 `openspec/context/product.md` glossary: add **ⓘ (Tiedot)**: a food row's info button, shows
  the annos's details, no actions; adjust the ⋮ line ("the row's one action button").
- [ ] 3.2 Roadmap: mark "rows with name and kcal only" done (food-row-info, version).
- [ ] 3.3 Bump `appVersion` to 1.62 in `gradle.properties`.
- [ ] 3.4 `./gradlew test lint assembleDebug`.
