# Proposal

## Why

Sometimes a weigh-in is only a test (user 2026-11-09) and should not be counted. Saving stays the
default, but the result needs a way to throw the weigh-in away. "Punnitse uudelleen" did not work
either: the scale keeps broadcasting its last stable packet for at least ten seconds, so the new
scan caught the same reading within 0.2 s and saved the same weight again (shared log
2026-11-09, five redos in a row).

## What Changes

- The saved result has two buttons only: **Poista** and **Valmis** (user 2026-11-09).
- Poista deletes the just-saved weight from Health Connect and its raw archive entry, without a
  confirmation and without undo, and closes the dialog at once. Weighing again = Poista, then open
  the widget again.
- **Removed:** "Punnitse uudelleen" and its Kumoa (the `Weigh again` requirement).
- Lingering broadcast: for two minutes after a weigh-in is saved or discarded, a new weigh-in
  ignores packets carrying that weigh-in's company id (the scale's per-weighing counter, seen as
  5fc0 → 60c0 → 61c0), so reopening the widget waits for a new weighing instead of saving the old
  reading again. The two-minute limit keeps the counter's wrap-around from ever hiding a real
  weighing.
- A failed delete keeps the saved result and says the deletion failed.
- The backup file follows the delete through the existing weigh-in trigger.
- Log: `weighin.discard ok|failed kg= time=`, `scale.ignore key=` (once per scan);
  `weighin.redo` and `weighin.undo` go away.

## Capabilities

### New Capabilities

### Modified Capabilities
- `scale-weigh-in`: the saved result offers Poista and Valmis; new requirements "Discard the
  weigh-in" and "Ignore the scale's lingering broadcast"; "Weigh again" removed.

## Impact

Weigh-in dialog and flow (`scale/WeighIn`, `ui/WeighInDialog`, `ui/WeighInViewModel`,
`WeighInActivity`, strings) and a small prefs store for the last weigh-in's company id. Health
Connect delete and raw archive reuse what Punnitse uudelleen used. No change to the widget, the
tile, the scale decoding or CI.
