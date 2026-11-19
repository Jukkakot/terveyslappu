# app-appearance Specification

## Purpose
TBD - created by archiving change main-screen-refresh. Update Purpose after archive.

## Requirements

### Requirement: Theme choice
Asetukset → Ulkoasu SHALL offer two choices. "Teema": Järjestelmä, Vaalea and Tumma; Järjestelmä
SHALL be the default and follow the phone's light/dark setting. "Värit": Terveyslappu and
Puhelimen värit; Terveyslappu SHALL be the default (also for users updating from an earlier
version). Terveyslappu SHALL use the app's own palette (see "Terveyslappu palette"); Puhelimen
värit SHALL take the base colours (backgrounds, cards, buttons, tabs, selections) from the phone's
own colour palette as before. The quantity colours (see "Quantity colours") and the card, number
and chart looks SHALL be the same in both. Both choices SHALL be remembered, combine freely and
apply at once to every screen of the app (home, Historia, Ruoka, Asetukset, Ohje, the weigh-in
dialog, manual entry, the Lisää annos dialog). Asetukset's Ulkoasu row SHALL read both choices
("Tumma · Terveyslappu").

#### Scenario: Default
- **GIVEN** the user has never chosen a theme or colours and the phone is in dark mode
- **WHEN** the app opens
- **THEN** it is dark on the graphite Terveyslappu palette

#### Scenario: Update keeps the theme
- **GIVEN** the user chose Vaalea in an earlier version
- **WHEN** the app is updated to this version
- **THEN** it is light, in the Terveyslappu palette

#### Scenario: Choose light
- **WHEN** the user picks Vaalea while the phone is in dark mode
- **THEN** the app turns light at once and stays light after a restart

#### Scenario: Back to system
- **WHEN** the user picks Järjestelmä
- **THEN** the app follows the phone's setting again

#### Scenario: Phone colours
- **WHEN** the user picks Puhelimen värit
- **THEN** backgrounds, cards and buttons take the wallpaper's colours at once, while the weight
  line stays mint and the Syöty bars amber

### Requirement: Undo messages
Every message with Kumoa in the app (deleted item, added item, import, removed product, weights)
shown as a bar at the bottom SHALL show for about 4 seconds, SHALL go away at once when swiped
sideways, when another message comes or when the screen changes, and SHALL sit above the screen's
bottom buttons without covering them. The add search shows its messages as the same timed bar at
its bottom, above the keyboard when it is up. Other bar messages keep their length but can be
swiped away too.

#### Scenario: Gone in time
- **WHEN** the user deletes an item on Ruoka and does nothing
- **THEN** the "Kumoa" message is gone after about 4 seconds

#### Scenario: Swiped away
- **WHEN** the user swipes the message sideways
- **THEN** it disappears at once and nothing is undone

### Requirement: Delete and Kumoa
Every delete in the app (a weight, a food item, a product, an own chart, a row in the AI preview)
and skipping the clipboard offer SHALL happen at once, without a confirmation, and SHALL offer
Kumoa: the timed bar of "Undo messages", also in the add search. Kumoa SHALL restore
exactly what was removed. Every delete control (a row menu's "Poista", an editor's "Poista" or
"Poista ruoka" / "Poista kaavio" button) SHALL be drawn in the theme's error colour; no list row
SHALL show a red button of its own.

#### Scenario: Chart deleted
- **WHEN** the user taps Poista kaavio in the chart editor
- **THEN** the chart is gone at once, without a question, and "Kaavio poistettu · Kumoa" is shown

#### Scenario: Red everywhere
- **WHEN** the user opens the Historia list, Ruoka or Omat ruuat
- **THEN** no row shows a red control; the ⋮ menu's Poista and the editors' delete buttons are red

### Requirement: Buttons
A screen's main action SHALL be a filled button, a secondary action beside it an outlined button,
and other actions text buttons. Dialogs SHALL use text buttons, with a delete action (red) on the
left and Peruuta before the confirming action (Tallenna, Lisää) on the right.

#### Scenario: Ruoka
- **WHEN** the Ruoka screen is shown
- **THEN** "Lisää annos" at the bottom is a filled button

#### Scenario: Editor order
- **WHEN** the item editor opens for a logged item
- **THEN** Poista is on the left in red, and Peruuta and Tallenna on the right in that order

