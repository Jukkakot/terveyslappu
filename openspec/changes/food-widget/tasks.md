# Tasks

## 1. Add sheet outside the main screen

- [ ] 1.1 Make the add sheet and the item editor usable from a second activity: the sheet's
  import button and clipboard offer get a hook the host decides (main screen: as now; dialog:
  open `MainActivity` on the import preview with the text, like the share path).
- [ ] 1.2 New dialog activity (own task, excluded from recents, transparent like the weigh-in
  dialog) that loads the food state, opens the sheet on today, reads the clipboard offer when it
  opens, shows the editor for Muuta and returns to the field after it, and finishes when the
  sheet closes. Logs `food.sheet.open from=widget|shortcut`.
- [ ] 1.3 Permission: without the nutrition permission the dialog shows the existing explanation
  with the grant button and requests it in place; once granted the field opens.
- [ ] 1.4 Smoke test: the dialog opens the add field on today and an added item lands in the
  food store (made-up item).

## 2. Widget and shortcut

- [ ] 2.1 1x1 widget provider "Lisää annos" (layout like the Punnitse widget: round plate, line
  icon of a plate/cutlery, label), preview, description string; no updates from food data.
- [ ] 2.2 Launcher shortcut "Lisää annos" in `res/xml/shortcuts.xml` after Punnitse.
- [ ] 2.3 Manifest entries for the dialog activity and the widget receiver.

## 3. Help, docs, release

- [ ] 3.1 Help page food section names the widget and the shortcut.
- [ ] 3.2 `product.md`: replace "No food widget for now" with the add-only widget decision;
  roadmap: mark food widget done; project CLAUDE.md map: the new activity and widget.
- [ ] 3.3 Bump `appVersion` MINOR; `./gradlew test lint assembleDebug`.
