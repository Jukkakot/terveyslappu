## 1. Fixed tabs show one data kind

- [ ] 1.1 `ui/EnergyPage.kt`: remove the weight strip, its label width share and the "paino" legend entry; drop the `weights`/`zone` parameters if unused; remove `PeriodWeights`, `PeriodWeightData`, `periodWeights` (and `WEIGHT_STRIP` in `ChartAxes.kt`) if nothing else uses them
- [ ] 1.2 `ui/StepsPage.kt`: same for Askeleet; update callers (`StepsTab`, Historia)
- [ ] 1.3 Strings: `energy_chart_description` and `steps_chart_description` without "paino samalla aika-akselilla"; `help_food_body` without "painon rinnalla"
- [ ] 1.4 Tests: drop the `stepsWeights*` checks in `StepsScreenTest` and `LargeTextTest`; add one assertion that Energia and Askeleet have no weight strip (tags `energyWeights`, `stepsWeights` absent) even with weights in the period
- [ ] 1.5 Bump `appVersion` to 1.35; `./gradlew test lint assembleDebug`
