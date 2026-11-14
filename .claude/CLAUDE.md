# Terveyslappu – working instructions

Android app for effortless weight logging into Health Connect. Product decisions:
`openspec/context/product.md`; requirements: `openspec/context/nfr.md`; plan:
`openspec/context/roadmap.md`; open questions: `openspec/context/questions.md`.
General domain knowledge (weight data, charts, data files) with sources:
`openspec/context/knowledge.md`; never read it whole: find the topic in its index, read that
section before researching, add what new research taught and keep the index in step. Topics
that hold in any project (charts, data files) are in the global bank `~/.claude/knowledge.md`.

## Never commit health data

The repo is public (NFR-1). Logs, exports, screenshots and values the user shares stay in the
session scratchpad. Tests use made-up weights. Check `git diff --cached` before every commit.

## Session start

Read `openspec/context/roadmap.md` and run `openspec list`, then tell the user in two or three
lines where the project stands and the natural next step. Project memory is `.claude/memory/`
(index `MEMORY.md`); write new project memories there.

## Specs first

Read the specs of the parts concerned (`openspec/specs/<capability>/spec.md`) before proposing or
reading code. Every request goes through the matching OpenSpec skill (explore, propose, apply,
archive); a `UserPromptSubmit` hook repeats this.

## Working agreements

- Commit and push to `main` yourself (public GitHub repo `Jukkakot/terveyslappu`).
- The user develops from the phone through cloud sessions and installs builds with Obtainium.
  There is no device in the cloud: compile and unit-test here, the user tests on the phone and
  shares the app log (Quick Share link → `quickshare` skill) when something fails.
- Summaries: what the user will notice and every decision taken on their behalf; no "how to
  check" lists.

## Master session

The global CLAUDE.md's master-session rule applies; worktree checks here are
`./gradlew test lint assembleDebug`.

## Build, release, logs

