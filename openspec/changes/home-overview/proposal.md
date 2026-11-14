# Proposal: home-overview

## Why

The front page grew a feature at a time: the weight card, the derived-values row, the three latest
weights, and now the food row and Lisää annos. The user (2026-11-14) wants the front page to show
each feature clearly at a glance, every feature in the same way: a card with its latest value and
a small chart of a chosen period, opening its details. The weight rows repeat what the card's line
already shows and what Historia lists per day, so they go.

## What Changes

- **Parity rule** (user 2026-11-14): what one feature has on the front page and in its details,
  the others get too, unless it means nothing for that quantity. Weight and food now follow it;
  steps will.
- The three latest weights are removed. The weight card keeps value, change, line, derived values
  and its period menu, and opens Historia on Paino.
- A **food card** built like the weight card: today's kcal large with the treats' share, small
  daily kcal bars of its own chosen period (7 / 30 / 90 pv, own menu, remembered), under them the
  first and last day and the period's average kcal per logged day, then the outlined Lisää annos
  and "Tänään" (today's items on Ruoka) inside the card. A tap elsewhere opens Historia on Energia.
  What weight has and food does not: no change arrow from the previous day (a day's kcal is not a
  trend), bars instead of a line (daily amounts, as on Energia).
- An **automatic weigh-in card** only while it is switched on: its state line as on the Asetukset
  row; a tap opens its Asetukset page.
- Order fixed for now: Paino, Ruoka, Automaattinen punnitus. Reordering the cards by dragging in an
  edit mode goes to the backlog.
- The "?" and backup notices stay above the cards; Punnitse vaa'alla stays the only filled action.

## Capabilities

### Modified Capabilities
- `home-screen`: the front page is feature cards with the parity rule; the latest-weights list is
  removed; the food card gets bars and its own period.

## Impact

`ui/HomeScreen.kt`, `chart/PeriodPrefs` (the food card's period), a pure home food summary next to
`chart/HomeSummary`, smoke tests. No data, Health Connect or storage format changes.
