# Proposal

## Why

On Ruoka the user could not tell that a row can be edited or deleted (tap opens the editor, delete
is inside it). The user wants a general rule (2026-11-15): whatever can be edited shows an edit
button, whatever can be deleted shows a delete button, always.

## What Changes

- Every list row that can be edited shows a pencil button; every row that can be deleted shows a
  red trash button (pencil first, trash at the end), as the Lisää annos sheet's own-product rows do.
- Ruoka's items: pencil (opens the item editor) and trash (deletes at once with Kumoa).
- Ruokakirjaus's products: pencil (product editor) and trash (Poista tuote with Kumoa).
- AI preview rows: pencil added next to the existing trash.
- Historia's weights keep the trash only (a weight cannot be edited).
- Tapping the row itself keeps doing what it does today (opens the editor), so nothing is lost.

## Decisions taken without asking

- In Ruoka's Järjestä mode the row shows the move controls instead of pencil and trash.
- Icon buttons are 48 dp touch targets; the kcal value stays before the buttons.
- Base products in the sheet keep no trash (they cannot be deleted), as today.

## Capabilities

### Modified Capabilities
- `app-appearance`: List rows (visible edit and delete buttons).

## Impact

`ui/FoodScreen.kt`, `ui/ProductsPage.kt`, `ui/AiImportPreview.kt`, strings for content
descriptions, their UI smoke tests. No data or Health Connect change.
