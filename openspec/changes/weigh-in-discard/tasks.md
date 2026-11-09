# Tasks

## 1. Discard in the weigh-in flow

- [ ] 1.1 `WeighIn.discard(scope)`: delete the saved weight and its raw entry (shared with `redo`),
  set `removed`, move to a new `WeighInState.Discarded` without scanning; failure returns to
  `Saved(deleteFailed = true)`; log `weighin.discard ok|failed`. `undo` works from `Discarded`.
- [ ] 1.2 Unit tests (made-up weights): discard deletes weight and raw and starts no scan; undo
  writes both back and shows Saved; failed delete stays on Saved with the flag.

## 2. Dialog

- [ ] 2.1 Saved result: "Poista" button beside Punnitse uudelleen and Valmis (fits the dialog width;
  wraps rather than clips). `Discarded` shows "Punnitus poistettu" with Kumoa and Sulje;
  `WeighInActivity` closes it after about five seconds unless tapped. Finnish and English strings.
- [ ] 2.2 One smoke test: the Saved state shows Poista and tapping it calls the discard action.

## 3. Wrap-up

- [ ] 3.1 Bump MINOR in `gradle.properties`; add `weighin.discard` to the log events in
  `.claude/CLAUDE.md`; note the change in the roadmap backlog as done.
