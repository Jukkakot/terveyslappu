# Design

## Context

Builds on phase 1 (`openspec/changes/weigh-in-listen-trial/design.md`): a PendingIntent scan
delivers results to a manifest receiver, alarms (`setAndAllowWhileIdle`) start, stop and check the
window hourly, an accumulator in app-private preferences survives process death, the status choice
and window maths are pure. The dialog saves through `weight/HealthConnectWeights.writeScale`
(already `Metadata.autoRecorded` with a scale `Device`, upsert by `clientRecordId` from the
reading's second), keeps raw data in `raw/RawArchive` and triggers the backup. The widget is a
plain `AppWidgetProvider` with RemoteViews and `updatePeriodMillis="0"`. No WorkManager in the
project. targetSdk 37, minSdk 34.

## Goals / Non-Goals

**Goals:** save from the receiver without a foreground service or new library; never read Health
Connect in the background; keep every new rule (plausibility, reference, widget state, notification
texts) in pure classes with unit tests.

**Non-Goals:** a background-read permission, a foreground service, other scales than the
remembered one, a time factor in the plausibility rule.

## Research: platform rules

**Health Connect from the background.**
- Reads are foreground-only by default; a background read needs the extra user-granted permission
  `android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND` and a feature check
  (`HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND`), and the app must work without it
  ([read data](https://developer.android.com/guide/health-and-fitness/health-connect/develop/read-data)).
- The docs put the foreground restriction on reads only; inserts, updates and deletes are not
  listed as foreground-only, and the rate-limit guide speaks of background writes
  ([rate limiting](https://developer.android.com/health-and-fitness/health-connect/rate-limiting)).
  So a write or delete from a BroadcastReceiver is allowed. Not yet seen on this phone: the first
  real background write is logged with its outcome (task 3.4), and a `SecurityException` falls
  back to the pending queue (decision 3).
- Rate limits: fixed quotas per period and per day for reads, and for insert/update/delete plus
  memory limits; background quotas are stricter than foreground. The numbers are not published. A
  weigh-in costs one insert (two with a notification Poista); far below any quota. A quota error
  surfaces as an exception ("quota exceeded"); treat it like any failed write (pending queue,
  retry later, never delete-and-resend).
- What counts as a read here: the backup (reads all weights), the change shown in the dialog, the
  Historia refresh. None of these may run in the receiver.

**Receiver time budget.** A manifest receiver runs `onReceive` on the main thread; with
`goAsync()` work can continue on a coroutine until `finish()`, within the broadcast timeout
(about 10 s for a foreground-priority broadcast, longer for background ones; keep the whole save
under a few seconds). A Health Connect insert took well under a second in the dialog's logs
(`weighin.save ms=`).

**Notifications.** `POST_NOTIFICATIONS` is a runtime permission on Android 13+; without it
`notify()` does nothing (no crash). Ask with the standard permission request when the switch is
turned on; check `NotificationManagerCompat.areNotificationsEnabled()` and the channel importance
for the status line. Posting from a receiver needs no foreground state. Actions are
`PendingIntent.getBroadcast` to an explicit, non-exported receiver with `FLAG_IMMUTABLE` and a
distinct request code per weight and action; the tap goes to `MainActivity` (opens Historia) with
`PendingIntent.getActivity`, immutable. `setTimeoutAfter(1 h)` removes a normal notification by
itself. A notification action reaches a stopped-but-not-force-stopped app.

**Widget updates.** `AppWidgetManager.updateAppWidget(ComponentName, RemoteViews)` can be called
from any receiver; no broadcast to the provider is needed. `updatePeriodMillis` stays 0 (it wakes
the phone and its minimum is 30 min). Bluetooth on/off (`BluetoothAdapter.ACTION_STATE_CHANGED`)
and power saving changes are not in the implicit-broadcast exceptions, so a manifest receiver
cannot hear them; battery restriction (`ActivityManager.isBackgroundRestricted`) has no broadcast at
all. The widget therefore recomputes its state at each phase 1 alarm (start, end, hourly check),
on boot and package replace, when the app resumes, after a settings change, after an automatic
weigh-in and after a notification action. Outside the window it always shows Lepää, so no wake-up
is needed at night.

## Decisions

1. **Save in the scan receiver with `goAsync()`.** After the accumulator updates, the stable-weight
   rule runs; a decided weight is written at once. Alternatives: WorkManager expedited job (new
   dependency, and Samsung defers jobs: knowledge.md, Android), foreground service (permanent
   notification, more battery). Rejected for now; the pending queue covers a failed write.
2. **When a weight is decided.** Two equal stable packets in a row decide at once (the usual case:
   the scale repeats its stable packet for 13 s+). A lone stable packet with no different reading
   after it is decided at the next delivery, hourly check or window end, timed at the packet; the
   phase 1 trial shows how often this happens.
3. **Pending queue.** A weight whose write fails is kept (kg, time, offset, raw, mark) in an
   app-private file and retried at the next weigh-in, the hourly check and app start; the upsert by
   `clientRecordId` makes a retry safe. Logged `auto.save failed cause= queued=`.
4. **Reference without reading.** The reference (time and kg of the latest accepted own weight) is
   kept in app storage and updated by every save, delete, undo, import and confirm the app makes,
   and recomputed from the read list whenever the app reads its weights in the foreground. A weight
   deleted in another app while ours is closed leaves a stale reference until the app is opened;
   acceptable. Alternative: the background-read permission (extra Health Connect dialog, not on
   every phone); kept as a later option.
5. **Marks store.** A small JSON file in app storage keyed by the weight's epoch second (like the
   raw archive), holding only "?" entries; removed with the weight, restored with undo, written by
   import from `check`. Not in Android backup (phone-bound like the raw archive; the data file is
   the portable copy). `via=auto` goes into the raw entry's fields so the export shows which weights
   were automatic without a new column.
6. **One weighing, one save.** The lingering-broadcast state (last saved or discarded company id and
   its time) moves from the dialog's view model to a shared, synchronized store in `TerveysApp`
   persisted in preferences, read by both the dialog and the receiver. Whoever saves first records
   the key; the other ignores it. The same `clientRecordId` rule is a second guard.
7. **Notifications.** Two channels: "Punnitukset" (low importance, silent) and "Tarkistettavat
   punnitukset" (default importance). Notification id from the weight's second. The action receiver
   uses `goAsync()`, deletes by `clientRecordId` or removes the mark, cancels the notification,
   marks the backup as due, updates the widget; a failed delete re-posts the notification with
   "Poisto epäonnistui". Confirm or delete in Historia cancels the same id.
8. **Backup due flag.** A change made in the background sets a "backup due" flag; app start runs the
   backup when the flag is set or the daily rule says so, then clears it.
9. **Widget state** is a pure function of: feature on, inside window, Bluetooth permission, Health
   Connect write permission, Bluetooth on, power saving pause, battery restricted. The view gets a
   symbol (filled dot / hollow moon / warning triangle) and a content description; colours from the
   widget's existing theme, both light and dark.
10. **Log events:** `auto.save kg= check=ok|? ref= ms=`, `auto.save failed cause= queued=`,
    `auto.retry ok|failed count=`, `auto.skip reason=key|range|outside`, `auto.notify check=`
    (or `auto.notify denied`), `auto.action delete|confirm ok|failed`, `auto.confirm from=historia|notification`,
    `widget.state state=`, `backup.due`. Weights only appear as kg, like `scale.stable`.

## Risks / Trade-offs

- [Health Connect refuses background writes on this phone] → pending queue + log; fallback in a
  follow-up: a short foreground service around the write.
- [Receiver killed before the write finishes] → the accumulator keeps the decided weight; the next
  delivery or check finishes it (the upsert makes doubles impossible).
- [Widget state lags up to an hour inside the window] → accepted; opening the app refreshes it.
- [Someone else weighs inside the window] → saved but marked "?" when far enough; a close weight is
  accepted (that is the simple rule the user chose) and can be deleted from the notification.
- [Backup lags until the app opens] → the home screen's backup notice still warns after three days.

## Migration Plan

Phase 1 is archived first, so `background-weigh-in` exists in the specs. On update the trial's
prefs are kept (switch, window, mode); the limit defaults to 5 kg; no data migration (no marks
exist yet). Rollback: turning the switch off stops everything; marks stay in the data file.
