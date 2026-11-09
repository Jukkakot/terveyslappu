# Tasks

## 1. Formulas and classes (pure)

- [x] 1.1 `derived/`: BMI from kg and height cm; WHO class with word and tone (yellow 17–18.5 and
  25–30, red under 17 and from 30, green between); normal-weight range for a height rounded to
  whole kg; Mifflin-St Jeor rounded to 10 kcal with age = weight's year − birth year; adult check
  (≥ 18 in the weight's year) hides class, range and BMR. `Quantity` (WEIGHT, BMI, BMR) with the
  value of an entry and whether the profile allows it. Verify: unit tests for every spec scenario
  of derived-values (25,3; 60–81 kg; 1 770 and 1 290 kcal; class borders 16.9/17/18.5/25/30/35/40;
  under 18; Ei valittu), made-up values.

## 2. Chart, stats and scale for any quantity (pure)

- [x] 2.1 `ChartData` and `periodStats` take the quantity's values instead of `kg`; derived
  quantities have no per-week rate. Verify: existing chart and stats tests unchanged and green;
  new tests for BMI stats (latest, change −0,3) and a BMR series.
- [x] 2.2 The scale rule with "kg worth" (`perKg`, rounding step): weight unchanged, BMI by 1/h²
  rounded to whole units, BMR by 10 kcal/kg rounded to tens. Verify: existing `WeightScaleTest`
  green; tests for the steady BMI (span ≥ 1,2) and steady BMR (span ≥ 40 kcal) scenarios.

- [x] 2.3 Plot flush with the y-axis on every chart (full-width layout, no extreme-label padding).
  Verify: a Robolectric screenshot of a made-up multi-year chart shows the first point on the axis;
  the screenshot stays out of the repo.

## 3. Home row

- [x] 3.1 Derived values of the latest weight in the home summary, recomputed when the weights or
  the owner details change; row under the card with BMI + class word in its tone, normal range,
  kcal/vrk; in place of a value that cannot be computed, its reason naming the missing details,
  opening Asetukset scrolled to Omat tiedot; nothing without weights. Tone colours in the theme for
  light and dark. Verify: reason tests in 1.1's style (gender missing, several missing, under 18);
  home summary unit tests (all set, gender missing, no details); one MainScreen smoke test that
  the row shows with a made-up profile and a reason opens Asetukset.

## 4. Historia tabs and swipe

- [x] 4.1 Tabs Paino / BMI / Perusaineenvaihdunta bound to a pager around chart + stats (list
  outside), always all three, an uncomputable quantity shows its reason and a button to Omat
  tiedot, opens on Paino; axis labels and
  point details per quantity (`kg`, bare BMI under a "BMI" title, `kcal`); BMI class bands behind
  the line. Verify: smoke test that tapping the BMI tab switches the chart title/stats with a
  made-up height, and that without a height the BMI tab shows the reason; LargeTextTest still passes at 200 %.

## 5. Context

- [x] 5.1 product.md: derived values decision (what is shown, where, traffic-light classes, current
  height for old weights, not in Health Connect or export, why); roadmap: "derived values" done;
  CLAUDE.md map gains `derived/`; bump `appVersion` to 1.12. Verify: `./gradlew test lint
  assembleDebug` passes.
