# Tasks

## 1. App menu

- [x] 1.1 One shared top bar actions composable (help `IconButton` + `MoreVert` `IconButton` with a Material 3 `DropdownMenu`, content description "Lisää toimintoja"). Items with leading icons from existing drawables: Punnitse (`ic_scale`, opens `WeighInActivity` as the old card button did; log source `main` from the front page, `history` from Historia), Lisää paino käsin (`ic_add`, opens the manual entry dialog), Lisää annos (`ic_restaurant`, add sheet for today, also from Historia's Syöty with a past day), `HorizontalDivider`, Asetukset (`ic_settings`). Used on the front page (also when Health Connect access is missing: targets explain as today) and on every Historia tab; not on other screens. The gear and Historia's "+" go.
- [x] 1.2 Lift `ManualEntryDialog` from `HistoryScreen` to `MainScreen` so it opens on either screen; the save confirmation shows on the screen it was opened from; no navigation.
- [x] 1.3 Back from every target (Asetukset, Ohje, dialogs, sheet) returns to the screen it was opened from (`MainUiState.back()`); Ohje opened from Historia returns to Historia.
- [x] 1.4 Edit mode start and any outside intent close an open menu.
- [x] 1.5 Tests: each menu item opens its target from the front page and from Historia; back returns to the opener; no gear or "+" nodes; menu closes when edit mode starts.

## 2. Cards without buttons

- [x] 2.1 Weight card: remove Punnitse at every size; empty state "Ei vielä painoja" + "Punnitse ⋮-valikosta tai widgetistä" (content description "Punnitse Lisää toimintoja -valikosta tai widgetistä"); small size: title one line (day and time only, ellipsized), change chip under the number.
- [x] 2.2 Food card: remove "+ Annos" at every size; the bars take the room; the period/average row unchanged; title/number still open Ruoka, bars Historia Energia.
- [x] 2.3 Decision (user 2026-11-16, after seeing renders at 248/268/292): `ROW_UNIT` = 268 dp (still × font scale up to 1.3). Then every card plot ≥ 56 dp at 1.0 and 1.3 (chart height minus 2 × inset; Allekkain own charts: panels together). The Kulutus card is expected to stay short: make its content more compact without dropping information (e.g. "aktiivinen N kcal" on one line, not wrapped, or beside the number when it fits) until it reaches 56 dp; if it still cannot, report the numbers. Previously measured at 248: weight 36, food 48, steps 48, Kulutus 12, first wide own chart 54.
- [x] 2.4 Tests (Robolectric, 360 × 640 dp): plot ≥ 56 dp for each card at small/wide/big at 1.0 and 1.3; the "A week's spike shows" scenario (98,0 kg point ≥ 25 % of the plot height above 97,2 kg); small food card with "1 240 kcal" and the herkkuja line not cut at 1.0 and 1.3; no Punnitse/+ Annos nodes; long-press on the food title starts edit mode; the existing fits-without-scrolling test still passes.

## 3. Copy

- [x] 3.1 `strings.xml`: `help_weigh_body` (adds the ⋮ menu), `help_manual_body` (⋮ → Lisää paino käsin instead of the plus button), `help_food_body` (⋮ → Lisää annos instead of the card button); menu item and empty-card strings.

## 4. Finish

- [x] 4.1 `./gradlew test lint assembleDebug`
- [x] 4.2 CLAUDE.md map if the top bar moves to a new file; product.md decision "front page is a dashboard, actions in ⋮" (user 2026-11-16, reverses the 2026-11-09 no-⋮ rule); bump `appVersion` to 1.53.
