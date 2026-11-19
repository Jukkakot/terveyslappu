# Design

## Context

See proposal.md for why. Today `ui/WeighInClose.autoCloseDelayMs` closes the dialog at 0 ms on
`NotFound`, and `WeighIn.decide` drops ignored packets silently (`scale.ignore` only), so the
dialog keeps saying "Astu vaa'alle". `LastWeight` keeps kg, address and `heardAt`, which the
same-weight rule slides forward on every ignored hearing, so the save time is lost. The
notification's Poista paino deletes at once (`AutoWeights.delete`) and cancels the notification.
`WidgetStateRule` already returns `DONE`; the clear-ui-terms change maps it to the dot when
drawing. `showTimed` keeps a fixed 4 s timer for bars with an action.

## Goals / Non-Goals

**Goals:** every everyday end says what happened; Kumoa wherever something is deleted outside the
app; timers follow Android's accessibility timeout; no new stores where existing data answers.

**Non-Goals:** U6's "tap an added mark to edit", U7 (amount chips), U9 and the polish list,
except the countdown on Valmis (decision 3). No change to the repeat rules' timings (2 min key,
30 s same weight) or to the dialog's Poista (still no Kumoa there).

## Decisions

1. **End reasons in `WeighIn`, pure.** `NotFound(heard)` becomes
   `NotFound(reason: NotHeard | Unsettled | Repeat(last: LastWeight?))`; `decide` counts ignored
   packets and keeps the newest `LastWeight` it ignored for. A new `Scanning(repeat: LastWeight?)`
   (or a field on `Scanning`) tells the dialog to show the repeat line. `autoCloseDelayMs`
   returns null for `NotFound`. "Punnitse uudelleen" calls `start()` again, which already resets
   the scan; `weighin.end` is logged by the activity from the state it leaves (one helper
   `endReason(state, keepOpen…)` in `WeighInClose.kt`, unit-tested). Alternative: keep closing and
   show a toast. Rejected: a toast fades while the user is still on the scale.
2. **`LastWeight` gains `at` (save or delete time, never slid) and `deleted`.** `remember(kg,
   address, deleted)` is called from the dialog save/discard, `AutoWeights` decision and delete,
   and Historia's delete (`MainViewModel`). Old prefs without the fields read as `at = heardAt`,
   `deleted = false`. The repeat line uses `at` in local `H.mm` time.
3. **Countdown on Valmis** ("Valmis (8)") on the saved result and the recent-automatic view: the
   ui skill's rule for anything that ends by itself, and the same composable as today. Cheap, so
   taken from the polish list into this change.
4. **Recent automatic weight from Health Connect, not a new store.** On open (in the Preparing
   step, after the access check) `HealthConnectWeights.recentAuto(since = now − 10 min)` reads this
   app's weights recorded automatically (`RecordingMethod.AUTOMATICALLY_RECORDED`, own origin);
   the newest not in the marks store is shown as `WeighInState.Recent(kg, time, id, change)`.
   Health Connect is the truth, so a delete anywhere (Historia, notification) is respected with no
   bookkeeping. Poista calls `AutoWeights.delete` (the notification's path: raw data, marks,
   reference, listening, notification cancel, `remember(deleted = true)`), then closes. A failed
   read is logged (`weighin.recent failed`) and scanning starts. Window 10 minutes: long enough
   to walk from the bathroom to the phone, short enough that a later deliberate weigh-in scans.
   Alternative: a "last automatic save" record in `auto-flags.json`. Rejected: must be cleared by
   every delete path.
