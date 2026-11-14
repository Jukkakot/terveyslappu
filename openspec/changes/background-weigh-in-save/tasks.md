# Tasks

Start only after the phase 1 trial results are in and `weigh-in-listen-trial` is archived.

## 1. Pure core

- [ ] 1.1 Plausibility: reference choice (latest own accepted weight before the new one, not "?"),
      the unknown state (mark) and the mark decision (strictly over the limit, none with a known
      empty reference). Unit tests per the spec's scenarios (within, exactly, over, earlier "?"
      skipped, first weight, unknown).
- [ ] 1.2 Acceptance chain (design 7): pure function from "?" weights in time order, anchor and
      limit to the weights to accept; stops at the first outside the limit. Unit tests (chain, stop,
      single, none).
- [ ] 1.3 Decision timing for background packets (design 2) with the shared `StableWeight` fed with
      packet timestamps: the dialog and the background decide the same kg and the deciding packet's
      time is saved; two equal stable packets, a lone stable packet decided at the next event,
      range 20-300 kg. Unit tests with made-up packets (NFR-1).
- [ ] 1.4 Widget state (design 10) as a mapping of the extended status (Health Connect permission,
      Tauolla, Pysäytetty, warnings never Estetty, Lepää outside the window). Unit tests.
- [ ] 1.5 Notification texts (normal and "?", no reference, late save with time, change from the
      reference, Finnish number format). Unit test.

## 2. Storage

- [ ] 2.1 Marks store keyed by `clientRecordId` (design 5): add, remove, removed with the weight,
      restored by undo, orphans dropped by a foreground read, two weights in the same second. Unit
      test.
- [ ] 2.2 Reference store (design 4): latest ~10 accepted own pairs plus the unknown state; updated
      by save, delete, undo, edit, import and acceptance, reference recomputed from the pairs; a
      foreground read replaces the list and seeds it. Unit test.
- [ ] 2.3 Pending queue (design 3, also fed by the time budget) with its count, and the "backup due"
      flag (design 9). Unit test.
- [ ] 2.4 Shared weighing-key store with an atomic claim, used by the dialog and the background,
      plus the dialog-in-foreground flag (design 6); the dialog's existing tests stay green and new
      tests cover "saved in the background first" and two simultaneous claimers.
- [ ] 2.5 Settings: limit (0.5-20 kg, 0.5 steps, default 5), migration from the trial prefs keeps
      switch, window and mode. Unit test.
- [ ] 2.6 Weights-changed signal (design 12) and the main view model collecting it and reloading on
      resume. Unit test.

## 3. Background save

- [ ] 3.1 Scan receiver saves a decided weight with `goAsync()` (design 1): claim the key, Health
      Connect write, raw entry with `via=auto`, mark, reference, backup-due flag, signal, widget
      update; no save while the dialog is in the foreground. Robolectric test with a fake weight
      store: a stable packet pair saves once.
- [ ] 3.2 Time budget (design 11): time the block, log `ms=` per step, give up at about 8 s and
      queue. Test with a fake slow store.
- [ ] 3.3 Retry the pending queue at the next weigh-in, the hourly check and at the start of the
      first foreground read, before the list is shown. Test: a failed write is retried and saved
      once, and the list includes it.
- [ ] 3.4 Health Connect write permission in the status's blocked reasons; no scan without it.
      Test via 1.4.
- [ ] 3.5 Log events per design 15, incl. the outcome and cause of every background write.

## 4. Notifications

- [ ] 4.1 Manifest `POST_NOTIFICATIONS`; silent and default-importance channels under one group
      (design 8); post after every automatic save (late saves with the weighing time); none times
      out; tap opens Historia. Robolectric test: a "?" save posts a notification with two actions
      and no timeout.
- [ ] 4.2 Action receiver: Poista deletes the weight and raw data, Hyväksy accepts with the chain;
      closes the notification, re-posts on a failed delete. Robolectric test for both actions.
- [ ] 4.3 Permission request when the switch is turned on; "Ilmoitukset estetty" warning line with a
      button to the notification settings; the weight is still saved when denied.

## 5. Screens and widget

- [ ] 5.1 Asetukset: section renamed, the limit setting, the new status (Tauolla, Pysäytetty) and
      warning lines, "N punnitusta odottaa tallennusta". Finnish strings, light/dark. Smoke test.
- [ ] 5.2 Historia: "?" row as an amber-tinted outlined card (theme tokens, readable in light and
      dark; "?" sign and "tarkista" too), "?" sign and "tarkista", listed whatever the period filter, no change on the row
      and the next change skips it, tap opens Hyväksy / Poista / Peruuta (chained acceptance), undo
      restores the mark, group rows skip "?" for first/last; accepting or deleting cancels the
      notification. Unit test for the list data, one smoke test.
- [ ] 5.3 Chart, period statistics, weight scale, calendar days and the home card leave "?" weights
      out (only marks that match a read weight); home notice "Punnitus odottaa tarkistusta" (same amber tint as the row) opens
      Historia scrolled to the newest "?" weight. Unit tests on the pure data (chart input,
      `HomeSummary`).
- [ ] 5.4 Widget: status symbol and content description; stored window end so a passed window shows
      Lepää at the next redraw; updates per design 10 (alarms, boot, package replaced, app resume,
      settings change, save, action). Robolectric test: Lepää outside the window, nothing with the
      feature off, warnings do not give Estetty.

## 6. Data file

- [ ] 6.1 CSV `check` column (last, header `date;time;weight_kg;source;raw;check`, time with offset
      as in data-robustness-fixes) and JSON `check` field in export and backup; read back on import
      (`?` marks, anything else no mark); files without it still import. Round-trip unit tests in
      both formats, and a test that the current `HistoryParser` reads a file with the extra `check`
      column.
- [ ] 6.2 Backup runs on app start when the backup-due flag is set. Unit test on the trigger rule.

## 7. Finish

- [ ] 7.1 `./gradlew test lint assembleDebug` green.
- [ ] 7.2 Update `.claude/CLAUDE.md` (map: new code's place; log events), `product.md` (decisions:
      save in the receiver, no background reads, "?" in own storage), `knowledge.md` Android section
      (Health Connect background rules and widget-update limits from design.md), roadmap (phase 2
      done); bump `appVersion` MINOR.
