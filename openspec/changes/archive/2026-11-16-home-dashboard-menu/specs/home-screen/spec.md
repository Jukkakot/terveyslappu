## ADDED Requirements

### Requirement: App menu
The front page and Historia SHALL have the same top bar actions: the help button (opens Ohje) and,
at the end, a ⋮ menu button. The menu SHALL hold, in this order: "Punnitse" (opens the weigh-in
dialog), "Lisää paino käsin" (opens the manual entry dialog), "Lisää annos" (opens the food add
sheet for today), a divider, and "Asetukset" (opens Asetukset). Each item SHALL have a leading icon (scale, add,
cutlery, gear). There SHALL be no settings (gear) button and no add (+) button in either top bar, and
no action button inside a front page card. The menu SHALL be the same on every Historia tab and SHALL
NOT be on other screens (Ruoka keeps its own adding). The menu button SHALL be named "Lisää
toimintoja" for TalkBack.

#### Scenario: Back to where it was opened
- **GIVEN** Historia is shown
- **WHEN** the user opens Asetukset, Ohje, the weigh-in dialog or the add sheet from its top bar
- **THEN** back or closing returns to Historia

#### Scenario: Food always for today
- **GIVEN** Historia on Syöty with a past day chosen
- **WHEN** the user taps ⋮ and then Lisää annos
- **THEN** the add sheet opens for today

#### Scenario: Without Health Connect
- **GIVEN** Health Connect access is missing
- **WHEN** the user opens ⋮
- **THEN** the menu is shown and each item opens its target, which explains and asks for access as
  it does today

#### Scenario: Edit mode closes the menu
- **GIVEN** the menu is open
- **WHEN** edit mode starts or another intent arrives
- **THEN** the menu closes and the edit bar replaces the help and menu buttons

#### Scenario: Weigh from the menu
- **WHEN** the user taps ⋮ on the front page and then Punnitse
- **THEN** the weigh-in dialog opens

#### Scenario: Manual entry from Historia
- **WHEN** the user taps ⋮ on Historia and then Lisää paino käsin
- **THEN** the manual entry dialog opens with the date and time set to now

#### Scenario: Add food from the menu
- **WHEN** the user taps ⋮ on the front page and then Lisää annos
- **THEN** the add sheet opens for today with the keyboard up

#### Scenario: Settings from the menu
- **WHEN** the user taps ⋮ and then Asetukset
- **THEN** Asetukset opens; back returns to the screen the menu was opened on

#### Scenario: No gear or plus
- **WHEN** the front page or Historia is shown
- **THEN** its top bar shows the help button and ⋮ only

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
time, the change from the weight before it with a direction sign, not by colour alone), a small line
chart of the chosen period without axes. The card SHALL hold no button. The small chart SHALL span the whole
chosen period ending now, use the weight scale of the weight chart, and show under it the first and
last day of the period, the first and the latest weight of the period with `kg`, and the change over
it. Both changes SHALL show a small arrow up for a gain and down for a loss next to the number, and
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
- **THEN** the card shows 82,4 kg, "tänään 7.05" and −0,5 kg with an arrow down, and no list of
  weights is shown

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
- **THEN** the home screen shows one notice that opens Asetukset at Tiedot ja varmuuskopio;
  otherwise nothing about the backup

### Requirement: Historia screen
Historia SHALL hold the period chips, the chart, the period statistics and the list of this app's
weights with delete and undo, and the App menu in its top bar (manual entry is "Lisää paino käsin" there).

#### Scenario: Historia contents
- **WHEN** the user opens Historia
- **THEN** it shows the chosen period's chips, chart, statistics and weights, newest first

