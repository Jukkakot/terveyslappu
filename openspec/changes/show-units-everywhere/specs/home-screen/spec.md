## MODIFIED Requirements

### Requirement: Food row on the home screen
The food card SHALL be built like the weight card: "Syöty tänään" with today's total kcal large and
the treats' share (or "ei kirjauksia"), small daily kcal bars of the card's own period (7, 30 or 90
days ending today, chosen from its own menu and remembered, 7 by default) with the treats' share in
its tone as on Energia, and under them the period's first and last day and its average kcal per
logged day (today left out, as on Energia). Inside the card SHALL be an outlined "+ Annos" that
opens the add field for today. A tap on the card's title or today's number SHALL open Ruoka on
today; a tap on the bars or what is under them (not the period label) SHALL open Historia on
Energia. There SHALL be no change arrow from the previous day and no separate button for today.
Nothing on the card SHALL be a filled button. At small size the period label and the average SHALL
share one row (the average shortens to "ka 1 900 kcal/pv", wrapping to two lines when it does not fit) and "+ Annos" SHALL sit on its own row
under it; all parts SHALL stay readable and tappable (44 dp touch targets). Its wide and big looks
are in Card sizes. The card SHALL be shown also when no weights exist yet; without the nutrition
permission it SHALL read "Ruokakirjaus: anna lupa" and a tap SHALL ask for it.

#### Scenario: Something eaten
- **GIVEN** today 1 240 kcal is logged, 230 of it Herkku
- **WHEN** the front page is shown
- **THEN** the food card shows 1 240 kcal and "herkkuja 230 kcal", and a bar per logged day of the last 7 days

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
- **GIVEN** the food card is small, on a phone of about 6 inches in portrait
- **WHEN** the front page is shown
- **THEN** the food card fills its half with its number, bars, period label and + Annos all visible and none cut off

#### Scenario: Nothing logged
- **WHEN** nothing is logged today
- **THEN** the card reads "Syöty tänään: ei kirjauksia" with no warning tone

### Requirement: Steps card on the home screen
The steps card SHALL be built like the food card: the title "Askeleet tänään", today's steps large with "askelta" beside them in a smaller type, as the food card's "kcal"
(thin-space thousands, "6 420"), small daily bars of the card's own period (7, 30 or 90 days ending
today, chosen from its own menu, remembered, 7 by default and separate from the weight and food
periods) in one tone, and under them the period's first and last day and the average ("keskim.
7 800 askelta/pv", "ka 7 800 askelta/pv" at small size, wrapping to two lines when it does not fit) of days with steps, today left out. Its wide
and big looks are in Card sizes. The card has no quick action, no change arrow and no goal. A tap
anywhere on the card except its period label SHALL open Historia on Askeleet; back SHALL return to
the home screen. The card SHALL be shown also when no weights exist yet and SHALL reload when the
app is resumed and when the home screen is shown.

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
- **GIVEN** Health Connect holds steps for earlier days but none for today
- **THEN** the card shows "ei tietoa" in the number slot and the earlier days' bars and average

#### Scenario: No steps data at all
- **GIVEN** Health Connect holds no steps
- **THEN** the card keeps the title "Askeleet tänään", shows "ei tietoa" in the number slot and
  "ei vielä keskiarvoa" for the average
