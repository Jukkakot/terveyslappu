# Tasks

## 1. Scale rule (pure)

- [ ] 1.1 `chart/WeightScale`: y range from the weights (0.5 kg room, at least 4 kg, centred,
      whole kilos); used by `ChartData`
- [ ] 1.2 `HomeSummary`: the 30-day window's start, first weight in it and the y range
- [ ] 1.3 Unit tests: steady weight, real change, single weight; home summary window

## 2. Screens

- [ ] 2.1 Home sparkline: x over the whole 30 days, shared y range; under it the window's dates,
      `first → latest kg` and the change
- [ ] 2.2 Historia: y-axis labels with `kg`; stats tiles show `kg`
- [ ] 2.3 Smoke tests still pass (home card, Historia)

## 3. Docs and release

- [ ] 3.1 product.md decision (scale rule and why), roadmap backlog: trend line idea
- [ ] 3.2 Bump `appVersion` to 1.9; `./gradlew test lint assembleDebug`
