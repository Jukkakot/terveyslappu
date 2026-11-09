## REMOVED Requirements

### Requirement: Basal metabolic rate
**Reason**: The user has no use for energy figures and wants a simpler app (2026-11-09).
**Migration**: None; nothing was stored.

## MODIFIED Requirements

### Requirement: Reason when a value cannot be computed
Wherever a derived value would be shown but cannot be computed, the app SHALL show a short text
naming the missing owner details instead ("BMI vaatii pituuden"), and tapping it SHALL open Asetukset at Omat
tiedot. When the owner was under 18, the text SHALL say that the adult classes do not apply.

#### Scenario: Height missing
- **GIVEN** the owner has not set a height
- **THEN** in place of the BMI the text reads "BMI vaatii pituuden"

#### Scenario: Gender missing
- **GIVEN** height and birth year set, gender Ei valittu
- **THEN** the BMI and the normal-weight range show and no reason text is shown

#### Scenario: Several missing
- **GIVEN** only the birth year is set
- **THEN** one text reads "BMI vaatii pituuden"

### Requirement: Derived values stay in the app
Derived values SHALL NOT be written to Health Connect nor carried in the export or the backup; they
are computed again from the weights and the owner details whenever shown.

#### Scenario: Export
- **WHEN** the user exports the data
- **THEN** the file has no BMI
