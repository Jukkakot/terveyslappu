# Proposal

## Why

When no weight comes, the weigh-in dialog scans for 60 seconds and then stays open on "Vaakaa ei
löytynyt" until someone closes it. Scanning longer than needed costs battery and the dialog is left
hanging. The user wants it to give up and close by itself after 30 seconds (user 2026-11-09).

## What Changes

- The weigh-in gives up 30 seconds after scanning started when no weight has been saved (was 60).
- On giving up, scanning stops and the dialog closes by itself; a short toast says the weigh-in was
  stopped because no weight came ("Punnitus lopetettu: painoa ei saatu"), so the closing is not
  silent. Try again = tap the widget, tile or button again.
- The 30 seconds count from the start, also while the scale already shows a changing weight (a
  normal weigh-in settles in a few seconds).
- A scan error (Bluetooth off etc.) keeps showing its message and actions as today.

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- `scale-weigh-in`: "Weigh-in dialog" — 30 s limit, closes by itself with a toast instead of a
  "not found" view.

## Impact

- Code: `scale/WeighIn.TIMEOUT_MS` 30 s; `WeighInActivity` closes on `NotFound` with a toast;
  the NotFound view in `ui/WeighInDialog` goes. Log keeps `weighin.timeout heard= ms=`.
