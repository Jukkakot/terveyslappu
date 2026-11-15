## ADDED Requirements

### Requirement: Undo messages
Every message with Kumoa in the app (deleted item, added item, import, removed product, weights)
shown as a bar at the bottom SHALL show for about 4 seconds, SHALL go away at once when swiped
sideways, when another message comes or when the screen changes, and SHALL sit above the screen's
bottom buttons without covering them. While a sheet is open, its messages SHALL appear as the line
inside the sheet (which stays until the next action, as today) and no bar SHALL be drawn over the
sheet. Other bar messages keep their length but can be swiped away too.

#### Scenario: Gone in time
- **WHEN** the user deletes an item on Ruoka and does nothing
- **THEN** the "Kumoa" message is gone after about 4 seconds

#### Scenario: Swiped away
- **WHEN** the user swipes the message sideways
- **THEN** it disappears at once and nothing is undone