- Map: `app/src/main/kotlin/fi/jukkakot/terveyslappu/` — `weight/` (input rules, Health Connect
  read/write/delete), `scale/` (scale packet decoding, stable-weight rule, packet log + replay,
  BLE scanner, `WeighIn` flow; all pure except `ScaleScanner`; `LastWeighIn` also remembers the scale for listening; `WeighingKeys` = the atomic weighing-key claim shared by the dialog and the background, `WeighInForeground` flag), `listen/` (automatic weigh-in's listening:
  `ListenWindow`, `ListenStatusRule`, `WidgetStateRule`, `HeardPackets` accumulator and background decision (first stable packet of a company id decides, no quiet time), `LastHeardText`, `ListenController` (all pure); `ListenAndroid` = prefs file `listen` (incl. Hyväksyntäraja and the window-done record `WindowDone`), PendingIntent scan, alarm, device state; `ListenReceiver` (scan results: save in `goAsync()`) / `ListenBootReceiver`; singleton in `TerveysApp`),
  `auto/` (automatic save: `Plausibility` (reference list rule, "?" rule, `AcceptChain`), `AutoText` (notification texts), `AutoStores` (`marks.json`, `reference.json`, `pending-weights.json`, `auto-flags.json` in app storage, never backed up), `AutoWeights` (claim, queue, write within 8 s, accept, delete; Android-free), `AutoAndroid` (Health Connect writer, notifications, `AutoActionReceiver`, `WeightsSignal`); singletons in `TerveysApp`), `food/` (food log: `Product`, `FoodItem`, `Energy`, name matching and suggestions (`FoodRules`), the record id codec `FoodRecordId`, pure edit/import plans `FoodEdits`, the Energia tab's `EnergySeries`, all pure; `ProductStore` = `products.json` (in Android backup), `BaseProducts` loads `assets/base-products.json` written by `scripts/base_products.py`; `AiPaste` = the AI food JSON reader (`assets/ai-food-prompt.txt` is its contract, copied by Kopioi tuontiohje; change both together) and `AiImport` (the append-only plan, product undo via `ProductStore.restore`), share target = `text/plain` `ACTION_SEND` filter on `MainActivity` (`openFrom`), preview `ui/AiImportPreview`; `HealthConnectFoods` = own `NutritionRecord`s; singletons in `TerveysApp`; data-file parts in `data/FoodData`; screens `ui/FoodScreen`, `FoodSheet` (add sheet, item editor), `EnergyPage`, `ProductsPage`, state in `FoodViewModel`), `history/` (history file
  parser: separator, columns, date order, CSV data-file sections; batch writer; `.xls` reader `XlsTable` (Apache POI) and `OkokLayout`; all pure), `data/` (the data file of Vie/Tuo tiedot and the backup: `DataSet` with one field per data kind, `CsvData`, `JsonData`, export file name; pure), `chart/` (periods, period stats, chart axes, Historia list tree `ListGroups`, `HomeSummary` (weight card) and `HomeFood` (food card: today, daily bars, average; `HomeScreen` = weight card (with Punnitse) on top, then the row of smaller cards (food, its own period `foodDays`; steps later takes the right half)); pure except `PeriodPrefs`), `derived/` (BMI, class and tone, normal range,
  `Quantity` for Historia's tabs, the home row's `LatestDerived`; pure), `raw/` (raw-data archive behind each weight: `RawArchive` JSON lines in app storage,
  `RawFormat` for the export's `raw` column; the singleton lives in `TerveysApp`), `profile/` (owner details: rules, `#profile;` export line (pure), `ProfilePrefs` singleton in
  `TerveysApp`), `backup/` (automatic backup file: status, triggers, prefs, SAF
  write; the singleton lives in `TerveysApp`), `ui/` (`RangeCalendarDialog` is Historia's Oma calendar, `MainScreen` switches Home/Historia/Asetukset/Ohje (`HelpScreen`, opened from the home top bar) by `Screen` in `MainViewModel`; Asetukset = list + sub-screens by `SettingsPage` (`SettingsScreen`; row state lines are the pure `SettingsSummary`; `MainUiState.back()` is the one back rule), manual-entry dialog, weigh-in
  dialog, view models, theme + `AppearancePrefs` (singleton in `TerveysApp`), formats; `chart/HomeSummary` is the home card's pure data, `chart/WeightScale` the shared y-scale), `log/` (file log, crash handler, share intent),
  `WeighInActivity` (dialog opened by `WeighInTileService`, `WeighInWidget`, the launcher shortcut
  in `res/xml/shortcuts.xml` and the main screen), `PrivacyActivity` (Health Connect's page).
- Replay a shared weigh-in log: copy its dialog `scale.packet` lines (not `src=bg`, the listen trial's; replay skips those) into
  `app/src/test/resources/scale-replays/<case>.replay`, replace the weight bytes with made-up
  ones (NFR-1), first line `# expect kg=<weight>` or `# expect none`; `ScaleReplayTest` runs all.
- Checks: `./gradlew test lint assembleDebug` (same in CI). The cloud session-start hook installs
  the Android SDK to `~/android-sdk` and writes `local.properties`. Maven Central answers 429 in
  cloud containers; claude-config's `install.sh` routes Gradle and Robolectric (build property
  `robolectricRepoUrl`) through Google's mirror.
- Release: every green code push to `main` publishes GitHub release `v<version>` with
  `terveyslappu.apk` and keeps the five newest (`.github/workflows/ci.yml`); Obtainium shows the tag. Version `MAJOR.MINOR.BUILD` (e.g. 1.0.24):
  MAJOR.MINOR = `appVersion` in `gradle.properties`, BUILD = commits outside the CI-ignored paths
  since the commit that last changed `appVersion` (that commit is `.0`; version-build-reset), version code = all commits. Bump MINOR in the last task of a change the user
  notices, MAJOR when an update breaks something; fixes only raise BUILD. Asetukset shows `v1.0.24`,
  `app.start` logs `version=` and `commit=`.
- Signing (NFR-4): secret `RELEASE_KEYSTORE_BASE64` = a dedicated PKCS12 key made in a cloud
  session (2026-11-08, alias `androiddebugkey`, passwords `android`, the CI defaults, so one secret
  is enough; SHA-256 `04:01:E0:47:...:FB:6B:B5:8A`). The user keeps the base64 text as the backup;
  losing it means one uninstall. Without the secret CI builds but publishes nothing.
- Install/update: Obtainium → Add app → `https://github.com/Jukkakot/terveyslappu`.
- Logs: in the app, Asetukset (gear) → Tuki → Jaa loki (sends `app.log` and `app.log.1`, newest 1 MB); Kopioi loki copies the last 24 h compacted for pasting into chat. Lines are
  `time level event message key=value`; events: `app.start`, `app.crash`, `hc.access`,
  `hc.permission.*`, `weight.save|read|delete`, `log.share`, `log.copy lines= chars=`; weigh-in: `weighin.open from=`,
  `scale.scan start|stop|failed`, `scale.packet t= key= data= rssi= name= addr=` (raw, DEBUG),
  `scale.stable kg= t=`, `weighin.save ms=`, `weighin.total ms=`, `weighin.timeout`, `weighin.discard ok|failed`, `scale.ignore key=` (previous weigh-in's lingering broadcast) / `scale.ignore reason=same kg=` (same kg from the same scale within 30 s of a save or delete, sliding; also `src=bg`), `weighin.raw` / `raw.archive` (archive errors), `tile.click`; listen trial: `listen.enable|disable`, `listen.settings window= mode= powerSave=`, `listen.arm next= action=start|check|end` (`rearm` at app start), `listen.start late= mode= offloadFilter= offloadBatch=`, `listen.stop reason=`, `listen.check bt= powerSave= restricted=`, `listen.failed code=` (-1 no scanner/Bluetooth off, -2 no permission), `scale.packet ... src=bg screen=on|off`, `listen.repeat count= rssiMin= rssiMax=`, `listen.other addr= name= rssi=`, `listen.summary reason=end|off|stale ...`, `listen.stop reason=hc`, `listen.stop reason=done` (the window's weight is saved; `listen.summary reason=done`), automatic save: `scale.stable kg= src=bg`, `auto.save ok kg= check=ok|? ref= late= writeMs= markMs= notifyMs= widgetMs= ms=`, `auto.save failed cause= queued=`, `auto.timeout queued=`, `auto.retry ok|failed count= queued= reason=`, `auto.skip reason=key|range|outside|dialog`, `auto.notify check=` / `auto.notify denied|granted`, `auto.action ok|delete ok|delete failed|confirm ok|confirm failed`, `auto.confirm from=historia|notification chain=`, `widget.state state=on|done|resting|blocked|off`, `backup.due`, `marks.orphans dropped=`; import: `import.pick`, `import.read` (`format=text|json|xls`, `unknown=` data kinds not read, `okok=`, detected layout and
  counts), `import.skip line= reason= raw=` (max 20), `import.write ok|failed`; export: `export.pick format=`, `export.history granted|denied`,
  `export.write ok format= count= history= ms=`; backup: `backup.enable|disable|pick`,
  `backup.run ok reason= count= history= ms=` / `backup.run failed` / `backup.run refused reason=shrink trigger= last=` (history= is false without the history permission); owner details: `profile.set fields=` (field names, never values),
  `profile.height.hc ok|denied|failed`; food log (counts and timings, never names or kcal): `food.add|edit|delete|order ok|failed`, `food.read ok count=`, `hc.permission.nutrition request|granted|denied`, `import.foods written= products= notWritten=`; AI import: `food.import ok|failed from=share|paste count= products= skipped= reason=`, `food.import.save ok count= products=`, `food.import.undo`, `food.prompt.copy chars=`, `food.import.offer shown|open|skip count=` (clipboard line; clip text is read only on Ruoka or Lisää annos, never in the background); chart: `chart.period`, `home.period card=weight|food days=`, `weight.read ok count= history=`; theme: `theme.set`.
