## ADDED Requirements

### Requirement: Latest weight summary
The main screen SHALL show at its top the latest weight this app wrote, large, with its date and time
and the change from the weight before it (kg, with a direction sign and not by colour alone). Below
the list the only main-screen action SHALL be Punnitse vaa'alla.

#### Scenario: Latest with change
- **GIVEN** the latest weights are 82.4 kg today 7.05 and 82.9 kg yesterday
- **WHEN** the main screen is shown
- **THEN** the top shows 82,4 kg, "tänään 7.05" and −0,5 kg

#### Scenario: No weights yet
- **WHEN** the app has written no weights
- **THEN** the top invites to weigh with the scale instead of showing a number
