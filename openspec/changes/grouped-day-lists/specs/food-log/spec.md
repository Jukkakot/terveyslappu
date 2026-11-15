## ADDED Requirements

### Requirement: Item row buttons
Every logged item row in the add sheet's day list and on the Ruoka screen SHALL end with the same
buttons as the other food lists: an edit button (pencil, named "Muokkaa <name>") that opens the
item's editor, and a red delete button (trash, named "Poista <name>") that deletes the item at once
and shows "<name> poistettu · Kumoa" (in the sheet in its line, on Ruoka as its Kumoa bar); Kumoa
SHALL bring the item back in its place. Tapping elsewhere on the row SHALL still open the editor.
Järjestä keeps its own move buttons and shows no edit or delete buttons.

#### Scenario: Delete from the sheet
- **WHEN** the user taps the trash on Tomaatti in the sheet's day list
- **THEN** Tomaatti is gone from the day, the header total drops, the line reads "Tomaatti poistettu
  · Kumoa", and Kumoa brings it back in its place

#### Scenario: Edit from Ruoka
- **WHEN** the user taps the pencil on Kurkku on the Ruoka screen
- **THEN** Kurkku's editor opens
