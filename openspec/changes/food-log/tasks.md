## 1. Food model and rules (pure, `food/`)

- [ ] 1.1 `MealSlot` (five slots, Finnish labels, Health Connect meal type, typical hour for past days), `FoodItem` (name, herkku, per-100 g with usual grams or per-serving), `SavedMeal`, `FoodEntry` (time, offset, slot, name, kcal, grams/servings, treat, record ids)
- [ ] 1.2 kcal from amount, client record id format `terveyslappu-food-<slot>-<t|m>-<amount>-<uuid>` with parse and fallback from meal type (design 5, 6)
- [ ] 1.3 Day sums (per slot, day, treat share), add-list ordering (slot's meals, 60-day frequency, rest by name), unique names ignoring case
- [ ] 1.4 Unit tests for 1.1–1.3 (made-up foods)

## 2. Storage and Health Connect

- [ ] 2.1 `FoodStore` (`food-items.json`, saved items and meals) in `TerveysApp`; included in the app's Android backup like the other app files
- [ ] 2.2 `HealthConnectFoods`: write, read own for a range, delete with undo, rewrite on edit; READ/WRITE_NUTRITION in the manifest and the permission contract, asked only from the Ruoka screen
- [ ] 2.3 Log events `food.save|read|delete|edit` and `hc.permission.nutrition` (counts, slots and timings only; no food names or kcal values in the log)
- [ ] 2.4 Tests: store round trip, record mapping to/from `FoodEntry` (Robolectric or pure mapper)

## 3. Ruoka screen

- [ ] 3.1 `Screen.FOOD` with its view model: day navigation, slots, totals, herkku sign, empty day without judgement, permission reason with grant button
- [ ] 3.2 Add list, amount dialog with live kcal, Pikalisäys, Uusi ruoka, saved meal one-tap
- [ ] 3.3 Item tap: change amount or slot, delete with Kumoa
- [ ] 3.4 Home row "Syöty tänään" opening Ruoka on today
- [ ] 3.5 Smoke tests: Ruoka screen shows slots and total; home row opens Ruoka

## 4. Energia tab in Historia

- [ ] 4.1 Pure series: daily or weekly bars with treat share, 7-day average over logged days (≥3), period statistics, logged-day list
- [ ] 4.2 Energia tab: bars + average line, small weight chart under it on the same x-axis, statistics, day list opening Ruoka; reason + grant button without permission
- [ ] 4.3 Unit tests for 4.1; one smoke test that Energia shows bars and the day list

## 5. Asetukset as a list of sub-screens

- [ ] 5.1 Main list with icon, name, state line, chevron and warning tint; pure state-line rules per row (unit tests)
- [ ] 5.2 Move today's sections into sub-screens (Omat tiedot, Automaattinen punnitus, Tiedot ja varmuuskopio, Ulkoasu, Tuki) unchanged; deep links open the sub-screen and back returns to the origin
- [ ] 5.3 Ruokakirjaus sub-screen: saved meals and items with add, edit, remove
- [ ] 5.4 Smoke tests: list shows six rows; a row opens its sub-screen; the BMI link opens Omat tiedot and back returns home

## 6. Data file

- [ ] 6.1 `DataSet` fields `foods` and `foodItems`; CSV and JSON parts; import writes missing foods and merges saved items by name; backup and export follow
- [ ] 6.2 Tests: CSV/JSON round trip, import twice, file with weights only

## 7. Docs and release

- [ ] 7.1 Ohje section on the food log
- [ ] 7.2 Update product.md (decisions taken here), roadmap (food-log done, steps next), `.claude/CLAUDE.md` map (`food/`, log events)
- [ ] 7.3 `appVersion=1.21`; `./gradlew test lint assembleDebug` green
