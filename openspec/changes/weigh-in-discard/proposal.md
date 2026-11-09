# Proposal

## Why

Sometimes a weigh-in is only a test (user 2026-11-09) and should not be counted. Saving stays the
default, but the result needs a way to throw the weigh-in away. Today the only workaround is
"Punnitse uudelleen" and then closing the dialog, which is hidden and starts a needless scan.

## What Changes

- The saved result in the weigh-in dialog gets a "Poista" button next to "Punnitse uudelleen" and
  "Valmis".
- Poista deletes the just-saved weight from Health Connect and its raw archive entry, without a
  confirmation, and does not scan again. The dialog says "Punnitus poistettu" with Kumoa (writes
  the same weight back, same as after Punnitse uudelleen) and closes by itself after about five
  seconds.
- A failed delete keeps the saved result and says the deletion failed (as for Punnitse uudelleen).
- The backup file follows the delete through the existing weigh-in trigger.
- Log: `weighin.discard ok|failed kg= time=`.

## Capabilities

### New Capabilities

### Modified Capabilities
- `scale-weigh-in`: the saved result offers Poista; new requirement "Discard the weigh-in".

## Impact

Weigh-in dialog only (`scale/WeighIn`, `ui/WeighInDialog`, `WeighInActivity`, strings). Health
Connect delete and raw archive reuse what Punnitse uudelleen uses. No change to the widget, the
tile, the scale decoding or CI.
