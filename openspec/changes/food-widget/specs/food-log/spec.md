## ADDED Requirements

### Requirement: Adding from the home screen
The app SHALL offer a 1x1 home-screen widget "Lisää annos" in the same style as the Punnitse
widget, and a launcher shortcut "Lisää annos" on the app icon. Both SHALL open the add field (as
in "Adding an item") as a sheet over whatever is on screen, without the app's main screen behind
it, adding to today, with the keyboard up. The sheet SHALL work as on Ruoka: suggestions, search,
enter, the "<name> · Muuta · Kumoa" line, and it stays open for the next item. Muuta SHALL open the
item's editor in the same sheet; leaving the editor SHALL return to the add field. "Tuo
ruoka-annokset" and the clipboard offer's Näytä SHALL open the app on the import preview. Closing
the sheet (back, a tap outside or swiping it down) SHALL return to where the user was. Without the
nutrition permission the sheet SHALL explain why it is needed and offer to grant it there. The
widget SHALL never change with what was logged.

#### Scenario: Breakfast from the home screen
- **GIVEN** on most mornings around 7 the user logged Rahka 200 g
- **WHEN** they tap the Lisää annos widget at 7.20 and tap Rahka
- **THEN** Rahka 200 g is added to today, the sheet stays open with "Rahka 200 g · 130 kcal ·
  Muuta · Kumoa", and closing it shows the home screen again

#### Scenario: Several items in a row
- **WHEN** the user types "kahvi", presses enter, then taps Pulla
- **THEN** both are added to today in that order, and the app's Ruoka screen shows them when opened

#### Scenario: From the shortcut
- **WHEN** the user long-presses the app icon and taps Lisää annos
- **THEN** the same sheet opens over the home screen

#### Scenario: Import from the sheet
- **WHEN** the user taps Tuo ruoka-annokset in the sheet opened from the widget
- **THEN** the app opens on the import preview, as when the estimate is shared to the app

#### Scenario: Permission missing
- **GIVEN** the nutrition permission is not granted
- **WHEN** the user taps the widget
- **THEN** the sheet explains why the permission is needed and offers to grant it; once granted the
  add field is shown

## MODIFIED Requirements

### Requirement: Food only inside the app
Food and kcal SHALL be shown only inside the app: no widget, tile, shortcut or notification SHALL
show what was eaten, how many items or how many kcal. A widget or shortcut that only opens the add
field (see "Adding from the home screen") is allowed.

#### Scenario: Nothing outside
- **WHEN** the user logs food
- **THEN** no notification appears and no widget changes
