# Design

## Context

See proposal.md for why. Today `ui/WeighInClose.autoCloseDelayMs` closes the dialog at 0 ms on
`NotFound`, and `WeighIn.decide` drops ignored packets silently (`scale.ignore` only), so the
dialog keeps saying "Astu vaa'alle". `LastWeight` keeps kg, address and `heardAt`, which the
same-weight rule slides forward on every ignored hearing, so the save time is lost. The
notification's Poista paino deletes at once (`AutoWeights.delete`) and cancels the notification.
`WidgetStateRule` already returns `DONE`; the clear-ui-terms change maps it to the dot when
drawing. `showTimed` keeps a fixed 4 s timer for bars with an action. `WeighInForeground.shown`
is true from the dialog's `onStart` to `onStop`, whatever it shows. The dialog and the background
write weights with the same Health Connect metadata (`writeScale`: auto-recorded, scale device,
client id `terveyslappu-scale-<second>`); only the raw archive's `via=auto` tells them apart.

## Goals / Non-Goals

**Goals:** every everyday end says what happened; Kumoa wherever something is deleted outside the
app; timers follow Android's accessibility timeout; no new stores where existing data answers.

**Non-Goals:** U6's "tap an added mark to edit", U7 (amount chips), U9 and the polish list,
except the countdown on Valmis (decision 3). No change to the repeat rules' timings (2 min key,
30 s same weight) or to the dialog's Poista after its own save (still no Kumoa there).

## Decisions

1. **End reasons in `WeighIn`, pure.** `NotFound(heard)` becomes
   `NotFound(reason: NotHeard | Unsettled | Repeat(line: RepeatLine), heard, ignored)`. `decide`
   counts ignored packets (key and same-weight) and keeps the newest ignored packet's
   `RepeatLine(last: LastWeight?)`: `last` is set only when that packet decodes to a stable kg
   within 0.1 kg of the stored `LastWeight` (address equal or either unknown), so the dialog can
   show "(82,5 kg, tallennettu klo 7.12)"; otherwise the line has no brackets. The reason at the
   timeout: `Repeat` when anything was ignored and either nothing else was heard or a stable
   reading was among the ignored (a live weight that settled on the repeat), else `Unsettled`
   when anything was heard, else `NotHeard`. `Scanning(repeat: RepeatLine?)` and
   `Live(kg, stable, repeat: RepeatLine?)` carry the line (on `Live` only while its stable
   reading is being ignored, i.e. the newest ignored packet came after the live reading started
   showing that weight; simplest: set on an ignored stable packet, cleared by the next accepted
   reading of a different kg). `autoCloseDelayMs` returns null for `NotFound`. "Punnitse
   uudelleen" goes through the activity's `prepare(again = true)` (Health Connect, nearby devices,
   Bluetooth, as an opening) and skips the recent-weight check; `start()` already resets the
   scan. Alternative: keep closing and show a toast. Rejected: a toast fades while the user is
   still on the scale.
2. **`LastWeight` gains `at` (save or delete time, never slid) and `deleted`.**
   `remember(kg, address, deleted)` is called from the dialog save/discard, `AutoWeights`
   decision (counts as saved even while the weight is only queued: "tallennettu" is accepted),
   delete and restore, Historia's delete and Historia's Kumoa (`MainViewModel`). The same-weight
   rule's slide updates only `heardAt`. Old prefs without the fields read as `at = heardAt`,
   `deleted = false`. The repeat line uses `at` in local `H.mm` time.
3. **Countdown on Valmis** ("Valmis (8)") on the saved result and the recent-automatic view: the
   ui skill's rule for anything that ends by itself, and the same composable as today. It counts
   down from the lengthened time (decision 6), so with a one-minute accessibility timeout it starts
   at 60. Cheap, so taken from the polish list into this change.
4. **Recent automatic weight from Health Connect, not a new store.** On open (in the Preparing
   step, after the access check, not on Punnitse uudelleen) the view model reads this app's
   weights of the last 2 minutes (`HealthConnectWeights.recent(since)`, own origin) under a
   1.5 s `withTimeoutOrNull`. A pure `RecentAuto.pick(candidates, now, isAuto, marked)` keeps
   those whose client id starts `terveyslappu-scale-` and whose raw-archive entry has
   `via=auto` (the dialog writes the same Health Connect metadata), not in the marks store,
   with a weighing time at most 2 minutes ago, and returns the newest. It is shown as
   `WeighInState.Recent(kg, time, id, change, deleteFailed)`; the change comes from
   `latestBefore(time)` as after a dialog save. Without the read permission the check is skipped
   (`weighin.recent none reason=noread`); a failed or slow read logs `weighin.recent failed` and
   scanning starts. A background weight still in the pending queue is not in Health Connect: the
   repeat line covers that case. Poista calls `AutoWeights.delete(..., from = "dialog")` (the
   notification's path: raw data, marks, reference, listening, notification cancel,
   `remember(deleted = true)`) and additionally touches the weighing's company id (from the raw
   entry) as the dialog's discard does, then shows `Discarded`; a failed delete stays on `Recent`
   with `deleteFailed = true`. Window 2 minutes (user 2026-11-19): enough to walk to the phone
   right after stepping off, short enough that a later deliberate weigh-in scans.
   Alternative: a "last automatic save" record in `auto-flags.json`. Rejected: must be cleared by
   every delete path.
