# Design: food-log

## Context

The app logs weight to Health Connect and shows its own weights in Historia (Paino and BMI tabs).
The food log adds a second measured kind with the same shape: written to Health Connect, read back
as this app's own records, carried in the data file. Requirements and how the user logs:
product.md "Food log: why" and "Food log: how it is used". The adding pattern is borrowed from the
user's shopping-list app kauppalappu (one add field, suggestions while typing and at the empty
field, one tap adds, enter adds the typed text, undo instead of confirmations, a shipped base
catalogue). The Asetukset list itself comes from the change settings-list; this change adds its Ruokakirjaus row.

## Goals / Non-Goals

**Goals:** a repeat in one or two taps; catching up later as easy as logging now; learn a
product's kcal once; daily kcal with base food vs treats and weight on one time axis at week level;
Health Connect as the store of the log; a calmer Asetukset.

**Non-Goals:** steps and the combined chart (backlog), meals or meal names, entering clock times,
"eat now or later" switches, day templates, categories other than Ruoka/Herkku, macros, barcode or
photo AI, other apps' nutrition, budgets, targets, TDEE or inferred energy balance, marking
unrealistically low days, streaks, reminders, any food or kcal outside the app (widget, tile,
notification), a server database.

## Decisions

1. **Package `food/`** holds the pure parts: `Product`, `FoodItem` (one logged item), `Energy`
   (per 100 g | per piece | total), kcal arithmetic, name matching (kauppalappu's rules: ä/a, ö/o,
   word and compound-part starts, one typo from four letters), suggestions, day sums, the record
   id codec and the data file parts. Android glue: `HealthConnectFoods` next to
   `HealthConnectWeights`; `ProductStore` (`JsonFileValue`, `products.json`) in `TerveysApp`.
   Screens: `Screen.FOOD` (day) with its view model; the add field is a bottom sheet shared by the
   front page and the day screen.

2. **Products are learnt, not set up**: every distinct typed name becomes a product (kauppalappu's
   "one item per distinct name"). No "save as product" step. Kcal is optional until known; filling
   it on one item fills the product and that product's items without kcal (one fill per thing).

3. **Base products** in `assets/base-products.json` (~150 generic foods: name, other names, kcal per
   100 g or per piece, typical portion) built once from Fineli open data (THL, CC BY 4.0) with a
   small script kept in `scripts/`; attribution in Ohje. Fineli's site refuses automated fetches
   from the cloud session, so the source zip may need to come from the user's phone (open item).
   A base product becomes an own product the first time it is logged.

4. **Suggestions at the empty field**: up to eight own products, scored by logs within ±2 h of
   the current clock time over 30 days, then by all logs over 30 days, ties by most recent.
   Computed from the read records; no stored counters. This is why logging times are kept, and it
   replaces day templates (a usual day surfaces by itself, a trip day just picks other things).

5. **One Health Connect `NutritionRecord` per item**: `name`, `energy` (null while unknown),
   `mealType` unknown, manual entry metadata. Time: logging time for an item logged on its own day;
   for an earlier day, that day at 12:00 local plus the position in seconds, flagged as "no time"
   so it is never shown.

6. **Order, amount, unit, product and Herkku in the client record id**:
   `tl-food-<pos>-<g|k|n><amount>-<t|m>-<timeFlag>-<uuid>`, parsed by one pure codec with tests.
   Health Connect alone rebuilds the day, and the data file and a restore keep it. Changing an
   item or the order rewrites the affected records (a day holds about ten). Alternative (a local
   marks file keyed by record id) rejected: two stores to keep in step.

7. **Reading**: own records only (`dataOriginFilter`), as for weights; a record with an unknown id
   format is shown in time order, as Ruoka, with its record's kcal.

8. **Permissions** READ_NUTRITION and WRITE_NUTRITION asked on first food use; weigh-in paths never
   ask. No history permission beyond what weights already have.

9. **Energia chart**: bars for logged days only, base food and Herkku stacked in two tones from the
   theme (neither red); 7-day trailing average over logged days (≥3 in the window); under it the
   weight line on the same x-axis with the shared weight scale; weekly bars from 90 days up. No
   second y-axis.

10. **Storage**: the log in Health Connect; products in app storage; both in the data file, so the
    automatic backup file in the user's own cloud and Android backup/Smart Switch carry them. No
    server database (product decision: health data, no backend).

## Risks / Trade-offs

- Half-logged days lower the averages → statistics count logged days; the user sees the day list.
- Base-product kcal are averages; a home recipe differs → the first log copies the values into an
  own product the user can correct.
- The record id format is a contract with data on the phone → one codec with tests, fallback for
  unknown ids.
- Samsung Health shows our records among its food entries; that is only a view (BR6 accepts it).
