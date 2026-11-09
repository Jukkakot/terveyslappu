## ADDED Requirements

### Requirement: Keep the full scale reading
For every saved scale weight the app SHALL keep on the phone the reading behind it: time, weight,
impedance, the scale's display unit and the raw bytes of the deciding packet, and SHALL drop or
restore it together with the weight.

#### Scenario: Weigh-in kept
- **WHEN** a scale weigh-in saves 82.5 kg with impedance 512.3 Ω
- **THEN** the phone keeps that reading with the weight's time and the packet's raw bytes

#### Scenario: Weigh again
- **WHEN** the user taps Punnitse uudelleen and then Kumoa
- **THEN** the reading is removed and then kept again with the weight

#### Scenario: No impedance
- **WHEN** the scale reports impedance 0 (e.g. shoes on)
- **THEN** the reading is kept with an empty impedance
