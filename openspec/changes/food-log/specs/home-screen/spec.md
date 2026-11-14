## MODIFIED Requirements

### Requirement: Asetukset screen
Asetukset SHALL be a list of rows, each with an icon, a name, one line of its current state and a
chevron, and each SHALL open its own sub-screen holding what was its section: Omat tiedot,
Automaattinen punnitus, Ruokakirjaus, Tiedot ja varmuuskopio (Tuo tiedot, Vie tiedot and the
backup), Ulkoasu (theme) and Tuki (Ohje, Jaa loki, Kopioi loki, the version). A warning of a
section (backup in trouble, weighings waiting to be saved, listening blocked) SHALL show on its
row in the warning tint. A link elsewhere that opens Asetukset at a section SHALL open that
sub-screen directly; back from a sub-screen SHALL return to where it was opened from.

#### Scenario: Asetukset contents
- **WHEN** the user opens Asetukset
- **THEN** six rows are shown with their state lines, e.g. Omat tiedot "180 cm · 1990 · Mies",
  Automaattinen punnitus "Päällä 6.00–9.00", Ulkoasu "Teema: järjestelmä"

#### Scenario: Open a sub-screen
- **WHEN** the user taps Tiedot ja varmuuskopio
- **THEN** its sub-screen shows Tuo tiedot, Vie tiedot and the backup; back returns to Asetukset

#### Scenario: Warning on the row
- **GIVEN** the backup's last run failed
- **WHEN** Asetukset is shown
- **THEN** the Tiedot ja varmuuskopio row says so in the warning tint

#### Scenario: Link from home
- **WHEN** the user taps "BMI vaatii pituuden" on the home screen
- **THEN** the Omat tiedot sub-screen opens, and back returns to the home screen

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
