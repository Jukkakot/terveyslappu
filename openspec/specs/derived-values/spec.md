# derived-values Specification

## Purpose
Values computed from a weight and the owner details (BMI with its WHO class and tone, the
normal-weight range, the basal metabolic rate), when each can be shown and why not; shown on the
home screen and Historia, never stored.

## Requirements

### Requirement: BMI
The app SHALL compute the BMI of a weight as the weight in kg divided by the square of the owner's
height in metres, shown with one decimal. It SHALL use the current height for every weight, old ones
too. Without a height no BMI SHALL be computed.

#### Scenario: BMI from height
- **GIVEN** the height is 180 cm
- **WHEN** a weight of 82.0 kg is shown
- **THEN** its BMI is 25,3

#### Scenario: No height
- **GIVEN** the owner has not set a height
- **THEN** no BMI is computed, and where it would be shown the reason is shown instead

### Requirement: BMI category with tone
Each BMI SHALL be shown with its category by the WHO adult classes, as a word and a tone: alipaino
(under 18.5), normaalipaino (18.5 to under 25), ylipaino (25 to under 30), lihavuus (30 to under 35),
vaikea lihavuus (35 to under 40), sairaalloinen lihavuus (40 and over). The tone SHALL be green for
normaalipaino, yellow for 17 to under 18.5 and for ylipaino, and red under 17 and from 30 up. The
word SHALL always be shown with the tone, so the category is not told by colour alone. When the
birth year shows the owner was under 18 in the weight's year, the category and tone SHALL be left
out (the adult classes do not apply).

#### Scenario: Normal
- **WHEN** the BMI is 22,0
- **THEN** it reads "normaalipaino" in the green tone

#### Scenario: Borderline
- **WHEN** the BMI is 25,3
- **THEN** it reads "ylipaino" in the yellow tone

#### Scenario: Bad
- **WHEN** the BMI is 31,0
- **THEN** it reads "lihavuus" in the red tone

#### Scenario: Under 18
- **GIVEN** the birth year 2012 and a weight from 2026
- **THEN** the BMI number is shown without a category or tone

### Requirement: Normal-weight range
With a height set, the app SHALL show the normal-weight range for that height: the weights giving BMI
18.5 and 25, rounded to whole kilos ("Normaalipaino 60–81 kg"). It SHALL follow the same under-18
rule as the category.

#### Scenario: Range for 180 cm
- **GIVEN** the height is 180 cm and the owner is an adult
- **THEN** the normal-weight range reads 60–81 kg

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
