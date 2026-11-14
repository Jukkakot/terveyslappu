# Proposal: food-ai-import

## Why

The user (2026-11-14) estimates each day's food with an AI chat (Claude on the phone). Typing the
result into Lisää annos item by item is slow, and a single Pikalisäys loses the items. The AI can
answer in a fixed JSON shape, and the app takes it in with one share or paste, shows it for
fine-tuning, and logs every item with its kcal.

## What Changes

- **Share to the app**: in the AI chat, share the answer → Terveyslappu opens a preview. Also
  **Liitä tekoälyltä** in the Lisää annos sheet reads the clipboard. The JSON is found also inside
  a code block or with chat text around it.
- **Kopioi ohje tekoälylle**: copies a short Finnish instruction with the JSON shape and an
  example, to paste into the chat or the user's own calorie skill.
- The JSON shape (the interface; the prompt file `app/src/main/assets/ai-food-prompt.txt` documents each
  field's purpose, is what Kopioi ohje copies, and is kept in git with the reader): `{"items": [{"name", "amount", "unit": "g"|"kpl", "kcal", "herkku"}],
  "products": [{"name", "kcal", "per": "100g"|"kpl"|"annos", "herkku"}]}`; a bare array of items
  is accepted too. `products` is the chat's "Sovellukseen" list: meals and foods to keep.
- The items go to the end of **today** by default; the preview's day opens a calendar to pick
  another day (user 2026-11-14). No portion sizes (pieni/normaali/iso) in this change.
- **Dish alone in `products`** (user 2026-11-14): the same dish is eaten with different sides, so
  grams of the whole plate say little. Items carry the AI's kcal for what was eaten; a product
  holds the dish alone (best per 100 g with a usual amount), and later logs pick grams or kcal in
  the existing item editor.
- **Preview with fine-tuning**: every item and product editable or removable, the sum;
  nothing is saved before Lisää. Kumoa takes the whole import back.
- **Append only** (user 2026-11-14): an import never changes or deletes logged items and never
  touches weights; only the `products` list may change existing products, and only on Lisää.
- Considered and dropped (user 2026-11-14): an MCP service the AI app calls directly. It needs a
  server between the chat and the phone, against the no-backend decision; sharing gives nearly
  the same flow without one. Not in the backlog.

## Capabilities

### New Capabilities

### Modified Capabilities
- `food-log`: new requirements "Food from an AI chat", "AI food JSON shape", "Preview and
  fine-tuning", "Saving an AI import".

## Impact

- Parts: a share target in the manifest, the Lisää annos sheet and a preview screen (food screens),
  the food logic (parsing, plan), Health Connect nutrition writes (existing path), Ohje (one line).
  No scale, widget or CI change.
- No new dependency (the JSON library is already in use).
