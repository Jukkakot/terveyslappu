## ADDED Requirements

### Requirement: Owner details in Asetukset
Asetukset SHALL have an "Omat tiedot" section with the phone owner's birth year, height in
centimetres and gender (Mies, Nainen or Ei valittu). Each field SHALL be optional and start empty.
The birth year SHALL be shown together with the age it gives this year. The details SHALL be kept on
the phone across restarts and updates, one set per phone.

#### Scenario: First visit
- **WHEN** the user opens Asetukset for the first time after the update
- **THEN** Omat tiedot shows birth year and height empty and gender Ei valittu

#### Scenario: Enter details
- **WHEN** the user sets birth year 1990, height 180 and gender Mies
- **THEN** Omat tiedot shows them (with the age for 1990) and they are still there after a restart

#### Scenario: Clear a field
- **WHEN** the user empties the height
- **THEN** the height is no longer kept and shows empty

### Requirement: Plausible values only
A birth year SHALL be accepted only from 1900 to the current year, a height only from 50 to 250 cm in
whole centimetres. An unaccepted value SHALL not be kept and the field SHALL say what range is
allowed.

#### Scenario: Height out of range
- **WHEN** the user types height 25
- **THEN** the value is not kept and the field shows the allowed range

#### Scenario: Future birth year
- **WHEN** the user types a birth year after the current year
- **THEN** the value is not kept and the field shows the allowed range

### Requirement: Details stay out of the log
Changing a detail SHALL be logged with which field changed but never with its value.

#### Scenario: Log line
- **WHEN** the user sets the height
- **THEN** the log gets a profile line naming the height field and no number
