# Tasks

## 1. Data model and codecs (pure)

- [ ] 1.1 New `data/` package: `DataSet` (weights), `ExportRow` moved from `history/`, `DataRead`
      result with skipped rows, layout and unknown kinds
- [ ] 1.2 `CsvData.write` (version line, `#table;weights`, today's weight columns) and
      `CsvData.read` (sections; no version line → today's path with a leading `#profile;` line
      dropped); remove `ProfileLine` and `CsvExport` once callers move
- [ ] 1.3 `JsonData.write` / `JsonData.read` (`app`, `version`, `weights` array, `raw` object,
      unknown keys reported, same plausibility rules as table rows)
- [ ] 1.4 Unit tests: CSV layout, JSON item format, CSV↔JSON same data, unknown kind in both
      formats, older export with `#profile;` line, OKOK path unchanged; made-up weights only

## 2. Export, import and backup wiring

- [ ] 2.1 Vie tiedot: format dialog (CSV / JSON), picker MIME and name follow the choice, log
      `format=`
- [ ] 2.2 Tuo tiedot: detect JSON by content; preview note for unknown parts; drop the owner
      details restore and its preview text; log `format=` `unknown=`
- [ ] 2.3 Backup through `CsvData.write`; due check = no success yet today (local day) on app open;
      `WeightBackup` unit tests updated
- [ ] 2.4 Tests: existing import/export/backup tests updated; one smoke test that the format dialog
      opens

## 3. Asetukset names

- [ ] 3.1 Strings: section "Tiedot", "Vie tiedot", "Tuo tiedot", import dialog title, format
      dialog, unknown-parts note

## 4. Docs and release

- [ ] 4.1 `.claude/CLAUDE.md` map (`data/` package, log keys), product.md (one data file in CSV and
      JSON, owner details not carried, backup timing), roadmap backlog entry done
- [ ] 4.2 Bump `appVersion` to 1.8; `./gradlew test lint assembleDebug`
