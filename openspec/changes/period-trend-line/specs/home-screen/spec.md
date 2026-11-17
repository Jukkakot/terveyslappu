# Delta for home-screen

## MODIFIED Requirements

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
  weight scale with `kg` at its left edge and the weights as dots when they fit (weight-chart "Dots when they fit"), and on 3 kk the trend line (weight-chart "Trend line on long periods").
- **Food card.** Small: as described in Food row on the home screen. Wide: the title, today's kcal
  and the treats' share on top, the bars across the full width, then one row with the period label,
  the average in its long form ("keskiarvo 1 900 kcal/pv"). Big: the
  wide look with taller bars, the top of the kcal scale (the Energia tab's rule, e.g. "2 500 kcal") at the bars' top left, and
  a dashed line across the bars at the period's average when there is one (the trend line on 3 kk, weight-chart "Trend line on long periods").
- **Steps card.** Small: as described in Steps card on the home screen. Wide: the title and today's
  steps on top, the bars across the full width, then one row with the period label and the average
  in its long form ("keskiarvo 7 800 askelta/pv"). Big: the wide look with taller bars, the top of
  the steps scale (the Askeleet tab's rule, e.g. "14 000 askelta") at the bars' top left, and a dashed line at the period's average when there is one (the trend line on 3 kk).
- A card without its permission, unavailable, loading or failed SHALL show the same text or
  indicator as today at every size, centred in the card's free space, with its tap unchanged.

#### Scenario: Small weight card
- **GIVEN** the weight card is small and the latest weight is 82,4 kg, −0,5 kg from the one before
- **THEN** the card shows 82,4 kg, the change with its arrow, the small chart and the period label,
  all within the left half

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

#### Scenario: A week's spike shows
- **GIVEN** the weight card is wide, 1 vk, weights 97,3, 97,2, 98,0, 97,2, 97,2, 97,1 and 97,0 kg
- **THEN** the plot is at least 56 dp high and the 98,0 kg point is at least a quarter of the plot's
  height above its 97,2 kg neighbours (the 96,5–98,5 kg scale)
