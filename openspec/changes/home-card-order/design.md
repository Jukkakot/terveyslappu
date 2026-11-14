# Design: home-card-order

## Context

`ui/HomeScreen.kt` `HomeContent` is a scrolling Column: notices, `LatestCard` (full width), then a `Row` with `FoodCard` and `StepsCard` (equal halves, `IntrinsicSize.Min` height; added by `steps`). Periods live in `PeriodPrefs`. Back rules are `MainUiState.back()`. Specs: home-screen (and the steps delta).

## Goals / Non-Goals

**Goals:** a user-chosen card order that works with the half-width row; finger-friendly; an accessible non-drag path; remembered.
**Non-Goals:** hiding or resizing cards, per-card settings, backup of the order, other screens, a long-press entry.

## Decisions

Decided here; the open taste choices are Q8-Q10.

1. **Draggable unit = one feature card (three units).** The row is not a unit: dragging pairs would force a "row" concept and make a lone card impossible. Layout is derived from the order: weight is always full width; adjacent half-width cards (food, steps) pair into a row; a half card with no partner (weight between them) goes full width. Alternative rejected: a fixed two-slot row plus a movable weight (weight only top or bottom): simpler, but fewer orders and a special case; the derived layout needs one small pure function (`HomeLayout.rows(order)`), unit-tested. The "fits without scrolling" scenario stays for the default order; other orders may scroll (the column already scrolls).
2. **Edit mode = a top-bar button, not long-press (Q8).** Long-press on a card is invisible, collides with the tap targets inside the cards (period menus, buttons) and with later features; a button is discoverable and also the accessible switch. In edit mode the card interiors are inert and show a handle plus move up/down buttons.
3. **Library: `sh.calvin.reorderable` (`ReorderableColumn`).** Maintained, small, Compose-native, works in a plain (non-lazy) Column inside the existing vertical scroll, handles auto-scroll, drag handle (`draggableHandle`), haptics and drop animation. Hand-rolling `detectDragGesturesAfterLongPress` for three items is about 80 lines but means our own drop animation, scroll collision and accessibility; "libraries first". Compose's `Modifier.animateItem` only helps lazy lists and has no drag. Pin the version at apply time to the newest release that resolves in the cloud containers (Maven Central answers 429 there; claude-config's `install.sh` routes through Google's mirror, so check it resolves; if not, ship move buttons only and note it in task 1.1). The up/down buttons are also the accessibility path (also exposed as custom semantics actions "Siirrä ylös" / "Siirrä alas").
4. **Persistence:** one string preference `homeOrder` ("weight,food,steps") next to the period prefs (`PeriodPrefs` style, own small `HomePrefs`), not in the data file or backup (a presentation choice like the card periods). Pure `HomeOrder` rule: parse (drop unknown, de-duplicate, append missing in default order), move up/down, move to index, serialise. Saved on every change; no "cancel".
5. **Edit state is UI-local** (`rememberSaveable` in HomeScreen), not in `MainUiState`; a `BackHandler(enabled = editing)` leaves it. Leaving the front page drops it.
6. **Notices are outside the order** (backup, "?" check, clipboard offer): transient messages, not features.
7. **No hiding (Q10).** A permission-less card already shrinks to its one-line state; hiding adds a way to lose a feature with no visible way back except edit mode.
8. **Reset** as a text button under the cards in edit mode.
9. Log `home.order set=weight,food,steps` (card names only) on every change; no health data. Ohje gets one sentence.
10. **Replaced wording:** home-screen "Simple home screen" says the order is fixed for now (also in the steps delta); the new "Card order" requirement supersedes it, and the sentence is edited at archive (task 3.2) since the steps delta rewrites the same requirement and must land first.

## Risks / Trade-offs

- Dragging in a vertically scrolling Column can fight the scroll: the drag starts only from the handle, so scrolling elsewhere is unaffected.
- A half card moved between others becomes full width, which looks different; acceptable and visible at once in edit mode.
- Card heights differ (weight tall, row cards shorter): the library handles variable heights.
- Moving weight below the row loses "Punnitse first"; the user's choice.
