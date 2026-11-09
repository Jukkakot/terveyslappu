# Tasks

Small change, no design.md. Decisions: include files by allow-list (anything new stays out until
added on purpose); the backup file's URI stays out because its file permission does not move,
so a restored URI would only show a failing backup; cloud backup requires end-to-end encryption
(`disableIfNoEncryptionCapabilities`); the release key is stable (NFR-4), which a restore needs.

## 1. Backup rules

- [x] 1.1 Manifest: `allowBackup="true"`; keep `dataExtractionRules`
- [x] 1.2 `data_extraction_rules.xml`: `cloud-backup` with `disableIfNoEncryptionCapabilities="true"`
      and `device-transfer`, each including only the shared prefs `profile`, `appearance`, `chart`
      and the file `raw-archive.jsonl`; comment why `backup`, `weighin` and `logs` are left out
- [x] 1.3 Update the "not backed up" comment in `PrefsBackupStore`
- [x] 1.4 Unit test reading the rules file: both sections include exactly the allow-list and never
      `logs`, `backup` or `weighin` (guards against the log or the backup URI slipping in)
- [x] 1.5 `./gradlew test lint assembleDebug`

## 2. Docs

- [x] 2.1 product.md: decision under "Decisions and reasons" (Android carries app data, the backup
      file carries weights); questions.md: fold Q3 if the user has decided; roadmap phase 4 note
- [x] 2.2 Bump MINOR in `gradle.properties`
