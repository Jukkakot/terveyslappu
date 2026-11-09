# Proposal

## Why

The user has no use for the energy figures (basal metabolic rate, total expenditure) and wants the
app simpler (user 2026-11-09). On the home screen the BMI class word ("ylipaino") reads as a
judgement at first glance; the tone colour already tells enough.

## What Changes

- **BREAKING (feature removed):** the basal metabolic rate goes away everywhere: from the home row
  and Historia's third tab (Perusaineenvaihdunta). Historia keeps two tabs, Paino and BMI.
- Home row: the BMI number alone in its tone colour ("BMI 25,3" in yellow), no class word; the
  normal-weight range stays. The class word stays in the screen reader text, so the meaning is not
  in colour alone for those who cannot see it.
- Historia's BMI tab keeps the class word on the latest BMI and the coloured bands (the place to look
  closer).
- Reasons now only concern the BMI and range (height missing, under 18).
- Birth year and gender stay in Omat tiedot (birth year keeps the under-18 rule; nothing else uses
  gender now, but removing a field the user filled is not asked for).

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- `derived-values`: basal metabolic rate removed; reasons only for BMI and range.
- `home-screen`: the derived row shows the BMI number in its tone without the class word, and the
  normal-weight range.
- `weight-chart`: tabs Paino and BMI only; kcal units, statistics and scale rule removed.

## Impact

- Parts touched: home row, Historia tabs. Not touched: Health Connect, scale, widget, export, Omat
  tiedot.
- Code: `derived/Derived` (drop `Bmr`, `Quantity.BMR`, BMR in `LatestDerived`), `ui/DerivedUi`
  (row, kcal formats), `ui/HistoryScreen` (tabs, chart labels), `ui/Format` (kcal helpers),
  strings, tests.
