## MODIFIED Requirements

### Requirement: Recent weights from Health Connect
The app SHALL show the weights of the period chosen for the chart read from Health Connect, newest
first, each with its date, time, value and the name of the app that wrote it.

#### Scenario: Weights from several apps
- **GIVEN** Health Connect holds weights written by this app and by another app
- **WHEN** the main screen is shown
- **THEN** both appear in the list, each marked with its source app

#### Scenario: List follows the period
- **WHEN** the user chooses the period 1 kk
- **THEN** the list shows the weights of the last 30 days

#### Scenario: Refresh after saving
- **WHEN** a weight is saved
- **THEN** it appears at the top of the list without leaving the screen

#### Scenario: Nothing yet
- **WHEN** Health Connect has no weights for the chosen period
- **THEN** the list area says that there are no weights in the period
