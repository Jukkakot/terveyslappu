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
size and whether a card is shown SHALL be the user's (see Card grid). The steps card opens Askeleet
from anywhere except its period label, unlike the food card's split taps (title and number open Ruoka).

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

### Requirement: Card sizes
Every card SHALL have a defined look at each of the three sizes, with nothing cut off and every tap
target at least 44 dp, at the system's default text size and up to 130 % of it. A card's small
chart SHALL take the height its size leaves free, so a bigger card has a taller chart, and its plot
(the chart's height inside its inset; for an Allekkain own chart the panels together) SHALL be at
least 56 dp high at every size and text size, so a change in the data reads at a glance (user
2026-11-16). To make room the small weight card's title SHALL be one line with the day and time
only ("eilen 7.05", ellipsized). The card's
own taps and period label SHALL work the same at every size.

- **Weight card.** Wide: as described in Simple home screen. Small: the title row with the latest
  weight's day and time, the latest weight with `kg`, the small chart, the first and last day of the period, the period label
  with the period's change; the `81,0 → 82,4 kg` range text is left out. Big: the wide look with a taller chart that shows the top and bottom of its
  weight scale with `kg` at its left edge and the weights as dots when they fit (weight-chart "Dots
  when they fit").
- **Food card.** Small: as described in Food row on the home screen. Wide: the title, today's kcal
  and the treats' share on top, the bars across the full width, then one row with the period label,
  the average in its long form ("keskim. 1 900 kcal/pv"). Big: the
  wide look with taller bars, the top of the kcal scale (the Energia tab's rule, e.g. "2 500 kcal") at the bars' top left, and
  a dashed line across the bars at the period's average when there is one.
- **Steps card.** Small: as described in Steps card on the home screen. Wide: the title and today's
  steps on top, the bars across the full width, then one row with the period label and the average
  in its long form ("keskim. 7 800 askelta/pv"). Big: the wide look with taller bars, the top of
  the steps scale (the Askeleet tab's rule, e.g. "14 000 askelta") at the bars' top left, and a dashed line at the period's
  average when there is one.
- A card without its permission, unavailable, loading or failed SHALL show the same text or
  indicator as today at every size, centred in the card's free space, with its tap unchanged.

#### Scenario: Small weight card
- **GIVEN** the weight card is small and the latest weight is 82,4 kg, −0,5 kg from the one before
- **THEN** the card shows 82,4 kg, the change with its arrow, the small chart and the period label,
  all within the left half

#### Scenario: Wide food card
- **GIVEN** the food card is wide and 1 900 kcal per logged day is the period's average
- **THEN** the average reads "keskim. 1 900 kcal/pv" and no button is on the card

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

#### Scenario: A week's spike shows
- **GIVEN** the weight card is wide, 1 vk, weights 97,3, 97,2, 98,0, 97,2, 97,2, 97,1 and 97,0 kg
- **THEN** the plot is at least 56 dp high and the 98,0 kg point is at least a quarter of the plot's
  height above its 97,2 kg neighbours (the 96,5–98,5 kg scale)
