## ADDED Requirements

### Requirement: Carry custom charts
The own charts (their names, data, styles, layouts) and the chart cards' periods SHALL move with
Android's phone-to-phone transfer and cloud backup like the chart periods, and SHALL NOT be part of
the data file or the backup file.

#### Scenario: New phone
- **GIVEN** the old phone has the own chart "Liike"
- **WHEN** the user moves to a new phone with Smart Switch and opens the app there
- **THEN** Historia has the Liike tab