5. **Notification Kumoa by re-writing.** Poista paino deletes at once (as now) and re-posts the
   same notification id silently (`setOnlyAlertOnce`, `setSilent`) as "Paino poistettu" with
   Kumoa and `setTimeoutAfter(timeout)`. The Kumoa PendingIntent carries everything needed to
   restore (kg, weighing time ms, zone offset, company id, packet hex, questioned) because the
   receiver may run in a fresh process; the raw entry is read before it is removed. Request
   codes: one per weight id and action (`(id + action).hashCode()`, as today), so an older
   notification's Kumoa never carries another weight's data. `AutoWeights.restore(...)` writes a
   `PendingWeight` through the same writer (same client id, so a double tap stays one record),
   re-adds the raw entry, the mark or the reference pair, `weightSaved`, `remember(deleted =
   false)`, `flags.backupDue = true`, signals, and re-posts the original note text silently. A
   failed restore only re-posts the notification as "Painoa ei voitu palauttaa" with Kumoa (no
   timeout); it is not queued. The group summary: when "Paino poistettu" is the group's only
   child, the summary is re-posted with the same `setTimeoutAfter`, so it goes with it.
   Alternative: delay the delete until the undo time ends. Rejected: needs a scheduled job and
   loses the delete when the process dies (product.md: no background jobs).
