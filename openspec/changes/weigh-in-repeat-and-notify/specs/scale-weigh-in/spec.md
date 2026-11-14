## ADDED Requirements

### Requirement: Ignore the same weight repeated
After a weigh-in is saved or deleted, from the dialog, the background, a notification or Historia,
a stable reading from the same scale address within 0.1 kg of that weight SHALL be ignored, by the
dialog and the background, for 30 seconds whatever its company id. Each time such a reading is
heard and ignored, the 30 seconds SHALL start again. A different weight, or the same weight after
30 seconds without hearing it, SHALL be used as usual. Ignoring SHALL be logged
(`scale.ignore reason=same kg=`).

#### Scenario: Re-broadcast after Poista
- **GIVEN** 99.05 kg from the remembered scale was saved automatically and deleted 5 seconds ago
- **WHEN** the scale sends 99.05 kg again with a new company id
- **THEN** nothing is saved and the 30 seconds start again

#### Scenario: Keeps repeating
- **GIVEN** the scale repeats 99.05 kg every 20 seconds with a new company id after a save
- **WHEN** two minutes pass
- **THEN** none of the repeats is saved

#### Scenario: Another weight
- **GIVEN** 99.05 kg was deleted 5 seconds ago
- **WHEN** the scale sends a stable 98.6 kg
- **THEN** it is used as usual

#### Scenario: Quiet long enough
- **GIVEN** 99.05 kg was last heard 40 seconds ago after a delete
- **WHEN** the user steps on the scale and it settles at 99.05 kg
- **THEN** it is used as usual
