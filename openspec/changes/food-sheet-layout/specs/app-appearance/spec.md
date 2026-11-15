## ADDED Requirements

### Requirement: Undo messages
Every message with Kumoa in the app (deleted item, added item, import, removed product, weights)
SHALL show for about 4 seconds, SHALL go away at once when swiped sideways or when the user taps
elsewhere to act, and SHALL sit at the bottom as a narrow bar that does not cover the open sheet's
buttons or the field (in the Lisää annos sheet it is the line inside the sheet, as today). A new
message SHALL replace the one shown.

#### Scenario: Gone in time
- **WHEN** the user deletes an item on Ruoka and does nothing
- **THEN** the "Kumoa" message is gone after about 4 seconds

#### Scenario: Swiped away
- **WHEN** the user swipes the message sideways
- **THEN** it disappears at once and nothing is undone
