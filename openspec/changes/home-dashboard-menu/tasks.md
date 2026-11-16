# Tasks

## 1. App menu

- [ ] 1.1 One shared top bar actions composable (help `IconButton` + `MoreVert` `IconButton` with a Material 3 `DropdownMenu`): Punnitse (scale icon, opens the weigh-in dialog as the old card button did), Lisää paino käsin (opens the manual entry dialog as Historia's "+" did), Lisää annos (opens the add sheet for today as "+ Annos" did), `HorizontalDivider`, Asetukset (opens Asetukset). Menu button content description "Lisää toimintoja". Used on the front page and Historia; the gear and Historia's "+" go. Edit mode's edit bar still replaces it.
- [ ] 1.2 Back from each target returns to the screen the menu was opened on (existing `MainUiState.back()` rule).
- [ ] 1.3 Tests: each menu item opens its target from the front page and from Historia; no gear or "+" nodes in either top bar.

## 2. Cards without buttons

- [ ] 2.1 Weight card: remove Punnitse at every size; the empty state reads "Ei painoja vielä" and "Punnitse ⋮-valikosta tai widgetistä"; the card's tap rules otherwise unchanged.
- [ ] 2.2 Food card: remove "+ Annos" at every size; the bars take the room; title/number still open Ruoka, bars Historia Energia.
- [ ] 2.3 Minimum plot height (home-screen "Card sizes"): every card chart's plot (inside its inset) ≥ 56 dp at font scale 1.0 and ≥ 40 dp at 1.3, all sizes and cards (weight, food, steps, Kulutus, own charts). If a card's content cannot fit the row height with that plot, shorten its secondary texts (not the plot) and note what was done here.
- [ ] 2.4 Tests: plot height ≥ 56 dp for each card at small/wide/big at 1.0 and ≥ 40 dp at 1.3 (Robolectric, about 360 dp wide); no Punnitse/+ Annos nodes on the front page; long-press on the food card's title starts edit mode; the default layout still fits without scrolling (existing test).

## 3. Copy

- [ ] 3.1 `strings.xml`: `help_weigh_body` (Punnitse from the ⋮ menu instead of the card button), `help_manual_body` (⋮ → Lisää paino käsin instead of the plus button), `help_food_body` (⋮ → Lisää annos instead of the card button); menu item strings.

## 4. Finish

- [ ] 4.1 `./gradlew test lint assembleDebug`
- [ ] 4.2 Update the CLAUDE.md map if the top bar moves to a new file; product.md decision "front page is a dashboard, actions in ⋮" (user 2026-11-16, reverses the 2026-11-09 no-⋮ rule); bump `appVersion` to 1.53.
