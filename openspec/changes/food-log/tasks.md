## 1. Food rules (pure, `food/`)

- [ ] 1.1 `Product`, `FoodItem`, `Energy` (per 100 g, per piece, total), kcal from amount, unknown kcal
- [ ] 1.2 Name matching (case, spaces, ä/a ö/o, word and compound-part starts, one typo from four letters) and the add rules (bare number → Pikalisäys, known name → its product, else a new product without kcal)
- [ ] 1.3 Suggestions at the empty field (±2 h of clock time over 30 days, then all logs, ties by recency) and while typing (own before base, max five)
- [ ] 1.4 Fill-later rule (energy saved on the product, fills its items without kcal only), day sums (total, Herkku share, count without kcal), order
- [ ] 1.5 Record id codec (position, amount and unit, Herkku, time flag) with fallback for unknown ids
- [ ] 1.6 Unit tests for 1.1–1.5 (made-up foods)

## 2. Base products

- [ ] 2.1 Build about 300 generic foods (dishes, breads, dairy, fruit and vegetables, treats, drinks, raw ingredients) with kcal per 100 g or per piece and a typical portion from open sources (Open Food Facts) and generic estimates; a script in `scripts/` writes `assets/base-products.json`
- [ ] 2.1b Add generic home dishes with estimated kcal per 100 g (user's everyday cooking, 2026-11-14): kinkkukiusaus, tortillapizza (tomaatti, juustoraaste, kalkkunapepperoni, feta, punasipuli), kanakeitto, makkarakeitto, uuniranskalaiset, nakit, lihapullat (jauheliha 10 %, kermaviili, sipulikeittoaines), kananugetit, lasagnette (jauheliha 10 %), kermaviilikana, keitetty riisi, keitetty peruna, makaroni; jauheliha 10 % / 9,5 %
- [ ] 2.2 Loader and a test that every base product has a name and energy

## 3. Storage and Health Connect

- [ ] 3.1 `ProductStore` (`products.json`) in `TerveysApp`; in Android backup like the other app files
- [ ] 3.2 `HealthConnectFoods`: write, read own for a range, delete with undo, rewrite on edit and reorder; times per design 5; READ/WRITE_NUTRITION in the manifest and permission contract, asked on first food use only
- [ ] 3.3 Log events `food.add|edit|delete|order|read` and `hc.permission.nutrition` with counts and timings only (no names or kcal)
- [ ] 3.4 Tests: store round trip, record ↔ item mapping

## 4. Adding and the Ruoka screen

- [ ] 4.1 Add sheet: field with keyboard, suggestion buttons, typing suggestions, one-tap add with Muuta · Kumoa line, enter rules; permission reason with grant button
- [ ] 4.2 `Screen.FOOD`: day list in order with small times, top line (total · herkkuja · ilman kcal), day arrows, empty-day line
- [ ] 4.3 Item editor (amount with unit, kcal, Herkku, Poista + Kumoa) and Järjestä drag mode
- [ ] 4.4 Front page: "Syöty tänään" row and outlined Lisää annos
- [ ] 4.5 Smoke tests: add sheet adds a suggestion; Ruoka shows items and total; front page button opens the add sheet

## 5. Energia tab in Historia

- [ ] 5.1 Pure series: daily or weekly bars with Herkku share, 7-day average over logged days (≥3), statistics, logged-day list
- [ ] 5.2 Energia tab: bars + average line, weight chart under it on the same x-axis, statistics, day list opening Ruoka; reason + grant button without permission
- [ ] 5.3 Unit tests for 5.1; one smoke test that Energia shows bars and the day list

## 6. Ruokakirjaus in Asetukset

- [ ] 6.3 Ruokakirjaus: a sixth row before Ulkoasu in the Asetukset list (from settings-list), its sub-screen with own products, edit and remove

## 7. Data file

- [ ] 7.1 `DataSet` fields `foods` and `products`; CSV and JSON parts; import writes missing foods and adds unknown products; backup and export follow
- [ ] 7.2 Tests: CSV/JSON round trip, import twice, file with weights only

## 8. Docs and release

- [ ] 8.1 Ohje section on the food log with the Fineli attribution
- [ ] 8.2 Update product.md, roadmap (food-log done, steps next), `.claude/CLAUDE.md` map (`food/`, log events)
- [ ] 8.3 `appVersion=1.24`; `./gradlew test lint assembleDebug` green
