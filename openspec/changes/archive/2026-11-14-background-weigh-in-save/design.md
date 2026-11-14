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
texts, acceptance chain) in pure classes with unit tests.

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
under a few seconds; the design gives up at about 8 s, decision 11). A Health Connect insert took well under a second in the dialog's logs
(`weighin.save ms=`).

**Notifications.** `POST_NOTIFICATIONS` is a runtime permission on Android 13+; without it
`notify()` does nothing (no crash). Ask with the standard permission request when the switch is
turned on; check `NotificationManagerCompat.areNotificationsEnabled()` and the channel importance
for the status line. Posting from a receiver needs no foreground state. Actions are
`PendingIntent.getBroadcast` to an explicit, non-exported receiver with `FLAG_IMMUTABLE` and a
distinct request code per weight and action; the tap goes to `MainActivity` (opens Historia) with
`PendingIntent.getActivity`, immutable. Without `setTimeoutAfter` a notification stays until the
user dismisses it. A notification action reaches a stopped-but-not-force-stopped app.

**Widget updates.** `AppWidgetManager.updateAppWidget(ComponentName, RemoteViews)` can be called
from any receiver; no broadcast to the provider is needed. `updatePeriodMillis` stays 0 (it wakes
the phone and its minimum is 30 min). Bluetooth on/off (`BluetoothAdapter.ACTION_STATE_CHANGED`)
and power saving changes are not in the implicit-broadcast exceptions, so a manifest receiver
cannot hear them; battery restriction (`ActivityManager.isBackgroundRestricted`) has no broadcast at
all. The widget therefore recomputes its state at each phase 1 alarm (start, end, hourly check),
on boot and package replace, when the app resumes, after a settings change, after an automatic
weigh-in and after a notification action. Outside the window it always shows Lepää, so no wake-up
is needed at night; the widget stores the window end and shows Lepää at its next redraw once it has
passed.

## Decisions

1. **Save in the scan receiver with `goAsync()`.** After the accumulator updates, the stable-weight
   rule runs; a decided weight is written at once. Alternatives: WorkManager expedited job (new
   dependency, and Samsung defers jobs: knowledge.md, Android), foreground service (permanent
   notification, more battery). Rejected for now; the pending queue covers a failed write.
2. **Same stable-weight rule as the dialog.** The background feeds the existing `StableWeight` with
   the packets' own timestamps, so the dialog and the background decide the same kg; the saved time
   is the deciding packet's. Two equal stable packets in a row decide at once (the scale repeats its
   stable packet for 13 s+). A lone stable packet with no different reading after it is decided at
   the next delivery, hourly check or window end, still timed at that packet; the phase 1 trial
   shows how often this happens.
3. **Pending queue.** A weight whose write fails (or whose time budget ran out, decision 11) is kept
   (kg, time, offset, raw, mark) in an app-private file and retried at the next weigh-in, the hourly
   check and at the start of the first foreground read, before the list is shown (so the list
   includes it). Asetukset shows "N punnitusta odottaa tallennusta". The upsert by `clientRecordId`
   makes a retry safe. A queued weight's notification is posted when it is written and carries the
   weighing time. Logged `auto.save failed cause= queued=`.
4. **Reference without reading.** The reference store keeps the latest ~10 accepted own (time, kg)
   pairs and an explicit "unknown" state. Every save, delete, undo, edit, import and acceptance the
   app makes updates the list and the reference is recomputed from it (so a delete falls back to the
   previous pair); a foreground read of the weights replaces the list, which also seeds it. Right
   after the update (store never seeded) the state is unknown: the automatic weight is saved and
   marked "?" until the app is opened once. A weight deleted in another app while ours is closed
   leaves a stale reference until the app is opened; acceptable. Alternative: the background-read
   permission, kept as a later option.
5. **Marks store.** A small JSON file in app storage keyed by the weight's own `clientRecordId`
   (not its second: two weights can share a second), holding only "?" entries; removed with the
   weight, restored with undo, written by import from `check`. Filters, statistics and the home
   notice use only marks that match a read weight; a foreground read drops orphan marks. Not in
   Android backup (phone-bound like the raw archive; the data file is the portable copy). `via=auto`
   goes into the raw entry's fields so the export shows which weights were automatic.
6. **One weighing, one save.** The lingering-broadcast state (last saved or discarded company id and
   its time) moves from the dialog's view model to a shared, synchronized store in `TerveysApp`
   persisted in preferences. The key is claimed atomically (compare-and-set under one lock) before
   anything is written, by the dialog and the background alike; whoever claims first saves, the
   other ignores it. The same `clientRecordId` rule is a second guard. The background also skips
   saving while the weigh-in dialog is in the foreground (a process-wide flag the dialog sets).
7. **Acceptance and chain.** A pure function takes the "?" weights in time order, the accepted
   anchor (the accepted weight) and the limit, and returns the ones to accept: walk forward, accept
   each whose difference to the previous accepted weight of the chain is within the limit, stop at
   the first that is not. Used by Historia's Hyväksy and the notification's Hyväksy. A chain accepts
   marks and updates the reference store in one step.
8. **Notifications.** Two channels: a silent channel "Punnitukset" for normal weigh-ins and a
   default-importance channel "Tarkistettavat punnitukset" for "?" ones, both under one notification group (summary) so many
   mornings do not flood the shade. Nothing uses `setTimeoutAfter`: each stays until dismissed.
   Notification id from the weight's `clientRecordId`. Texts come from a pure function: with no
   reference only "82,5 kg tallennettu"; a late save adds "klo 7.12". The action receiver uses
   `goAsync()`, deletes by `clientRecordId` or accepts (with chain), cancels the notification, marks
   the backup as due, emits the weights-changed signal, updates the widget; a failed delete re-posts
   the notification with "Poisto epäonnistui". Accept or delete in Historia cancels the same id.
