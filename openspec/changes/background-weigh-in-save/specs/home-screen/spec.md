## ADDED Requirements

### Requirement: Questioned weights on the home screen
The home card, its small chart, its changes and the derived-values row SHALL use only weights not
marked "?". The three latest weights SHALL include a "?" weight with its "?" sign and no change.
When a "?" weight waits, the home screen SHALL show one notice "Punnitus odottaa tarkistusta" that
opens Historia.

#### Scenario: Questioned latest weight
- **GIVEN** 82,0 kg yesterday and 61,3 kg marked "?" today
- **WHEN** the home screen is shown
- **THEN** the card shows 82,0 kg, the list starts with 61,3 kg "?", and the notice is shown

#### Scenario: Nothing to check
- **WHEN** no weight is marked "?"
- **THEN** no notice about checking is shown
