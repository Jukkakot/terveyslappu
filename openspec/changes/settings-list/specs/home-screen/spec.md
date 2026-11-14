## MODIFIED Requirements

### Requirement: Simple home screen
The home screen SHALL show only: a card with the latest weight this app wrote (large, with its date
and time, the change from the weight before it with a direction sign, not by colour alone, and a
small line chart of the chosen period without axes), under the card a row of the latest weight's
derived values, the three latest weights, a help button and a settings button in the top bar, and
Punnitse vaa'alla as the only filled action at the bottom. The small chart SHALL
span the whole chosen period ending now, use the weight scale of the weight chart, and show under it
the first and last day of the period, the first and the latest weight of the period with `kg`, and
the change over it. Both changes on the card SHALL show a small arrow up for a gain and down for a
loss next to the number, and no arrow when the shown value is zero. Tapping the card SHALL open
Historia; the help button SHALL open Ohje; the settings button SHALL open Asetukset. The home
screen SHALL have no ⋮ menu.

The derived-values row SHALL show, of the latest weight, the BMI number in its category tone without
the category word (the word SHALL stay in the screen reader text), and the normal-weight range; a value that cannot be computed SHALL be
replaced by its short reason naming the missing owner details, which opens Asetukset at Omat
tiedot. Without weights the row SHALL NOT be shown.

#### Scenario: Latest with change
- **GIVEN** the latest weights are 82.4 kg today 7.05 and 82.9 kg yesterday
- **WHEN** the home screen is shown
- **THEN** the card shows 82,4 kg, "tänään 7.05" and −0,5 kg with an arrow down, and the list starts
  with them

#### Scenario: 30-day change
- **GIVEN** the home period is 1 kk, the weight 30 days ago was 81.0 kg and the latest is 82.4 kg
- **WHEN** the home screen is shown
- **THEN** under the small chart the card shows `81,0 → 82,4 kg` and +1,4 kg with an arrow up

#### Scenario: Short history
- **GIVEN** the home period is 1 kk and the only weights are from yesterday and today
- **WHEN** the home screen is shown
- **THEN** the line sits at the right end of the small chart, and the dates under it still span 30 days

#### Scenario: Steady weight
- **GIVEN** the period's weights differ by less than 0.1 kg
- **WHEN** the home screen is shown
- **THEN** the small chart's line looks flat

#### Scenario: Open Historia
- **WHEN** the user taps the card
- **THEN** Historia opens; back returns to the home screen

#### Scenario: Open Asetukset
- **WHEN** the user taps the settings button
- **THEN** Asetukset opens; back returns to the home screen

#### Scenario: Open Ohje
- **WHEN** the user taps the help button
- **THEN** Ohje opens; back returns to the home screen

#### Scenario: No weights yet
- **WHEN** the app has written no weights
- **THEN** the card invites to weigh with the scale instead of showing a number, and no list and no
  derived-values row are shown

#### Scenario: Backup in trouble
- **GIVEN** the backup is on
- **WHEN** its last run failed or its last success is older than three days
- **THEN** the home screen shows one notice that opens Asetukset at Tiedot ja varmuuskopio;
  otherwise nothing about the backup

#### Scenario: All details set
- **GIVEN** height 180 cm, birth year 1990, gender Mies and the latest weight 82.0 kg in 2026
- **WHEN** the home screen is shown
- **THEN** the row shows "BMI 25,3" in the yellow tone without the word "ylipaino", and
  normaalipaino 60–81 kg, and no reason text

#### Scenario: No details
- **GIVEN** no owner details are set
- **WHEN** the home screen is shown
- **THEN** the row shows only "BMI vaatii pituuden", which opens Asetukset at Omat tiedot

#### Scenario: Gender missing
- **GIVEN** height and birth year set, gender Ei valittu
- **WHEN** the home screen is shown
- **THEN** the row shows the BMI and the normal-weight range and no reason text

### Requirement: Asetukset screen
Asetukset SHALL be a list of rows, each with an icon, a name, one line of its current state and a
chevron, and each SHALL open its own sub-screen holding what was its section: Omat tiedot,
Automaattinen punnitus, Tiedot ja varmuuskopio (Tuo tiedot, Vie tiedot and the backup; shown only
with the Health Connect permission, as before), Ulkoasu (theme) and Tuki (Jaa loki, Kopioi loki,
the version). The sections' contents and behaviour SHALL stay as they were. A section's warning
(backup failed or late, weighings waiting to be saved, listening blocked) SHALL show on its row's
state line in the warning tint. A link elsewhere that opens Asetukset at a section SHALL open that
sub-screen directly; back from a sub-screen SHALL return to where it was opened from: the
Asetukset list when opened from it, otherwise the screen holding the link.

#### Scenario: Asetukset contents
- **WHEN** the user opens Asetukset
- **THEN** five rows are shown with their state lines, e.g. Omat tiedot "180 cm · 1990 · Mies",
  Automaattinen punnitus "Päällä 6.00–10.00", Ulkoasu "Teema: Järjestelmä", Tuki with the version

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