9. **Backup due flag.** A change made in the background sets a "backup due" flag; app start runs the
   backup when the flag is set or the daily rule says so, then clears it.
10. **Widget state follows the Asetukset status.** It is a pure mapping of the status: blocked
    reasons, Tauolla (power saving) and Pysäytetty (force-stopped) give Estetty; the warnings
    (battery restricted, notifications off) never do; outside the window it is always Lepää; else
    Päällä. The widget stores the window end with its state and, once it has passed, shows Lepää at
    its next redraw. Updates run at the phase 1 alarms (start, end, hourly check), boot, package
    replace, app resume, settings change, an automatic weigh-in and a notification action, so state
    changes land within about 15 minutes of the window start and end (the start and end alarms
    already exist). Symbol (filled dot / hollow moon / warning triangle) and content description;
    colours from the widget's theme, light and dark.
11. **Receiver time budget.** The `goAsync()` block is timed; each step (claim, write, mark, notify,
    widget) logs `ms=`. At about 8 s the block gives up, puts the weight in the pending queue and
    calls `finish()`, well before the broadcast timeout.
12. **Weights-changed signal.** A process-wide `SharedFlow` in `TerveysApp`; every app-side change
    (receiver save, action, queue write, acceptance) emits it. The main view model collects it and
    also reloads on resume. When the process is not alive there is nothing to notify, and the next
    start reads fresh.
13. **CSV `check` column.** Follows the merged data-robustness-fixes layout: time with offset
    (`07:12:00+03:00`) and the header `date;time;weight_kg;source;raw;check`. A test proves the
    current `HistoryParser` reads a file with the extra column, so no scenario is needed for older
    apps.
14. **Home notice and list.** The notice "Punnitus odottaa tarkistusta" opens Historia's list
    scrolled to the newest "?" weight; "?" rows are listed whatever the period filter (the list
    data adds them to the filtered result).
15. **Log events:** `auto.save kg= check=ok|? ref= ms=` (plus per-step `ms=`), `auto.save failed
    cause= queued=`, `auto.timeout queued=`, `auto.retry ok|failed count=`,
    `auto.skip reason=key|range|outside|dialog`, `auto.notify check=` (or `auto.notify denied`),
    `auto.action delete|confirm ok|failed`, `auto.confirm from=historia|notification chain=`,
    `widget.state state=`, `backup.due`, `marks.orphans dropped=`. Weights only appear as kg, like
    `scale.stable`.

## Implementation notes (left open by the spec; simplest option taken, 2026-11-14)

- Status order keeps phase 1's (Pysäytetty before Tauolla and Kuuntelee); "Health Connect -lupa
  puuttuu" comes right after the Bluetooth permission.
- `listen.start late=` is measured for alarm starts and hourly checks; any other start logs 0.
- "Optimoitu" = the app does not ignore battery optimisation (`isIgnoringBatteryOptimizations`).
- The saved time is the first stable reading of the deciding pair (as the dialog); the raw entry
  is the latest stable packet. A tick runs before every background packet, as the dialog's ticker.
- A decided weight is claimed and put in the pending queue first, then written (so a killed
  receiver loses nothing); a late save = a weight whose earlier write failed or timed out. A late
  "?" title reads "61,3 kg klo 7.12 – tarkista".
- The reference is taken at write time (latest accepted pair before the weight's time). Accepting
  while it is unknown keeps it unknown until the first foreground read. The app has no edit, so
  nothing hooks "edit".
- Without the history permission a foreground read sees 30 days: only marks of that span can be
  orphans, and an empty read keeps the old reference pairs.
- The view model reloads itself after its own changes; the dialog, the background save and the
  notification actions emit the weights-changed signal.
- The widget recomputes its state at every own redraw; the stored state and window end are used
  only when the status cannot be read. The mark is a small badge on the plate's corner; the
  content description is "Punnitse, automaattinen punnitus: <state>".
- Hyväksyntäraja is a row with − / + buttons and its value. The notification tap also closes it;
  a silent summary notification holds the group. Notification texts come from `AutoText` in code.
- The home card with only "?" weights shows as with no weights; a group of only "?" weights shows
  its count only. The check notice and the backup notice can both show.
- The backup-due flag is cleared when the app-start backup is requested.

## Risks / Trade-offs

- [Health Connect refuses background writes on this phone] → pending queue + log; fallback in a
  follow-up: a short foreground service around the write.
- [Receiver killed before the write finishes] → the accumulator keeps the decided weight; the next
  delivery or check finishes it (the upsert makes doubles impossible); the 8 s budget makes this
  rare.
- [Widget state lags up to about 15 minutes] → accepted; opening the app refreshes it.
- [Someone else weighs inside the window] → saved but marked "?" when far enough; a close weight is
  accepted (that is the simple rule the user chose) and can be deleted from the notification.
- [Chained acceptance accepts a wrong run] → each step is bounded by the limit and stops at the
  first outside it; the user can still delete any accepted weight.
- [Backup lags until the app opens] → the home screen's backup notice still warns after three days.
- [Stale reference after a delete in another app] → corrected by the next foreground read.

## Migration Plan

Phase 1 is archived first, so `background-weigh-in` exists in the specs. On update the trial's
prefs are kept (switch, window, mode); the limit defaults to 5 kg; no data migration (no marks
exist yet). The reference store starts "unknown" and is seeded by the first foreground read, so
weights saved before the app is opened once are marked "?". Rollback: turning the switch off stops everything; marks stay in the data file.
