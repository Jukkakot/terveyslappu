## MODIFIED Requirements

### Requirement: Save the scale weight
The app SHALL write the decided weight to Health Connect as a weight measured by a scale, with the
time of the stable reading, and SHALL show the saved weight with the choices "Poista" and "Valmis"
for about ten seconds before the dialog closes; a tap on the dialog SHALL keep it open until the
user closes it.

#### Scenario: Saved
- **WHEN** the stable weight is decided
- **THEN** it is written to Health Connect within a second
- **AND** the dialog shows the weight, "Tallennettu", "Poista" and "Valmis" and closes after about
  ten seconds

#### Scenario: Keep the result open
- **WHEN** the user taps the dialog while it shows the saved weight
- **THEN** it no longer closes by itself

#### Scenario: Done
- **WHEN** the user taps Valmis
- **THEN** the dialog closes at once

#### Scenario: Same weigh-in seen twice
- **WHEN** the same stable reading would be saved a second time in the same weigh-in
- **THEN** Health Connect keeps one weight, not two

#### Scenario: Save fails
- **WHEN** writing to Health Connect fails
- **THEN** the dialog stays open, says the save failed and offers to retry, and the cause is logged

### Requirement: Keep the raw scale reading
For every saved scale weight the app SHALL keep on the phone the raw data behind it, without any
calculation: the deciding packet's bytes, and its fields as the scale sent them (company id, raw
weight value, decimals, display unit, stable flag, raw impedance, product id, attribute byte, the
scale's address and any further bytes), and SHALL drop them together with the weight.

#### Scenario: Weigh-in kept
- **WHEN** a scale weigh-in saves 82.5 kg from a packet whose impedance field is 5123
- **THEN** the phone keeps the packet's hex bytes and the fields, impedance 5123 as sent, with the
  weight's time

#### Scenario: Whole packet named
- **WHEN** the deciding packet is `033a1403abcd01aabbccddeeff99` with company id `12c0`
- **THEN** the phone keeps `key=12c0`, `weight_raw=826`, `decimals=1`, `unit=kg`, `stable=1`,
  `impedance=5123`, `product_id=abcd`, `attrib=01`, `mac=AA:BB:CC:DD:EE:FF` and `extra=99` (the bytes
  after the 13th)

#### Scenario: Weigh again
- **WHEN** the user taps Poista to weigh again
- **THEN** the raw data is removed with the weight

#### Scenario: No impedance
- **WHEN** the scale reports impedance 0 (e.g. shoes on)
- **THEN** the raw data is kept with impedance 0

## ADDED Requirements

### Requirement: Discard the weigh-in
From the saved result the app SHALL let the user throw the weigh-in away: "Poista" SHALL delete
the just-saved weight and its raw data without a confirmation and close the dialog at once.

#### Scenario: Discard
- **GIVEN** the dialog shows 82.5 kg as saved
- **WHEN** the user taps Poista
- **THEN** 82.5 kg and its raw data are removed from the phone and the dialog closes

#### Scenario: Discard fails
- **WHEN** deleting the saved weight fails
- **THEN** the dialog stays on the saved weight, says the deletion failed, and the cause is logged

### Requirement: Ignore the scale's lingering broadcast
For two minutes after a weigh-in is saved or discarded, a new weigh-in SHALL ignore scale packets
carrying that weigh-in's company id, so a reading the scale is still repeating is not saved again.

#### Scenario: Reopened right after Poista
- **GIVEN** a weigh-in with company id `61c0` was discarded 10 seconds ago
- **WHEN** the dialog is opened again and the scale still sends the old stable packet with `61c0`
- **THEN** nothing is saved and the dialog asks the user to step on the scale
- **AND** a new weighing with company id `62c0` is saved as usual

#### Scenario: Old enough
- **GIVEN** the last weigh-in with company id `61c0` ended three minutes ago
- **WHEN** a packet with `61c0` arrives
- **THEN** it is used as usual

## REMOVED Requirements

### Requirement: Weigh again
**Reason**: Replaced by Poista (user 2026-11-09): to weigh again, discard and open the widget again;
the button also never worked, as the new scan caught the scale's lingering broadcast.
**Migration**: Poista, then the widget or tile.
