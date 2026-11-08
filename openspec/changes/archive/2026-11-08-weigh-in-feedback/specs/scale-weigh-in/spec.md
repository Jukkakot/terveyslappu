# Spec Delta

## ADDED Requirements

### Requirement: Change from the previous weight
After saving a scale weight the dialog SHALL show the difference to the latest earlier weight in
Health Connect from any app, with that weight's day, or nothing when there is no earlier weight.

#### Scenario: Lighter than last time
- **GIVEN** the latest earlier weight is 82.8 kg yesterday
- **WHEN** 82.5 kg is saved
- **THEN** the dialog shows "−0,3 kg edellisestä (eilen)"

#### Scenario: Same weight
- **WHEN** the saved weight equals the previous one
- **THEN** the dialog shows "±0,0 kg edellisestä"

#### Scenario: First weight
- **WHEN** Health Connect has no earlier weight
- **THEN** only the saved weight is shown

### Requirement: Confirmation vibration
The phone SHALL give a short confirmation vibration when the scale weight has been saved.

#### Scenario: Saved
- **WHEN** the dialog switches to Tallennettu
- **THEN** the phone vibrates once briefly

### Requirement: Latest weight on the widget and tile
The home-screen widget SHALL show the latest weight known to the app with its day ("Tänään",
"Eilen" or the date) and the Quick Settings tile SHALL show it as its subtitle; both SHALL update
after the app saves, imports, deletes or reads weights.

#### Scenario: After a weigh-in
- **WHEN** 82.5 kg is saved this morning
- **THEN** the widget shows "82,5 kg" and "Tänään"

#### Scenario: Nothing known yet
- **WHEN** the app has not read or saved any weight
- **THEN** the widget shows only Punnitse
