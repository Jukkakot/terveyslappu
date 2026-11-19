# Tasks

## 1. Timeouts follow accessibility (U4)

- [ ] 1.1 Add the timeout helper (design 6): Android `recommendedMillis(base)` and the Compose
  path for `showTimed` (CompositionLocal or caller argument), bases 4 s bars, 10 s result and
  "Paino poistettu". Every `showTimed` caller with an action uses it. Unit test: the pure part
  returns the base when the system gives less and the system value when it gives more.
- [ ] 1.2 Ohje text (`help_food_body`): "Kumoa-ilmoitukset näkyvät noin 4 sekuntia (pidempään, jos
  puhelimen esteettömyysasetus pyytää)". Check: `./gradlew test`.

## 2. Weigh-in end view and repeat line (U1)

- [ ] 2.1 `LastWeight` gains `at` and `deleted` (design 2), prefs read old values as before;
  `remember(..., deleted)` from the dialog save/discard, `AutoWeights` decision/delete and the
  Historia delete. Unit tests: the sliding `heardAt` leaves `at` alone; old prefs load.
- [ ] 2.2 `WeighIn`: end reasons `NotHeard | Unsettled | Repeat(last)` and the repeat flag while
  scanning, counting ignored packets (design 1). Unit tests with the fake source and virtual
  time: nothing heard, unstable only, ignored key only, ignored same weight only, ignored then a
  new weighing saves; plus a replay case under `scale-replays/` with made-up bytes.
- [ ] 2.3 `WeighInClose`: `NotFound` never auto-closes; `endReason()` for `weighin.end` (unit
  tests per reason, including `cancelled` from Live and `saved` after auto-close).
- [ ] 2.4 `WeighInDialog`: the end view (reason text, Valmis text button, Punnitse uudelleen filled
  button), the repeat line in place of "Astu vaa'alle" with "tallennettu/poistettu klo H.mm",
  and "Valmis (N)" countdown on the saved result that disappears once the card is tapped
  (design 3). TalkBack: the end reason and the repeat line are a polite live region; Valmis keeps
  the stable name "Valmis" while its visible seconds change. `WeighInActivity`: Punnitse uudelleen restarts the scan and logs
  `weighin.open from=again`; `weighin.end` logged once on close or restart. Smoke test: the
  end view shows two buttons and stays after the time; Punnitse uudelleen shows the countdown.

## 3. Dialog opens on a recent automatic weight (U2)

- [ ] 3.1 `HealthConnectWeights.recentAuto(since)` (own origin, automatically recorded) and
  `WeighInState.Recent`; the view model checks it after the access check, skips "?" weights
  (marks store), falls back to scanning on failure, logs `weighin.recent ok|none|failed ageS= ms=`.
  Pure selection rule (newest, ≤ 10 min, not marked) unit-tested.
- [ ] 3.2 The Recent view ("Tallennettu automaattisesti klo H.mm", change, Poista, "Valmis (N)");
  Poista goes through `AutoWeights.delete` (notification closes, listening restarts) and closes;
  ends log `recent` / `recentdeleted`. Smoke test: a Recent state shows the line and both buttons.

## 4. Notification Poista with Kumoa (U3)

- [ ] 4.1 Drop the OK action and `ACTION_OK` (old PendingIntents of posted notifications just
  close: keep the receiver branch as a no-op cancel). Every notification swipeable.
- [ ] 4.2 Poista paino re-posts "Paino poistettu" silently with Kumoa and `setTimeoutAfter`
  (design 5); the Kumoa intent carries the restore data, read before the raw entry is removed.
  Log `auto.action delete ok ageS=`.
- [ ] 4.3 `AutoWeights.restore(...)`: write back with the same client id, raw entry, mark or
  reference, `weightSaved`, `remember(deleted = false)`, signal, widget redraw, original note
  re-posted silently; failure posts "Painoa ei voitu palauttaa" with Kumoa (no timeout). Logs
  `auto.action undo ok|failed`. Unit tests with fakes: normal and "?" restore, failed restore,
  a second Kumoa tap writes once.

## 5. Widget ✓ (U8)

- [ ] 5.1 `ic_state_done` vector (check glyph) in the dot's colour; `WeighInWidget.views` maps
  `DONE` to it with "tänään punnittu" in the readout; `ListenStatus.symbol()` gives the ✓ for the
  "Tänään punnittu" line. Unit test of the symbol mapping (DONE → check, BLOCKED → triangle, ON
  and RESTING → dot).

## 6. Add search keeps its adds visible (U5, U6)

- [ ] 6.1 Pure `AddedSummary` (design 7): per-ruoka counts and the strip's count, known kcal and
  without-kcal count from `addedIds`. Unit tests: repeats, Kumoa, failed write, Pikalisäys (no
  row, counted in the strip), no kcal, import adds.
- [ ] 6.2 `FoodSearch`: the ✓/×N mark before ⋮ (not clickable, merged into the row's TalkBack
  text), the bottom strip with filled Valmis wired to the back handler (in-app and
  `FoodAddActivity`), the bar above the strip; numbers with non-breaking spaces.
  `food.search.end added= via=back|done ms=` in `leaveSearch()`. Smoke test: after two taps the
  strip shows and Valmis leaves to Ruoka.
- [ ] 6.3 Add bar with the amount (design 8): "Rahka 200 g lisätty", "Pikalisäys 350 kcal
  lisätty", name only without an amount. Unit test of the bar text builder (not exact copy of
  the whole string: that the amount part is present).

## 7. Context, docs and version

- [ ] 7.1 `openspec/context/product.md`: weigh-in decision paragraph (end view with Punnitse
  uudelleen when nothing was saved; the recent automatic weight; dialog Poista still without
  Kumoa), the notification's Poista paino with Kumoa and no OK, the widget ✓, and the
  clarification that the automatic weigh-in notification is the one surface that reports a value
  (design risks). `roadmap.md`: U1–U6 and U8 done as everyday-feedback.
- [ ] 7.2 `.claude/CLAUDE.md` log lines: `weighin.end reason= heard= ignored= ms=`,
  `weighin.open from=again`, `weighin.recent`, `auto.action delete ok ageS=`,
  `auto.action undo ok|failed`, OK gone from `auto.action`, `food.search.end added= via= ms=`;
  map notes (`LastWeight` at/deleted, `AddedSummary`, timeout helper).
- [ ] 7.3 Bump `appVersion` in `gradle.properties` to 1.64 and run
  `./gradlew test lint assembleDebug`.
