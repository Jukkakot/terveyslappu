# home-screen Specification

## Purpose
TBD - created by archiving change main-screen-refresh. Update Purpose after archive.

## Requirements

### Requirement: Simple home screen
The home screen SHALL show, under the help and settings buttons of the top bar, one card per
feature in this order: the weight card, the food card, and, while automatic weigh-in is switched
on, the automatic weigh-in card; Punnitse vaa'alla SHALL be the only filled action, at the bottom.
The home screen SHALL NOT list single weights. The home screen SHALL have no ⋮ menu.

The weight card SHALL show the latest weight this app wrote (large, with its date and time, the
change from the weight before it with a direction sign, not by colour alone), a small line chart of
the chosen period without axes, and the latest weight's derived values. The small chart SHALL span
the whole chosen period ending now, use the weight scale of the weight chart, and show under it the
first and last day of the period, the first and the latest weight of the period with `kg`, and the
change over it. Both changes SHALL show a small arrow up for a gain and down for a loss next to the
number, and no arrow when the shown value is zero. Tapping the weight card SHALL open Historia; the
help button SHALL open Ohje; the settings button SHALL open Asetukset.

The derived values SHALL show, of the latest weight, the BMI number in its category tone without
the category word (the word SHALL stay in the screen reader text), and the normal-weight range; a
value that cannot be computed SHALL be replaced by its short reason naming the missing owner
details, which opens Asetukset at Omat tiedot. Without weights they SHALL NOT be shown.

The automatic weigh-in card SHALL show the same state line as its Asetukset row and open its
Asetukset page.

Feature cards SHALL follow one pattern (parity, user 2026-11-14): the latest value, a small chart of
the card's own chosen period, what the user does with the feature, and a tap that opens its
details; a part that means nothing for a quantity is left out. The order SHALL be fixed for now.

#### Scenario: Latest with change
- **GIVEN** the latest weights are 82.4 kg today 7.05 and 82.9 kg yesterday
- **WHEN** the home screen is shown
- **THEN** the card shows 82,4 kg, "tänään 7.05" and −0,5 kg with an arrow down, and no list of
  weights is shown

#### Scenario: 30-day change
- **GIVEN** the home period is 1 kk, the weight 30 days ago was 81.0 kg and the latest is 82.4 kg
- **WHEN** the home screen is shown
- **THEN** under the small chart the card shows `81,0 → 82,4 kg` and +1,4 kg with an arrow up

#### Scenario: Short history
- **GIVEN** the home period is 1 kk and the only weights are from yesterday and today
- **WHEN** the home screen is shown
- **THEN** the line sits at the right end of the small chart, and the dates under it still span 30 days

#### Scenario: Automatic weigh-in off
- **GIVEN** automatic weigh-in is switched off
- **WHEN** the home screen is shown
- **THEN** only the weight and food cards are shown

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
- **THEN** the card invites to weigh with the scale instead of showing a number, the food card is still shown, and no
  derived-values row is shown

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

### Requirement: Historia screen
Historia SHALL hold the period chips, the chart, the period statistics and the list of this app's
weights with delete and undo, and an add button in its top bar that opens the manual entry.

#### Scenario: Historia contents
- **WHEN** the user opens Historia
- **THEN** it shows the chosen period's chips, chart, statistics and weights, newest first

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
The weight card, its small chart, its changes and the derived values SHALL use only weights not
marked "?". When a "?" weight waits, the home screen SHALL show one notice "Punnitus odottaa
tarkistusta" above the cards that opens Historia's list scrolled to the newest "?" weight. The
notice SHALL count only marks that match a read weight. The home screen SHALL reload on the
weights-changed signal and when the app resumes.

The notice SHALL use the same warning tint (amber family, outlined, readable in both themes) as
the "?" row in Historia, and still carry its text.

#### Scenario: Questioned latest weight
- **GIVEN** 82,0 kg yesterday and 61,3 kg marked "?" today
- **WHEN** the home screen is shown
- **THEN** the card shows 82,0 kg and the notice is shown

#### Scenario: Nothing to check
- **WHEN** no weight is marked "?"
- **THEN** no notice about checking is shown

### Requirement: Food row on the home screen
The food card SHALL be built like the weight card: "Syöty tänään" with today's total kcal large and
the treats' share (or "ei kirjauksia"), small daily kcal bars of the card's own period (7, 30 or 90
days ending today, chosen from its own menu and remembered, 7 by default) with the treats' share in
its tone as on Energia, and under them the period's first and last day and its average kcal per
logged day (today left out, as on Energia). Inside the card SHALL be an outlined "Lisää annos" that
opens the add field for today and a "Tänään" button that opens Ruoka on today; a tap elsewhere on
the card SHALL open Historia on Energia. There SHALL be no change arrow from the previous day.
Nothing on the card SHALL be a filled button. The card SHALL be shown also when no weights exist
yet; without the nutrition permission it SHALL read "Ruokakirjaus: anna lupa" and a tap SHALL ask
for it.

#### Scenario: Something eaten
- **GIVEN** today 1 240 kcal is logged, 230 of it Herkku
- **WHEN** the front page is shown
- **THEN** the food card shows 1 240 kcal and "herkkuja 230", and a bar per logged day of the last 7 days

#### Scenario: Own period
- **WHEN** the user picks 30 pv in the food card's menu
- **THEN** the bars span 30 days, the weight card keeps its own period, and the choice stays after a restart

#### Scenario: Add from the front page
- **WHEN** the user taps Lisää annos
- **THEN** the add field opens for today with the keyboard up

#### Scenario: Nothing logged
- **WHEN** nothing is logged today
- **THEN** the card reads "Syöty tänään: ei kirjauksia" with no warning tone
