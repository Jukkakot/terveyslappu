## ADDED Requirements

### Requirement: Food row on the home screen
Under the three latest weights the home screen SHALL show one row "Syöty tänään" with today's
total kcal (or "ei kirjauksia" when nothing is logged), which opens the Ruoka screen on today. The
row SHALL be plain text, not a filled button, so Punnitse vaa'alla stays the only filled action. It
SHALL be shown also when no weights exist yet.

#### Scenario: Something eaten
- **GIVEN** today 1 240 kcal is logged
- **WHEN** the home screen is shown
- **THEN** the row reads "Syöty tänään 1 240 kcal" and tapping it opens Ruoka on today

#### Scenario: Nothing logged
- **WHEN** nothing is logged today
- **THEN** the row reads "Syöty tänään: ei kirjauksia" with no warning tone
