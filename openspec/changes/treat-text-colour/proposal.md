# Proposal

## Why

The treat share is shown apart in the bars (treat tone) but in text it reads like any muted line, so
"herkkuja 230 kcal" does not link to the bars' treat part at a glance (backlog, user 2026-11-16).

## What Changes

- The treat share text takes the treat tone of the Energia bars (`colorScheme.tertiary`) wherever
  it is shown: the Ruoka day summary line, the add sheet's line under its header, the home food
  card, Energia day list rows, Energia group rows ("herkkuja ka N kcal/pv") and the Energia
  statistics' Herkut value.
- Only the treat part is coloured; the other parts of a joined line ("1 ilman kcal", the total)
  keep their present colour. Size and weight stay as they are.
- The herkku sign on items stays as is (tertiaryContainer chip). Own charts are untouched (their
  treat tone follows the chart's colour and has no treat text).

- Weight gain text (`changeColor`) also uses tertiary, so on the front page a gain and the treat
  share share a colour; the user accepted this (2026-11-16) to keep the link to the bars.

## Capabilities

### New Capabilities

### Modified Capabilities
- `food-log`: "Treats apart" gains the rule that the treat share text uses the bars' treat tone.

## Impact

App screens only: `ui/FoodScreen.kt` and `ui/FoodSheet.kt` (summary line), `ui/ListGroupRows.kt`, `ui/HomeScreen.kt`
(food card), `ui/EnergyPage.kt` (list rows, statistics), `ui/HistoryScreen.kt` (Energia group
rows). No Health Connect, scale, widget or build change. Dynamic colour's tertiary is designed for
text contrast on surfaces in both themes.
