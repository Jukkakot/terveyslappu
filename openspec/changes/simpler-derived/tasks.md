# Tasks

## 1. Remove the basal metabolic rate

- [x] 1.1 `derived/`: drop `Derived.bmr`/`shownBmr`, `Quantity.BMR` (and its scale/step), BMR from
  `LatestDerived`; `Field.GENDER` goes if nothing uses it. UI: drop the BMR row part, the
  Perusaineenvaihdunta tab, kcal formats (`formatKcal`, `formatKcalChange`, `Quantity` kcal
  branches), BMR strings. Verify: `DerivedTest` without BMR cases still green; no `kcal` left in
  `app/src/main`.

## 2. BMI on the home row without the word

- [x] 2.1 Home row: "BMI 25,3" in the tone colour, no class word; the word stays in the row's
  content description; range unchanged; reasons only for BMI/range. Historia's BMI stats keep the
  word. Verify: MainScreen smoke test that the home row has no "ylipaino" text but its description
  names the class; the BMI tab test still passes; LargeTextTest green.

## 3. Groups closed by default

- [x] 3.1 Historia's list starts with every group closed (no default open path; `ListGroups.defaultOpen`
  goes); period change resets to closed. Verify: `ListGroupsTest` default-open test replaced; the
  MainScreen grouped-list smoke test still opens a year on tap.

- [x] 3.2 Systematic levels: year → month → week → weight rows, no day level, no single-child
  collapse; the year level left out when all weights are in one year; at most 14 weights flat; a
  one-weight group shows its weight and no change. Verify: `ListGroupsTest` rewritten for every
  Grouped list scenario (month with one weight, one year, two in a day, border, gaps).

## 4. Context

- [x] 4.1 product.md: derived values decision updated (no energy figures, colour only on home, why) and the grouped list now starts closed;
  bump `appVersion` minor. Verify: `./gradlew test lint assembleDebug` passes.
