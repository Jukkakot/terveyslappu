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
- The JSON shape: `{"date", "items": [{"name", "amount", "unit": "g"|"kpl", "kcal", "herkku"}],
  "products": [{"name", "kcal", "per": "100g"|"kpl"|"annos", "herkku"}]}`; a bare array of items
  is accepted too. `products` is the chat's "Sovellukseen" list: meals and foods to keep.
- **Preview with fine-tuning**: the day, every item and product editable or removable, the sum;
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
