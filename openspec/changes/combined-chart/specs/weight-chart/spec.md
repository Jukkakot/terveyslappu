## ADDED Requirements

### Requirement: Combined chart tab
Historia SHALL have a fifth tab "Yhdistelmä" after Askeleet that shows the weight, the energy and
the steps of the chosen period as separate panels stacked on one shared time axis, never as one
plot with two value axes. This is the one tab that shows more than one quantity; "One quantity at
a time" otherwise stays. Above the panels, filter chips Paino, Energia and Askeleet SHALL let the
user pick which panels are shown, in any combination; the last shown panel cannot be switched
off. The choice SHALL be remembered between app starts, and by default all three are on. The
panels SHALL always be in the order Paino, Energia, Askeleet from top to bottom, each with its own
value scale and unit label, drawn like on its own tab (weight line on the weight scale; kcal bars
with the treats' tone and the 7-day average line; step bars and the 7-day average line; weekly
bars over 90 days; gaps for days without data, never zero). The time axis SHALL span the chosen
period (Kaikki: from the first day with data in any shown panel), with its dates only under the
bottom panel. The tab SHALL show no statistics and no list. Opening the tab SHALL NOT ask for any
permission by itself.

#### Scenario: All three panels
- **GIVEN** weights, food and steps in the last 30 days and all chips on
- **WHEN** the user opens Yhdistelmä for 1 kk
- **THEN** three panels are stacked, weight, energy, steps, over the same 30 days, with dates only under the steps panel

#### Scenario: Pick panels
- **GIVEN** all chips on
- **WHEN** the user switches Energia off
- **THEN** only the weight and steps panels are shown, on the same axis, and the choice stays after a restart

#### Scenario: Last panel stays
- **GIVEN** only Paino is on
- **WHEN** the user taps Paino
- **THEN** it stays on

#### Scenario: No second axis
- **WHEN** weight and steps are both shown
- **THEN** each has its own panel and scale; no panel has a second value axis

#### Scenario: Swipe still switches tabs
- **WHEN** the user swipes the panels sideways without holding
- **THEN** Historia switches tab as on the other tabs and no crosshair appears

### Requirement: Combined chart crosshair
Tapping a panel, or pressing and holding and then dragging sideways, SHALL place one vertical
line at that day through all shown panels and SHALL show above them one readout of the day: the
date and, for each shown panel, its value (weight in kg of that day, or "ei painoa"; kcal;
steps, or "ei tietoa"). Tapping the same place again or tapping outside the panels SHALL remove
it. Changing the period or the chips SHALL remove it. In weekly bars the readout SHALL name the
week and give the averages.

#### Scenario: Tap a day
- **GIVEN** 1 kk with all three panels
- **WHEN** the user taps a day with all three quantities
- **THEN** a line crosses the three panels and the readout shows that date, the weight, the kcal and the steps

#### Scenario: Day without steps
- **WHEN** the user taps a day with weight and food but no steps
- **THEN** the readout shows the weight and kcal and "ei tietoa" for the steps

#### Scenario: Scrub
- **WHEN** the user presses and holds on a panel and drags sideways
- **THEN** the line and the readout follow the finger day by day

### Requirement: Combined chart panels without data
A panel whose quantity has no data in the chosen period SHALL keep its place and show one line
instead of the drawing: "Ei painoja tällä jaksolla", "Ei ruokakirjauksia tällä jaksolla" or
"Ei askeleita tällä jaksolla". A panel without its permission SHALL show the reason of its own tab
with its grant button (Ruokakirjaus and steps; weights need none). While a panel's data is still
loading it SHALL show a progress indicator. The other panels SHALL be unaffected.

#### Scenario: No steps permission
- **GIVEN** the steps permission is not granted and all chips are on
- **WHEN** Yhdistelmä is shown
- **THEN** the weight and energy panels are drawn and the steps panel shows the reason and a grant button

#### Scenario: No food in the period
- **GIVEN** nothing is logged in the period
- **THEN** the energy panel shows "Ei ruokakirjauksia tällä jaksolla" and the other panels are drawn
