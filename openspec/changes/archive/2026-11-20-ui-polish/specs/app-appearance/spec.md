## MODIFIED Requirements

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
("Tumma · Terveyslappu"). Each choice SHALL be a single-choice list: one full-width row per option
with a radio button and the option's name, the whole row tappable (at least 48 dp high), so no
option name wraps or breaks at any width or text size.

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

#### Scenario: Options on one line each
- **GIVEN** a 360 dp phone at 100 % or 200 % text size
- **WHEN** Ulkoasu is shown
- **THEN** Järjestelmä, Vaalea, Tumma, Terveyslappu and Puhelimen värit each sit whole on their own
  row with a radio button, the chosen one filled, and TalkBack reads each as a radio button with
  its state
