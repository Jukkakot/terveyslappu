## Why

An audit found two ways the weigh-in dialog breaks its own spec: a rotation or other config change
while saving restarts the weigh-in, cancels the save half-way and can save the same reading twice
("Same weigh-in seen twice"); and save, Poista and their follow-ups die with the dialog when it is
closed (backdrop, back, the 10 s auto-close racing Poista).

## What Changes

- "Prepared" lives in the ViewModel; once a weight is decided, a restart neither shows Preparing
  nor scans again.
- Save, retry, Poista and their follow-ups (backup request, raw archive, remembering the id and
  the scale) run in the app-wide scope, not the dialog's, so closing or recreating the dialog does
  not cancel them.
- While saving, tapping outside the card and Back do nothing.
- Poista stops the 10 s auto-close; a second Poista tap while deleting is ignored.
- No version bump (fixes raise BUILD only).

## Capabilities

### Modified Capabilities
- `scale-weigh-in`: Save the scale weight and Discard the weigh-in gain scenarios for a dialog
  recreated or closed while saving or deleting.

## Impact

`scale/WeighIn.kt`, `ui/WeighInViewModel.kt`, `WeighInActivity.kt`, `ui/WeighInDialog.kt`; tests in
`scale/WeighInTest.kt` and a small pure auto-close rule.
