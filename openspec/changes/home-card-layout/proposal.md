# Proposal: home-card-layout

## Why

The front page has three feature cards (weight, food, steps) in a fixed layout. The user wants the
front page to be theirs (roadmap "Reorder the front page cards", user 2026-11-14; answers to Q8-Q10
and Q14 on 2026-11-15): their own order, their own size per card, and cards they do not use hidden.
The fixed layout was always meant as "for now" (home-screen, feature card parity). The next change,
`custom-charts`, adds user-made chart cards, so the layout must take more card types than three.

## What Changes

- **Card grid:** the front page cards sit in a two-column grid filled in the user's order. Each card
  has a user-chosen size: **small** (1x1: half width, one row), **wide** (1x2: full width, one row,
  like today's weight card) or **big** (2x2: full width, two rows high). All cards of one row have
  the same height. Default: weight wide, food small, steps small (today's layout).
- **Every card has a defined look at each size** (weight, food, steps); the chart grows with the
  card, the big size adds the scale labels.
- **Edit mode by long-press on a card** (Q8). It shows the real cards (Q14), unmistakably: an edit
  top bar ("Muokkaa etusivua", Peruuta, Valmis), outlined and dimmed cards with a drag handle and a
  menu (size, move earlier or later, hide). While editing, the cards' own taps, buttons and menus do
  nothing. Cards are reordered by dragging the real cards, or with the menu's move items and
  TalkBack actions.
- **Hide and bring back** (Q10): a hidden card leaves the grid; "Lisää kortti" in edit mode lists
  the hidden cards and puts the chosen one back last. The last shown card cannot be hidden.
- **Changes count only on "Valmis"**; Peruuta or back discards them without asking. A "Palauta
  oletus" action in edit mode restores the default layout (also only on Valmis).
- **Remembered** in app preferences (order, size per card, hidden cards), next to the periods, so
  Android's phone transfer carries it; not in the data file or the backup file. A stored layout that cannot be read means the default; unknown cards are dropped,
  cards added in a later version are appended.
- Notices (backup, "?" check, clipboard offer) stay above the grid outside edit mode and are hidden
  while editing.
- Not in this change: chart cards (`custom-charts`), per-card settings other than size, other
  screens.
- Parts touched: front page (HomeScreen and its cards), a new pure layout rule and its preference,
  Ohje (one sentence), the event list; a new dependency (`sh.calvin.reorderable`, drag in a lazy
  grid).

## Capabilities

- home-screen (modified: Simple home screen, Food row on the home screen, Steps card on the home
  screen; removed: Steps card height; added: Card grid, Card sizes, Card edit mode, Hidden cards)
- help-page (modified: Ohje screen names the front page editing)
- app-data-transfer (modified: the layout moves with Android's transfer and cloud backup, as the
  periods do)

## Impact

`custom-charts` depends on this change (a chart is a card type in this grid): implement this one
first. No conflict with other active changes.
