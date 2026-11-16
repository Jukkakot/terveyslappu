# Proposal

## Why

The front page's weight line looks flat: on a wide weight card the plot is about 11 dp high, so a
0,7 kg spike that fills a third of Historia's chart is a few pixels (user 2026-11-16, "Data reads
one way only"). The Punnitse button and "+ Annos" take the room, yet the user rarely uses them:
weighing is automatic and food is added from the widget. The front page should be a dashboard that
shows data; the rare actions move to a ⋮ menu.

## What Changes

- **BREAKING (look):** the front page and Historia top bars hold the help button and a ⋮ menu:
  Punnitse, Lisää paino käsin, Lisää annos, a divider, Asetukset. The gear and Historia's "+" go.
- Punnitse leaves the weight card and "+ Annos" the food card; cards show data only.
- Every card chart's plot is at least 56 dp high at every size and text size; the freed room goes to the
  charts.
- The empty weight card points to the menu and the widget instead of its button.
- Ohje's texts on weighing, manual entry and adding food name the menu.

## Capabilities

### New Capabilities

### Modified Capabilities
- `home-screen`: new "App menu"; "Simple home screen", "Historia screen", "Food row on the home
  screen", "Card sizes" (minimum plot height), "Card edit mode" lose the card buttons, gear and "+".
- `weight-entry`: the manual entry opens from the menu.
- `scale-weigh-in`: the in-app start is the menu's Punnitse.
- `food-log`: "Lisää annos" in the app is the menu item (and the Ruoka screen).
- `help-page`: the help button on both screens; Ohje names the menu; back to the opener.

## Impact

- App screens: front page top bar and cards (weight, food at every size), Historia top bar, Ohje
  texts. Not Health Connect, scale, widgets, tile, shortcuts or build/CI.
- Code: `ui/HomeEdit` / `ui/HomeScreen` (top bar, cards), `ui/HistoryScreen` (top bar),
  `ui/MainScreen` (shared top bar menu), `strings.xml`; tests in `HomeCardsTest`, `HomeEditTest`,
  history screen tests.
