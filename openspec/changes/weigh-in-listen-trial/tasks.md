# Tasks

## 1. Pure core

- [ ] 1.1 Listen window: inside-or-not, next start and next end for a start/end pair, over midnight,
      DST through the phone's zone; reject start == end. Unit tests (incl. midnight and DST days).
- [ ] 1.2 Trial status choice from (switch, scale remembered, inside window, Bluetooth on,
      permission, battery optimisation): listening / resting until HH.MM / blocked with reason /
      no scale yet. Unit tests.
- [ ] 1.3 Heard-packet accumulator: dedupe identical data (count only), summary counters (packets,
      distinct, rssi max/min, first/last, stable weights decoded with the existing decoder). Unit
      tests with made-up packets (NFR-1).

## 2. Remember the scale

- [ ] 2.1 On a saved dialog weigh-in store the deciding packet's name and address in the `weighin`
      preferences; Poista keeps them. Unit test.

## 3. Background listening

- [ ] 3.1 Trial preferences: switch, start, end (defaults 06:00/10:00), last-heard time and rssi,
      accumulator state; survives process death.
- [ ] 3.2 PendingIntent scan start/stop with the two filters (address, name), low power, batching
      when supported; result receiver feeds the accumulator and logs per design decision 9 with
      `src=bg screen=`.
- [ ] 3.3 Window alarms (inexact, 15 min window) start/stop the scan and schedule the next;
      the end alarm logs `listen.summary` and clears the accumulator.
- [ ] 3.4 Re-arm on boot and app update; switch off stops the scan and cancels alarms at once.
- [ ] 3.5 Manifest: receivers, `RECEIVE_BOOT_COMPLETED`; no exact-alarm permission. Robolectric
      test that enabling inside the window starts the scan and outside schedules the start.

## 4. Asetukset section

- [ ] 4.1 Section "Automaattinen punnitus (kokeilu)": switch (disabled with the hint before a
      scale is remembered), start/end time pickers, status line with the fix button per blocked
      reason, last-heard line. Finnish strings, light/dark. One smoke test.

## 5. Finish

- [ ] 5.1 `./gradlew test lint assembleDebug` green.
- [ ] 5.2 Update `.claude/CLAUDE.md` (map: the new code's place; log events), `product.md`
      (decision: PendingIntent scan, trial), roadmap (phase 1 implemented, waiting for the
      trial's mornings); bump `appVersion` MINOR to 1.19.
