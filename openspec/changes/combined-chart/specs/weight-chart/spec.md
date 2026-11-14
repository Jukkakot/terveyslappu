## ADDED Requirements

### Requirement: Combined chart tab
Historia SHALL have a fifth tab "Yhdistelmä" after Askeleet that shows the weight, the energy and
the steps of the chosen period as separate panels stacked on one shared time axis, never as one
plot with two value axes. It is the one tab whose quantities are chosen and stacked; the other
tabs stay one quantity each. Filter chips Paino, Energia and Askeleet SHALL let the user pick the
shown panels in any combination; the last shown panel cannot be switched off. The choice SHALL be
remembered, all three on by default. Panels SHALL be in the order Paino, Energia, Askeleet, each
with its own scale and unit, drawn like on its own tab. The time axis SHALL span the chosen
period, with one date row under the whole stack. Kaikki starts at the first day with data among
the shown panels whose data is loaded. The tab SHALL show no statistics and no list, and SHALL NOT
ask for any permission by itself. Historia SHALL stay on Yhdistelmä while it is composed, and
reopening Historia shows Paino.

#### Scenario: All three panels
- **GIVEN** weights, food and steps in the last 30 days and all chips on
- **WHEN** the user opens Yhdistelmä for 1 kk
- **THEN** three panels are stacked, weight, energy, steps, over the same 30 days, with one date row under the stack

#### Scenario: Pick panels
- **GIVEN** all chips on
- **WHEN** the user switches Energia off
- **THEN** only the weight and steps panels are shown, on the same axis and taller, and the choice stays after a restart

#### Scenario: Last panel stays
- **GIVEN** only Paino is on
- **WHEN** the user taps Paino
- **THEN** it stays on

#### Scenario: No second axis
- **WHEN** weight and steps are both shown
- **THEN** each has its own panel and scale; no panel has a second value axis

#### Scenario: Kaikki start
- **GIVEN** Kaikki and steps data that loads later than the weights
- **THEN** the axis starts at the first day with data of the loaded panels and may move earlier when the steps load

#### Scenario: Weekly bars
- **WHEN** the period is over 90 days
- **THEN** kcal and steps are weekly bars, one "viikoittain" note is above the panels, and partial first and last weeks are clipped to the period

#### Scenario: Swipe still switches tabs
- **WHEN** the user swipes or flings the panels sideways without holding
- **THEN** Historia switches tab as on the other tabs and no crosshair appears

#### Scenario: Staying on the tab
- **GIVEN** Yhdistelmä is shown
- **WHEN** the user leaves Historia and opens it again
- **THEN** Paino is shown

### Requirement: Combined chart crosshair
Tapping a panel, or pressing and holding and then dragging sideways, SHALL place one vertical
line at that day through all shown panels and SHALL show above them one readout: the date and, for
each shown panel, its value (weight in kg or "ei painoa"; kcal or "ei kirjauksia"; steps or "ei
tietoa"). A panel without its permission SHALL be left out of the readout. After a press-and-hold
the drag SHALL own both axes until release. Tapping a panel SHALL never open anything. Tapping the
readout or the panels' empty margin SHALL remove the crosshair; leaving the tab, a period or chip
change and leaving Historia SHALL remove it; a data reload SHALL keep it. Over 90 days the line
SHALL snap to the week and be drawn at the week's centre. TalkBack SHALL offer "Edellinen päivä"
and "Seuraava päivä" actions on the panels, starting from the newest day, and the readout SHALL be
a polite live region updated only on tap or drop.

#### Scenario: Tap a day
- **GIVEN** 1 kk with all three panels
- **WHEN** the user taps a day with all three quantities
- **THEN** a line crosses the three panels and the readout shows that date, the weight, the kcal and the steps

#### Scenario: Day without data
- **WHEN** the user taps a day with weight but no food and no steps
- **THEN** the readout shows the weight, "ei kirjauksia" for kcal and "ei tietoa" for the steps

#### Scenario: Today
- **WHEN** the user taps today
- **THEN** the readout shows today's running values without a mark

#### Scenario: Scrub
- **WHEN** the user presses and holds on a panel and drags sideways and up or down
- **THEN** the line and the readout follow the finger day by day and the page does not scroll until release

#### Scenario: Week readout
- **GIVEN** a period over 90 days
- **WHEN** the user taps a week
- **THEN** the line is at the week's centre and the readout reads "vk 46 (10.–16.11.)" with the week's average weight (without "?" weights), the average kcal and the average steps per logged day, today left out, and "ei tietoa" for a part with no days

#### Scenario: Clearing
- **GIVEN** a crosshair
- **WHEN** the user taps the readout, or changes a chip, the period or the tab
- **THEN** the crosshair is removed

#### Scenario: Step through days
- **GIVEN** TalkBack on the panels
- **WHEN** the user runs "Edellinen päivä" with no crosshair
- **THEN** the newest day is chosen and the readout is announced

### Requirement: Combined chart panels without data
A panel whose quantity has no data in the chosen period SHALL keep its place and its height and
show one centred line instead of the drawing: "Ei painoja tällä jaksolla", "Ei ruokakirjauksia
tällä jaksolla" or "Ei askeleita tällä jaksolla". A panel without its permission SHALL show the
reason of its own tab with its grant button (Ruokakirjaus and steps; weights need none). While a
panel's data is still loading it SHALL show a progress indicator. The other panels SHALL be
unaffected. Panel heights SHALL be 112 dp for three shown panels, 136 dp for two and 160 dp for
one.

#### Scenario: No steps permission
- **GIVEN** the steps permission is not granted and all chips are on
- **WHEN** Yhdistelmä is shown
- **THEN** the weight and energy panels are drawn and the steps panel, at the same height, shows the reason and a grant button

#### Scenario: No food in the period
- **GIVEN** nothing is logged in the period
- **THEN** the energy panel shows "Ei ruokakirjauksia tällä jaksolla" at its usual height and the other panels are drawn
