# Tasks

## 1. Store

- [ ] 1.1 The stable decision carries the deciding packet's raw bytes, impedance and unit; a small
  store in app storage (one line per reading, keyed by the weight's second like the scale client id)
  with add, remove and lookup. Verify: unit tests (add, remove, read back, empty impedance).
- [ ] 1.2 Weigh-in save adds the reading; redo removes it; undo adds it back; deleting a scale weight
  from the list removes it and Kumoa restores it. Verify: `WeighInTest` cases with a fake store.

## 2. Export, backup, import

- [ ] 2.1 Export/backup writer adds `impedance_ohm;packet_hex` columns from the store; the import
  parser reads them when present and refills the store. Verify: `CsvExportTest` row formats,
  `HistoryParserTest` read-back with and without the new columns (made-up values).

## 3. Context

- [ ] 3.1 product.md (scale readings kept, why), `.claude/CLAUDE.md` map (where the store lives).
  Verify: `./gradlew test lint assembleDebug` passes and the files read correctly.
