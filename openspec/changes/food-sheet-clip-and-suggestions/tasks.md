# Tasks

## 1. Offer rule (pure)

- [x] 1.1 `ClipOffers.key(parsed)` canonical hash, and the offer decision from (description has text, timestamp, cached result, done keys); unit tests: same JSON in different chat text gives the same key, changed kcal gives another key, cached timestamp skips the read, done key hides the offer, 0 timestamp reads
- [x] 1.2 `ClipOffer(key, count, kcal, productsOnly)` with kcal = sum of items' kcal (null when none has); unit tests for items, no-kcal and products-only JSON

## 2. Offer state and reading

- [x] 2.1 `FoodViewModel`/`FoodClipboard`: prefs for the cached last read and the done keys (max 50); check on every open/resume of front page, Ruoka and sheet (existing call sites); Ohita and `addAi` add the key; `AiPreview.sourceKey` set by offer, paste and share; Peruuta keeps it; log `food.import.offer check ...`; view-model tests: offer survives a second check, gone after save and after Ohita, kept after Peruuta
- [x] 2.2 Tapping the offer reads the clipboard text and opens the preview (in the widget sheet: hands over as now); test with a fake clipboard text

## 3. Offer UI

- [x] 3.1 `ClipOfferButton` under the field in the sheet and at the top of Ruoka; front page notice text updated; new strings; smoke test that the button shows and opens the preview

## 4. Suggestion rows and edit before adding

- [x] 4.1 `SuggestionRow` (name, amount · kcal, treat sign, pencil) for the empty-field list and typed matches, replacing the chips; smoke test: rows show, tapping a row adds
- [x] 4.2 Draft mode of the item editor (Lisää/Peruuta), view model builds the draft like a tap; Lisää adds with edited values and shows "Muuta · Kumoa", Peruuta adds nothing; view-model tests for both and for the product remembering the edited amount

## 5. Docs and release

- [ ] 5.1 Ohje text on the clipboard offer and the edit button; CLAUDE.md log events (`food.import.offer check`, `kind=edited`); roadmap entry done; bump MINOR in `gradle.properties`; `./gradlew test lint assembleDebug` green
