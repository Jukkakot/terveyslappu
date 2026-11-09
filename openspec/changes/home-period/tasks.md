# Tasks

## 1. Logic

- [ ] 1.1 `HomeSummary.of(weights, now, days)`: window of the chosen days (7, 30, 90); default 7
- [ ] 1.2 Remembered home period (own preference key, choices 7/30/90, anything else gives 7);
      `MainUiState.homeDays`, `MainViewModel.onHomePeriod`, log `home.period days=`
- [ ] 1.3 Unit tests: window per period, preference decode fallback

## 2. Home card

- [ ] 2.1 Period label `1 vk ▾` under the chart with a dropdown (1 vk, 1 kk, 3 kk); its tap does
      not open Historia; the range row text follows the period
- [ ] 2.2 Smoke test: picking 1 kk from the label calls the action and does not open Historia

## 3. Docs and release

- [ ] 3.1 product.md (home period decision), roadmap entry, CLAUDE.md log key
- [ ] 3.2 Bump `appVersion` to 1.10; `./gradlew test lint assembleDebug`
