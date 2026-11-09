# Tasks

## 1. Weigh-in flow

- [x] 1.1 `WeighIn.discard(scope)`: delete the saved weight and its raw entry, log
  `weighin.discard ok|failed`, emit a closing signal; failure returns to `Saved(deleteFailed = true)`.
  Remove `redo`, `undo` and `removed`.
- [x] 1.2 Lingering broadcast: `WeighIn` takes the last weigh-in's company id and end time
  (`LastWeighIn` prefs store) and ignores packets with that id for two minutes (log
  `scale.ignore key=` once per scan); saving and discarding record the deciding packet's id.
- [x] 1.3 Unit tests (made-up weights): discard deletes weight and raw and closes; failed delete
  stays on Saved with the flag; a packet with the last id is ignored within two minutes and used
  after; a new id is used at once.

## 2. Dialog

- [x] 2.1 Saved result: Poista (text button) and Valmis; drop Punnitse uudelleen, Kumoa and
  "Edellinen poistettu"; `WeighInActivity` finishes on discard.
- [x] 2.2 Smoke test: the Saved state shows Poista and tapping it calls the discard action.

## 3. Wrap-up

- [x] 3.1 Bump MINOR in `gradle.properties`; update log events in `.claude/CLAUDE.md`; product.md
  (redo decision → Poista) and roadmap backlog.
