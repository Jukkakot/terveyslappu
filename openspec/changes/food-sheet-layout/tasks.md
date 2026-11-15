# Tasks

## 1. Card data and Lisää kaikki

- [x] 1.1 `ClipOffer` preview lines and total from the dry-run plan; unit tests: lines and total match the preview, "+N muuta" after five, products-only lines
- [x] 1.2 `FoodViewModel.addAllFromClip()` reads, checks the key, saves via the import path to the shown day, marks done, message with Kumoa; view-model tests: added and offer gone, changed clip saves nothing and refreshes, Kumoa removes items but offer stays gone

## 2. Sheet layout

- [x] 2.1 Header with day and ⋮ (Kopioi tuontiohje), card above the field, field hint and search icon, list headings, bottom "Tuo ruoka-annokset"; keyboard only without the card; same card on Ruoka; widget sheet saves in place; smoke tests: card shows and Lisää kaikki adds, keyboard focus without card
- [x] 2.2 Enter row "Lisää ”<text>” uutena" with its second line; test that tapping it equals Enter for a number and a name

## 3. Delete product

- [x] 3.1 Trash button on own-product rows, product removed with Kumoa line, none on base products; view-model test remove + undo; smoke test the button exists only on own rows

## 4. Undo messages

- [x] 4.1 App snackbar: 4 s timer, swipe to dismiss, above the nav bar, not over an open sheet; test that an action message is gone after the timeout

## 5. Docs and release

- [x] 5.1 Ohje text (card, delete), CLAUDE.md log events (`food.import.offer addall`, `food.product.delete`), roadmap: this done and a backlog item "consistency pass: rows, buttons, delete and Kumoa the same on every screen"; bump MINOR; `./gradlew test lint assembleDebug` green
