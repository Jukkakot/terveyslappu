# Design: settings-list

Decisions from the spec review (2026-11-14), taken on the user's behalf; the user asked to
implement right away.

## Navigation

1. `SettingsPage { LIST, PROFILE, LISTEN, DATA, APPEARANCE, SUPPORT }` and
   `settingsOrigin: Screen` (HOME, HISTORY or SETTINGS = the list) live in `MainUiState` (the
   ViewModel), so rotation and returning from a system screen (file picker, notification settings,
   permission dialog) keep the sub-screen. Process death returns HOME, as today.
2. One pure `back(state)` rule, used by both the top-bar arrow and system back:
   LIST → HOME; a sub-screen opened from the list → LIST; a deep-linked sub-screen → its origin;
   HELP → HOME; HISTORY → HOME. A deep-linked sub-screen has no route to the list (accepted).
3. Links: gear → LIST; BMI reason on home → PROFILE (origin HOME); BMI reason in Historia →
   PROFILE (origin HISTORY; back returns to Historia, its page and scroll may reset); backup
   notice → DATA (origin HOME). `profileFocus` goes away.
4. DATA without Health Connect access (e.g. revoked while open) falls back to LIST.

## Rows (order fixed; food-log's Ruokakirjaus goes before Ulkoasu later)

Each row: icon, name, state line, chevron (decorative), one click target; screen reader text =
name + state line; sub-screen title = row name. A warning state line uses the error colour (as the
warnings inside the sub-screens do today) and starts with a warning icon, so not by colour alone.

- **Omat tiedot** (`ic_person` or nearest existing): set fields in the order height · birth year ·
  gender, e.g. "180 cm · 1990 · Mies" or "180 cm · Mies"; Ei valittu omitted; no age; a height read
  from Health Connect counts as set; nothing set → "Ei asetettu" (not a warning).
- **Automaattinen punnitus**: off → "Pois" (no symbol). On, precedence: weighings waiting
  "N punnitusta odottaa tallennusta" (warning) > blocked status's existing text (warning, incl.
  Health Connect missing) > battery restricted (warning) > notifications denied (warning) >
  "Päällä 6.00–10.00" with the widget state symbol first.
- **Tiedot ja varmuuskopio** (only with Health Connect access; four rows without): reuse the
  backup group's detail texts: off "Varmuuskopio pois" (not a warning), running, ok with time,
  never run, failed/refused (warning), stale (warning); `recentOnly` not on the row.
- **Ulkoasu**: "Teema: Järjestelmä | Vaalea | Tumma".
- **Tuki**: "Loki · v<full version>" (the row satisfies the version scenarios).

## Refresh and hosts

- The existing one-minute tick (`listenTick`) drives the list and the sub-screens; the backup row is
  computed against now on each tick.
- Each sub-screen has its own Scaffold with the shared `SnackbarHost`; the export format dialog
  lives in DATA; ImportDialog and the permission launchers stay where they are.
- Existing test tags (import, export, backupStatus, listenSwitch, shareLog, copyLog, …) keep their
  names inside the sub-screens; "contents stay as they were" is checked by those tests.

## Ohje

Help (?) button left of the gear in the home top bar, in every Health Connect state,
contentDescription "Ohje". Help texts saying "Asetuksissa" stay as they are. Other main specs that
say "in Asetukset" mean its sub-screen; no wording change needed there.

## Implementation notes

- Back arrow and system back both call `MainActions.onBack` (`MainUiState.back()`); the sub-screens
  of DATA without Health Connect fall back to the list on screen only (state keeps DATA, back follows
  its origin).
- Sub-screens keep today's section headers under the top bar title, so the name shows twice (kept
  "unchanged"; trivial to drop later).
- Row icons: `ic_person` and `ic_palette` added; Automaattinen punnitus uses `ic_scale`, Tiedot ja
  varmuuskopio `ic_backup`, Tuki `ic_file`.
- A running backup shows "Varmuuskopioidaan…" on its row; a refused backup uses the refused text.
- Listen row with no status yet (null) and no warnings shows "Päällä H.mm–H.mm" without a symbol.
- Row state lines are the pure `SettingsSummary` (profile line as a string, listen and backup as
  `ListenLine`/`BackupLine` mapped to text in `SettingsScreen`).
