# Tasks

## 1. Archive

- [ ] 1.1 Raw-data archive in app storage: entries keyed by the weight's time (second), each an
  ordered map of named string fields plus a source tag (`scale`, `okok`, ...); add, remove, lookup,
  all. Use a library for storage/serialisation where it fits (e.g. kotlinx.serialization JSON lines
  or DataStore), not a hand-written format. Verify: unit tests (add, remove, read back).
- [ ] 1.2 The stable decision carries the deciding packet; the scale decoder exposes the raw fields
  as sent (`key`, `weight_raw`, `decimals`, `unit`, `stable`, `impedance`, `product_id`, `attrib`,
  `mac`, `extra`) next to the converted kg; the packet log line gets `addr=` (optional when
  replaying old logs). Verify: `ScalePacketTest` checks the raw fields on made-up packets,
  `PacketLogTest` reads lines with and without `addr`.
- [ ] 1.3 Weigh-in save adds the entry; redo removes it; undo adds it back; deleting a weight from
  the list removes its entry and Kumoa restores it. Verify: `WeighInTest` cases with a fake archive.

## 2. Export, backup, import

- [ ] 2.1 Export/backup writer adds the `raw` column (`name=value|...`, escaped); the import parser
  reads it when present and refills the archive. Verify: `CsvExportTest` row formats,
  `HistoryParserTest` read-back with and without the column (made-up values).

## 3. Context

- [ ] 3.1 product.md: "raw data kept, only weight shown, calculations later from raw data" decision;
  `.claude/CLAUDE.md` map (where the archive lives). Verify: `./gradlew test lint assembleDebug`
  passes and the files read correctly.
