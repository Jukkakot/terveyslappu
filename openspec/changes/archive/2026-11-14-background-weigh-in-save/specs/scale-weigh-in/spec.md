## MODIFIED Requirements

### Requirement: One-tap start
The weigh-in dialog SHALL open with one tap from a Quick Settings tile, a home-screen widget, a
launcher shortcut on the app icon and a button on the main screen. The widget and the tile SHALL
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
- **WHEN** the user taps Punnitse on the main screen
- **THEN** the weigh-in dialog opens and the list shows the new weight after it closes

### Requirement: Ignore the scale's lingering broadcast
For two minutes after a weigh-in is saved or discarded, from the dialog or in the background, a
new weigh-in in either SHALL ignore scale packets carrying that weigh-in's company id, so a reading
the scale is still repeating is not saved again.

#### Scenario: Reopened right after Poista
- **GIVEN** a weigh-in with company id `61c0` was discarded 10 seconds ago
- **WHEN** the dialog is opened again and the scale still sends the old stable packet with `61c0`
- **THEN** nothing is saved and the dialog asks the user to step on the scale
- **AND** a new weighing with company id `62c0` is saved as usual

#### Scenario: Saved in the background first
- **GIVEN** the background saved the weighing with company id `61c0` a moment ago
- **WHEN** the dialog is open and receives `61c0`
- **THEN** the dialog does not save it again

#### Scenario: Old enough
- **GIVEN** the last weigh-in with company id `61c0` ended three minutes ago
- **WHEN** a packet with `61c0` arrives
- **THEN** it is used as usual
