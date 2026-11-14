## 1. Food rules (pure, `food/`)

- [x] 1.1 `Product`, `FoodItem`, `Energy` (per 100 g, per piece, total), kcal from amount, unknown kcal
- [x] 1.2 Name matching (case, spaces, ä/a ö/o, word and compound-part starts, one typo from four letters) and the add rules (bare number → Pikalisäys, known name → its product, else a new product without kcal)
- [x] 1.3 Suggestions at the empty field (±2 h of clock time over 30 days, then all logs, ties by recency) and while typing (own before base, max five)
- [x] 1.4 Fill-later rule (energy saved on the product, fills its items without kcal only), day sums (total, Herkku share, count without kcal), order
- [x] 1.5 Record id codec (position, amount and unit, Herkku, time flag) with fallback for unknown ids
- [x] 1.6 Unit tests for 1.1–1.5 (made-up foods)

## 2. Base products

- [x] 2.1 Build about 300 generic foods (dishes, breads, dairy, fruit and vegetables, treats, drinks, raw ingredients) with kcal per 100 g or per piece and a typical portion from open sources (Open Food Facts) and generic estimates; a script in `scripts/` writes `assets/base-products.json`
- [x] 2.1b Add generic home dishes with estimated kcal per 100 g (user's everyday cooking, 2026-11-14): kinkkukiusaus, tortillapizza (tomaatti, juustoraaste, kalkkunapepperoni, feta, punasipuli), kanakeitto, makkarakeitto, uuniranskalaiset, nakit, lihapullat (jauheliha 10 %, kermaviili, sipulikeittoaines), kananugetit, lasagnette (jauheliha 10 %), kermaviilikana, keitetty riisi, keitetty peruna, makaroni; jauheliha 10 % / 9,5 %
- [x] 2.2 Loader and a test that every base product has a name and energy

## 3. Storage and Health Connect

- [x] 3.1 `ProductStore` (`products.json`) in `TerveysApp`; in Android backup like the other app files
- [x] 3.2 `HealthConnectFoods`: write, read own for a range, delete with undo, rewrite on edit and reorder; times per design 5; READ/WRITE_NUTRITION in the manifest and permission contract, asked on first food use only
- [x] 3.3 Log events `food.add|edit|delete|order|read` and `hc.permission.nutrition` with counts and timings only (no names or kcal)
- [x] 3.4 Tests: store round trip, record ↔ item mapping

## 4. Adding and the Ruoka screen

- [x] 4.1 Add sheet: field with keyboard, suggestion buttons, typing suggestions, one-tap add with Muuta · Kumoa line, enter rules; permission reason with grant button
- [x] 4.2 `Screen.FOOD`: day list in order with small times, top line (total · herkkuja · ilman kcal), day arrows, empty-day line
- [x] 4.3 Item editor (amount with unit, kcal, Herkku, Poista + Kumoa) and Järjestä drag mode
- [x] 4.4 Front page: "Syöty tänään" row and outlined Lisää annos
- [x] 4.5 Smoke tests: add sheet adds a suggestion; Ruoka shows items and total; front page button opens the add sheet

## 5. Energia tab in Historia

- [x] 5.1 Pure series: daily or weekly bars with Herkku share, 7-day average over logged days (≥3), statistics, logged-day list
- [x] 5.2 Energia tab: bars + average line, weight chart under it on the same x-axis, statistics, day list opening Ruoka; reason + grant button without permission
- [x] 5.3 Unit tests for 5.1; one smoke test that Energia shows bars and the day list

## 6. Ruokakirjaus in Asetukset

- [x] 6.3 Ruokakirjaus: a sixth row before Ulkoasu in the Asetukset list (from settings-list), its sub-screen with own products, edit and remove

## 7. Data file

- [x] 7.1 `DataSet` fields `foods` and `products`; CSV and JSON parts; import writes missing foods and adds unknown products; backup and export follow
- [x] 7.2 Tests: CSV/JSON round trip, import twice, file with weights only

## 8. Docs and release

- [x] 8.1 Ohje section on the food log with the base products' sources (Open Food Facts, ODbL, and generic estimates; Fineli dropped, design 3)
- [x] 8.2 Update product.md, roadmap (food-log done, steps next), `.claude/CLAUDE.md` map (`food/`, log events)
- [x] 8.3 `appVersion=1.24`; `./gradlew test lint assembleDebug` green
