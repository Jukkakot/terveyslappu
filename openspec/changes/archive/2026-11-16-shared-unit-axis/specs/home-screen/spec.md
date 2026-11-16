## MODIFIED Requirements

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
