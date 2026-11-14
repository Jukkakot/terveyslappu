# Design

## Context

Log of 2026-11-14 (kept in the session, never committed): the scale sends `0000…24` packets while
measuring and then the final stable reading (`…25`), and afterwards re-advertises the same final
reading with a new company id every 20 s to 2 min (`7ac0`, `7cc0`, `7dc0` all 99.05 kg). The
background decides a stable reading only on the next packet's tick or the hourly check, so a lone
reading waited ~2 min. The background never reads Health Connect (writes only); `ListenAlarm` has
one PendingIntent shared by start/check/end. The normal notification channel `weighins` was
created at `IMPORTANCE_LOW`, which an app cannot raise later.

## Goals / Non-Goals

**Goals:** one save per real weighing despite re-broadcasts; notification within ~30 s; watch
forwarding; no scanning after the window's weight is in.

**Non-Goals:** reading other apps' weights for the stop rule; exact alarms (no new permission).

## Decisions

1. **Same-weight rule next to the key rule.** `WeighingKeys` also stores the last saved or deleted
   weighing's kg, scale address and last-heard time (same `weighin` prefs). `sameWeight(kg,
   address)` is true within 0.1 kg and 30 s of last heard, and refreshes last heard (sliding).
   Checked by the dialog (`WeighIn`) and the background (`AutoWeights.decided`) before claiming;
   deletes (dialog discard, notification, Historia) call `remember(kg, address)` too.
   Sliding window, because re-broadcasts come every 20 s – 2 min: 30 s fixed would miss most; a
   repeat heard within 30 s of the previous one keeps it ignored. Risk noted below.
2. **Tick alarm.** A second PendingIntent (own request code, action `tick`) set with
   `setAndAllowWhileIdle` at pending reading time + quiet time + 5 s whenever a delivery leaves a
   pending stable reading; cancelled when decided. The tick runs `HeardPackets.tick` like the
   hourly check. Inexact while idle, but the screen-off light-idle delay is usually seconds.
3. **New channel id** `weighins_alert` at `IMPORTANCE_DEFAULT`; `deleteNotificationChannel
   ("weighins")` in `channels()`. The "?" channel is already default; both stay in the group.
4. **Window done record** in `ListenPrefs`: window start + the clientRecordIds of this app's
   unmarked weights in that window. Added by the dialog save, manual entry save (if its time is
   inside the current window), the background save with `check=ok`, and acceptance of a "?";
   removed by any delete or unmark of that id. `ListenController.replan` treats a non-empty
   record for the current window as Outside-until-next-start (`listen.stop reason=done`); a change
   to the record calls `replan("done")`. A record of an older window is ignored.
5. **Status and widget**: `ListenStatusRule` gets a `Done(nextStart)` state ahead of "Kuuntelee
   nyt" and after the blocked reasons; `WidgetStateRule` maps it to "Lepää".

## Risks / Trade-offs

- Sliding 30 s: if the scale's re-broadcast gap is longer than 30 s after a delete, a repeat can
  still be saved. With the stop rule this matters only after a delete; the log will show
  `scale.ignore reason=same` vs a new save, and the time can be raised.
- A real second weighing of the identical kg within 30 s of the last repeat is ignored; acceptable.
- Weights from other apps don't stop listening.
