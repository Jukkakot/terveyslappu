# Design

## Context

Export (`history/CsvExport`), backup (`backup/AndroidBackup.writeExport`) and import
(`history/HistoryParser`, `ui/MainViewModel` import flow) are written around weights; the owner
details ride along as a special first line (`profile/ProfileLine`). Adding the next data kind the
same way means touching all three paths again. Settings live in `AppearancePrefs` (theme) and
`PeriodPrefs` (chart period).

## Goals / Non-Goals

**Goals:** one file format where a data kind is a section; adding a kind = one new section
definition plus registering it; older files and OKOK still import; the backup follows for free.

**Non-goals:** choosing per kind what to import (all known kinds are restored); JSON or zip
formats; carrying device-bound state; merging settings (the file's values win).

## Decisions

1. **Keep a semicolon CSV with `#` section lines** rather than JSON. The user keeps the export
   readable in a spreadsheet (product decision "Readable CSV export"), the weight table stays as it
   is, and older app versions still find the weights (their parser skips the `#` lines it does not
   know only partly; acceptable, old versions are not in use). JSON was the alternative: more
   generic, but unreadable in a spreadsheet and a second weight format to keep.
2. **Two section shapes**: value section `#<kind>;a=1|b=2` (one line) and table section
   `#table;<kind>` + header + rows. Covers single values (profile, settings, later goal weight) and
   lists (weights, later steps). A version line `#terveyslappu;version=2` marks the new format.
3. **Section codec per kind** (pure, in a new `data/` package): an interface with `kind`, `encode`
   (to lines) and `decode` (from a value map or a table's lines) returning a typed value; the
   `DataFile` writer/reader iterates over a registered list. Weights keep their existing row
   writer and the existing `HistoryParser` table reader, so OKOK and other histories keep one
   parser. `ProfileLine` becomes the `profile` codec.
4. **Reading**: if the first non-blank line is the version line, split the file into sections;
   unknown kinds are collected into `unknownKinds`; else fall back to today's path (optional
   `#profile;` line + history table). The result is a `DataBundle` (weights parse result, profile,
   settings, unknown kinds) that the preview and the import use.
5. **Restoring**: profile keeps "fill only empty" (owner-profile decision); settings overwrite,
   since they always have a value and the user chose the file. Done after the weights are written,
   so a cancelled import changes nothing.
6. **Backup triggers** gain `profile` and `settings` reasons (debounced by the existing request).
7. **Names**: "Vie tiedot", "Tuo tiedot", section "Tiedot", file `terveyslappu-<date>.csv`. Log
   events stay (`import.*`, `export.*`), with `kinds=` listing the sections written/read and
   `unknown=` for ignored ones.

## Risks / Trade-offs

- A settings value restored from an old file can surprise (e.g. theme flips) → the preview lists
  "Asetukset" as restored.
- An older app version reading a new file sees the `#` lines as unreadable rows and skips them;
  harmless.

## Open Questions

None blocking; see the proposal's questions to the user (settings carried, settings overwrite).
