## MODIFIED Requirements

### Requirement: Food row on the home screen
The food card SHALL be built like the weight card: the title "Syöty" with today's total kcal large and
the treats' share (or "ei kirjauksia"), small daily kcal bars of the card's own period (7, 30 or 90
days ending today, chosen from its own menu and remembered, 7 by default) with the treats' share in
its tone as on Energia, and under them the period's first and last day and its average kcal per
logged day (today left out, as on Energia). A tap on the card's title or today's number SHALL open Ruoka on
today; a tap on the bars or what is under them (not the period label) SHALL open Historia on
Energia. There SHALL be no change arrow from the previous day and no separate button for today.
The card SHALL hold no button (adding is "Lisää annos" in the App menu, the widget or Ruoka). At small size the period label and the average SHALL
share one row (the average reads "keskiarvo 1 900 kcal/pv", wrapping to two lines when it does not fit) and the bars take the rest of the height; all parts SHALL stay readable and tappable (44 dp touch targets). Its wide and big looks
are in Card sizes. The card SHALL be shown also when no weights exist yet; without the nutrition
permission it SHALL read "Ruoka: anna lupa" and a tap SHALL ask for it.

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
- **WHEN** the user taps the food card's bars
- **THEN** Historia opens on Energia; back returns to the home screen

#### Scenario: Half width
- **GIVEN** the food card is small, on a phone of about 6 inches in portrait
- **WHEN** the front page is shown
- **THEN** the food card fills its half with its number, bars and period label all visible and none cut off

#### Scenario: Nothing logged
- **WHEN** nothing is logged today
- **THEN** the card reads "Syöty" and "ei kirjauksia" with no warning tone

### Requirement: Steps card on the home screen
The steps card SHALL be built like the food card: the title "Askeleet", today's steps large with "askelta" beside them in a smaller type, as the food card's "kcal", and "tänään" under them
(thin-space thousands, "6 420"), small daily bars of the card's own period (7, 30 or 90 days ending
today, chosen from its own menu, remembered, 7 by default and separate from the weight and food
periods) in one tone, and under them the period's first and last day and the average ("keskiarvo
7 800 askelta/pv", "keskiarvo 7 800 askelta/pv" at small size, wrapping to two lines when it does not fit) of days with steps, today left out. Its wide
and big looks are in Card sizes. The card has no quick action, no change arrow and no goal. A tap
anywhere on the card except its period label SHALL open Historia on Askeleet; back SHALL return to
the home screen. The card SHALL be shown also when no weights exist yet and SHALL reload when the
app is resumed and when the home screen is shown.

#### Scenario: Steps today
- **GIVEN** Health Connect holds 6 420 steps for today
- **WHEN** the front page is shown
- **THEN** the steps card shows 6 420 with "askelta" beside it and a bar per day with steps of the last 7 days

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
- **THEN** the card keeps the title "Askeleet", shows "ei tietoa" in the number slot and
  "ei vielä keskiarvoa" for the average