### Requirement: List rows
Every list row (Historia's weights, Syöty, Askeleet and Kulutus days, Ruoka's items, Omat ruuat's
foods, the AI preview's rows and the add search's ruoka rows) SHALL be at least 56 dp
high, with its content 16 dp from the screen's edge. A row whose thing can be edited or deleted
SHALL end with one ⋮ button (44 dp touch target, named "Toiminnot: <name>" for TalkBack) that opens
a menu next to it with "Muokkaa" (when it can be edited) and "Poista" (when it can be deleted, in
the error colour, acting as "Delete and Kumoa"), in that order, plus any action the row already has (a questioned weight's menu: Hyväksy, then Poista; its tap
still opens its dialog). The AI preview's item and product rows both offer Muokkaa and Poista.
Controls that are not list rows keep their look (Asetukset's Garmin "Poista" text button, the
own chart tab's edit button). A row that can be edited SHALL also open its editor on tap,
except where a tap does something else (the add search's ruoka rows add at once). A row with nothing to
edit or delete SHALL show no ⋮ button, and its values SHALL end at the same edge as the rows that
have one.

#### Scenario: Same edge
- **WHEN** the user switches between Ruoka and Historia
- **THEN** the rows' text starts at the same distance from the edge and the rows are equally high

#### Scenario: Ruoka row
- **WHEN** the Ruoka screen lists Rahka 250 g and the user taps its ⋮
- **THEN** a menu offers Muokkaa and a red Poista; Poista deletes it with "Rahka poistettu · Kumoa"

#### Scenario: Weight row
- **WHEN** Historia lists a weight and the user taps its ⋮
- **THEN** the menu offers only a red Poista

#### Scenario: Day row
- **WHEN** Historia's Syöty list shows a day or a week
- **THEN** the row has no ⋮ button

#### Scenario: Products
- **WHEN** the user opens Asetukset → Omat ruuat and taps a product's ⋮
- **THEN** the menu offers Muokkaa and a red Poista

### Requirement: Terveyslappu palette
The Terveyslappu palette SHALL be built from one mint brand colour (seed `#2E7D6B`, the launcher
icon's teal family) and SHALL use it for the app's main actions and selections: filled buttons, the
chosen tab and its indicator, the chosen period, switches and the progress indicators. Its dark
theme SHALL use a graphite background (`#0E1013`) with cards one step lighter (`#16191E`) and
raised parts (menus, the hero figure's base, sheets) a further step (`#1E2229`), never pure black;
its light theme SHALL use a calm near-white grey background with white cards. Text SHALL use two or
three tones (main, muted, faint) with at least 4.5:1 contrast for body text against its surface in
both themes.

#### Scenario: Dark front page
- **GIVEN** Tumma and Terveyslappu
- **THEN** the background is graphite, the cards are a slightly lighter graphite without an outline,
  and Historia's chosen tab is mint

#### Scenario: Light
- **GIVEN** Vaalea and Terveyslappu
- **THEN** the background is a light grey, the cards white, and the chosen tab a darker mint that
  reads at least 3:1 against white

### Requirement: Quantity colours
Each quantity SHALL have one fixed colour, with a light-theme and a dark-theme shade, used for its
data everywhere (Historia's tabs, front page cards, own charts' Oletus, the icon tile, the hero
figure's tint): paino and BMI mint, Syöty amber, Askeleet blue, Kulutus coral. Herkku SHALL be a
lighter amber and Kulutus's lepo a lighter coral. Every full shade SHALL reach at least 3:1 against the Terveyslappu card surface of its theme and
against the test's reference surfaces for Puhelimen värit; the dimmed past-day shade of a bar's
main part (see "Chart look") SHALL reach at least 2:1; the lighter parts (herkku, lepo, a
yhteensä day) need only be told apart from the main part. The quantity colours SHALL never mean good or bad. Every change value and its arrow (Historia's
statistics and rows, the front page cards) SHALL be drawn in the neutral text tones (main text
for statistics, muted for rows and cards) in both palettes, the same for a gain and a loss.
The own charts' eight fixed colours stay as they are; a chart with a chosen colour keeps it.

#### Scenario: Same colour everywhere
- **GIVEN** Askeleet on the front page and on Historia
- **THEN** the card's icon tile, its bars and the Askeleet tab's bars are the same blue

#### Scenario: Loss is not green
- **GIVEN** the weight dropped 0,3 kg over the period
- **THEN** the change reads "↓ −0,3 kg" in the neutral text tone, not in mint or green

### Requirement: Card look
Every card (front page cards, Historia's chart, statistics and list containers, Asetukset's
sub-screen groups) SHALL have 24 dp corners, no outline and no shadow; it SHALL stand out only by
its surface tone. A front page card's title row SHALL start with a 28 dp round tile in the quantity's colour at 16 %
(dark) / 12 % (light) opacity holding the quantity's icon in its full colour (an own chart card: a
neutral tile with the chart icon in the muted tone), then the title in 600 weight, and end with
the chevron as today. A period label on a card SHALL be an outlined chip with a neutral outline (no fill), and
the card's average text SHALL be muted and smaller than the period label. The chip SHALL keep its
▾ sign and a 44 dp touch target. Exempt from "no outline": the "?" weight's amber outlined
row and notice, and edit mode's outline (they keep today's look). In Puhelimen värit the cards
SHALL use the scheme's container tone one step above the background, so they never vanish into
it.

#### Scenario: Food card title
- **WHEN** the front page shows the food card at small size
- **THEN** the title row reads the amber tile with the food icon and "Syöty" in full, with no "…"

### Requirement: Big numbers
A screen's or card's main number (the latest weight, today's kcal, steps and burned kcal, the hero
statistic) SHALL be set larger than today and semibold (600), with figures of equal width so that
digits do not shift between values; its unit SHALL follow on the same line in a smaller, regular,
muted style. A number and its unit SHALL never be split over two lines; when they do not fit the
number's size SHALL shrink instead.

#### Scenario: Weight card
- **WHEN** the front page shows 79,9 kg
- **THEN** "79,9" is the biggest text on the screen, semibold, and "kg" sits after it smaller and muted

#### Scenario: No mid-unit wrap
- **GIVEN** Historia's Syöty statistics with 1 012 kcal/pv at 130 % text size
- **THEN** "1 012 kcal/pv" stays on one line

### Requirement: Chart look
Every chart (Historia tabs, own charts, front page cards) SHALL draw each data in its quantity
colour (or its own chart's chosen colour), with:
- **Lines:** the paino and BMI line 2.5 dp wide; in the dark theme with a soft glow (a 6 dp line of
  the same colour at 25 % under it). The paino line (not BMI, whose bands stay clear) SHALL have a
  fill under it that fades from the line's colour (28 % dark, 18 % light) at the plot's top to
  transparent at its bottom. The last weight shown in the period (the latest in range, also for an
  Oma range ending in the past) SHALL be drawn as a larger ring (5 dp, hole in the card colour),
  also when the other dots are not drawn. Layer order from the bottom: fill, glow, trend line, the
  line, dots, ring, crosshair. Own charts get the same look for Paino in Allekkain and none of it
  (no fill, glow or ring) in Päällekkäin.
- **Bars:** rounded top corners (4 dp, at most half the bar's width; a split bar rounds only its top
  part). On the fixed tabs and the feature cards (not own charts), when the bars are daily and the period's
last day is today with a bar, the earlier days SHALL be drawn at 60 % opacity and today at full
colour (all parts of a split bar alike); without a bar today, and for weekly bars, every bar SHALL
be at full colour. Today's bar is never the brand mint unless it is a paino bar (the mockup's mint
today is superseded).
- **Grid:** horizontal gridlines faint (onSurface at about 8 % opacity), no vertical gridlines.
- Average and trend lines, axes, legends, markers and the crosshair keep their rules
  (weight-chart).

#### Scenario: Today stands out
- **GIVEN** Syöty 1 vk with food logged every day including today
- **THEN** six bars are dimmed amber, today's bar is full amber with its treat share in the lighter
  amber, and all have rounded tops

#### Scenario: Weekly bars
- **GIVEN** Askeleet 1 v
- **THEN** every weekly bar is full blue and none is dimmed

#### Scenario: Weight line
- **GIVEN** Tumma and a weight card for 1 vk
- **THEN** the line is mint with a soft glow, a fading fill under it and a ring on the latest weight