### Requirement: Food row on the home screen
The food card SHALL be built like the weight card: "Syöty tänään" with today's total kcal large and
the treats' share (or "ei kirjauksia"), small daily kcal bars of the card's own period (7, 30 or 90
days ending today, chosen from its own menu and remembered, 7 by default) with the treats' share in
its tone as on Energia, and under them the period's first and last day and its average kcal per
logged day (today left out, as on Energia). A tap on the card's title or today's number SHALL open Ruoka on
today; a tap on the bars or what is under them (not the period label) SHALL open Historia on
Energia. There SHALL be no change arrow from the previous day and no separate button for today.
The card SHALL hold no button (adding is "Lisää annos" in the App menu, the widget or Ruoka). At small size the period label and the average SHALL
share one row (the average shortens to "ka 1 900 kcal/pv", wrapping to two lines when it does not fit) and the bars take the rest of the height; all parts SHALL stay readable and tappable (44 dp touch targets). Its wide and big looks
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
- **THEN** the card reads "Syöty tänään: ei kirjauksia" with no warning tone

### Requirement: Card sizes
Every card SHALL have a defined look at each of the three sizes, with nothing cut off and every tap
target at least 44 dp, at the system's default text size and up to 130 % of it. A card's small
chart SHALL take the height its size leaves free, so a bigger card has a taller chart, and its plot
(the chart's height inside its inset; for an Allekkain own chart the panels together) SHALL be at
least 56 dp high at every size and text size, so a change in the data reads at a glance (user
2026-11-16). To make room the small weight card's title SHALL be one line with the day and time
only ("eilen 7.05", ellipsized), and the change chip SHALL sit under the number. The card's
own taps and period label SHALL work the same at every size.

- **Weight card.** Wide: as described in Simple home screen. Small: the title row with the latest
  weight's day and time, the latest weight with `kg` and the change chip (under the number when it
  does not fit beside it), the small chart, the first and last day of the period, the period label
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

### Requirement: Card edit mode
A long-press on any part of any card SHALL start edit mode, with a haptic tick, and SHALL NOT also
trigger what that part does on a tap. Each card SHALL also offer the TalkBack action "Muokkaa
etusivua". Edit mode SHALL be unmistakable: the top bar SHALL become an edit bar in a different tone
with "Peruuta" (a close icon) at the start, the title "Muokkaa etusivua" and a "Valmis" text button
at the end, replacing the help and ⋮ menu buttons; every card SHALL be drawn at its size with its
real, live content dimmed under an outline, and with a strip across its top holding a drag handle,
the card's name and a menu button. While editing, the cards' own taps, period labels and
menus SHALL do nothing, and the notices SHALL be hidden.

