# Tasks

## 1. Dashed line across gaps

- [ ] 1.1 `ChartData.gapLinks`: index pairs (last of a segment, first of the next) for every gap
      over 30 days; unit tests in `ChartDataTest` (no gap, one gap, lone weight between gaps)
- [ ] 1.2 `WeightChart`: after the solid segment series, one two-point series per gap link drawn
      with a dashed stroke in the primary colour, no dots, sharp (straight) interpolation; tap
      markers still resolve to real weights; `./gradlew test lint assembleDebug` passes

## 2. Docs and version

- [ ] 2.1 Roadmap: add chart-gap-lines as done; bump `appVersion` MINOR (1.17 → 1.18)

## Workflow follow-up

- Archive with `/opsx:archive` once merged and green.
