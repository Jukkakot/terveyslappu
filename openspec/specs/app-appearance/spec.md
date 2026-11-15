# app-appearance Specification

## Purpose
TBD - created by archiving change main-screen-refresh. Update Purpose after archive.

## Requirements

### Requirement: Theme choice
Asetukset SHALL offer a theme choice with Järjestelmä, Vaalea and Tumma. Järjestelmä SHALL be the
default and follow the phone's light/dark setting; the colours SHALL come from the phone's own colour
palette in every choice. The choice SHALL be remembered and apply to every screen of the app (home,
Historia, Asetukset, the weigh-in dialog, manual entry).

#### Scenario: Default
- **GIVEN** the user has never chosen a theme and the phone is in dark mode
- **WHEN** the app opens
- **THEN** it is dark

#### Scenario: Choose light
- **WHEN** the user picks Vaalea while the phone is in dark mode
- **THEN** the app turns light at once and stays light after a restart

#### Scenario: Back to system
- **WHEN** the user picks Järjestelmä
- **THEN** the app follows the phone's setting again

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
