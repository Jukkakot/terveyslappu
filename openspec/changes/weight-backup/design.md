# Design

## Context

weight-export already reads every weight (`HealthConnectWeights.all`), formats CSV
(`history/CsvExport`) and handles the history permission. This change runs the same export to one
persisted document URI automatically. Planned in autopilot (user 2026-11-08, "plan and implement
what we talked about"); choices below were made on the user's behalf.

## Goals / Non-Goals

**Goals:** a backup that runs by itself and whose failure the user sees on the main screen.

**Non-Goals:** background scheduling when the app is not used (WorkManager), two-way sync,
versioned backups, encryption (the file lives in the user's own cloud, NFR-2).

## Decisions

1. **One file, rewritten whole** (`"wt"` mode). Simple, always a complete export; a few thousand
   rows is ~100 KB. Alternative: append rows → breaks on deletes and edits.
2. **Persisted SAF URI** from `CreateDocument` with `takePersistableUriPermission(READ|WRITE)`;
   released on turn-off. Works with Google Drive, local storage and others the picker offers.
3. **Triggers without a scheduler:** after the app's own changes, and on main-screen resume when
   the last success is older than 24 h. No WorkManager: the app is opened often enough (every
   weigh-in) and a missed day is visible in the status. Weigh-ins started from the tile run the
   backup in the app's process scope so closing the dialog does not cancel it.
4. **`backup/WeightBackup` singleton in `TerveysApp`**: holds a `StateFlow<BackupStatus>` loaded
   from SharedPreferences, a `Mutex` so runs never overlap (a trigger during a run marks one
   more run), and `run(reason)`. Pure parts in `backup/BackupStatus` (`isDue`, `isStale`).
5. **Status card** above the weight list, one line plus one button; colours from the theme
   (`errorContainer` for failure/stale), so light and dark both work.
6. **Log:** `backup.run reason= ok count= ms=`, `backup.run failed`, `backup.enable`,
   `backup.disable`.

## Risks / Trade-offs

- [Drive provider offline] → the write goes to Drive's local cache and syncs later; a provider
  error shows as a failure with retry.
- [History permission denied] → backup holds only what can be read; the status says so
  (count is visibly small) and the export's note applies.
- [Old file deleted by the user] → failure status; "Vaihda tiedosto" picks a new one.
