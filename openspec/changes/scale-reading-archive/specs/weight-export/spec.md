## MODIFIED Requirements

### Requirement: File content
The file SHALL hold every weight in Health Connect from every app, oldest first, one row each with
date (`yyyy-MM-dd`), time (`HH:mm:ss`, the record's own offset), weight in kg with a comma decimal,
the writing app's name, the impedance in ohms and the raw scale packet in hex (both empty when the
phone keeps no scale reading for that weight), separated by semicolons under a header row.

#### Scenario: Row format
- **GIVEN** an 82.4 kg weight at 1 March 2024 07:12 written by this app by hand
- **THEN** the row is `2024-03-01;07:12:00;82,4;Terveyslappu;;`

#### Scenario: Scale row
- **GIVEN** an 82.5 kg scale weigh-in with impedance 512.3 Ω
- **THEN** its row ends with `;512,3;` followed by the packet's hex bytes

#### Scenario: Read back
- **WHEN** an exported file is imported with Tuo historia
- **THEN** the same weights and times are found, and the scale readings are kept on the phone again

#### Scenario: Older export
- **WHEN** a file without the impedance and packet columns is imported
- **THEN** the weights are imported as before
