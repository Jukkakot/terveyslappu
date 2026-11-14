# Design

## Context

The add sheet lives in `FoodSheet`/`FoodViewModel` and is shown by the main screen. The widget
needs it over the home screen without the app behind it, like the weigh-in dialog
(`WeighInActivity`, own task, transparent). Device lifecycle (focus, task flags, process death)
cannot be tested in the cloud, so the implementer should be an opus agent.

## Decisions

### Surfaces (user)
1. Widget: 1x1 "Lisää annos" button opening the add sheet. No food on the home screen. Same round
   plate and colours as Punnitse (follows system dark mode), new cutlery line icon, never a state
   mark; redrawn only on install/update. "Widget never changes" is checked as "no widget update
   from food code".
2. Launcher shortcut "Lisää annos" after Punnitse, icon in the existing `ic_shortcut_*` style.
3. No Quick Settings tile for food.

### Editor return (user)
4. After the item editor (Muuta), on all three exits (Tallenna, Peruuta, Poista) the user returns
   to the open add field, everywhere: the widget's sheet and the app (front page, Ruoka). The
   "Muuta · Kumoa" line shows the edited item (none after Poista). This changes today's behaviour
   (`FoodViewModel.openEditor` sets `sheetDay=null`, `closeEditor` does not reopen it). The dialog
   finishes only on an explicit sheet dismiss, never when the editor replaces the sheet.

### Import from the sheet (user)
5. "Tuo ruoka-annokset" and the clipboard offer's Näytä in the widget sheet: the dialog finishes
   and `MainActivity` opens (`NEW_TASK|CLEAR_TOP|SINGLE_TOP`, through the same `openFrom` path as
   sharing) on the import preview over the front page. Any sheet or editor open in the app closes.
   After Lisää or Peruuta the user stays in the app.

### Writes survive closing
6. Food adds, undo, edit and delete run in an app-level scope (`TerveysApp`, like the auto-save),
   not the dialog's view model scope, so closing the sheet right after an add never cancels the
   Health Connect write.

### Messages in the dialog
7. A failed write ("Tallennus epäonnistui", NFR-9) and a delete's Kumoa show as a line inside the
   sheet, in the same slot as the "Muuta · Kumoa" line. A failure keeps the sheet open. Closing the
   sheet drops a pending Kumoa.

### Loading
8. Field and keyboard are up at once. Suggestions appear when the day is read; until then a quiet
   "Ladataan…" and no empty hint. An enter or tap before the load ends waits for it, then appends
   at the end. A read failure shows a short line, typing still works, and the add appends after a
   re-read. Speed target (NFR-8): field usable within ~300 ms of the tap, suggestions within ~1 s.

### Permission and availability states
9. Checking: sheet frame only, no keyboard. Needs permission: the existing explanation plus Salli,
   requesting in place; the field opens with the keyboard once granted. Denied: the same text plus
   a button opening Health Connect's app permissions, re-checked on resume. Health Connect
   unavailable: the existing text plus a close button.

### Day and lifecycle
10. Midnight: the sheet keeps the day it opened with; after midnight the existing "Lisätään päivälle
    <day>" label shows. A new open always uses the new today.
11. A second widget or shortcut tap while the dialog is open keeps the state and logs
    `food.sheet.open` again. Rotation keeps state; after process death start fresh on today.
12. Closing wording: closing the sheet finishes the dialog and shows what was behind it (normally
    the home screen). Only the import buttons open the app.

### Clipboard
13. The clipboard is read once when the dialog window first gains focus (Android 10+ allows reads
    only with focus), with the same offered-timestamp rule as now. The offer line sits above the
    suggestions. Näytä takes the import path (5); Ohita only hides the line. The "Clipboard offer"
    requirement names the widget sheet explicitly.

### Refresh
14. The dialog's writes emit the same food-changed signal the app listens to (or the app reloads on
    resume), so the front page food card and Ruoka show the items when the app is next shown. The
    backup is requested as for in-app adds.

### Look
15. The same bottom sheet as in the app, using the app's own theme choice (light/dark/system),
    standard scrim; a tap on the scrim closes.

### "Food only inside the app"
16. The app's own dialog counts as inside the app. Only always-visible surfaces (widget face, tile,
    shortcut label, notification) stay food-free.

### Tests and logs
17. The smoke test asserts through a fake Health Connect foods writer (or the view model's items)
    plus the learnt product, not "the food store". Log `food.sheet.open from=widget|shortcut` and
    `from=` on the dialog's `food.add` lines; add both to the project CLAUDE.md log map in the last
    task group.

## Risks

- Focus-gated clipboard read and task flags only show on a device; the user's own use is the test.
- Moving writes to an app scope changes the in-app add path too; keep the existing food tests green.
