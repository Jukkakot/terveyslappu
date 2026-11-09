# Terveyslappu – working instructions

Android app for effortless weight logging into Health Connect. Product decisions:
`openspec/context/product.md`; requirements: `openspec/context/nfr.md`; plan:
`openspec/context/roadmap.md`; open questions: `openspec/context/questions.md`.

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

## Build, release, logs

- Map: `app/src/main/kotlin/fi/jukkakot/terveyslappu/` — `weight/` (input rules, Health Connect
  read/write/delete), `scale/` (scale packet decoding, stable-weight rule, packet log + replay,
  BLE scanner, `WeighIn` flow; all pure except `ScaleScanner`), `history/` (history file
  parser: separator, columns, date order; batch writer; CSV export format; all pure), `backup/` (automatic backup file: status, triggers, prefs, SAF
  write; the singleton lives in `TerveysApp`), `ui/` (main screen, weigh-in
  dialog, view models, theme, formats), `log/` (file log, crash handler, share intent),
  `WeighInActivity` (dialog opened by `WeighInTileService`, `WeighInWidget`, the launcher shortcut
  in `res/xml/shortcuts.xml` and the main screen), `PrivacyActivity` (Health Connect's page).
- Replay a shared weigh-in log: copy its `scale.packet` lines into
  `app/src/test/resources/scale-replays/<case>.replay`, replace the weight bytes with made-up
  ones (NFR-1), first line `# expect kg=<weight>` or `# expect none`; `ScaleReplayTest` runs all.
- Checks: `./gradlew test lint assembleDebug` (same in CI). The cloud session-start hook installs
  the Android SDK to `~/android-sdk` and writes `local.properties`. Maven Central answers 429 in
  cloud containers; claude-config's `install.sh` routes Gradle and Robolectric (build property
  `robolectricRepoUrl`) through Google's mirror.
- Release: every green code push to `main` replaces the GitHub release `latest-build` with
  `terveyslappu.apk` (`.github/workflows/ci.yml`). Version `MAJOR.MINOR.BUILD` (e.g. 1.0.24):
  MAJOR.MINOR = `appVersion` in `gradle.properties`, BUILD = commits outside the CI-ignored paths
  (never resets), version code = all commits. Bump MINOR in the last task of a change the user
  notices, MAJOR when an update breaks something; fixes only raise BUILD. Menu shows `v1.0.24`,
  `app.start` logs `version=` and `commit=`.
- Signing (NFR-4): secret `RELEASE_KEYSTORE_BASE64` = a dedicated PKCS12 key made in a cloud
  session (2026-11-08, alias `androiddebugkey`, passwords `android`, the CI defaults, so one secret
  is enough; SHA-256 `04:01:E0:47:...:FB:6B:B5:8A`). The user keeps the base64 text as the backup;
  losing it means one uninstall. Without the secret CI builds but publishes nothing.
- Install/update: Obtainium → Add app → `https://github.com/Jukkakot/terveyslappu`.
- Logs: in the app, menu ⋮ → Jaa loki (sends `app.log` and `app.log.1`, newest 1 MB). Lines are
  `time level event message key=value`; events: `app.start`, `app.crash`, `hc.access`,
  `hc.permission.*`, `weight.save|read|delete`, `log.share`; weigh-in: `weighin.open from=`,
  `scale.scan start|stop|failed`, `scale.packet t= key= data= rssi= name=` (raw, DEBUG),
  `scale.stable kg= t=`, `weighin.save ms=`, `weighin.total ms=`, `weighin.timeout`, `weighin.redo ok|failed`, `weighin.undo`, `tile.click`; import: `import.pick`, `import.read` (detected layout and
  counts), `import.skip line= reason= raw=` (max 20), `import.write ok|failed`; export: `export.pick`, `export.history granted|denied`,
  `export.write ok count= history= ms=`; backup: `backup.enable|disable|pick`,
  `backup.run reason= ok count= ms=` / `backup.run failed`.
