# home-screen Specification

## Purpose
TBD - created by archiving change main-screen-refresh. Update Purpose after archive.

## Requirements

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

### Requirement: Historia screen
Historia SHALL hold the period chips, the chart, the period statistics and the list of this app's
weights with delete and undo, and the App menu in its top bar (manual entry is "Lisää paino käsin" there).

#### Scenario: Historia contents
- **WHEN** the user opens Historia
- **THEN** it shows the chosen period's chips, chart, statistics and weights, newest first

### Requirement: Asetukset screen
Asetukset SHALL be a list of rows, each with an icon, a name, one line of its current state and a
chevron, and each SHALL open its own sub-screen holding what was its section: Omat tiedot,
Automaattinen punnitus, Tiedostot ja varmuuskopio (Tuo tiedostosta, Vie tiedostoon and the backup; shown only
with the Health Connect permission, as before), Omat ruuat, Askeleet (the steps source), Ulkoasu
(theme) and Tuki (Jaa loki, Kopioi loki, the version). The sections' contents and behaviour SHALL
stay as they were. A section's warning (backup failed or late, weighings waiting to be saved,
listening blocked, a chosen steps source without steps) SHALL show on its row's state line in the
warning tint. A link elsewhere that opens Asetukset at a section SHALL open that sub-screen
directly; back from a sub-screen SHALL return to where it was opened from: the Asetukset list when
opened from it, otherwise the screen holding the link.

#### Scenario: Asetukset contents
- **WHEN** the user opens Asetukset
- **THEN** seven rows are shown with their state lines, in the order Omat tiedot, Automaattinen
  punnitus, Tiedostot ja varmuuskopio, Omat ruuat, Askeleet, Ulkoasu and Tuki, e.g. Omat tiedot
  "180 cm · 1990 · Mies", Automaattinen punnitus "Päällä 6.00–10.00", Askeleet "Automaattinen:
  Garmin Connect", Ulkoasu "Teema: Järjestelmä", Tuki with the version

#### Scenario: Open a sub-screen
- **WHEN** the user taps Tiedostot ja varmuuskopio
- **THEN** its sub-screen shows Tuo tiedostosta, Vie tiedostoon and the backup; back returns to Asetukset

#### Scenario: Warning on the row
- **GIVEN** the backup's last run failed
- **WHEN** Asetukset is shown
- **THEN** the Tiedostot ja varmuuskopio row says so in the warning tint

#### Scenario: Link from home
- **WHEN** the user taps the backup notice on the home screen
- **THEN** the Tiedostot ja varmuuskopio sub-screen opens, and back returns to the home screen

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
The weight card, its small chart and its changes SHALL use only weights not marked "?". When a "?"
weight waits, the home screen SHALL show one notice "Punnitus odottaa tarkistusta" above the cards
that opens Historia's list scrolled to the newest "?" weight. The notice SHALL count only marks
that match a read weight. The home screen SHALL reload on the weights-changed signal and when the
app resumes.

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
logged day (today left out, as on Energia). A tap on the card's title or today's number SHALL open Ruoka on
today; a tap on the bars or what is under them (not the period label) SHALL open Historia on
Energia. There SHALL be no change arrow from the previous day and no separate button for today.
The card SHALL hold no button (adding is "Lisää annos" in the App menu, the widget or Ruoka). At small size the period label and the average SHALL
share one row (the average shortens to "ka 1 900 kcal/pv", wrapping to two lines when it does not fit) and the bars take the rest of the height; all parts SHALL stay readable and tappable (44 dp touch targets). Its wide and big looks
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
- **THEN** the card keeps the title "Askeleet tänään", shows "ei tietoa" in the number slot and
  "ei vielä keskiarvoa" for the average

### Requirement: Steps card without access
Without the steps permission the steps card SHALL read "Askeleet: anna lupa" and a tap SHALL ask
for it (or, after a denial, open Askeleet as described in the steps capability). When Health
Connect is unavailable it SHALL read "Askeleet: ei saatavilla" and have no tap action.

#### Scenario: No permission
- **GIVEN** the steps permission is not granted
- **THEN** the card reads "Askeleet: anna lupa" and the food card is unchanged

#### Scenario: Unavailable
- **GIVEN** Health Connect is unavailable
- **THEN** the card reads "Askeleet: ei saatavilla" and tapping it does nothing

### Requirement: Card grid
The front page SHALL place its cards (Paino, Ruoka, Askeleet, Kulutus) in a two-column grid, filled row by
row in the user's order. Each card SHALL have its own size: small (half width, one row high), wide
(full width, one row high) or big (full width, two rows high). There SHALL be no narrow-tall size.
Two small cards that follow each other in the order SHALL share a row, the first one on the left; a
small card followed by a wide or big card, or last in the order, SHALL stay half width on the left
and the right half of its row SHALL stay empty. The grid SHALL NOT move a later card forward to fill
such a gap. All cards of one row SHALL have the same height, and a big card SHALL be as high as two
rows and the gap between them. The default layout SHALL be weight wide, food small, steps small
(weight on top, food and steps side by side under it). Notices (backup, "?" check, clipboard offer)
SHALL stay above the grid and SHALL NOT be part of the order. The order, the size of every card and
the hidden cards SHALL be remembered between app starts, SHALL be independent of the card periods,
and SHALL NOT be part of the data file or the backup file. A remembered layout that cannot be read
SHALL mean the default layout; a card the app does not know SHALL be ignored, and a card the stored
layout lacks (added in a later version) SHALL be shown last at its default size, except Kulutus,
which SHALL be hidden by default (in the default layout too) and is added from "Lisää kortti".

#### Scenario: Default layout
- **GIVEN** the user never edited the front page
- **THEN** the weight card spans the width on top and the food and steps cards share the row under it

#### Scenario: Two small cards pair
- **GIVEN** the order steps (small), food (small), weight (wide)
- **THEN** steps is left and food right in the first row, and weight spans the second row

#### Scenario: Lone small card
- **GIVEN** the order food (small), weight (wide), steps (small)
- **THEN** food sits in the left half of the first row with the right half empty, weight spans the
  second row, and steps sits in the left half of the third row

#### Scenario: Big card
- **GIVEN** weight is big and food and steps are small
- **THEN** the weight card spans the width and is as high as two rows, and the food and steps cards
  share the row under it

#### Scenario: Remembered
- **GIVEN** the user made steps big, moved it first and saved
- **WHEN** the app is started again
- **THEN** the front page shows steps big on top

#### Scenario: Unusable stored layout
- **GIVEN** the stored layout is empty or unreadable
- **THEN** the default layout is shown

#### Scenario: Unknown and later-added cards
- **GIVEN** the stored layout names an unknown card and lacks a card added in a later version
- **THEN** the unknown card is ignored and the added card is shown last at its default size

#### Scenario: Larger layouts scroll
- **GIVEN** all three cards big
- **THEN** the front page scrolls to show them

#### Scenario: Kulutus hidden by default
- **GIVEN** a layout stored before the Kulutus card existed
- **THEN** the front page looks as before and Lisää kortti offers Kulutus

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

A chart card SHALL show the chart's name as its title with a chart icon and a chevron, the chart as
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
