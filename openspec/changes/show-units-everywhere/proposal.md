# Proposal

## Why

Some numbers in the app are shown bare ("+ Rahka 145", "herkkuja 230", "−0,3"), so the user has
to guess whether they are kcal, grams, pieces or kilos. Rule from the user (2026-11-16): every
number shown has its unit next to it, everywhere in the app.

## What Changes

- New project rule NFR-10 in `openspec/context/nfr.md`: every number shown in the UI (and in
  TalkBack text) carries its unit; counts read as words ("8 painoa", "3 pv"). BMI is the one
  unitless quantity (its label "BMI" is the unit, as is conventional).
- Fix every place found in the audit (2026-11-16) that shows a bare number:
  - Food sheet and Ruoka chips: "+ Rahka 145" → "+ Rahka 145 kcal", chip text one step smaller
    (labelMedium) so about as many chips fit (user 2026-11-16).
  - Treat share everywhere ("herkkuja 230" → "herkkuja 230 kcal"): Ruoka summary line, the sheet,
    the home food card, Energia day list.
  - Steps and Kulutus home cards: the big number gets its unit beside it ("askelta", "kcal"), as the
    weight and food cards already have.
  - Home card averages at small size: "keskim. 1 900/pv" and "2 300/pv" carry the unit with the
    short word "ka" ("ka 1 900 kcal/pv", "ka 7 800 askelta/pv"), wrapping to two lines when needed.
  - Historia group rows: "herkkuja ka 230" → "herkkuja ka 230 kcal/pv", "aktiivinen ka 450" →
    "aktiivinen ka 450 kcal/pv".
  - Weight change labels in Historia's list (day rows and group rows): "−0,3" → "−0,3 kg".
  - Kulutus readouts (Historia marker card, own charts): "aktiivinen 465 · lepo 2 481" →
    "aktiivinen 465 kcal · lepo 2 481 kcal".
- A unit test sweep is not attempted; the rule is checked by review.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `food-log`: chip text and treat-share text carry kcal.
- `home-screen`: steps and burn cards show the unit by the big number; small-size averages carry
  the unit.
- `energy-burn`: Kulutus readout and card texts carry kcal on every part.
- `weight-chart`: Historia list change labels carry kg; group averages carry kcal/pv.

## Impact

UI text only: `res/values/strings.xml`, `ui/FoodSheet.kt` (chip), `ui/HomeScreen.kt`,
`ui/ListGroupRows.kt`, `ui/HistoryScreen.kt`, `chart/TabReadouts.kt`, and tests that assert these
strings. No data, storage or logic changes.
