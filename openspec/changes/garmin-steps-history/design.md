# Design

Decisions from the spec review (master, 2026-11-15); the implementer follows them as written.

1. **Partial days:** for a day both Health Connect (the source in use) and the import have, the
   larger count wins. Both are Garmin's own count (or, under Kaikki, the merge), so max is safe
   and repairs link days and sync gaps.
2. **Which source uses the import:** decided on the resolved source the reader actually uses:
   Garmin Connect's package set (chosen, or Automaattinen's current or stored pick), or no origin
   filter at all (Kaikki, and Automaattinen's fallback with no pick). Any other app: ignored. An
   automatic switch away from Garmin hides the import; Ohje says so in one sentence.
3. **No steps permission / Health Connect unavailable:** the card and Askeleet stay as today; only
   the Asetukset row shows the import.
4. **Merge point:** after the Health Connect reader, in the controller state (`StepsUiState.days`).
   The Health Connect read start is unchanged (Health Connect's first readable day); the merged
   list's first day sets the Kaikki start through `StepsSeries.of`. Own charts read the same
   state, so they get the import too.
5. **History notice:** unchanged (the shared Historia notice); the delta's notice sentence is void.
6. **Failures:** a failed Health Connect read keeps today's failure state (the import is only
   merged into a successful read); stale keeps the last merged numbers; an import, delete or undo
   re-merges in memory onto the last read, no new Health Connect read.
7. **Import dialog:** a separate `ImportState.Garmin(preview)` in the same dialog: Luetaan… (with
   Peruuta, cancels) → preview (title, range with years, day count, Peruuta/Tuo). Tuo saves at
   once, closes the dialog and shows the bar; nothing of the weight path runs (no marks, foods,
   load, `import.write`). This preview replaces the weight preview's rows and skip counts.
   Log `import.pick` as today plus the `steps.garmin.*` lines; `entries=` = UDS files read.
8. **Large ZIPs:** a ZIP is detected by its first bytes `PK\x03\x04`, then the URI is reopened
   and streamed with `ZipInputStream`; non-matching entries (nested zips, FIT files) are skipped
   without buffering. No size cap for ZIPs; the 10 MB cap stays for the other formats. A corrupt
   ZIP gives the existing read-failed message and `steps.garmin.read failed cause=`. `.xlsx` is a
   ZIP too and now gets "Tiedostosta ei löytynyt Garminin askelia" (accepted).
9. **Dates:** `calendarDate` is taken as a `LocalDate`, no zone conversion. Only days before the
   import day are stored (today's partial day is never stored).
10. **Backup:** `garmin-steps.json` joins `data_extraction_rules.xml` (cloud backup and device
    transfer sections) and `DataTransferRulesTest`; no legacy rules exist.
11. **Replace and undo:** Tuo replaces without undo; Poista's Kumoa restores from memory (lost on
    process death, accepted). When the resolved source does not use the import, the saved message
    adds "· näkyy, kun lähde on Garmin Connect tai Kaikki".
12. `steps.read days=` keeps counting Health Connect days only.
