## MODIFIED Requirements

### Requirement: Carry the app's own data
The app SHALL let Android's phone-to-phone transfer and cloud backup carry its owner details,
appearance choice, chart and home periods, the front page layout (order, sizes, hidden cards), and the raw-data archive, and restore them when the
app is installed on the new phone.

#### Scenario: New Samsung phone with Smart Switch
- **GIVEN** the old phone has owner details, the dark theme and a raw-data archive
- **WHEN** the user moves to a new phone with Smart Switch and opens the app there
- **THEN** Omat tiedot, the theme, the periods, the front page layout and the archive are as on the old phone

#### Scenario: Weights still come from the backup file
- **GIVEN** the app data was restored on the new phone
- **WHEN** the user has not yet run Tuo tiedot
- **THEN** the app shows no weights of its own until the backup file is imported or Health
  Connect holds them
