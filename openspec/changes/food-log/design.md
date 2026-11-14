# Design: food-log

## Context

The app logs weight to Health Connect and shows its own weights in Historia (Paino and BMI tabs).
The food log adds a second measured kind with the same shape: written to Health Connect, read back
as this app's own records, carried in the data file. Requirements: product.md "Food log: why".
Packages follow the existing split: pure rules in their own package, Android glue at the edge.

## Goals / Non-Goals

**Goals:** log a day's food in a few taps per slot; learn the usual dishes once; see daily kcal,
treats and weight on one time axis at week level; Health Connect as the store of the log.

**Non-Goals:** steps (own change next), macros or other nutrients, food databases or barcode
scanning, photo AI, reading other apps' nutrition, budgets, targets, TDEE or any inferred energy
balance, streaks, reminders, notifications.

## Decisions

1. **Package `food/`** holds the pure parts: `FoodItem` (saved item), `SavedMeal`, `FoodEntry`
   (one logged item), `MealSlot`, kcal arithmetic, frequency ordering, day and period sums, the
   data file parts. Android glue: `HealthConnectFoods` next to `HealthConnectWeights`, a
   `FoodStore` (`JsonFileValue`, file `food-items.json`) for saved items and meals. The Ruoka
   screen is a new `Screen.FOOD` in `MainViewModel`, with its own view model.

2. **Saved item has one of two energy forms:** kcal per 100 g with a usual portion in grams
   (dishes; logging asks only the grams, prefilled with the usual portion), or kcal per serving
   (fixed items such as a yoghurt; logging asks the number of servings, default 1). Grams are as
   eaten. Alternative (always per 100 g) rejected: a packaged item has a serving, not a weight
   the user knows.

3. **Saved meal = named list of (item, amount)** bound to one slot; one tap writes one record per
   item. Records stay per item so the learning view can tell bread from the dish.

4. **One Health Connect `NutritionRecord` per logged item**: `name` = item name (or "Pikalisäys"),
   `energy` = kcal, `mealType` from the slot (Aamupala → breakfast, Lounas → lunch, Päivällinen →
   dinner, Iltapala and Väliin → snack), time = now for today, and for an earlier day that day at
   the slot's typical hour (7, 11, 17, 20; Väliin 14), manual entry metadata. Grams and the treat
   mark are not standard fields (decision 5).

5. **Slot, treat and grams in the client record id**: `terveyslappu-food-<slot>-<t|m>-<grams or
   0>-<uuid>`. Health Connect then holds everything needed to rebuild the day, nothing extra
   needs an orphan-prone local store, and a restore from the data file keeps it. An edit deletes
   and rewrites the record (new id), as the weight's undo already does. Alternative (a local
   `food-marks.json` keyed by id, like the "?" marks) rejected: two stores to keep in step.

6. **Reading**: own records only (`dataOriginFilter` = this package), like weights; records
   without our id format (written by an older build or by hand edit) fall back to the meal type's
   slot (snack → Väliin) and no treat.

7. **Permissions** READ_NUTRITION and WRITE_NUTRITION are requested the first time the Ruoka
   screen opens; denial shows a short reason and a button, and the rest of the app is unaffected.
   Weigh-in paths never ask for them.

8. **Frequency ordering**: per slot, items logged in the last 60 days ordered by count, then the
   rest by name; saved meals of the slot on top. Computed from the read records, no stored counter.

9. **Energia chart**: daily bars for logged days only, the treat part stacked in a second tone;
   a 7-day trailing average line over logged days (needs at least 3 logged days in the window, else
   no point); under it a small weight line on the same x-axis using the shared weight scale. No
   dual y-axis. Periods as Historia's; 1 v and Kaikki show weekly average bars instead of days so
   bars stay readable.

10. **Data file kinds** `foods` (each logged item: time with offset, slot, name, kcal, grams,
    treat) and `food_items` (saved items and meals). Import of `foods` writes nutrition records
    skipping ones already present (same client record id); import of `food_items` merges by name.

## Risks / Trade-offs

- Half-logged days lower the averages → the statistics count logged days and say so; the user
  sees a day's list before trusting a number. No "complete day" flag (extra tap, BR3).
- Health Connect read of nutrition over a year per period switch may be slow → read a period
  once and cache per screen session, like weights.
- The client record id format is a contract with the data on the phone → parsed in one pure
  function with tests; unknown formats fall back (decision 6).
- Samsung Health shows our records as its own food entries; if it sums them differently it is
  only a view (BR6 accepts that).
