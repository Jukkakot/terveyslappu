# Proposal

## Why

In v1.21.57 the automatic weigh-in notified 9-17 s after the scale settled (log 2026-11-14,
kept in the session): the scan batches deliveries (report delay 5 s, longer in Säästö), and the
background then waited 2 s quiet time plus a 5 s tick margin. The user wants the notification
while still on the scale, as a confirmation. The notification's lone "Poista" also reads as
"dismiss this notification".

## What Changes

- The background decides a weight at its first stable packet (the scale sends only zero packets
  while measuring and then the final reading), with no quiet time; the tick alarm goes.
- The background scan reports each packet at once (no batching).
- Notification buttons: normal weigh-in **OK** (closes the notification) and **Poista
  punnitus**; "?" weigh-in **Hyväksy** and **Poista punnitus**. Swiping a "?" notification away
  keeps the mark.
- The dialog's stable-weight rule is unchanged.

## Capabilities

### New Capabilities

### Modified Capabilities
- `background-weigh-in`: Save the automatic weight (first stable packet), Timely background
  decision (immediate, no batching), Weigh-in notification and Notification actions (OK,
  Poista punnitus).

## Impact

Scale/background listening (`listen/HeardPackets`, `ListenController`, `ListenAndroid` scan
settings and tick alarm), notifications (`auto/AutoAndroid`, `AutoActionReceiver`, strings).
No Health Connect or CI change.
