# Tasks

## 1. Pure core

- [ ] 1.1 Listen window: inside-or-not, next start, next end and next hourly check for a start/end
      pair, over midnight, DST through the phone's zone; reject start == end. Unit tests (incl.
      midnight and DST days).
- [ ] 1.2 Trial status choice in the spec's order (permission, Bluetooth, power saving pause,
      listening, resting until HH.MM, no scale yet) plus the separate battery-restricted warning.
      Unit tests.
- [ ] 1.3 Heard-packet accumulator: address check, dedupe on company id + data, repeat runs with
      rssi min/max, summary counters, stable weights via the existing stable-weight rule,
      stale-window detection. Unit tests with made-up packets (NFR-1).
- [ ] 1.4 Last-heard text ("Ei vielä kuultu", tänään / eilen / date). Unit test.

## 2. Remember the scale

- [ ] 2.1 On a saved dialog weigh-in store the deciding packet's name and address in the `weighin`
      preferences; Poista keeps them. Unit test.

## 3. Background listening

- [ ] 3.1 Trial preferences (own file, excluded from backup and device transfer): switch, start,
      end, mode (default Säästö), power-saving switch (default on), last heard, accumulator state.
- [ ] 3.2 PendingIntent scan start/stop per design 2-3; result receiver feeds the accumulator,
      logs per design 10.
- [ ] 3.3 Alarms per design 4 (start, end, hourly check); end logs the summary and clears;
      settings changes re-plan at once.
- [ ] 3.4 Re-arm on boot, app update and app start when nothing is armed; switch off stops the
      scan, cancels alarms and logs `reason=off`.
- [ ] 3.5 Manifest: receivers, `RECEIVE_BOOT_COMPLETED`; backup exclusion rules. Robolectric test:
      enabling inside the window starts the scan, outside schedules the start.
- [ ] 3.6 `ScaleReplayTest` and the replay notes in `.claude/CLAUDE.md` skip `src=bg` lines.

## 4. Asetukset section

- [ ] 4.1 Section "Automaattinen punnitus (kokeilu)": switch (disabled with the hint before a scale
      is remembered), start/end pickers, mode Säästö/Tasapaino, "Kuuntele myös
      virransäästötilassa", status line with its fix button, battery warning line with its button,
      last-heard line refreshing every minute. Finnish strings, light/dark. One smoke test.

## 5. Finish

- [ ] 5.1 `./gradlew test lint assembleDebug` green.
- [ ] 5.2 Update `.claude/CLAUDE.md` (map: the new code's place; log events), `product.md`
      (decision: PendingIntent scan, listen-only trial), roadmap (phase 1 implemented, waiting for
      the trial's mornings incl. one morning next to the scale); bump `appVersion` MINOR to 1.19.
