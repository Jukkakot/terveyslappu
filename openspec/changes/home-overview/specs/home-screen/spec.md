## MODIFIED Requirements

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

### Requirement: Food row on the home screen
The food card SHALL show "Syöty tänään" with today's total kcal and the treats' share (or "ei
kirjauksia" when nothing is logged) and, inside the card, an outlined "Lisää annos" that opens the
add field for today. Tapping the card elsewhere SHALL open the Ruoka screen on today. Neither SHALL
be a filled button. The card SHALL be shown also when no weights exist yet.

#### Scenario: Something eaten
- **GIVEN** today 1 240 kcal is logged, 230 of it Herkku
- **WHEN** the front page is shown
- **THEN** the food card reads "Syöty tänään 1 240 kcal · herkkuja 230" and tapping it opens Ruoka on today

#### Scenario: Add from the front page
- **WHEN** the user taps Lisää annos
- **THEN** the add field opens for today with the keyboard up

#### Scenario: Nothing logged
- **WHEN** nothing is logged today
- **THEN** the card reads "Syöty tänään: ei kirjauksia" with no warning tone

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
