# Proposal: home-overview

## Why

The front page grew a feature at a time: the weight card, the derived-values row, the three latest
weights, and now the food row and Lisää annos. The user (2026-11-14) wants the front page to show
each feature clearly at a glance. The weight rows repeat what the card's line already shows and
what Historia lists per day, so they can go.

## What Changes

- The three latest weights are removed from the front page. The weight card keeps its value,
  change, line and derived values, and a tap still opens Historia, where the weights are listed
  per day.
- The front page is one card per feature, in a fixed order:
  1. **Paino**: the current card, with the derived-values row inside it.
  2. **Ruoka**: today's kcal with the treats' share, and the outlined Lisää annos inside the card.
     A tap on the card opens Ruoka.
  3. **Automaattinen punnitus** (only when it is switched on): its state line, as on the
     Asetukset row. A tap opens its Asetukset page.
- The "?" notice and the backup notice stay above the cards. Punnitse vaa'alla stays the only
  filled action, at the bottom.
- Not here: steps (own change next), which will become one more card.

## Capabilities

### Modified Capabilities
- `home-screen`: the front page is feature cards; the latest-weights list is removed.

## Impact

`ui/HomeScreen.kt` (and its smoke tests); no data, Health Connect or storage changes.
