# Design: food-ai-import

## Context

The user estimates a day's food with an AI chat (Claude on the phone, also their own calorie
skill) and wants the result in the food log without retyping. The interface between the chat and
the app is a JSON block in the chat's answer; the user shares or copies the answer to the app.

## Goals / Non-Goals

**Goals:** one share (or paste) logs the day's items; a strict, documented shape the AI can follow;
a lenient reader in the app; the user fine-tunes before saving; nothing existing is changed.

**Non-Goals:** portion sizes pieni/normaali/iso; an MCP service or
any server (dropped by the user 2026-11-14); meal names or times.

## Decisions

- **Share target, not a server.** `ACTION_SEND` with `text/plain` reaches the app from the chat's
  share button; the data never leaves the phone (no-backend decision). Paste stays as the fallback
  for apps whose share sends something else.
- **Strict prompt, lenient reader.** The prompt asks for one fenced JSON block with plain numbers;
  the reader still accepts surrounding text, a bare array, numbers as strings, a decimal comma and
  unknown keys, because chats drift. Only `name` is required, so a partial answer still helps.
- **Field names**: English keys like the data file, except `herkku`, which is the app's own word
  for the category (no English term would mean the same to the AI or the user).
- **`kcal` is the item's total**, not per 100 g: that is what a chat estimates and what the day
  sum needs; the app derives kcal per 100 g or per piece from `amount` for the product.
- **`products` replaces, items only fill.** `products` is the chat's explicit "keep this" list, so
  it may overwrite a product; an item only teaches a product that has no energy yet, so a one-off
  estimate never silently changes a product the user tuned.
- **Today by default, calendar to change.** The user mostly shares in the evening for the same
  day; no date in the shape, so the AI cannot put items on a wrong day (a date key is ignored);
  the preview's day opens a single-day calendar for a catch-up.
- **Dish alone in products.** A dish is eaten with different sides, so a product holds the dish
  without sides (best per 100 g with a usual amount); an item carries the AI's kcal for what was
  eaten. The existing item editor already logs a product by grams, pieces or a total kcal.
- **The prompt file is the contract** (see below).

## The prompt

The prompt lives in `app/src/main/assets/ai-food-prompt.txt` (user 2026-11-14: kept in git and
maintained with the reader). Kopioi ohje tekoälylle copies the file as is; a change to the shape
changes the file, the reader and its tests in the same commit.

## Risks / Trade-offs

- The chat's share may send only part of the answer or formatted text → the reader searches the
  whole text for the block; paste is the fallback.
- A name drift ("Kaurapuuro" vs "kaurapuuro kaurahiutaleista") makes duplicate products → the
  prompt asks for stable names; the products list in Asetukset can rename or remove.