5. **Notification Kumoa by re-writing.** Poista paino deletes at once (as now) and re-posts the
   same notification id silently (`setOnlyAlertOnce`, `setSilent`) as "Paino poistettu" with
   Kumoa and `setTimeoutAfter(timeout)`. The Kumoa PendingIntent carries everything needed to
   restore (kg, weighing time ms, zone offset, company id, packet hex, questioned) because the
   receiver may run in a fresh process; the raw entry is read before it is removed.
   `AutoWeights.restore(...)` writes a `PendingWeight` through the same writer (same client id, so
   a double tap stays one record), re-adds the raw entry, the mark or the reference pair,
   `weightSaved`, `remember(deleted = false)`, signals, and re-posts the original note text
   silently. Alternative: delay the delete until the undo time ends. Rejected: needs a scheduled
   job and loses the delete when the process dies (product.md: no background jobs).
6. **One timeout helper.** `a11y/Timeouts.kt`: `recommendedMillis(base, controls)` wraps
   `AccessibilityManager.getRecommendedTimeoutMillis(base, FLAG_CONTENT_TEXT | FLAG_CONTENT_CONTROLS)`
   (minSdk 34, always available) for the notification and the weigh-in activity; in Compose the
   bar host reads `LocalAccessibilityManager.current?.calculateRecommendedTimeoutMillis(...)` and
   passes it to `showTimed(actionMillis = …)` at every caller (or a CompositionLocal set once in
   `TerveysTheme`, so callers keep the default). Bases stay 4 s (bars), 10 s (result,
   "Paino poistettu"). Plain bars keep Material's Short, which M3 already scales.
7. **Search marks and strip from the session state.** `FoodViewModel` already has `addedIds`;
   a pure `food/AddedSummary.of(addedIds, items, products)` returns per-ruoka counts (keyed as
   the row's ruoka key) and the strip's count / known kcal / without-kcal count. Unit-tested;
   rows read the count, the strip is a `bottomBar` of the search's Scaffold under the snackbar
   host. Valmis calls the existing back handler, so widget, shortcut and in-app searches all keep
   their exit rule. `food.search.end` is logged in `leaveSearch()` with `via`.
8. **Bar text with the amount** from the same formatter the rows use for "200 g · 130 kcal"
   (amount part only); Pikalisäys shows its kcal. String `food_added` becomes `%1$s lisätty`
   fed with "<name> <amount>" (non-breaking space inside the amount).
9. **Widget ✓.** New `ic_state_done` (Material "check" glyph) in `R.color.widget_state_on`;
   `WeighInWidget.views` maps `DONE` → ✓ with "tänään punnittu"; `ListenStatus.symbol()` maps the
   Done line the same way. No new state, prefs or log values (`widget.state state=done` already
   exists). Redraws on save and delete already happen (`weightSaved` / `weightRemoved`). A plain
   status mark: no animation and no extra colour (product.md "A plain tool").

## Risks / Trade-offs

- [Health Connect read on every dialog open adds latency] → one small time-range read inside the
  existing Preparing step; log its `ms=` in `weighin.recent`; scanning starts on failure.
- [Kumoa re-write gets a new Health Connect record id] → marks and the reference use the client
  record id (`scaleClientId(time)`), which stays the same; Samsung Health may re-sync it once.
- [Same weight after a delete is ignored for 30 s, so a quick re-weigh shows the repeat line] →
  accepted; the line now says why. Timings unchanged.
- [The weigh-in notification shows the weight while product.md lists notifications among surfaces
  that never show values] → existing behaviour since background-weigh-in-save; the new "Paino
  poistettu" shows none. The last task clarifies product.md (the automatic weigh-in notification
  reports an automatic save with its value, the ui skill's rule) instead of changing behaviour.

## Review decisions (2026-11-19)

No subagent tool was available in the proposing session, so the review was a self-review against
the spec-review checklist. Decided without asking:

- The end view stays until Valmis, Back or a tap outside (no auto-close: the user may have walked
  to the scale; the reason must still be readable).
- Recent automatic weight: only weights not marked "?" (a "?" weight keeps listening on, so a
  real weighing may still follow); the newest one when several; measured from the weighing time;
  the dialog's Poista keeps "no Kumoa" (product.md), the notification's gets Kumoa.
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
