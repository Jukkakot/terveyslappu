## 1. Scale rule

- [ ] 1.1 `chart/WeightScale.kt`: ROOM 0.2, MIN_SPAN 1.0, round outwards to half units (`floor(x*2)/2`, `ceil(x*2)/2`), then if `(max - min)` is not a whole multiple of 1.0 (so the middle is off the half grid) add 0.5 to max; BMI through `perKg` as now, rounding in half BMI units. Rewrite the doc comment (fit to data, user 2026-11-16).
- [ ] 1.2 Check the three axis values (`AxisTicks.line`) and the home card's top/bottom labels read "96,5 kg" style, never "97,75"; the BMI band chart still draws.
- [ ] 1.3 Update `WeightScaleTest` with the spec scenarios (steady weight, a week of small moves, real change, steady BMI) and fix other tests whose expected scales change.

## 2. Finish

- [ ] 2.1 `./gradlew test lint assembleDebug`
- [ ] 2.2 Bump `appVersion` to the next MINOR
