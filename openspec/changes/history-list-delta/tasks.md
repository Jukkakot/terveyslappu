## 1. Change per row

- [ ] 1.1 Pure function: for a newest-first list, the change of each weight from the one before it in time (oldest none); unit tests with made-up weights, incl. rounding to ±0,0
- [ ] 1.2 Historia row shows the change (existing `formatChange`, small text left of the weight); gain and loss colours from the theme's palette (e.g. tertiary vs primary), zero in onSurfaceVariant; the row's spoken label includes the change
- [ ] 1.3 Smoke test: a list of three weights shows two changes

## 2. Docs and release

- [ ] 2.1 product.md decision (neutral colours, no red/green), roadmap entry; bump MINOR in `gradle.properties`
- [ ] 2.2 `./gradlew test lint assembleDebug`
