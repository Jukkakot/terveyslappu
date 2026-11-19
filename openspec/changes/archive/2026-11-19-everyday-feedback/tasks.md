# Tasks

## 1. Timeouts follow accessibility (U4)

- [x] 1.1 Add the timeout helper (design 6): `ActionTimeouts.millis(base)` installed in
  `TerveysApp`, feeding `showTimed`'s default, bases 4 s bars, 10 s result and "Paino
  poistettu". Unit test: a replaced source reaches `showTimed`'s timer (the wiring, not
  Android's maximum).
- [x] 1.2 Ohje text (`help_food_body`): "Kumoa-ilmoitukset näkyvät noin 4 sekuntia (pidempään, jos
  puhelimen esteettömyysasetus pyytää)". Check: `./gradlew test`.

## 2. Weigh-in end view and repeat line (U1)

- [x] 2.1 `LastWeight` gains `at` and `deleted` (design 2), prefs read old values as before;
  `remember(..., deleted)` from the dialog save/discard, `AutoWeights` decision/delete/restore,
  the Historia delete and Historia's Kumoa. Unit tests: the sliding `heardAt` leaves `at` alone;
  old prefs load.
- [x] 2.2 `WeighIn`: end reasons `NotHeard | Unsettled | Repeat(line)` with `heard` / `ignored`,
  the repeat line on `Scanning` and on `Live` while its stable reading is ignored, brackets only
  for a stable kg within 0.1 kg of `LastWeight` (design 1). Unit tests with the fake source and
  virtual time: nothing heard, unstable only, ignored key only, ignored same weight only, live →
  ignored same stable → timeout = Repeat, ignored then a new weighing saves; plus a replay case
  under `scale-replays/` with made-up bytes.
- [x] 2.3 `WeighInClose`: `NotFound` never auto-closes; `endReason()` for `weighin.end` over every
  `WeighInState` (design 11), unit-tested.
- [x] 2.4 `WeighInDialog`: the end view (reason text, Valmis text button, Punnitse uudelleen filled
  button), the repeat line in place of "Astu vaa'alle" (and under a live weight) with
  "tallennettu/poistettu klo H.mm", and "Valmis (N)" countdown from the lengthened time on the
  saved result that disappears once the card is tapped (design 3). TalkBack: the end reason and
  the repeat line are a polite live region; Valmis keeps the stable name "Valmis" while its
  visible seconds change. `WeighInActivity`: Punnitse uudelleen goes through `prepare()` without
  the recent check and logs `weighin.open from=again`; `weighin.end` logged once, only when
  finishing or before a restart. `WeighInForeground.shown` only while Preparing, Scanning, Live
  or Saving (design 10). Smoke test: the end view shows two buttons and stays after the time;
  the scanning view shows the 30 s count.

## 3. Dialog opens on a recent automatic weight (U2)

- [x] 3.1 `HealthConnectWeights.recent(since)` (own origin) and `WeighInState.Recent`; pure
  `RecentAuto.pick` (newest, ≤ 2 min, client id `terveyslappu-scale-`, raw entry `via=auto`, not
  marked) unit-tested. The view model checks it after the access check under a 1.5 s cap, skips
  without read permission (`weighin.recent none reason=noread`), falls back to scanning on
  failure, logs `weighin.recent ok|none|failed ageS= ms=`.
- [x] 3.2 The Recent view ("Tallennettu automaattisesti klo H.mm", change, Poista, "Valmis (N)");
  Poista goes through `AutoWeights.delete(from = dialog)` (notification closes, listening
  restarts) plus the key touch, then closes; a failed delete stays with the delete-failed text.
  Ends log `recent` / `recentdeleted`. Smoke test: a Recent state shows the line and both buttons.

## 4. Notification Poista with Kumoa (U3)

- [x] 4.1 Drop the OK action and `ACTION_OK` (old PendingIntents of posted notifications just
  close: keep the receiver branch as a no-op cancel). Every notification swipeable.
- [x] 4.2 Poista paino re-posts "Paino poistettu" silently with Kumoa and `setTimeoutAfter`
  (design 5); the summary times out with it when it is the only child; the Kumoa intent carries
  the restore data, read before the raw entry is removed. Log
  `auto.action delete ok from=notification|dialog ageS=`.
- [x] 4.3 `AutoWeights.restore(...)`: write back with the same client id, raw entry, mark or
  reference, `weightSaved`, `remember(deleted = false)`, `flags.backupDue`, signal, widget
  redraw, original note re-posted silently; failure posts "Painoa ei voitu palauttaa" with Kumoa
  (no timeout, not queued). Logs `auto.action undo ok|failed`. Unit tests with fakes: normal and
  "?" restore, failed restore, a second Kumoa tap writes once.

## 5. Widget ✓ (U8)

- [x] 5.1 `ic_state_done` vector (check glyph) in the dot's colour; `WeighInWidget.views` maps
  `DONE` to it, TalkBack "tänään punnittu" or, after midnight / from the stored state,
  "punnittu"; `ListenStatus.symbol()` gives the ✓ for the Done line; `ListenStatusRule` puts
  `Done` before the "Ei toimi" reasons; the Done line reads "Punnittu, seuraavan kerran klo …"
  when the next start is today (design 9). Unit tests: symbol mapping (DONE → check, BLOCKED →
  triangle, ON and RESTING → dot); Done wins over Bluetooth off.

## 6. Add search keeps its adds visible (U5, U6)

- [x] 6.1 Pure `AddedSummary` (design 7): per-ruoka counts (resolved name, `FoodRules` match,
  base alias row) and the strip's count, known kcal and without-kcal count from `addedIds`. Unit
  tests: repeats, Kumoa, failed write, Pikalisäys (no row, counted in the strip), no kcal, import
  adds, alias row.
- [x] 6.2 `FoodSearch`: the ✓/×N mark before ⋮ (not clickable, merged into the row's TalkBack
  text), the bottom strip with filled Valmis wired to the back handler (in-app and
  `FoodAddActivity`), the bar above the strip; numbers with non-breaking spaces.
  `food.search.end added= via=back|done ms=` in `leaveSearch()`. Smoke test: after two taps the
  strip shows and Valmis leaves to Ruoka.
- [x] 6.3 Add bar with the amount (design 8): "Rahka 200 g lisätty", "Pikalisäys 350 kcal
  lisätty", name only without an amount; bulk bar "Lisättiin <N> annosta". Unit test of the bar
  text builder (not exact copy of the whole string: that the amount part is present).

## 7. Context, docs and version

- [x] 7.1 `openspec/context/product.md`: weigh-in decision paragraph (end view with Punnitse
  uudelleen when nothing was saved; the recent automatic weight; dialog Poista after its own save
  still without Kumoa), the notification's Poista paino with Kumoa and no OK, the widget ✓, and
  the values rule narrowed to the home screen (widgets, tile, shortcut labels; notifications may
  show the weight, user 2026-11-19). `roadmap.md`: U1–U6 and U8 done as everyday-feedback.
- [x] 7.2 `.claude/CLAUDE.md` log lines: `weighin.end reason= heard= ignored= ms=`,
  `weighin.open from=again`, `weighin.recent`, `auto.action delete ok from= ageS=`,
  `auto.action undo ok|failed`, OK gone from `auto.action`, `food.search.end added= via= ms=`;
  map notes (`LastWeight` at/deleted, `AddedSummary`, `RecentAuto`, timeout helper,
  `WeighInForeground` only while scanning or saving).
- [x] 7.3 Bump `appVersion` in `gradle.properties` to 1.64 and run
  `./gradlew test lint assembleDebug`.
