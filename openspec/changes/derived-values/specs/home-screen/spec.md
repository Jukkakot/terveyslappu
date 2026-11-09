## MODIFIED Requirements

### Requirement: Simple home screen
The home screen SHALL show only: a card with the latest weight this app wrote (large, with its date
and time, the change from the weight before it with a direction sign, not by colour alone, and a
small line chart of the chosen period without axes), under the card a row of the latest weight's
derived values, the three latest weights, a settings button in the top bar, and Punnitse vaa'alla as
the only filled action at the bottom. The small chart SHALL
span the whole chosen period ending now, use the weight scale of the weight chart, and show under it
the first and last day of the period, the first and the latest weight of the period with `kg`, and
the change over it. Both changes on the card SHALL show a small arrow up for a gain and down for a
loss next to the number, and no arrow when the shown value is zero. Tapping the card SHALL open
Historia; the settings button SHALL open Asetukset. The home screen SHALL have no ⋮ menu.

The derived-values row SHALL show, of the latest weight, the BMI with its category word and tone,
the normal-weight range and the basal metabolic rate, each only when it can be computed. When the
owner details leave any of them out, the row SHALL end with one short hint that opens Asetukset at
Omat tiedot; when none can be computed, the hint alone SHALL be shown. Without weights the row SHALL
NOT be shown.

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

#### Scenario: No weights yet
- **WHEN** the app has written no weights
- **THEN** the card invites to weigh with the scale instead of showing a number, and no list and no
  derived-values row are shown

#### Scenario: Backup in trouble
- **GIVEN** the backup is on
- **WHEN** its last run failed or its last success is older than three days
- **THEN** the home screen shows one notice that opens Asetukset; otherwise nothing about the backup

#### Scenario: All details set
- **GIVEN** height 180 cm, birth year 1990, gender Mies and the latest weight 82.0 kg in 2026
- **WHEN** the home screen is shown
- **THEN** the row shows BMI 25,3 "ylipaino" in the yellow tone, normaalipaino 60–81 kg and
  1 770 kcal/vrk, and no hint

#### Scenario: Gender missing
- **GIVEN** height and birth year set, gender Ei valittu
- **WHEN** the home screen is shown
- **THEN** the row shows the BMI and the normal-weight range and a hint; tapping the hint opens
  Asetukset at Omat tiedot

#### Scenario: No details
- **GIVEN** no owner details are set
- **WHEN** the home screen is shown
- **THEN** only the hint is shown under the card
