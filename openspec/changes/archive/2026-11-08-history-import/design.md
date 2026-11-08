# Design

## Context

Health Connect access and the main screen exist (`weight/HealthConnectWeights`, `ui/MainScreen`).
The OKOK export format is unknown (Q2; no public documentation found), so the parser must be
general and its guesses visible. Planned in autopilot (user 2026-11-08); every choice below was
made on the user's behalf.

## Goals / Non-Goals

**Goals:** one file → preview → confirm → all weights in Health Connect, safely re-runnable.

**Non-Goals:** Excel/xlsx, body composition columns (fat %, BMI are ignored), choosing columns by
hand (added only if a real file needs it), reading other apps' weights back for dedupe.

## Decisions

1. **Pure parser `history/HistoryParser`** (text in, `ParsedHistory` out: weights, skipped
   reasons, detected layout). Detection is a scoring rule over the whole file, not a per-format
   check (memory: general fixes only):
   - Separator: the one of `;`, tab, `,` that gives the same column count ≥2 on most lines
     (quote-aware split, so `"82,4"` stays one field).
   - Header: first line whose fields are mostly non-numeric. Header keywords pick columns:
     date/päivä/日期/time-stamp, time/aika/时间, weight/paino/体重/kg/lb.
   - Without a header (or keywords missing): the column where most values parse as dates is the
     date; the column where most values parse as numbers in 20–300 (kg) is the weight; when
     several numeric columns qualify, the leftmost after the date wins.
   - Date order for slash dates is decided for the whole file: day first unless a first number
     above 12 never appears but a second one does (then month first).
2. **Time zone:** local wall time in the phone's current zone (the export has no zone).
3. **Deterministic ids:** `terveyslappu-import-<epoch second>`. Re-import upserts instead of
   duplicating (the openScale-sync lesson). Within the file the same second keeps the last row.
4. **Metadata:** `autoRecorded(Device(type = SCALE))`: the values came from a scale.
5. **Batches of 200** records per `insertRecords`, progress after each; a failed batch stops,
   the saved count is reported and retry restarts from the first unsaved batch (safe because of
   the ids).
6. **UI:** menu item → `ActivityResultContracts.OpenDocument` (`text/*`, `application/csv`,
   `text/comma-separated-values`, `application/octet-stream` as some file managers label CSV so)
   → read on IO → preview `AlertDialog` → progress in the same dialog → snackbar with the result.
   State lives in `MainViewModel` (one more sub-state), so rotation keeps it.
7. **Log:** `import.read header= sep= dateCol= timeCol= weightCol= unit= order= rows= ok=
   skipped=<reason:count>`, `import.skip line=<n> reason= raw=<line>` (max 20),
   `import.write ok count= ms=` / `import.write failed saved=`.

## Risks / Trade-offs

- [The OKOK export is xlsx or something odd] → the log shows the header; a follow-up supports it.
- [Wrong day/month guess on an ambiguous file] → preview shows first and last dates.
- [Health Connect rate limits on ~1500 records] → batches of 200 are well inside the limits; a
  failure is retryable.
- [Old weights only visible in the list for 30 days] → the result message says how many were
  imported; Samsung Health shows the history.
