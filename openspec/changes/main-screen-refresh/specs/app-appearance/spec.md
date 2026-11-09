## ADDED Requirements

### Requirement: Theme choice
The app SHALL offer in menu ⋮ a theme choice with Järjestelmä, Vaalea and Tumma. Järjestelmä SHALL be
the default and follow the phone's light/dark setting; the colours SHALL come from the phone's own
colour palette in every choice. The choice SHALL be remembered and apply to every screen of the app
(main screen, weigh-in dialog, manual entry).

#### Scenario: Default
- **GIVEN** the user has never chosen a theme and the phone is in dark mode
- **WHEN** the app opens
- **THEN** it is dark

#### Scenario: Choose light
- **WHEN** the user picks Teema → Vaalea while the phone is in dark mode
- **THEN** the app turns light at once and stays light after a restart

#### Scenario: Back to system
- **WHEN** the user picks Järjestelmä
- **THEN** the app follows the phone's setting again
