# Tasks

## 1. Give up after 30 seconds, with a visible countdown

- [x] 1.1 `WeighIn.TIMEOUT_MS` = 30 s; `WeighIn.secondsLeft` (StateFlow, null when not scanning)
  ticks each second while scanning. Verify: `WeighInTest` timeout cases green with the new limit;
  a test that the count is 28 after 2 s and null after the timeout.
- [x] 1.2 Dialog: in Scanning a determinate ring with the seconds in it, in Live a line with the
  seconds left; `WeighInActivity` finishes on `NotFound` (no toast); the NotFound view and strings
  go. Bluetooth-off view unchanged. Verify: `WeighInDialogTest` shows the count while scanning and
  no longer expects the not-found view.

## 2. Context

- [x] 2.1 Bump `appVersion` minor. Verify: `./gradlew test lint assembleDebug` passes.
