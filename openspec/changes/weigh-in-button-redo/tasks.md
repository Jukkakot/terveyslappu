# Tasks

## 1. Weigh again in the weigh-in flow

- [ ] 1.1 Add redo and undo to the Android-free weigh-in flow: redo deletes the saved weight (by its
  scale client id) and starts a new scan, remembering the deleted weight; undo stops the scan,
  writes the same weight back (same client id, so an upsert) and returns to the saved result with
  its change; a new save forgets the deleted weight; a failed delete stays on the saved result
  marked as failed. Verify: new `WeighInTest` cases for redo, undo, new save and delete failure
  with fakes and virtual time.
- [ ] 1.2 Wire delete and write-back in the view model through Health Connect (delete by client
  record id; write-back as a scale weight with the original time and offset); log
  `weighin.redo ok|failed` and `weighin.undo ok|failed`. Verify: `./gradlew test` compiles and passes.

## 2. Dialog

- [ ] 2.1 Saved state: "Punnitse uudelleen" and "Valmis" buttons; the deletion-failed message when
  the delete failed; auto-close after about 10 s, cancelled by a tap on the card. While a
  deleted weight can be restored, the scanning, live and failure states show "Edellinen poistettu"
  with Kumoa. Verify: `WeighInDialogTest` smoke tests (buttons present on Saved and trigger their
  actions; Kumoa shown while scanning after a redo).

## 3. Widget and tile without a weight

- [ ] 3.1 Widget: one layout with the scale icon and "Punnitse", default and minimum size one cell,
  no weight, no periodic updates. Tile: no subtitle. Remove the latest-weight cache and its calls
  from the weigh-in and main screen, and the unused strings. Verify: `./gradlew test lint
  assembleDebug` passes.

## 4. Context

- [ ] 4.1 Update `openspec/context/product.md` (widget shows no weight, redo from the result),
  mark the roadmap Priority 1 item done, and update the log-event list in `.claude/CLAUDE.md`
  (`weighin.redo`, `weighin.undo`) and the map (no `LastWeight`). Verify: files read correctly.

## Workflow follow-up

- Archive with `/opsx:archive` once merged and CI is green.
