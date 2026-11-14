# home-screen Specification

## Purpose
TBD - created by archiving change main-screen-refresh. Update Purpose after archive.

## Requirements

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

### Requirement: Historia screen
Historia SHALL hold the period chips, the chart, the period statistics and the list of this app's
weights with delete and undo, and an add button in its top bar that opens the manual entry.

#### Scenario: Historia contents
- **WHEN** the user opens Historia
- **THEN** it shows the chosen period's chips, chart, statistics and weights, newest first

### Requirement: Asetukset screen
Asetukset SHALL group, with section titles: Ulkoasu (theme), Tiedot (Tuo tiedot, Vie tiedot),
Varmuuskopio (state and its actions) and Tuki (Jaa loki, the version).

#### Scenario: Asetukset contents
- **WHEN** the user opens Asetukset
- **THEN** the theme choice, the import and export, the backup and Jaa loki with the version are
  shown in those groups

### Requirement: Home chart period
The home card's small chart SHALL show the last 7 days the first time. A period label under the
chart (`1 vk`, `1 kk` or `3 kk` with a small menu sign) SHALL open a menu with 1 vk, 1 kk and 3 kk;
choosing one SHALL redraw the chart and what is under it at once. Tapping the label SHALL NOT open
Historia. The choice SHALL be remembered between app starts and SHALL be separate from Historia's
period.

#### Scenario: First time
- **GIVEN** the user has never chosen a home period
- **WHEN** the home screen is shown
- **THEN** the small chart shows the last 7 days and the label reads 1 vk

#### Scenario: Change the period
- **WHEN** the user taps the label and picks 1 kk
- **THEN** the small chart and the dates, weights and change under it cover the last 30 days, and
  Historia does not open

#### Scenario: Remembered and separate
- **GIVEN** the home period is 3 kk and Historia's period is 1 v
- **WHEN** the app is opened again
- **THEN** the home card shows 3 kk and Historia 1 v

### Requirement: Questioned weights on the home screen
The home card, its small chart, its changes and the derived-values row SHALL use only weights not
marked "?". The three latest weights SHALL include a "?" weight with its "?" sign and no change.
When a "?" weight waits, the home screen SHALL show one notice "Punnitus odottaa tarkistusta" that
opens Historia's list scrolled to the newest "?" weight. The notice SHALL count only marks that
match a read weight. The home screen SHALL reload on the weights-changed signal and when the app
resumes.

The notice SHALL use the same warning tint (amber family, outlined, readable in both themes) as
the "?" row in Historia, and still carry its text.

#### Scenario: Questioned latest weight
- **GIVEN** 82,0 kg yesterday and 61,3 kg marked "?" today
- **WHEN** the home screen is shown
- **THEN** the card shows 82,0 kg, the list starts with 61,3 kg "?", and the notice is shown

#### Scenario: Nothing to check
- **WHEN** no weight is marked "?"
- **THEN** no notice about checking is shown
