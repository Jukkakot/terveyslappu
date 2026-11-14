## ADDED Requirements

### Requirement: Questioned weight in the list
Historia's list SHALL show a weight marked "?" whatever the period filter, with a "?" sign next to its value and the text
"tarkista" (also read by a screen reader). It SHALL show no change value, and the next weight's
change SHALL skip it. Tapping a "?" row SHALL open a small choice "Hyväksy" / "Poista" /
"Peruuta"; Hyväksy SHALL remove the mark and apply the chained acceptance of the background
weigh-in, Poista SHALL delete the weight as the row's delete does
(with Kumoa). A group row SHALL count a "?" weight but SHALL NOT use it as its first or last
weight. Rows without the mark SHALL behave as before.

#### Scenario: Questioned row
- **GIVEN** 61,3 kg was saved automatically and marked "?"
- **WHEN** Historia is shown
- **THEN** its row reads 61,3 kg with "?" and "tarkista" and no change

#### Scenario: Accept in Historia
- **WHEN** the user taps the "?" row and chooses Hyväksy
- **THEN** the mark is gone, the row shows its change, and the chart and statistics include it

#### Scenario: Period filter
- **GIVEN** the period filter shows only the last 7 days and a "?" weight is a month old
- **THEN** the "?" row is still listed

#### Scenario: Delete in Historia
- **WHEN** the user taps the "?" row and chooses Poista
- **THEN** the weight is removed and "Paino poistettu" with Kumoa is shown

#### Scenario: Undo brings the mark back
- **WHEN** the user taps Kumoa after deleting a "?" weight
- **THEN** the weight returns marked "?"

#### Scenario: Change skips the questioned weight
- **GIVEN** 82,0 kg, then 61,3 kg marked "?", then 82,4 kg
- **WHEN** Historia is shown
- **THEN** 82,4 kg shows +0,4

### Requirement: Questioned row look
A "?" row SHALL be drawn as a highlighted card: a warning-tinted container (amber family, readable
in the light and the dark theme) with a matching outline, in addition to the "?" sign and the text
"tarkista", so the state never depends on colour alone.

#### Scenario: Highlighted row
- **WHEN** Historia shows a "?" weight in the light or the dark theme
- **THEN** its row is an amber-tinted outlined card with "?" and "tarkista", and its text is
  readable against the tint
