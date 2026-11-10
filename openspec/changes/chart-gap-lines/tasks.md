# Tasks

## 1. One unbroken line

- [x] 1.1 `ChartData`: replace `segments` with `lone` (indices of weights over 30 days from both
      neighbours, or from the one neighbour at an end); unit tests in `ChartDataTest`
- [x] 1.2 `WeightChart`: one series through all weights, straight solid line; dots on every weight
      when they fit, else only on `lone` weights; `./gradlew test lint assembleDebug` passes

## 2. Docs and version

- [x] 2.1 Roadmap: add chart-gap-lines as done; bump `appVersion` MINOR (1.17 → 1.18)

## Workflow follow-up

- Archive with `/opsx:archive` once merged and green.
