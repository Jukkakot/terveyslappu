# Proposal: home-card-order

## Why

The front page now has three feature cards (weight, food, steps; food-widget adds a widget, not a card). The user wants the order to be theirs: roadmap "Reorder the front page cards" (user 2026-11-14): an edit mode where the feature cards are put into the user's order. The fixed order was always meant as "for now" (home-screen, feature card parity).

## What Changes

- **Edit mode** on the front page, entered with a sort button (ic_sort, "Järjestä kortit") in the top bar next to help and settings, left with a visible "Valmis" text button (like Ruoka's order list).
- **In edit mode the three cards are a full-width list of compact rows** (card title, move up, move down, drag handle) in pure order, no pairing (Q14, default). Rows are reordered by the handle or the buttons. The real cards are not shown while editing, so their taps and quick actions are unreachable.
- **Layout follows the order outside edit mode:** each card has a width (weight full, food and steps half); half cards pair greedily left to right, a leftover half card is full width and then uses its long forms.
- **Remembered** in app preferences, not in the data file or backup. Default order weight, food, steps. An empty or corrupt stored order means the default; unknown ids are ignored; cards the stored order lacks (added later) are appended in default order.
- A "Palauta oletusjärjestys" action in edit mode, disabled when the order already is the default.
- Notices (backup, "?" check, clipboard offer) stay above the cards, visible and tappable in edit mode, and are not part of the order.
- Not in this change: hiding cards (Q10), reordering inside a card, any other screen.
- Parts touched: front page (HomeScreen), app preferences, Ohje (one sentence), Ruoka's order list (extracted for reuse); no new dependency.

## Impact

home-screen spec (Card order, Card edit mode; the archive edit of "Simple home screen" also allows the edit button in the top bar and the reset action). Depends on `steps` (the half-width row) being merged first; no conflict with `food-widget`.
Questions for the user: Q8-Q10 and Q14 in context/questions.md.
