## ADDED Requirements

### Requirement: Restore owner details
When an imported file starts with a `#profile;` line, the app SHALL read the owner details from it,
SHALL not count the line as a skipped row, and on confirming the import SHALL fill only the owner
details that are empty on the phone; details already set SHALL stay. The preview SHALL say when the
file holds owner details that will be filled in. Unknown or implausible values in the line SHALL be
ignored.

#### Scenario: New phone
- **GIVEN** the phone has no owner details
- **WHEN** the user imports a file starting with `#profile;birth_year=1990|height_cm=180`
- **THEN** the preview says the owner details will be filled in, and after confirming Omat tiedot
  shows birth year 1990 and height 180

#### Scenario: Details already set
- **GIVEN** the phone's height is 182
- **WHEN** the user imports a file whose profile line has `height_cm=180`
- **THEN** the height stays 182

#### Scenario: Implausible value
- **WHEN** the profile line has `height_cm=999`
- **THEN** the height is not filled in and the weights import as usual
