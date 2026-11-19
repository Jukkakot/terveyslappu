## MODIFIED Requirements

### Requirement: Undo messages
Every message with Kumoa in the app (deleted item, added item, import, removed product, weights)
shown as a bar at the bottom SHALL show for about 4 seconds, or longer when Android's
accessibility timeout ("Time to take action") asks for longer, SHALL go away at once when swiped
sideways, when another message comes or when the screen changes, and SHALL sit above the screen's
bottom buttons without covering them. The add search shows its messages as the same timed bar at
its bottom, above the keyboard when it is up. Other bar messages keep their length (which also
follows the accessibility timeout) but can be swiped away too. Ohje SHALL say that Kumoa bars show
about 4 seconds, longer when the phone's accessibility setting asks for it.

#### Scenario: Gone in time
- **GIVEN** Android's accessibility timeout is at its default
- **WHEN** the user deletes an item on Ruoka and does nothing
- **THEN** the "Kumoa" message is gone after about 4 seconds

#### Scenario: Longer accessibility timeout
- **GIVEN** Android's accessibility timeout is set to 30 seconds
- **WHEN** the user deletes an item on Ruoka and does nothing
- **THEN** the "Kumoa" message stays for about 30 seconds

#### Scenario: Swiped away
- **WHEN** the user swipes the message sideways
- **THEN** it disappears at once and nothing is undone

