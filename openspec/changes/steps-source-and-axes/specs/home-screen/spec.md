## MODIFIED Requirements

### Requirement: Asetukset screen
Asetukset SHALL be a list of rows, each with an icon, a name, one line of its current state and a
chevron, and each SHALL open its own sub-screen holding what was its section: Omat tiedot,
Automaattinen punnitus, Tiedot ja varmuuskopio (Tuo tiedot, Vie tiedot and the backup; shown only
with the Health Connect permission, as before), Ruokakirjaus, Askeleet (the steps source), Ulkoasu
(theme) and Tuki (Jaa loki, Kopioi loki, the version). The sections' contents and behaviour SHALL
stay as they were. A section's warning (backup failed or late, weighings waiting to be saved,
listening blocked, a chosen steps source without steps) SHALL show on its row's state line in the
warning tint. A link elsewhere that opens Asetukset at a section SHALL open that sub-screen
directly; back from a sub-screen SHALL return to where it was opened from: the Asetukset list when
opened from it, otherwise the screen holding the link.

#### Scenario: Asetukset contents
- **WHEN** the user opens Asetukset
- **THEN** seven rows are shown with their state lines, in the order Omat tiedot, Automaattinen
  punnitus, Tiedot ja varmuuskopio, Ruokakirjaus, Askeleet, Ulkoasu and Tuki, e.g. Omat tiedot
  "180 cm · 1990 · Mies", Automaattinen punnitus "Päällä 6.00–10.00", Askeleet "Automaattinen:
  Garmin Connect", Ulkoasu "Teema: Järjestelmä", Tuki with the version

#### Scenario: Open a sub-screen
- **WHEN** the user taps Tiedot ja varmuuskopio
- **THEN** its sub-screen shows Tuo tiedot, Vie tiedot and the backup; back returns to Asetukset

#### Scenario: Warning on the row
- **GIVEN** the backup's last run failed
- **WHEN** Asetukset is shown
- **THEN** the Tiedot ja varmuuskopio row says so in the warning tint

#### Scenario: Link from home
- **WHEN** the user taps the backup notice on the home screen
- **THEN** the Tiedot ja varmuuskopio sub-screen opens, and back returns to the home screen
