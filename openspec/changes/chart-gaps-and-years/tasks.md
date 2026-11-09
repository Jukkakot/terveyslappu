# Tasks

## 1. Chart

- [ ] 1.1 `ChartData` splits the weights into segments at gaps over 30 days and says whether the
  span is over a year; `WeightChart` draws one line series per segment (same colour), keeps a dot
  for one-point segments even above the dense-chart dot limit, and formats x labels as "M/yyyy"
  over a year. Verify: unit tests for the split (no gap, one gap, lone weight) and the label
  choice; existing chart smoke test still passes.

## 2. Year in single-weight times

- [ ] 2.1 Point detail and Historia's list rows add the year when the weight is not from the current
  year ("15.9.2024 8.33 97,6 kg", "Su 15.9.2024 8.33"). Verify: FormatTest cases for this and
  last year.

## 3. Context

- [ ] 3.1 product.md: chart decision (time axis kept, break at gaps over 30 days, year when needed;
  OKOK's per-measurement axis rejected as misleading); roadmap item; appVersion MINOR bump.
  Verify: `./gradlew test lint assembleDebug` passes.
