## MODIFIED Requirements

### Requirement: One-tap start
The weigh-in dialog SHALL open with one tap from a Quick Settings tile, a home-screen widget and a
launcher shortcut on the app icon; inside the app it is "Punnitse" in the ⋮ menu of the front page
and Historia (two taps). The widget and the tile SHALL
show only the Punnitse action, never a weight; while automatic weigh-in is on, the widget SHALL
also show its state (Päällä, Lepää or Estetty). The widget SHALL fit one home-screen cell.

#### Scenario: From the tile
- **WHEN** the user taps the Punnitse tile in Quick Settings
- **THEN** the panel collapses and the weigh-in dialog opens without the main screen

#### Scenario: From the widget
- **WHEN** the user taps the Punnitse widget on the home screen
- **THEN** the weigh-in dialog opens without the main screen, whatever state the widget shows

#### Scenario: No weight before weighing
- **GIVEN** weights have been saved earlier
- **WHEN** the user looks at the widget or the tile
- **THEN** neither shows a weight or a day

#### Scenario: From the main screen
- **WHEN** the user taps ⋮ and then Punnitse on the main screen
- **THEN** the weigh-in dialog opens and the list shows the new weight after it closes
