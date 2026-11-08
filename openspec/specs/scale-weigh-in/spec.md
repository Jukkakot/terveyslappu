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
time of the stable reading, and SHALL show the saved weight before the dialog closes.

#### Scenario: Saved
- **WHEN** the stable weight is decided
- **THEN** it is written to Health Connect within a second
- **AND** the dialog shows the weight and "Tallennettu" and closes after about three seconds

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
launcher shortcut on the app icon and a button on the main screen.

#### Scenario: From the tile
- **WHEN** the user taps the Punnitse tile in Quick Settings
- **THEN** the panel collapses and the weigh-in dialog opens without the main screen

#### Scenario: From the widget
- **WHEN** the user taps the Punnitse widget on the home screen
- **THEN** the weigh-in dialog opens without the main screen

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
