# Design: home-card-order

## Context

`ui/HomeScreen.kt` `HomeContent` is a scrolling Column: notices, `LatestCard` (full width), then a `Row` with `FoodCard` and `StepsCard` (equal halves, `IntrinsicSize.Min` height; added by `steps`). Periods live in `PeriodPrefs`. Back rules are `MainUiState.back()`. Ruoka already has a reorder list: `OrderList` in `ui/FoodScreen.kt` (hand-rolled handle drag, up/down buttons, icons `ic_drag_handle`, `ic_arrow_up`, `ic_arrow_down`, a "Valmis" button). Specs: home-screen (and the steps delta).

## Goals / Non-Goals

**Goals:** a user-chosen card order that works with the half-width row; finger-friendly; an accessible non-drag path; remembered; one reorder UI shared with Ruoka.
**Non-Goals:** hiding or resizing cards, per-card settings, backup of the order, other screens, a long-press entry.

## Decisions

Decided here; the open taste choices are Q8-Q10 and Q14.

1. **Draggable unit = one feature card (three units), edited as a compact list (Q14, default).** In edit mode the cards are not the real cards but full-width rows (title, up, down, handle) in pure order with no pairing. Dragging the real cards cannot work cleanly because food and steps share a row outside edit mode. Pairing applies only outside edit mode. Alternative rejected: weight only above or below the row (fewer orders, a special case).
2. **Width attribute and layout.** Each card has a width: weight full, food and steps half. A pure `HomeLayout.rows(order)` pairs half cards greedily left to right; a half card left without a partner is full width. The "fits without scrolling" scenario stays for the default order; other orders may scroll.
3. **A card picks its variant from its actual width.** A half card at full width (lone) uses the long forms; the choice is made from the measured width, not from a flag, so any future layout works. A layout test covers a lone half card at full width.
4. **Edit mode = a top-bar button, not long-press (Q8).** Long-press is invisible and collides with the taps inside the cards. Enter with `ic_sort`, contentDescription "Järjestä kortit"; leave with a visible "Valmis" text button (as in Ruoka), not an icon swap.
5. **Reuse, no library.** Extract Ruoka's `OrderList` (handle drag, up/down, icons, "Valmis") into a shared composable taking rows (title, id) and `onMove(from, to)`, used by Ruoka and the home edit mode. The `sh.calvin.reorderable` dependency and its fallback are dropped: the hand-rolled drag already exists and is enough for a short list.
6. **Persistence:** one string preference `homeOrder` ("weight,food,steps") in a small `HomePrefs` next to the period prefs, not in the data file or backup. Pure `HomeOrder` rule: parse (empty or corrupt = default; drop unknown, de-duplicate, append missing in default order), move up/down, move to index, serialise.
7. **Saving and logging:** the arrows save and log at once; a drag saves and logs once, on drop (not per step during the drag). Log `home.order set=weight,food,steps` (card names only). No "cancel".
8. **Edit state is UI-local** (`rememberSaveable` in HomeScreen), not in `MainUiState`. Rotation keeps edit mode; any screen change or the app going to background ends it. A `BackHandler(enabled = editing)` leaves edit mode. This HOME-only back handler is a deliberate exception to the one back rule (`MainUiState.back()`): the state is local to the page and ends with it, so adding a flag to the global state would buy nothing.
9. **Notices are outside the order and stay visible and tappable in edit mode** (backup, "?" check, clipboard offer): transient messages, not features.
10. **Reset** is a "Palauta oletusjärjestys" text action in edit mode, disabled when the order is already the default.
11. **No hiding (Q10).** Hiding adds a way to lose a feature with no visible way back except edit mode.
12. **Accessibility:** each edit row reads "<card>, <pos>/<count>"; after a move the new position is announced; arrow descriptions name the card ("Siirrä Ruoka ylös"). The arrows are the non-drag path.
13. **Replaced wording:** "Simple home screen" says the order is fixed for now and that there is "no action outside the cards"; the archive edit (task 3.2) rewrites those: the order is user-chosen (Card order), the top bar also has the edit button, and edit mode has the "Palauta oletusjärjestys" action. It happens at archive since the steps delta rewrites the same requirement and must land first.

## Risks / Trade-offs

- A hand-rolled drag in a scrolling page can fight the scroll: it starts only from the handle, and the list is three short rows.
- A half card moved between others becomes full width, which looks different from the edit list's rows; the edit list is plain on purpose, the result is visible at once on leaving edit mode.
- Moving weight below the row loses "Punnitse first"; the user's choice.
- Extracting `OrderList` touches working Ruoka code: its existing tests must stay green.
