# Design

## Context

Export (`history/CsvExport`), backup (`backup/AndroidBackup.writeExport`) and import
(`history/HistoryParser`, the import flow in `ui/MainViewModel`) are written around weights; the
owner details ride along as a `#profile;` first line (`profile/ProfileLine`). The backup runs after
each save/import/delete and when the home screen opens with the last success over 24 hours old
(`backup/WeightBackup`).

## Goals / Non-Goals

**Goals:** one data model, two formats (CSV, JSON); adding a data kind = one new part in the model
and in both codecs; older files and OKOK still import; backup reuses the same writer.

**Non-goals:** other formats; carrying owner details or settings; per-kind import choice;
background (WorkManager) backups.

## Decisions

1. **Data model** (pure, new `data/` package): `DataSet(weights: List<ExportRow>, …)` — one field
   per data kind; `ExportRow` moves here from `history/`. Reading gives `DataRead(set, skipped,
   layout, unknownKinds)` for the preview.
2. **Two codecs over the model**: `CsvData` (version line, `#table;<kind>` sections; the weight
   rows keep today's columns so the existing table reader and older files stay one path) and
   `JsonData` (`org.json`, one array per kind, `raw` as an object). Import picks JSON when the
   first non-blank character is `{`, else CSV/XLS as today.
3. **CSV reading**: version line present → split into sections, the `weights` section goes through
   `HistoryParser` as a table, unknown kinds collected; absent → today's path, with a leading
   `#profile;` line dropped. `ProfileLine` and the profile restore in the import are removed.
4. **JSON weights** run through the same plausibility rules as table rows (range, future, duplicate)
   so the preview's counts mean the same for both formats.
5. **Export format choice**: a small dialog (CSV / JSON) before the save picker; the picker's MIME
   type and suggested name follow the choice.
6. **Backup stays CSV** (the user left it to us): the existing backup file stays valid with no
   migration, and it remains readable in a spreadsheet. Writer shared with export.
7. **Backup timing**: event-driven after changes to carried data (unchanged triggers) + on app open
   when no success yet today (local calendar day) instead of "over 24 hours". The write is a local
   file write through the chosen document provider; cheap enough to run on every change. No
   background job: Samsung's battery rules make those unreliable and the daily check plus the
   stale/failed notice already cover a missed write.
8. **Names and logs**: "Vie tiedot", "Tuo tiedot", section "Tiedot"; `export.write` gains
   `format=`, `import.read` gains `format=json` and `unknown=`.

## Risks / Trade-offs

- Owner details no longer move to a new phone → three fields to type again; accepted by the user.
- An older app version reading a new CSV sees the `#` lines as unreadable rows and skips them;
  harmless.
