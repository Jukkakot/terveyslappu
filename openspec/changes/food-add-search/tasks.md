# Tasks

Start only after `food-row-info` is implemented and archived (its "Item row buttons" text is the
base of this change's delta).

## 1. Search session (pure + view model)

- [ ] 1.1 Pure `AddSearchExit` (food/ or ui/): from the ids added in this search that are still
  logged and the origin, return Ruoka(day) or Origin. Unit tests: none added, one added, added
  then undone, Lisää kaikki ids.
- [ ] 1.2 `FoodViewModel`: rename `sheetDay` → `searchDay`, add `addedIds` (append on tap, Enter,
  editor Lisää, Lisää kaikki; remove on Kumoa and delete), `highlight: Set<id>` set on exit to
  Ruoka and cleared on leaving Ruoka or opening a new search; drop fold, `sheetKeyboard` and the
  single-item highlight. The "<name> lisätty · Kumoa" bar event per add; "Tallennus epäonnistui"
  and "Päivän annoksia ei voitu lukea" as bar events. View-model tests (existing fake
  `TerveysApp.foodRecords`): adds collect ids, Kumoa removes, exit target as 1.1.

## 2. The search screen

- [ ] 2.1 `AddFoodSearch` composable on Material 3's full-screen search bar (design 1): top bar
  (back, field with hint, ✕ while text, ⋮ Kopioi tuontiohje / Tuo ruoka-annokset), "Lisätään:
  <day>" line only when the day is not today, clipboard card (hidden while typing), "Usein tähän
  aikaan" up to 8 rows or the empty line, typed matches up to 10 then the Enter row; `AddRow`
  with + icon, herkku sign, supporting text, ⋮ (Muokkaa, own only red Poista); no dividers;
  SnackbarHost for the bars. Remove chips, panel, handle, fold, day list and header from
  `FoodSheet.kt` (the editor `FoodEditor` stays).
- [ ] 2.2 Permission states (checking: top bar only; Salli; denied → Health Connect permissions;
  missing) inside the search; field focus and keyboard after GRANTED.
- [ ] 2.3 Strings: `food_search_hint` (reuse), `food_adding_to` ("Lisätään: %1$s"),
  `food_search_empty` ("Kirjoita ruuan nimi tai pelkkä kcal-määrä"), `food_added`
  ("%1$s lisätty"), `food_read_failed` ("Päivän annoksia ei voitu lukea"), `food_search_clear`
  ("Tyhjennä"), `food_frequent` ("Usein tähän aikaan"); remove unused sheet strings.
- [ ] 2.4 Smoke tests (Robolectric/Compose): open with a day of two annokset → no node with their
  names as day rows and no total; typing shows match rows and the Enter row last; tapping a row
  shows the bar.

## 3. Hosts and exit

- [ ] 3.1 `MainScreen`/`MainViewModel`: host the search full screen, record the origin screen;
  exit per 1.1 (Ruoka via `openFoodDay(day)` + highlight, else back to origin). From the front
  page and Historia menus too. Smoke test: add then back → Ruoka shown with the highlighted row.
- [ ] 3.2 `FoodScreen`: tonal highlight of every id in `highlight`, scroll to the last; cleared on
  leaving Ruoka; Poista of a highlighted annos drops its highlight.
- [ ] 3.3 `FoodAddActivity` + manifest: full-screen opaque theme (new style, no dialog window),
  `FoodAddDialog` hosts `AddFoodSearch`; exit with adds starts `MainActivity` with the Ruoka-day
  + highlight extra (handled in `onCreate` and `onNewIntent`) and finishes; without adds
  finishes. Keep `food.sheet.open`/`food.sheet.timing` log events (names unchanged). Check:
  `./gradlew test lint assembleDebug`.

## 4. Context and docs

- [ ] 4.1 Replace references to "Adding an item" with "Adding from the search" and remaining "add sheet"/"Lisää annos sheet" wording in `openspec/specs/food-log`
  (Treats apart: drop "the add sheet's line under its header"; Omat ruuat and Food from an AI chat
  scenarios; Import shows changed products' style line) and `openspec/specs/home-screen` with
  "the add search"; `openspec/context/product.md` glossary: add "Lisää annos -haku" (the
  full-screen search that adds annokset); `.claude/CLAUDE.md` map (`FoodSheet`, `FoodAddActivity`
  descriptions); roadmap entry done; bump MINOR in `gradle.properties`.
