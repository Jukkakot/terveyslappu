# Tasks

## 1. Remove the basal metabolic rate

- [ ] 1.1 `derived/`: drop `Derived.bmr`/`shownBmr`, `Quantity.BMR` (and its scale/step), BMR from
  `LatestDerived`; `Field.GENDER` goes if nothing uses it. UI: drop the BMR row part, the
  Perusaineenvaihdunta tab, kcal formats (`formatKcal`, `formatKcalChange`, `Quantity` kcal
  branches), BMR strings. Verify: `DerivedTest` without BMR cases still green; no `kcal` left in
  `app/src/main`.

## 2. BMI on the home row without the word

- [ ] 2.1 Home row: "BMI 25,3" in the tone colour, no class word; the word stays in the row's
  content description; range unchanged; reasons only for BMI/range. Historia's BMI stats keep the
  word. Verify: MainScreen smoke test that the home row has no "ylipaino" text but its description
  names the class; the BMI tab test still passes; LargeTextTest green.

## 3. Groups closed by default

- [ ] 3.1 Historia's list starts with every group closed (no default open path; `ListGroups.defaultOpen`
  goes); period change resets to closed. Verify: `ListGroupsTest` default-open test replaced; the
  MainScreen grouped-list smoke test still opens a year on tap.

## 4. Context

- [ ] 4.1 product.md: derived values decision updated (no energy figures, colour only on home, why) and the grouped list now starts closed;
  bump `appVersion` minor. Verify: `./gradlew test lint assembleDebug` passes.
