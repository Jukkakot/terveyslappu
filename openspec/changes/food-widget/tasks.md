# Tasks

Implementer: an opus agent (device lifecycle that cannot be tested in the cloud).

## 1. Writes and editor flow (app and dialog)

- [ ] 1.1 Move food add, undo, edit and delete to an app-level scope in `TerveysApp` (like the
  auto-save) so closing the sheet never cancels a Health Connect write; emit the food-changed
  signal the app listens to (or reload on resume) and request the backup as for in-app adds.
- [ ] 1.2 Editor return: leaving the editor by Tallenna, Peruuta or Poista reopens the add field
  on the same day (change `openEditor`/`closeEditor`); the Muuta line shows the edited item, none
  after Poista. Applies in the app too; update the existing view model tests.
- [ ] 1.3 Sheet messages: failed write ("Tallennus epäonnistui") and delete Kumoa as a line in the
  Muuta/Kumoa slot; a failure keeps the sheet open; closing drops a pending Kumoa.
- [ ] 1.4 Loading: field and keyboard at once, "Ladataan…" until the day is read, enter/tap waits
  for the load and appends at the end, read failure line and re-read before the append.

## 2. Add sheet outside the main screen

- [ ] 2.1 Make the add sheet and editor usable from a second activity: the import button and the
  clipboard offer's Näytä get a host hook (main screen: as now; dialog: finish and open
  `MainActivity` with `NEW_TASK|CLEAR_TOP|SINGLE_TOP` through `openFrom` on the preview; the app
  closes any open sheet or editor).
- [ ] 2.2 New dialog activity (own task, excluded from recents, transparent like the weigh-in
  dialog), the app's theme, standard scrim, scrim tap closes. Finishes only on an explicit sheet
  dismiss, never when the editor replaces the sheet. Keeps state on rotation and a second tap;
  starts fresh on today after process death; keeps its opening day past midnight.
- [ ] 2.3 Permission and availability states: checking (frame only), needs (explanation + Salli in
  place), denied (button to Health Connect's app permissions, re-check on resume), unavailable
  (text + close).
- [ ] 2.4 Clipboard: read once when the window first gains focus, same offered-timestamp rule,
  line above the suggestions; Näytä uses the import path, Ohita hides the line.
- [ ] 2.5 Logs: `food.sheet.open from=widget|shortcut` (again on a second tap) and `from=` on the
  dialog's `food.add` lines. Timings for NFR-8 (field usable ~300 ms, suggestions ~1 s).
- [ ] 2.6 Smoke test: the dialog opens the add field on today; an added made-up item reaches a fake
  Health Connect foods writer (or the view model's items) and the learnt product is stored. Test
  the editor-return rule and "close at once keeps the write".

## 3. Widget and shortcut

- [ ] 3.1 1x1 widget provider "Lisää annos": Punnitse's round plate and colours (system dark
  mode), new cutlery line icon, label, preview, description string; redrawn only on
  install/update, no widget update from food code, never a state mark.
- [ ] 3.2 Launcher shortcut "Lisää annos" in `res/xml/shortcuts.xml` after Punnitse, `ic_shortcut_*`
  style icon. No Quick Settings tile.
- [ ] 3.3 Manifest entries for the dialog activity and the widget receiver.

## 4. Help, docs, release

- [ ] 4.1 Help page food section names the widget and the shortcut.
- [ ] 4.2 `product.md`: replace "No food widget for now" with the add-only widget decision;
  roadmap: mark food widget done; project CLAUDE.md map: the new activity and widget, and the log
  map: `food.sheet.open from=`, `from=` on `food.add`.
- [ ] 4.3 Bump `appVersion` MINOR; `./gradlew test lint assembleDebug`.
