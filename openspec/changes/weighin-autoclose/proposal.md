# Proposal

## Why

When no weight comes, the weigh-in dialog scans for 60 seconds and then stays open on "Vaakaa ei
löytynyt" until someone closes it. Scanning longer than needed costs battery and the dialog is left
hanging. The user wants it to give up and close by itself after 30 seconds (user 2026-11-09).

## What Changes

- The weigh-in gives up 30 seconds after scanning started when no weight has been saved (was 60).
- The dialog shows the seconds left from the start (a ring counting down while waiting, a line under
  the live weight), so its closing is expected (user: no message at closing, a visible count from
  the start instead).
- At zero, scanning stops and the dialog closes by itself. Try again = tap the widget, tile or button.
- The count runs from the start, also while the scale already shows a changing weight (a normal
  weigh-in settles in a few seconds).
- Bluetooth off: the dialog keeps guiding the user to turn it on first; the count starts only with
  scanning.

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- `scale-weigh-in`: "Weigh-in dialog" — 30 s visible countdown, closes by itself instead of a
  "not found" view.

## Impact

- Code: `scale/WeighIn` (30 s, `secondsLeft`), `WeighInViewModel`, `WeighInActivity` closes on
  `NotFound`, `ui/WeighInDialog` countdown; the NotFound view goes. Log keeps `weighin.timeout heard= ms=`.
