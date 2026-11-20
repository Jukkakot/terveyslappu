## MODIFIED Requirements

### Requirement: Simple home screen
The home screen SHALL show, under the top bar (see App menu), one card per
feature, placed in the user's card grid (see Card grid; by default the weight card on top across the
whole width, and under it the food card in the left half and the steps card in the right half). The
home screen with its notices SHALL fit a phone screen in portrait without scrolling in the default
layout. The home screen SHALL NOT list single weights, SHALL NOT show the BMI, the normal-weight
range or the automatic weigh-in state, and SHALL have no action buttons on the cards (the actions are in the App menu);
the card edit mode (see Card edit mode) is started from the cards.

The weight card SHALL show, at wide size, the latest weight this app wrote (large, with its date and
time; no change from the weight before it, user 2026-11-17), a small line
chart of the chosen period without axes. The card SHALL hold no button. The small chart SHALL span the whole
chosen period ending now, use the weight scale of the weight chart, and show under it the first and
last day of the period, the first and the latest weight of the period with `kg`, and the change over
it. The period's change SHALL show a small arrow up for a gain and down for a loss next to the number, and
no arrow when the shown value is zero. Its small and big looks are in Card sizes. Tapping the weight
card elsewhere than its period label SHALL open Historia; the help button SHALL open Ohje.