The card's menu SHALL hold the sizes Pieni, Leveä and Iso (the current one marked), "Siirrä
aiemmaksi", "Siirrä myöhemmäksi" and "Piilota" (see Hidden cards); a move item SHALL be disabled at
the end it cannot pass. A card SHALL be dragged to a new place by a long-press anywhere on it or a
press on its handle; the long-press that starts edit mode SHALL continue into a drag when the
finger, still held, then moves; the other cards SHALL make room as it moves, and the layout SHALL be drawn as it
will be after the drop. Under the grid edit mode SHALL show "Lisää kortti" (see Hidden cards) and
"Palauta oletus", which restores the default layout and is disabled when the edited layout already
is the default. For TalkBack each card SHALL read its name, its place and its size ("Ruoka, 2/3,
pieni") and SHALL offer the actions of its menu; after a move or a resize the new place SHALL be announced.

Edits SHALL change only the edited layout. "Valmis" SHALL save the edited layout, show it with the
cards working again and leave edit mode. "Peruuta" and back SHALL leave edit mode and discard every
edit, without asking. Edit mode and its edits SHALL survive rotation and the app going to the
background; when any other intent arrives (another screen, the food sheet, the weigh-in, a notification action)
or Health Connect access is lost, edit mode SHALL end first and its edits SHALL be discarded.

#### Scenario: Enter by long-press
- **WHEN** the user presses and holds the food card's title
- **THEN** edit mode starts, Ruoka does not open, and the edit bar reads "Muokkaa etusivua"
  with Peruuta and Valmis

#### Scenario: Long-press continues into a drag
- **GIVEN** the default layout
- **WHEN** the user presses and holds the steps card, edit mode starts, and without lifting the
  finger the user moves it above the weight card and releases
- **THEN** edit mode is on and the order is steps, weight, food

#### Scenario: Drag from the card body
- **GIVEN** edit mode
- **WHEN** the user long-presses the food card's body and drags it before the weight card
- **THEN** the order is food, weight, steps, and a plain tap on a card still does nothing

#### Scenario: Outside intent
- **GIVEN** edit mode with unsaved edits
- **WHEN** the user taps the "Lisää annos" widget
- **THEN** edit mode has ended with its edits discarded and the add sheet opens

#### Scenario: Cards inert while editing
- **GIVEN** edit mode
- **WHEN** the user taps a period label, the weight card or the steps card
- **THEN** nothing opens and nothing changes

#### Scenario: Drag a card
- **GIVEN** edit mode with weight (wide), food (small), steps (small)
- **WHEN** the user drags the steps card before the weight card and drops it
- **THEN** the grid shows steps (left half, right half empty), then weight, then food (left half)

#### Scenario: Change a size
- **GIVEN** edit mode
- **WHEN** the user opens the weight card's menu and picks Pieni
- **THEN** the weight card becomes half width and the food card moves up beside it

#### Scenario: Move with the menu
- **GIVEN** edit mode with the order weight, food, steps
- **WHEN** the user picks "Siirrä aiemmaksi" in the steps card's menu
- **THEN** the order is weight, steps, food, and the first card's "Siirrä aiemmaksi" is disabled

#### Scenario: Save with Valmis
- **GIVEN** edit mode with steps moved first
- **WHEN** the user taps Valmis
- **THEN** the front page shows steps first with all cards working, and the layout stays after a restart

#### Scenario: Discard with back
- **GIVEN** edit mode with steps moved first and the food card made wide
- **WHEN** the user presses back or taps Peruuta
- **THEN** the front page shows the layout from before edit mode, and no question is asked

#### Scenario: Restore default
- **GIVEN** a saved layout other than the default
- **WHEN** the user taps Palauta oletus in edit mode and then Valmis
- **THEN** the default layout is shown and Palauta oletus was disabled after the tap

#### Scenario: Rotation
- **GIVEN** edit mode with unsaved edits
- **WHEN** the device is rotated
- **THEN** edit mode and the edits stay

#### Scenario: Screen reader
- **GIVEN** TalkBack and edit mode with the order weight, food, steps, food small
- **THEN** the food card reads "Ruoka, 2/3, pieni" and offers its move, size and hide actions

### Requirement: Hidden cards
"Piilota" in a card's menu SHALL take the card off the edited layout and SHALL be disabled when it is
the only shown card. "Lisää kortti" under the grid in edit mode SHALL open a scrolling menu of the hidden
cards (name and icon); choosing one SHALL put it last in the edited layout at the size it had and
close the list. "Lisää kortti" SHALL always be enabled: after the hidden cards it lists the own charts
not on the front page (added wide) and last "Uusi kaavio" (see Chart cards). A hidden card SHALL NOT be
shown on the front page and its features SHALL stay reachable elsewhere as before (weigh-in from the
widget, tile, shortcut and the ⋮ menu; Ruoka and Askeleet from Historia).

#### Scenario: Hide a card
- **GIVEN** edit mode
- **WHEN** the user picks Piilota on the steps card and taps Valmis
- **THEN** the front page shows only the weight and food cards

#### Scenario: Bring it back
- **GIVEN** the steps card is hidden and was small
- **WHEN** the user taps Lisää kortti in edit mode and picks Askeleet
- **THEN** the steps card appears last, small, and the menu closes

#### Scenario: Last card stays
- **GIVEN** weight and food are hidden
- **WHEN** the user opens the steps card's menu in edit mode
- **THEN** Piilota is disabled

#### Scenario: Nothing hidden
- **GIVEN** no card is hidden
- **THEN** Lisää kortti is disabled
