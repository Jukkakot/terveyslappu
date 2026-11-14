# Tasks

Start only after the phase 1 trial results are in and `weigh-in-listen-trial` is archived.

## 1. Pure core

- [ ] 1.1 Plausibility: reference choice (latest own weight before the new one, not "?") and the
      mark decision (strictly over the limit, none without a reference). Unit tests per the spec's
      scenarios (within, exactly, over, earlier "?" skipped, first weight).
- [ ] 1.2 Decision timing for background packets (design 2): two equal stable packets, a lone
      stable packet decided at the next event, range 20-300 kg, the shared lingering-key rule.
      Unit tests with made-up packets (NFR-1).
- [ ] 1.3 Widget state (design 9) and the extended status order (Health Connect permission,
      notifications warning). Unit tests.
- [ ] 1.4 Notification texts (normal and "?", change from the reference, Finnish number format).
      Unit test.

## 2. Storage

- [ ] 2.1 Marks store (design 5): add, remove, removed with the weight, restored by undo. Unit test.
- [ ] 2.2 Reference store (design 4): updated by save, delete, undo, import, confirm; recomputed
      from a foreground read. Unit test.
- [ ] 2.3 Pending queue (design 3) and the "backup due" flag (design 8). Unit test.
- [ ] 2.4 Shared lingering-key store used by the dialog and the background (design 6); the dialog's
      existing tests stay green and a new test covers "saved in the background first".
- [ ] 2.5 Settings: limit (0.5-20 kg, 0.5 steps, default 5), migration from the trial prefs keeps
      switch, window and mode. Unit test.

## 3. Background save

- [ ] 3.1 Scan receiver saves a decided weight with `goAsync()` (design 1): Health Connect write,
      raw entry with `via=auto`, mark, reference, lingering key, backup-due flag, widget update.
      Robolectric test with a fake weight store: a stable packet pair saves once.
- [ ] 3.2 Retry the pending queue at the next weigh-in, the hourly check and app start. Test: a
      failed write is retried and saved once.
- [ ] 3.3 Health Connect write permission in the status's blocked reasons; no scan without it.
      Test via 1.3.
- [ ] 3.4 Log events per design 10, incl. the outcome and cause of every background write.

## 4. Notifications

- [ ] 4.1 Manifest `POST_NOTIFICATIONS`; two channels (design 7); post after every automatic save;
      normal ones silent with a one-hour timeout; tap opens Historia. Robolectric test: a "?" save
      posts a notification with two actions.
- [ ] 4.2 Action receiver: Poista deletes the weight and raw data, Hyväksy removes the mark; closes
      the notification, re-posts on a failed delete. Robolectric test for both actions.
- [ ] 4.3 Permission request when the switch is turned on; "Ilmoitukset estetty" warning line with a
      button to the notification settings.

## 5. Screens and widget

- [ ] 5.1 Asetukset: section renamed, the limit setting, the new status and warning lines. Finnish
      strings, light/dark. Smoke test.
- [ ] 5.2 Historia: "?" sign and "tarkista", no change on the row and the next change skips it, tap
      opens Hyväksy / Poista / Peruuta, undo restores the mark, group rows skip "?" for first/last;
      confirming or deleting cancels the notification. Unit test for the list data, one smoke test.
- [ ] 5.3 Chart, period statistics, weight scale, calendar days and the home card leave "?" weights
      out; home notice "Punnitus odottaa tarkistusta". Unit tests on the pure data (chart input,
      `HomeSummary`).
- [ ] 5.4 Widget: status symbol and content description; updates per design (alarms, boot, package
      replaced, app resume, settings change, save, action). Robolectric test: the widget shows
      Lepää outside the window and nothing with the feature off.

## 6. Data file

- [ ] 6.1 CSV `check` column and JSON `check` field in export and backup; read back on import
      (`?` marks, anything else no mark); older files without it still import. Round-trip unit tests
      in both formats; check that `HistoryParser` ignores the extra column in older layouts.
- [ ] 6.2 Backup runs on app start when the backup-due flag is set. Unit test on the trigger rule.

## 7. Finish

- [ ] 7.1 `./gradlew test lint assembleDebug` green.
- [ ] 7.2 Update `.claude/CLAUDE.md` (map: new code's place; log events), `product.md` (decisions:
      save in the receiver, no background reads, "?" in own storage), `knowledge.md` Android section
      (Health Connect background rules and widget-update limits from design.md), roadmap (phase 2
      done); bump `appVersion` MINOR.