6. **One timeout helper.** `a11y/ActionTimeouts.millis(base)` wraps
   `AccessibilityManager.getRecommendedTimeoutMillis(base, FLAG_CONTENT_TEXT | FLAG_CONTENT_CONTROLS)`
   (minSdk 34, always available) and never goes below the base. The Android source is installed
   once in `TerveysApp.onCreate`; JVM tests get the base. `showTimed` is a suspend function, not
   a composable, so instead of a CompositionLocal its default `actionMillis` reads
   `ActionTimeouts.millis(4 s)` at each call (the setting is read fresh), and callers stay as
   they are; the weigh-in activity and the notification use the same helper. Bases stay 4 s
   (bars), 10 s (result, "Paino poistettu"). Plain bars keep Material's Short, which M3 already
   scales. The unit test covers the wiring (a replaced source reaches `showTimed`'s timer), not
   Android's own maximum.
7. **Search marks and strip from the session state.** `FoodViewModel` already has `addedIds`;
   a pure `food/AddedSummary.of(addedIds, items, products)` returns per-ruoka counts and the
   strip's count / known kcal / without-kcal count. The mark key is the annos's final ruoka name
   after `resolveTyped` (what the item was logged as), compared with `FoodRules`' name match
   (case, ä/a, ö/o); a Valmiit ruuat row whose alias became an own ruoka under the typed name
   matches through that own ruoka's name, so the base row is marked too. Annokset of ruuat not in
   the rows (Pikalisäys, the clipboard's, unknown names) count only in the strip, which counts the
   whole search. Unit-tested; rows read the count, the strip is a `bottomBar` of the search's
   Scaffold under the snackbar host. Valmis calls the existing back handler, so widget, shortcut
   and in-app searches all keep their exit rule. `food.search.end` is logged in `leaveSearch()`
   with `via`.
8. **Bar text with the amount** from the same formatter the rows use for "200 g · 130 kcal"
   (amount part only); Pikalisäys shows its kcal. String `food_added` becomes `%1$s lisätty`
   fed with "<name> <amount>" (non-breaking space inside the amount). The bulk bar of Lisää
   kaikki and the AI import becomes "Lisättiin <N> annosta" (string `ai_added`), so it reads as
   what that tap did and not as the strip's whole-search count.
9. **Widget ✓ and one status order.** New `ic_state_done` (Material "check" glyph) in
   `R.color.widget_state_on`; `WeighInWidget.views` maps `DONE` → ✓; `ListenStatus.symbol()` maps
   the Done line the same way. `ListenStatusRule` moves `Done` before every "Ei toimi" reason,
   so the widget (which follows the status) and Asetukset show the same thing; `Done` ends at the
   next window start, when a blocker shows again. After midnight the texts are date-neutral (user
   2026-11-19): `Done.tomorrow == false` means the next start is today, so the line reads
   "Punnittu, seuraavan kerran klo 6.00" and the widget's TalkBack "Punnitse, punnittu"; with
   `tomorrow` "Tänään punnittu, seuraavan kerran huomenna klo 6.00" / "Punnitse, tänään punnittu".
   The widget picks the label from the fresh status at each redraw (no new `WidgetState`); a
   redraw from the stored state (status unreadable) uses the date-neutral label. Redraws on save
   and delete already happen (`weightSaved` / `weightRemoved`); the midnight label change needs no
   extra redraw beyond the existing alarms (TalkBack may say "tänään" until the next redraw, at
   most until the next window start's alarm; accepted). A plain status mark: no animation and no
   extra colour (product.md "A plain tool").
10. **The dialog blocks the background only while it can save itself.** `WeighInForeground.shown`
    becomes true only while the dialog is started and its state is `Preparing`, `Scanning`,
    `Live` or `Saving`; on the end view, `Saved`, `Recent`, `Discarded` and the access-problem
    states the background saves as usual (the keys still keep one weighing to one save). The
    activity sets it from a state collector plus `onStart`/`onStop`.
11. **End log.** `endReason(state, deleted…)` in `WeighInClose.kt` maps every `WeighInState`:
    `Saved` → saved, `Discarded` → discarded (or `recentdeleted` after the Recent view),
    `Recent` → recent, `NotFound` → its reason, `Preparing`/`BluetoothOff`/`Scanning`/`Live` →
    cancelled, `NoHealthAccess`/`PermissionDenied`/`ScanFailed`/`SaveFailed`/a failed delete →
    failed (`Saving` cannot be closed). The activity logs it once from `onDestroy` only when
    `isFinishing` (not on a rotation), and before a Punnitse uudelleen restart. Unit tests over
    every `WeighInState`.

## Risks / Trade-offs

- [Health Connect read on every dialog open adds latency] → one small time-range read inside the
  existing Preparing step, capped at 1.5 s; log its `ms=` in `weighin.recent`; scanning starts on
  failure.
- [Kumoa re-write gets a new Health Connect record id] → marks and the reference use the client
  record id (`scaleClientId(time)`), which stays the same; Samsung Health may re-sync it once.
- [Same weight after a delete is ignored for 30 s, so a quick re-weigh shows the repeat line] →
  accepted; the line now says why. Timings unchanged.
- [The background may save while the dialog shows a result] → that is the point of decision 10;
  the shared key claim and the same-weight rule keep the dialog's own weighing from being saved
  twice.
- [Notifications show the weight] → allowed (user 2026-11-19): the product rule is about the
  phone's home screen (widgets, tile, shortcut labels), which never show a value; product.md's
  wording is narrowed accordingly. "Paino poistettu" shows no value anyway.

## Review decisions (2026-11-19)

Self-review first, then a rubber-duck review and the user's answers (2026-11-19) folded in
(decisions 1, 2, 4, 5, 6, 8–11). Decided without asking:

- The end view stays until Valmis, Back or a tap outside (no auto-close: the user may have walked
  to the scale; the reason must still be readable).
- Recent automatic weight: only weights not marked "?" (a "?" weight keeps listening on, so a
  real weighing may still follow); the newest one when several; measured from the weighing time;
  the dialog's Poista after its own save keeps "no Kumoa" (product.md), the notification's gets
  Kumoa.
- The recent view has no Punnitse uudelleen (product.md "two buttons are clearer"); weigh again =
  Poista and open again.
- Kumoa restores exactly, even if another weight was saved meanwhile.
- "Paino poistettu" shows for 10 s (accessibility timeout may lengthen it); tapping it opens
  Historia like the other notifications.
- Widget ✓ only while automatic weigh-in is on (its existing Done state, until the next window
  start); with the feature off the widget stays a plain Punnitse.
- Strip Valmis is a filled button (the one filled control of the search); the mark is "✓" / "×N"
  in the primary colour, not a control.
- Countdown "Valmis (N)" taken in from the polish list (same component, the ui skill's rule).
- TalkBack: end reason and repeat line are a polite live region; Valmis keeps a stable name.
- User 2026-11-19: recent window 2 minutes; values may show in notifications, never on the home
  screen; after midnight "Punnittu, seuraavan kerran klo 6.00" and "Punnitse, punnittu".
