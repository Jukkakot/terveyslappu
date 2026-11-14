# Proposal: home-card-order

## Why

The front page now has three feature cards (weight, food, steps; food-widget adds a widget, not a card). The user wants the order to be theirs: roadmap "Reorder the front page cards" (user 2026-11-14): an edit mode where the feature cards are dragged into the user's order. The fixed order was always meant as "for now" (home-screen, feature card parity).

## What Changes

- **Edit mode** on the front page, entered and left with a button in the top bar (next to help and settings). In it the three feature cards (Paino, Ruoka, Askeleet) are dragged by a handle into any order; each card also has move up / move down buttons (the non-drag way, for accessibility and precision). Taps on cards and their quick actions are off while editing.
- **Layout follows the order:** the weight card is always full width; food and steps are half-width cards that sit side by side when they are next to each other in the order, and a half-width card that has no partner (weight between them) spans the full width.
- **Remembered** in app preferences, not in the data file or backup (like the period choices). Default order stays weight, food, steps. A card the stored order does not know (a future card) is added at the end; an unknown stored id is ignored.
- A "Palauta oletusjärjestys" action in edit mode.
- Notices (backup, "?" check, clipboard offer) stay above the cards and are not part of the order.
- Not in this change: hiding cards (Q10), reordering inside a card, any other screen.
- Parts touched: front page (HomeScreen), app preferences, Ohje (one sentence), build (one library).

## Impact

home-screen spec (Card order, Card edit mode). Depends on `steps` (the half-width row) being merged first; no conflict with `food-widget`.
Questions for the user: Q8-Q10 in context/questions.md.
