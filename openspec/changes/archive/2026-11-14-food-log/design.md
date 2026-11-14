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

3. **Base products** in `assets/base-products.json` (~300 generic foods: name, other names, kcal per
   100 g or per piece, typical portion): dishes, breads, dairy, fruit, treats, drinks and raw
   ingredients, built once from open sources (Open Food Facts, ODbL) and generic estimates with a
   small script kept in `scripts/`; sources named in Ohje. Fineli was the first choice but refuses
   fetches from cloud sessions (user chose open sources, 2026-11-14). A base product becomes an own
   product the first time it is logged.

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

## Decisions from the spec review (2026-11-14)

User answers:
- No meals, sides or combos: every eaten thing is its own item ("keitto" and "leipä" are two items);
  a fixed combination may be saved as one product by the user. No "eaten with" suggestions.
- Herkku is on the product; switching it rewrites the product's items (all days).
- A whole day's total is one bare number on that day (Pikalisäys), added next to other items.

Taken on the user's behalf:
- Identity is the uuid part of the record id. Edit, reorder or Herkku switch: insert the new
  record, then delete the old; after any failure read the day again and show a short message.
  Import dedups by uuid. Codec: amounts with "." as decimal separator, `n` = no amount, pos 1..n.
- Permissions are asked on tapping Lisää annos or opening Ruoka or Energia. Before that the front
  row reads "Ruokakirjaus: anna lupa" and opens the request. Backup and export never write an
  empty foods part over foods they could not read (they keep the previous file's part); an import
  without the permission says the foods were not written.
- Adding shows the item at once; a failed write removes it with a short message, the cause is
  logged, the product is still learnt.
- An add goes to the day on screen; if that day is no longer today, the earlier-day rule applies.
  The front page re-evaluates today on resume.
- Suggestions: items with the no-time flag are left out of the ±2 h score but count in the 30-day
  counts; counts are distinct days, not items. With no own products the buttons' place shows a
  short hint to type.
- Usual amount: 1 kpl for per-piece products, the typical portion for base products, none for a
  name-only product. The Muuta · Kumoa line reads "ilman kcal" when unknown, refers to the latest
  add and stays until the next add or the sheet closes.
- Enter: only an integer is a bare number; other text is a trimmed name; empty does nothing; an
  exact (normalised) own or base name uses that product; fuzzy matches apply only from a
  suggestion button. Pikalisäys is never a product and never suggested.
- Editor: name, amount with a unit choice (kpl / g / kcal yhteensä), kcal, Herkku, Poista.
  Per piece and per 100 g are saved on the product and fill its items without kcal (every readable
  day); a total stays on the item. Changing the amount recomputes kcal from the product. Changing
  the name moves the item to that product (new if unknown).
- Ruokakirjaus: renaming a product rewrites its items; renaming to an existing name is refused
  with a message; a removed product typed again is learnt fresh.
- Järjestä has a Valmis button; the order is also saved on any other exit. Positions are
  renumbered 1..n and only moved records are rewritten. Undo of a delete reinserts the old id.
- Older history uses the weights' history permission and its hint (Energia 1 v / Kaikki / Oma,
  earlier days past 30 days).
- Energia: a day counts as logged with at least one item with kcal; today is listed but left out
  of averages and the line; weeks start on Monday, no average line in weekly mode; Kaikki counts
  days from the first log; the weight chart follows the weight-chart rules (no "?" weights);
  tapping a bar opens Ruoka on that day; back from Ruoka returns to Historia on Energia.
- Data file: kinds `foods` and `products` in both formats, the uuid carried; colliding positions
  on import go after the day's items; on a name clash the phone's product is kept; unknown-id
  records are exported as Ruoka without pos; oldest first.