Feature cards SHALL follow one pattern (parity, user 2026-11-14): the latest value, a small chart of
the card's own chosen period, no action button (actions are in the App menu, user 2026-11-16: the front page is a dashboard), and a
tap that opens its details; a part that means nothing for a quantity is left out. The order, the
size and whether a card is shown SHALL be the user's (see Card grid). Every feature card SHALL be one
tap target: a tap anywhere on it except its period label SHALL open its details (the weight card
Historia, the food card Ruoka on today, the steps card Askeleet, the Kulutus card Historia's
Kulutus tab, an own chart's card its tab); no part of a card SHALL open a different place than
the rest.

#### Scenario: Latest with change
- **GIVEN** the latest weights are 82.4 kg today 7.05 and 82.9 kg yesterday
- **WHEN** the home screen is shown
- **THEN** the card shows 82,4 kg and "tänään 7.05" with no −0,5 kg change chip at any card size,
  and no list of weights is shown

#### Scenario: 30-day change
- **GIVEN** the home period is 1 kk, the weight 30 days ago was 81.0 kg and the latest is 82.4 kg
- **WHEN** the home screen is shown
- **THEN** under the small chart the wide card shows `81,0 → 82,4 kg` and +1,4 kg with an arrow up

#### Scenario: Short history
- **GIVEN** the home period is 1 kk and the only weights are from yesterday and today
- **WHEN** the home screen is shown
- **THEN** the line sits at the right end of the small chart, and the dates under it still span 30 days

#### Scenario: Steady weight
- **GIVEN** the period's weights differ by less than 0.1 kg
- **WHEN** the home screen is shown
- **THEN** the small chart's line looks flat

#### Scenario: Weigh from the card
- **WHEN** the home screen is shown
- **THEN** the weight card holds no Punnitse button and its chart takes that room; weighing starts
  from ⋮ → Punnitse, the widget, the tile or the launcher shortcut

#### Scenario: Fits without scrolling
- **GIVEN** the default layout, weights, food and steps today and the "?" notice shown, on a phone of
  about 6 inches in portrait
- **WHEN** the home screen is shown
- **THEN** the weight card, the food and steps cards side by side and the notice are fully visible without scrolling
- Covered by design: one Robolectric layout smoke test at about 360 dp width.

#### Scenario: Automatic weigh-in off
- **GIVEN** automatic weigh-in is switched on or off
- **WHEN** the home screen is shown
- **THEN** only the shown feature cards are on it, and nothing about automatic weigh-in

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
- **WHEN** the user taps the weight card outside its period label
- **THEN** Historia opens; back returns to the home screen

#### Scenario: Open Asetukset
- **WHEN** the user taps ⋮ and then Asetukset
- **THEN** Asetukset opens; back returns to the home screen

#### Scenario: Open Ohje
- **WHEN** the user taps the help button
- **THEN** Ohje opens; back returns to the home screen

#### Scenario: No weights yet
- **WHEN** the app has written no weights
- **THEN** the weight card reads "Ei vielä painoja" and "Punnitse ⋮-valikosta tai widgetistä"
  (TalkBack: "Punnitse Lisää toimintoja -valikosta tai widgetistä") instead of a number, and the food and steps cards are still shown

#### Scenario: Backup in trouble
- **GIVEN** the backup is on
- **WHEN** its last run failed or its last success is older than three days
- **THEN** the home screen shows one notice that opens Asetukset at Tiedostot ja varmuuskopio;
  otherwise nothing about the backup

#### Scenario: One target per card
- **WHEN** the user taps any part of the weight, food, steps or Kulutus card or an own chart's card
  other than its period label
- **THEN** that card's details open, the same place whichever part was tapped

### Requirement: Food row on the home screen
The food card SHALL be built like the weight card: the title "Syöty" with today's total kcal large and
the treats' share (or "ei annoksia"), small daily kcal bars of the card's own period (7, 30 or 90
days ending today, chosen from its own menu and remembered, 7 by default) with the treats' share in
its tone as on Energia, and under them the period's first and last day and its average kcal per
logged day (today left out, as on Syöty). A tap anywhere on the card except its period label
SHALL open Ruoka on today, as one tap target with one accessible action ("Avaa päivän annokset"); the bars
SHALL NOT open Historia (Syöty's week is reached from Ruoka's Viikko or Historia's tabs). There
SHALL be no change arrow from the previous day and no separate button for today.
The card SHALL hold no button (adding is "Lisää annos" in the App menu, the widget or Ruoka). At small size the period label and the average SHALL
share one row when the average fits beside the label; otherwise the average SHALL take its own line under the label (see "Units on the cards") and the bars take the rest of the height; all parts SHALL stay readable and tappable (44 dp touch targets). Its wide and big looks
are in Card sizes. The card SHALL be shown also when no weights exist yet; without the nutrition
permission it SHALL read "Ruoka: anna lupa"; a tap SHALL ask for the permission while Android
still lets the app ask, and once it has been denied for good a tap SHALL open Ruoka on today (which
offers the grant there).

#### Scenario: Something eaten
- **GIVEN** today 1 240 kcal is logged, 230 of it Herkku
- **WHEN** the front page is shown
- **THEN** the food card shows 1 240 kcal and "herkkuja 230 kcal", and a bar per logged day of the last 7 days

#### Scenario: Own period
- **WHEN** the user picks 30 pv in the food card's menu
- **THEN** the bars span 30 days, the weight card keeps its own period, and the choice stays after a restart

#### Scenario: Add from the front page
- **WHEN** the user taps ⋮ and then Lisää annos
- **THEN** the add field opens for today with the keyboard up

#### Scenario: Today's foods
- **WHEN** the user taps today's kcal on the food card
- **THEN** Ruoka opens on today; back returns to the home screen

#### Scenario: Week from the bars
- **WHEN** the user taps the food card's bars or its average
- **THEN** Ruoka opens on today, not Historia on Syöty; back returns to the home screen, and
  Ruoka's Viikko then opens Historia on Syöty

#### Scenario: Period label keeps its menu
- **WHEN** the user taps the food card's period label
- **THEN** its period menu opens and Ruoka does not

#### Scenario: Half width
- **GIVEN** the food card is small, on a phone of about 6 inches in portrait
- **WHEN** the front page is shown
- **THEN** the food card fills its half with its number, bars and period label all visible and none cut off

#### Scenario: Nothing logged
- **WHEN** nothing is logged today
- **THEN** the card reads "Syöty" and "ei annoksia" with no warning tone

#### Scenario: No permission
- **GIVEN** the nutrition permission is not granted and can still be asked for
- **WHEN** the user taps anywhere on the food card
- **THEN** the permission is asked for

#### Scenario: Permission denied for good
- **GIVEN** the nutrition permission was denied and Android no longer shows its dialog
- **WHEN** the user taps anywhere on the food card
- **THEN** Ruoka opens on today, where the grant is offered

### Requirement: Card sizes
Every card SHALL have a defined look at each of the three sizes, with nothing cut off and every tap
target at least 44 dp, at the system's default text size and up to 200 % of it (above 130 % see
"Cards at large text"). A card's small
chart SHALL take the height its size leaves free, so a bigger card has a taller chart, and its plot
(the chart's height inside its inset; for an Allekkain own chart the panels together) SHALL be at
least 56 dp high at every size and text size, so a change in the data reads at a glance (user
2026-11-16). To make room the small weight card's title SHALL be one line, "Paino · eilen 7.05", where the day
and time are shortened first (see "Card titles"). The card's
own taps and period label SHALL work the same at every size.

- **Weight card.** Wide: as described in Simple home screen. Small: the title row with the latest
  weight's day and time, the latest weight with `kg`, the small chart, the first and last day of the period, the period label
  with the period's change; the `81,0 → 82,4 kg` range text is left out. Big: the wide look with a taller chart that shows the top and bottom of its
  weight scale with `kg` at its left edge and the weights as dots when they fit (weight-chart "Dots
  when they fit").
- **Food card.** Small: as described in Food row on the home screen. Wide: the title, today's kcal
  and the treats' share on top, the bars across the full width, then one row with the period label,
  the average in its long form ("keskiarvo 1 900 kcal/pv"). Big: the
  wide look with taller bars, the top of the kcal scale (the Syöty tab's rule, e.g. "2 500 kcal") at the bars' top left, and
  a dashed line across the bars at the period's average when there is one.
- **Steps card.** Small: as described in Steps card on the home screen. Wide: the title and today's
  steps on top, the bars across the full width, then one row with the period label and the average
  in its long form ("keskiarvo 7 800 askelta/pv"). Big: the wide look with taller bars, the top of
  the steps scale (the Askeleet tab's rule, e.g. "14 000 askelta") at the bars' top left, and a dashed line at the period's
  average when there is one.
- A card without its permission, unavailable, loading or failed SHALL show the same text or
  indicator as today at every size, centred in the card's free space, with its tap unchanged.

#### Scenario: Small weight card
- **GIVEN** the weight card is small, the period is 1 kk, the weight 30 days ago was 82,9 kg and the
  latest is 82,4 kg
- **THEN** the card shows 82,4 kg, the period's change −0,5 kg with its arrow, the small chart and
  the period label, all within the left half

#### Scenario: Wide food card
- **GIVEN** the food card is wide and 1 900 kcal per logged day is the period's average
- **THEN** the average reads "keskiarvo 1 900 kcal/pv" and no button is on the card

#### Scenario: Big steps card
- **GIVEN** the steps card is big and the period's highest day has 11 400 steps
- **THEN** the bars are taller than at wide size, the scale's top label reads "14 000 askelta" and a
  dashed line marks the average

#### Scenario: Same row, same height
- **GIVEN** the food and steps cards share a row and the food card holds more
- **THEN** both cards end at the same line and the steps card's free space is under its bars

#### Scenario: Big text
- **GIVEN** the system text size at 130 %
- **WHEN** the default layout is shown
- **THEN** no text or button of any card is cut off

#### Scenario: Largest text
- **GIVEN** the system text size at 200 %
- **WHEN** the default layout is shown and scrolled through
- **THEN** no text or button of any card is cut off and every plot is at least 56 dp high

#### Scenario: A week's spike shows
- **GIVEN** the weight card is wide, 1 vk, weights 97,3, 97,2, 98,0, 97,2, 97,2, 97,1 and 97,0 kg
- **THEN** the plot is at least 56 dp high and the 98,0 kg point is at least a quarter of the plot's
  height above its 97,2 kg neighbours (the 96,5–98,5 kg scale)

## ADDED Requirements

### Requirement: Units on the cards
On every front page card a number and its unit SHALL stay on one line ("7 517 askelta", "82,4 kg",
"81,0 → 82,4 kg"), "askelta/pv" and "kcal/pv" SHALL never split at the slash, and a number SHALL
never break; one test lists every card string. A small card's average SHALL be two lines,
end-aligned beside the period label; a wide or big card's one line. Where it does not fit beside
the label it SHALL move whole under it.

#### Scenario: Small steps card
- **GIVEN** the steps card is small at 360 dp with an average of 7 517 steps per day
- **WHEN** the front page is shown
- **THEN** the average reads "keskiarvo" on one line and "7 517 askelta/pv" whole on the next, never
  "askelta/" with "pv" on a line of its own

#### Scenario: Larger text
- **GIVEN** the system text size at 130 % and the same small steps card
- **WHEN** the two-line average no longer fits beside the period label
- **THEN** the average is shown whole, both lines, under the period label

#### Scenario: Wide card at large text
- **GIVEN** the steps card is wide and the system text size at 200 %
- **WHEN** "keskiarvo 7 800 askelta/pv" no longer fits beside the period label
- **THEN** it is shown whole on its own line under the label, never split inside "askelta/pv"

#### Scenario: Range on the weight card
- **GIVEN** a wide weight card with the range "81,0 → 82,4 kg"
- **THEN** a line break may fall only beside the arrow, never between a number and "kg"

### Requirement: Cards at large text
Above 130 % of the default text size (font scale greater than 1.3) every small card SHALL be drawn
across the full width in its wide look, one card per row, in the same order, and the rows SHALL
grow with the text without an upper limit. The stored sizes SHALL stay unchanged and apply again
at 130 % or less. Edit mode SHALL draw the same grid, its size menu still marking the stored size.

#### Scenario: 200 % text
- **GIVEN** the default layout and the system text size at 200 %
- **WHEN** the front page is shown
- **THEN** the weight, food and steps cards each take a full row, and the page scrolls to show them

#### Scenario: Exactly 130 %
- **GIVEN** the default layout and the system text size at 130 %
- **WHEN** the front page is shown
- **THEN** the food and steps cards still share a row

#### Scenario: Edit mode at large text
- **GIVEN** the food card is small and the system text size at 200 %
- **WHEN** the user enters the card edit mode
- **THEN** the food card is drawn across the full width as on the front page and its size menu still
  marks Pieni

#### Scenario: Back to normal text
- **GIVEN** the food card is small and the text size goes from 200 % back to 100 %
- **WHEN** the front page is shown
- **THEN** the food and steps cards share a row again
