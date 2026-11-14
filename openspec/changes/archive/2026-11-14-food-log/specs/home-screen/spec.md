## ADDED Requirements

### Requirement: Food row on the home screen
Under the three latest weights the app's front page SHALL show one row "Syöty tänään" with today's
total kcal (or "ei kirjauksia" when nothing is logged), which opens the Ruoka screen on today, and
an outlined "Lisää annos" button that opens the add field for today. Neither SHALL be a filled
button, so Punnitse vaa'alla stays the only filled action. Both SHALL be shown also when no weights
exist yet.

#### Scenario: Something eaten
- **GIVEN** today 1 240 kcal is logged
- **WHEN** the front page is shown
- **THEN** the row reads "Syöty tänään 1 240 kcal" and tapping it opens Ruoka on today

#### Scenario: Add from the front page
- **WHEN** the user taps Lisää annos
- **THEN** the add field opens for today with the keyboard up

#### Scenario: Nothing logged
- **WHEN** nothing is logged today
- **THEN** the row reads "Syöty tänään: ei kirjauksia" with no warning tone
