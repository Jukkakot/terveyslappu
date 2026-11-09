## ADDED Requirements

### Requirement: Simple home screen
The home screen SHALL show only: a card with the latest weight this app wrote (large, with its date
and time, the change from the weight before it with a direction sign, not by colour alone, and a
small line chart of the last 30 days without axes), the three latest weights, a settings button in
the top bar, and Punnitse vaa'alla as the only filled action at the bottom. Tapping the card SHALL
open Historia; the settings button SHALL open Asetukset. The home screen SHALL have no ⋮ menu.

#### Scenario: Latest with change
- **GIVEN** the latest weights are 82.4 kg today 7.05 and 82.9 kg yesterday
- **WHEN** the home screen is shown
- **THEN** the card shows 82,4 kg, "tänään 7.05" and −0,5 kg, and the list starts with them

#### Scenario: Open Historia
- **WHEN** the user taps the card
- **THEN** Historia opens; back returns to the home screen

#### Scenario: Open Asetukset
- **WHEN** the user taps the settings button
- **THEN** Asetukset opens; back returns to the home screen

#### Scenario: No weights yet
- **WHEN** the app has written no weights
- **THEN** the card invites to weigh with the scale instead of showing a number, and no list is shown

#### Scenario: Backup in trouble
- **GIVEN** the backup is on
- **WHEN** its last run failed or its last success is older than three days
- **THEN** the home screen shows one notice that opens Asetukset; otherwise nothing about the backup

### Requirement: Historia screen
Historia SHALL hold the period chips, the chart, the period statistics and the list of this app's
weights with delete and undo, and an add button in its top bar that opens the manual entry.

#### Scenario: Historia contents
- **WHEN** the user opens Historia
- **THEN** it shows the chosen period's chips, chart, statistics and weights, newest first

### Requirement: Asetukset screen
Asetukset SHALL group, with section titles: Ulkoasu (theme), Painot (Tuo historia, Vie painot),
Varmuuskopio (state and its actions) and Tuki (Jaa loki, the version).

#### Scenario: Asetukset contents
- **WHEN** the user opens Asetukset
- **THEN** the theme choice, the import and export, the backup and Jaa loki with the version are
  shown in those groups
