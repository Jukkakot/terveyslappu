## ADDED Requirements

### Requirement: Remember the scale
A weigh-in saved from the dialog SHALL remember the Bluetooth name and address of the scale whose
packet decided the weight, replacing the earlier one, so background listening knows which scale is
the user's. Nothing else about the weigh-in changes.

#### Scenario: Saved weigh-in
- **WHEN** a weigh-in is saved from the dialog
- **THEN** that scale's name and address are remembered

#### Scenario: Discarded weigh-in
- **WHEN** the user taps Poista on the result
- **THEN** the remembered scale stays (the scale was still the user's)
