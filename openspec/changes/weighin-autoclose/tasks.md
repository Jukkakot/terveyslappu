# Tasks

## 1. Give up after 30 seconds

- [ ] 1.1 `WeighIn.TIMEOUT_MS` = 30 s; `WeighInActivity` finishes on `NotFound` and shows the toast;
  the NotFound view and its strings go from `WeighInDialog`. Verify: `WeighInTest` timeout case
  green with the new limit; `WeighInDialogTest` no longer expects the not-found view.

## 2. Context

- [ ] 2.1 Bump `appVersion` minor. Verify: `./gradlew test lint assembleDebug` passes.