### Requirement: Card sizes
Every card SHALL have a defined look at each of the three sizes, with nothing cut off and every tap
target at least 44 dp, at the system's default text size and up to 130 % of it. A card's small
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
  wide look with taller bars, the top of the kcal scale (the Energia tab's rule, e.g. "2 500 kcal") at the bars' top left, and
  a dashed line across the bars at the period's average when there is one.
- **Steps card.** Small: as described in Steps card on the home screen. Wide: the title and today's
  steps on top, the bars across the full width, then one row with the period label and the average
  in its long form ("keskiarvo 7 800 askelta/pv"). Big: the wide look with taller bars, the top of
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

### Requirement: Chart cards
An own chart (weight-chart "Custom charts") SHALL be a card type of the front page's card grid. In
edit mode "Lisää kortti" SHALL list, after the hidden feature cards, the own charts that are not on
the front page, in the order of their Historia tabs, and a last item "Uusi kaavio"; "Lisää kortti"
SHALL therefore always be enabled. Choosing a chart SHALL put its card last in the edited layout at
wide size; "Uusi kaavio" SHALL open the chart editor, and saving there SHALL create the chart (in
Historia at once, also if the front page edits are later discarded) and put its card last in the
edited layout at wide size. "Piilota" on a chart card SHALL take it off the front page (it is not
listed among the hidden feature cards); the chart stays in Historia and in the "Lisää kortti" list,
from where it returns wide. A deleted chart's card SHALL disappear from the front page; when it was
the only shown card, the default layout (the three feature cards) SHALL be shown. "Palauta oletus"
SHALL take every chart card off the edited layout, and a layout with a chart card SHALL NOT count as
the default; the charts stay in Historia.

A chart card SHALL show the chart's name as its title after a neutral icon tile with the chart
icon (app-appearance "Card look") and with a chevron, the chart as
defined (its data, styles and layout, real daily values as in Historia) over the chart's own period,
filling the card's free space, and the period as a plain label ("1 kk", "Kaikki", "1.9.–15.10.").
The period SHALL be the one stored in the chart and shown on its Historia tab; it SHALL be changed
only there, and the card's label SHALL NOT be tappable. By size: small: the title, the chart
without scale labels or dates, the period label; wide: also the first and last day under the chart
and the legend; big: also the top of each scale with its unit (Allekkain: per panel, so two panels on a shared kcal scale show the same top; Päällekkäin:
one top per value axis, on that axis's side and in its tone, so a shared kcal scale shows one
neutral top and a chart whose drawn data share one scale shows only a left top). On a card data without permission or without values SHALL be left out silently
(Allekkain: its panel shows a short "anna lupa" or "ei tietoa" line); a chart whose shown data all
lack values SHALL read "Ei tietoja tällä jaksolla". The card SHALL have no crosshair: a tap anywhere
on it SHALL open Historia on the chart's tab, and back SHALL return to the front page.

#### Scenario: Add a chart card
- **GIVEN** the chart "Liike" exists and is not on the front page
- **WHEN** the user taps Lisää kortti in edit mode, picks Liike and taps Valmis
- **THEN** a wide Liike card is last on the front page

#### Scenario: Charts in tab order
- **GIVEN** the charts "Liike" (made first) and "Ilta" are not on the front page
- **WHEN** the user taps Lisää kortti
- **THEN** the list reads Liike, Ilta, Uusi kaavio

#### Scenario: New chart from the front page
- **GIVEN** edit mode
- **WHEN** the user picks Uusi kaavio in Lisää kortti and saves the editor
- **THEN** the new chart's card is last in the edited layout and the chart has a Historia tab

#### Scenario: Open from the card
- **WHEN** the user taps the Liike card's chart
- **THEN** Historia opens on the Liike tab; back returns to the front page

#### Scenario: Period follows the tab
- **GIVEN** the Liike card shows 1 kk
- **WHEN** the user opens the Liike tab and picks 3 kk
- **THEN** back on the front page the Liike card's label reads 3 kk and its chart spans 90 days

#### Scenario: Small chart card
- **GIVEN** the Liike card is small
- **THEN** it shows the name, the chart and the period label, all inside its half, and no dates

#### Scenario: Hide keeps the chart
- **WHEN** the user hides the Liike card and taps Valmis
- **THEN** the card is gone from the front page, the Liike tab is still in Historia, and Lisää kortti lists Liike

#### Scenario: Hidden chart returns wide
- **GIVEN** the Liike card was small and hidden
- **WHEN** the user picks Liike in Lisää kortti
- **THEN** its card is last and wide

#### Scenario: Restore default clears charts
- **GIVEN** a Liike card on the front page
- **WHEN** the user taps Palauta oletus in edit mode and then Valmis
- **THEN** the default layout is shown without the Liike card, and the Liike tab is still in Historia

#### Scenario: Deleted chart
- **GIVEN** the Liike card is on the front page
- **WHEN** the user deletes Liike in the editor
- **THEN** the front page no longer shows it

#### Scenario: Deleted the only shown card
- **GIVEN** weight, food and steps are hidden and the Liike card is the only shown card
- **WHEN** the user deletes Liike
- **THEN** the front page shows the default layout with the weight, food and steps cards

#### Scenario: Eaten and burned on a big card
- **GIVEN** a big card of a chart with Syöty and Kulutus, Päällekkäin, highest day 2 800 kcal
- **THEN** one scale top "3 500 kcal" is shown, top left, in the neutral tone, and none on the right

## ADDED Requirements

### Requirement: Card titles
Every front page card's title SHALL be its quantity's name and nothing else: "Paino", "Syöty",
"Askeleet", "Kulutus" (an own chart card: the chart's name), after the quantity's icon tile
(app-appearance "Card look"), in full at every size and at 130 % text size. The weight card SHALL
show the latest weight's day and time after its title in the muted tone ("Paino · tänään 7.05");
at small size, where both do not fit, the day and time SHALL be shortened first ("…"), never the
title. The weight card SHALL read "Paino" also when no weights exist yet and in edit mode. Only the
steps card shows "tänään" under its number; the food card shows the treats' share there and the
Kulutus card "aktiivinen N kcal", as today. Without permission a card SHALL keep its title and
read "anna lupa" in the number's place (no "Ruoka:" / "Askeleet:" / "Kulutus:" prefix); its
tap is unchanged. The
period's change SHALL stay where it is today (under the chart, by the range text or the period
label); no change SHALL be shown next to the big number.

#### Scenario: Small cards
- **GIVEN** the food and steps cards share a row at small size
- **THEN** their titles read "Syöty" and "Askeleet" in full, with no "…"

#### Scenario: No weights yet
- **GIVEN** no weights exist
- **THEN** the weight card's title reads "Paino"
