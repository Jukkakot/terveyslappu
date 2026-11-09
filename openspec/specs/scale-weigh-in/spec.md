# scale-weigh-in Specification

## Purpose
Saves the weight from the user's Bluetooth scale to Health Connect with one tap, without going
through the main screen, and records what the scale sent so failures can be replayed.

## Requirements

### Requirement: Read the scale broadcast
The app SHALL recognise the scale's advertisements (name starting with `Yoda`, or no name, with
manufacturer data whose company id low byte is `0xC0`) and decode the weight, the stable flag, the
number of decimals, the unit and the impedance from them, without pairing or connecting.

#### Scenario: Stable kilogram packet
- **WHEN** a packet carries raw weight 0x0339 with attributes stable, one decimal, kilograms
- **THEN** it is read as a stable 82.5 kg

#### Scenario: Pounds
- **WHEN** a stable packet reports its weight in pounds
- **THEN** the weight is converted to kilograms

#### Scenario: Other device
- **WHEN** an advertisement comes from another device or its data is shorter than the layout
- **THEN** it is ignored and nothing is saved

### Requirement: Decide the stable weight
The app SHALL save a weight only after the scale reports it stable: two stable packets with the
same weight in a row, or one stable packet followed by two seconds without a different reading.
Weights outside 20–300 kg SHALL NOT be saved.

#### Scenario: Scale settles
- **GIVEN** the scale sends unstable packets 81.2, 82.0, 82.4 and then stable 82.5 twice
- **WHEN** the second stable packet arrives
- **THEN** 82.5 kg is the result, timed at the first stable packet

#### Scenario: Only one stable packet
- **WHEN** one stable 82.5 packet arrives and nothing different follows for two seconds
- **THEN** 82.5 kg is the result

#### Scenario: Weight changes after a stable packet
- **WHEN** a stable 82.5 packet is followed by an unstable 83.1
- **THEN** nothing is decided yet and the app keeps waiting for a stable weight

#### Scenario: Empty scale
- **WHEN** the scale reports a stable 0.0 kg
- **THEN** nothing is saved

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

### Requirement: Weigh-in dialog
The app SHALL offer a small weigh-in dialog over whatever is on screen, which starts scanning at
once, tells the user to step on the scale and shows the live weight while it settles.

#### Scenario: Step on the scale
- **WHEN** the dialog is open and the user steps on the scale
- **THEN** the changing weight is shown until the stable weight is saved

#### Scenario: No scale heard
- **WHEN** no scale packet arrives within 60 seconds
- **THEN** scanning stops and the dialog says the scale was not found and offers to try again

#### Scenario: Closed early
- **WHEN** the user closes the dialog before a weight is saved
- **THEN** scanning stops and nothing is saved

### Requirement: One-tap start
The weigh-in dialog SHALL open with one tap from a Quick Settings tile, a home-screen widget, a
launcher shortcut on the app icon and a button on the main screen. The widget and the tile SHALL
show only the Punnitse action, never a weight; the widget SHALL fit one home-screen cell.

#### Scenario: From the tile
- **WHEN** the user taps the Punnitse tile in Quick Settings
- **THEN** the panel collapses and the weigh-in dialog opens without the main screen

#### Scenario: From the widget
- **WHEN** the user taps the Punnitse widget on the home screen
- **THEN** the weigh-in dialog opens without the main screen

#### Scenario: No weight before weighing
- **GIVEN** weights have been saved earlier
- **WHEN** the user looks at the widget or the tile
- **THEN** neither shows a weight or a day, only Punnitse

#### Scenario: From the main screen
- **WHEN** the user taps Punnitse on the main screen
- **THEN** the weigh-in dialog opens and the list shows the new weight after it closes

### Requirement: Access for the weigh-in
The dialog SHALL ask for the nearby-devices permission when it is missing, offer to turn Bluetooth
on when it is off, and point to the app when Health Connect write access is missing.

#### Scenario: First weigh-in
- **WHEN** the dialog opens without the nearby-devices permission
- **THEN** the system permission request is shown and scanning starts once it is granted

#### Scenario: Permission denied
- **WHEN** the user denies the nearby-devices permission
- **THEN** the dialog says the scale cannot be heard without it and offers the app settings

#### Scenario: Bluetooth off
- **WHEN** Bluetooth is off
- **THEN** the dialog offers to turn it on and starts scanning once it is on

#### Scenario: No Health Connect access
- **WHEN** weight write access to Health Connect is missing
- **THEN** the dialog says so and offers to open the app, where access is granted

### Requirement: Scale packets in the log
The app SHALL write every received scale packet to the log raw, with milliseconds since the scan
started, and SHALL log the time from scan start to the stable reading and to the saved weight,
so a shared log can be replayed exactly as a test.

#### Scenario: Weigh-in logged
- **WHEN** a weigh-in completes
- **THEN** the log holds each packet's raw bytes and timing, the decided weight with its delay
  and the save with its duration

#### Scenario: Replay
- **WHEN** the packet lines of a log are fed back to the weight decision
- **THEN** it reaches the same result as on the phone

### Requirement: Change from the previous weight
After saving a scale weight the dialog SHALL show the difference to the latest earlier weight in
Health Connect from any app, with that weight's day, or nothing when there is no earlier weight.

#### Scenario: Lighter than last time
- **GIVEN** the latest earlier weight is 82.8 kg yesterday
- **WHEN** 82.5 kg is saved
- **THEN** the dialog shows "−0,3 kg edellisestä (eilen)"

#### Scenario: Same weight
- **WHEN** the saved weight equals the previous one
- **THEN** the dialog shows "±0,0 kg edellisestä"

#### Scenario: First weight
- **WHEN** Health Connect has no earlier weight
- **THEN** only the saved weight is shown

### Requirement: Confirmation vibration
The phone SHALL give a short confirmation vibration when the scale weight has been saved.

#### Scenario: Saved
- **WHEN** the dialog switches to Tallennettu
- **THEN** the phone vibrates once briefly

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
