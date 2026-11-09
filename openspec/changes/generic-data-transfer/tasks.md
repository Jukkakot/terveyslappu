# Tasks

## 1. Data file format (pure)

- [ ] 1.1 New `data/` package: section codec interface, value-section and table-section line
      helpers, version line, `DataFile.write` over a registered codec list (empty kinds left out)
- [ ] 1.2 `profile` codec from `ProfileLine`; `settings` codec (`theme`, `chart_period`, unknown
      names and values ignored); `weights` table writes `CsvExport`'s header and rows
- [ ] 1.3 `DataFile.read`: version line → split into sections, weights table through
      `HistoryParser`, unknown kinds collected; no version line → today's path (optional
      `#profile;` + history table); result `DataBundle`
- [ ] 1.4 Unit tests: layout scenario, empty kinds left out, round trip of all kinds, unknown table
      and unknown setting ignored and reported, older export, file with only settings, made-up
      weights only

## 2. Export, backup, import wiring

- [ ] 2.1 `writeExport` writes `DataFile` with profile, settings and weights; file name
      `terveyslappu-<date>.csv`; log `kinds=`
- [ ] 2.2 Backup requests on owner-detail and settings changes (theme, chart period)
- [ ] 2.3 Import reads `DataBundle`; preview lists restored kinds (owner details, settings) and an
      "unknown parts" note; import allowed with no weights but other data; on confirm restore
      profile (fill empty) and settings (overwrite) after the weights; log `kinds=` `unknown=`
- [ ] 2.4 Tests: view-model/import test for settings restore and only-settings file; existing
      import/export/backup tests updated

## 3. Asetukset names

- [ ] 3.1 Strings: section "Tiedot", "Vie tiedot", "Tuo tiedot", import dialog title, preview
      texts for restored kinds and unknown parts; existing smoke test still passes

## 4. Docs and release

- [ ] 4.1 `.claude/CLAUDE.md` map (`data/` package, log keys), product.md (one data file for all
      kinds, settings carried), roadmap backlog entry done
- [ ] 4.2 Bump `appVersion` to 1.8; `./gradlew test lint assembleDebug`
