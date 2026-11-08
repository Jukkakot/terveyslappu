# Tasks

## 1. Delete with undo

- [ ] 1.1 `WeightEntry` keeps the client record id and whether it came from the scale; `HealthConnectWeights.restore(entry)` writes it back with matching metadata; unit test for the restore metadata choice
- [ ] 1.2 Long-press deletes at once (dialog removed); `UiMessage` gets an optional action; the snackbar shows Kumoa (long duration) and calls `MainViewModel.restore`; backup runs after restore; smoke test that a long-press on an own row calls delete without a dialog
- [ ] 1.3 `./gradlew test lint assembleDebug` passes

## Workflow follow-up

- Archive after merged and CI green.
