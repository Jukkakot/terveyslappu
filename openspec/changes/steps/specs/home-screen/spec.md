## MODIFIED Requirements

### Requirement: Simple home screen
The home screen SHALL show, under the help and settings buttons of the top bar, one card per
feature: the weight card on top across the whole width, and under it a row of the smaller feature
cards: the food card in the left half and the steps card in the right half. The home screen with its notices SHALL fit a phone screen in portrait without scrolling.
The home screen SHALL NOT list single weights, SHALL NOT show the BMI, the normal-weight range or
the automatic weigh-in state, and SHALL have no ⋮ menu and no action outside the cards.

The weight card SHALL show the latest weight this app wrote (large, with its date and time, the
change from the weight before it with a direction sign, not by colour alone), a small line chart of
the chosen period without axes, and a filled "Punnitse" button that opens the weigh-in dialog; it
SHALL be the only filled action on the home screen. The small chart SHALL span the whole chosen
period ending now, use the weight scale of the weight chart, and show under it the first and last
day of the period, the first and the latest weight of the period with `kg`, and the change over it.
Both changes SHALL show a small arrow up for a gain and down for a loss next to the number, and no
arrow when the shown value is zero. Tapping the weight card elsewhere than its button or period
label SHALL open Historia; the help button SHALL open Ohje; the settings button SHALL open
Asetukset.

Feature cards SHALL follow one pattern (parity, user 2026-11-14): the latest value, a small chart of
the card's own chosen period, the feature's own quick action inside the card when it has one, and a
tap that opens its details; a part that means nothing for a quantity is left out. The order SHALL
be fixed for now.

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

#### Scenario: Steady weight
- **GIVEN** the period's weights differ by less than 0.1 kg
- **WHEN** the home screen is shown
- **THEN** the small chart's line looks flat

#### Scenario: Weigh from the card
- **WHEN** the user taps Punnitse in the weight card
- **THEN** the weigh-in dialog opens, and Historia does not open

#### Scenario: Fits without scrolling
- **GIVEN** weights, food and steps today and the "?" notice shown, on a phone of about 6 inches in portrait
- **WHEN** the home screen is shown
- **THEN** the weight card, the food and steps cards side by side and the notice are fully visible without scrolling

#### Scenario: Automatic weigh-in off
- **GIVEN** automatic weigh-in is switched on or off
- **WHEN** the home screen is shown
- **THEN** only the weight, food and steps cards are shown, and nothing about automatic weigh-in

#### Scenario: All details set
- **GIVEN** height 180 cm, birth year 1990, gender Mies and the latest weight 82.0 kg in 2026
- **WHEN** the home screen is shown
- **THEN** neither the BMI nor the normal-weight range is shown; Historia's BMI tab shows them

#### Scenario: No details
- **GIVEN** no owner details are set
- **WHEN** the home screen is shown
- **THEN** no "BMI vaatii pituuden" or other owner-details reason is shown

#### Scenario: Gender missing
- **GIVEN** height and birth year set, gender Ei valittu
- **WHEN** the home screen is shown
- **THEN** the home screen looks the same as with all details set

#### Scenario: Open Historia
- **WHEN** the user taps the weight card outside its button and period label
- **THEN** Historia opens; back returns to the home screen

#### Scenario: Open Asetukset
- **WHEN** the user taps the settings button
- **THEN** Asetukset opens; back returns to the home screen

#### Scenario: Open Ohje
- **WHEN** the user taps the help button
- **THEN** Ohje opens; back returns to the home screen

#### Scenario: No weights yet
- **WHEN** the app has written no weights
- **THEN** the weight card invites to weigh with its Punnitse button instead of showing a number,
  and the food and steps cards are still shown

#### Scenario: Backup in trouble
- **GIVEN** the backup is on
- **WHEN** its last run failed or its last success is older than three days
- **THEN** the home screen shows one notice that opens Asetukset at Tiedot ja varmuuskopio;
  otherwise nothing about the backup

### Requirement: Food row on the home screen
The food card, the left half of the row, SHALL be built like the weight card: "Syöty tänään" with today's total kcal large and
the treats' share (or "ei kirjauksia"), small daily kcal bars of the card's own period (7, 30 or 90
days ending today, chosen from its own menu and remembered, 7 by default) with the treats' share in
its tone as on Energia, and under them the period's first and last day and its average kcal per
logged day (today left out, as on Energia). Inside the card SHALL be an outlined "+ Annos" that
opens the add field for today. A tap on the card's title or today's number SHALL open Ruoka on
today; a tap on the bars or what is under them (not the period label) SHALL open Historia on
Energia. There SHALL be no change arrow from the previous day and no separate button for today.
Nothing on the card SHALL be a filled button. Its parts SHALL stay readable and tappable in half the width (44 dp touch targets, the average line may wrap or shorten), and it SHALL be as tall as the steps card. The card SHALL be shown also when no weights exist
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
- **WHEN** the user taps + Annos
- **THEN** the add field opens for today with the keyboard up

#### Scenario: Today's foods
- **WHEN** the user taps today's kcal on the food card
- **THEN** Ruoka opens on today; back returns to the home screen

#### Scenario: Week from the bars
- **WHEN** the user taps the food card's bars
- **THEN** Historia opens on Energia; back returns to the home screen

#### Scenario: Half width
- **WHEN** the front page is shown on a phone of about 6 inches in portrait
- **THEN** the food card fills the left half with its number, bars, period label and + Annos all visible and none cut off

#### Scenario: Nothing logged
- **WHEN** nothing is logged today
- **THEN** the card reads "Syöty tänään: ei kirjauksia" with no warning tone

## ADDED Requirements

### Requirement: Steps card on the home screen
The steps card SHALL fill the right half of the food card's row, as tall as the food card, and be
built like the food card: "Askeleet tänään" with today's steps large, small daily bars of the card's
own period (7, 30 or 90 days ending today, chosen from its own menu, remembered, 7 by default and
separate from the weight and food periods) in one tone, and under them the period's first and last
day and its average steps per day with steps (today left out, as on Askeleet). Today's number is
what Health Connect holds so far and SHALL NOT be judged against a goal. The card has no quick
action and no change arrow from the previous day. A tap anywhere on the card except its period
label SHALL open Historia on Askeleet; back SHALL return to the home screen. The card SHALL be shown
also when no weights exist yet. Without the steps permission it SHALL read "Askeleet: anna lupa" and
a tap SHALL ask for it; when Health Connect holds no steps for today it SHALL read "Askeleet tänään:
ei tietoa" without a warning tone. The card SHALL reload when the app is resumed and when the home
screen is shown, since steps arrive from another app.

#### Scenario: Steps today
- **GIVEN** Health Connect holds 6 420 steps for today
- **WHEN** the front page is shown
- **THEN** the steps card shows 6 420 and a bar per day with steps of the last 7 days

#### Scenario: Own period
- **WHEN** the user picks 30 pv in the steps card's menu
- **THEN** the bars span 30 days, the weight and food cards keep their own periods, and the choice stays after a restart

#### Scenario: Open Askeleet
- **WHEN** the user taps the steps card's number or bars
- **THEN** Historia opens on Askeleet; back returns to the home screen

#### Scenario: No steps yet today
- **GIVEN** Health Connect holds no steps for today
- **THEN** the card reads "Askeleet tänään: ei tietoa" and the earlier days' bars are still shown

#### Scenario: No permission
- **GIVEN** the steps permission is not granted
- **THEN** the card reads "Askeleet: anna lupa" and the food card is unchanged
