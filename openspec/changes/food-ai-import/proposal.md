# Proposal: food-ai-import

## Why

The user (2026-11-14) estimates each day's food with an AI chat. Typing the result into Lisää
annos item by item is slow, and a single Pikalisäys loses the items. The AI can answer in a fixed
JSON shape; the user copies that text and pastes it into the app, and every item becomes a logged
item with its kcal.

## What Changes

- **Liitä tekoälyltä** in the Lisää annos sheet: reads the copied text from the clipboard, finds
  the JSON in it (also inside a code block or with chat text around it) and shows a preview of the
  items with their amounts, kcal and the sum. **Lisää** adds them in that order at the end of the
  day shown; a "N annosta lisätty · Kumoa" line takes the whole paste back.
- **Kopioi ohje tekoälylle**: copies a short Finnish instruction with the JSON shape and an
  example, to paste at the start of the AI chat (or into the user's own calorie skill).
- The JSON shape: `{"items": [{"name", "amount", "unit": "g"|"kpl", "kcal", "herkku"}],
  "products": [{"name", "kcal", "per": "100g"|"kpl"|"annos", "herkku"}]}`; a bare array of items
  is accepted too. An item's `kcal` is its total; only `name` is required. `products` are the
  chat's "Sovellukseen" list (user 2026-11-14): meals and foods to keep as own products.
- Each item becomes a product like a typed name does; a product without energy learns it from the
  item (kcal per 100 g or per piece). Items are written to Health Connect like other items.
- Text with no usable JSON shows a short reason and adds nothing.

## Capabilities

### New Capabilities

### Modified Capabilities
- `food-log`: a new requirement "Paste from an AI chat".

## Impact

- Parts: the Lisää annos sheet (food screens), the food logic (parsing, plan), Health Connect
  nutrition writes (existing path), Ohje (one line on the feature). No scale, widget or CI change.
- No new dependency (the JSON library is already in use).
