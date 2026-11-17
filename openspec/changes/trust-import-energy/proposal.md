# Proposal

## Why

An imported item "Ranskalaiset" 188 g 270 kcal matched the base product "Ranskalaiset perunat"
(deep-fried, 290 kcal / 100 g, Herkku) by its alias. The item kept its 270 kcal, but it was
renamed, marked a treat, and the product stayed at 290, so the next log of the same food would
count far too much, unnoticed. The user's real use: lunch and dinner come as imported JSON whose
kcal is worked out from the actual package; the base products are not used at all. The import
must trust the JSON, and the user must see when an import changes a product they reuse.

## What Changes

- An imported item with amount and kcal sets its product's energy (per 100 g or per piece, from
  the item) also when the product is known; the item keeps the JSON's name, and its Herkku comes
  from the JSON (absent = not a treat). Within one import a `products` entry wins over items.
- Typing a name that is only a base product's alias (Enter) logs it under the typed name with the
  base energy, not under the base name and category.
- No migration: products and items made by earlier imports stay; the user can remove them under
  Ruokakirjaus.
- A base product is used by the import only to fill kcal for an item that has none, and the item
  then becomes an own product under the JSON's name, not the base product's.
- When an import changes an existing own product's energy, the preview and the clipboard card show
  one line per such product ("Rahka: 65 → 80 kcal / 100 g"). No dialogs.
- Asetukset → Ruokakirjaus gets a switch "Valmiit tuotteet" (on by default). Off: base products
  are not suggested, not used by Enter, and not used by the import.

## Capabilities

### New Capabilities

### Modified Capabilities
- `food-log`: "Saving an AI import" (JSON's energy wins, name and Herkku from JSON), "Preview and
  fine-tuning" and "Clipboard offer" (product change lines), "Base products" and "Ruokakirjaus in
  Asetukset" (the switch).

## Impact

- App: Lisää annos sheet (suggestions, Enter row), the import preview, the clipboard card (sheet,
  Ruoka, front page, Lisää annos dialog), Asetukset → Ruokakirjaus.
- Code: `food/AiPaste.kt` (`AiImport.plan`, plan run also for the preview), `food/FoodRules.kt` (`EnterRow`, typed alias), `food/ClipOffer.kt`,
  `ui/AiImportPreview.kt`, `ui/FoodClipboard.kt`, `ui/FoodViewModel.kt` (`base`), `ui/ProductsPage.kt`,
  a small prefs for the switch.
- Health Connect: unchanged (items are written as before). Scale, widget, CI: untouched.
