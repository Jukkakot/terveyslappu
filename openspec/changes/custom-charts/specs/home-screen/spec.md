## ADDED Requirements

### Requirement: Chart cards
An own chart (weight-chart "Custom charts") SHALL be a card type of the front page's card grid. In
edit mode "Lisää kortti" SHALL list, after the hidden feature cards, the own charts that are not on
the front page and a last item "Uusi kaavio"; "Lisää kortti" SHALL therefore always be enabled.
Choosing a chart SHALL put its card last in the edited layout at wide size; "Uusi kaavio" SHALL
open the chart editor, and saving there SHALL create the chart (in Historia at once, also if the
front page edits are later discarded) and put its card last in the edited layout at wide size.
"Piilota" on a chart card SHALL take it off the front page only; the chart stays in Historia and
in the "Lisää kortti" list. A deleted chart's card SHALL disappear from the front page. "Palauta oletus" SHALL take every chart
card off the edited layout; the charts stay in Historia.

A chart card SHALL show the chart's name as its title with a chart icon and a chevron, the chart as
defined (its data, styles and layout) over the card's own period, filling the card's free space,
and a period label (1 vk, 1 kk or 3 kk, own per card, remembered, 1 kk by default). By size:
small: the title, the chart without scale labels or dates, the period label; wide: also the first
and last day under the chart and, for Päällekkäin, a one-line legend; big: also the top of each
scale with its unit (Allekkain: per panel; Päällekkäin: the two axes). On a card data without
permission or without values SHALL be left out silently (Allekkain: its panel shows a short "anna
lupa" or "ei tietoa" line); a chart whose shown data all lack values SHALL read "Ei tietoja tällä
jaksolla". The card SHALL have no crosshair: a tap anywhere except its period label SHALL open
Historia on the chart's tab, and back SHALL return to the front page.

#### Scenario: Add a chart card
- **GIVEN** the chart "Liike" exists and is not on the front page
- **WHEN** the user taps Lisää kortti in edit mode, picks Liike and taps Valmis
- **THEN** a wide Liike card is last on the front page

#### Scenario: New chart from the front page
- **GIVEN** edit mode
- **WHEN** the user picks Uusi kaavio in Lisää kortti and saves the editor
- **THEN** the new chart's card is last in the edited layout and the chart has a Historia tab

#### Scenario: Open from the card
- **WHEN** the user taps the Liike card's chart
- **THEN** Historia opens on the Liike tab; back returns to the front page

#### Scenario: Small chart card
- **GIVEN** the Liike card is small
- **THEN** it shows the name, the chart and the period label, all inside its half, and no dates

#### Scenario: Hide keeps the chart
- **WHEN** the user hides the Liike card and taps Valmis
- **THEN** the card is gone from the front page and the Liike tab is still in Historia

#### Scenario: Deleted chart
- **GIVEN** the Liike card is on the front page
- **WHEN** the user deletes Liike in the editor
- **THEN** the front page no longer shows it
