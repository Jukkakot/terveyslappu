## Purpose

Listens for the user's own scale without any tap, inside a time window the user sets, so a
weigh-in can be caught while the phone lies elsewhere. In this phase it only listens and records
what it heard; saving the weight comes later.

## ADDED Requirements

### Requirement: Trial setting
Asetukset SHALL have a section "Automaattinen punnitus (kokeilu)" with an on/off switch, a start
time and an end time. The switch SHALL be off on a new install and after an update. Start and end
SHALL be chosen with a time picker in whole minutes; the defaults SHALL be 06:00 and 10:00. An end
earlier than the start SHALL mean the window runs over midnight. Start equal to end SHALL NOT be
accepted. The choices SHALL survive app restarts and phone restarts.

#### Scenario: Turn on
- **WHEN** the user turns the switch on
- **THEN** listening follows the window from then on, also when the app is closed

#### Scenario: Window over midnight
- **WHEN** the start is 22:00 and the end 02:00
- **THEN** the phone listens from 22:00 to 02:00 the next day

#### Scenario: Same start and end
- **WHEN** the user picks an end equal to the start
- **THEN** the choice is refused with a short message and the previous end stays

### Requirement: Which scale
The trial SHALL listen only to the scale of the latest weigh-in saved from the weigh-in dialog,
identified by its Bluetooth name and address. Before any such weigh-in the switch SHALL be
disabled with the text that one weigh-in from the widget or the app is needed first. A later
weigh-in from another scale SHALL replace the remembered one.

#### Scenario: No scale yet
- **WHEN** no weigh-in has been saved from the dialog since this change
- **THEN** the switch cannot be turned on and the section says to weigh in once first

#### Scenario: Another device nearby
- **WHEN** another device, or a scale with another address and name, advertises in the window
- **THEN** the trial does not record it

### Requirement: Listen in the background
While the switch is on and the current time is inside the window, the phone SHALL listen for the
remembered scale with a low-power Bluetooth scan that runs with the screen off and the app not
open, and that wakes the app only for the scale's packets. Outside the window it SHALL NOT scan.
The window start and end MAY be late by up to 15 minutes. Listening SHALL resume after a phone
restart or an app update without opening the app. Turning the switch off SHALL stop the scan at
once.

#### Scenario: Window starts
- **WHEN** the window starts and the switch is on
- **THEN** the scan starts within 15 minutes and `listen.start` is logged

#### Scenario: Window ends
- **WHEN** the window ends
- **THEN** the scan stops and a `listen.summary` line is logged

#### Scenario: Phone restarted inside the window
- **WHEN** the phone restarts at 07:00 with a 06:00-10:00 window
- **THEN** listening resumes after boot without the user opening the app

#### Scenario: Weigh-in dialog open
- **WHEN** the user opens the weigh-in dialog while the trial listens
- **THEN** the dialog works as before; the trial keeps listening and its packets are logged as its own

### Requirement: Save nothing
The trial SHALL NOT write anything to Health Connect, the raw-reading archive, the backup or the
export, and SHALL NOT show a notification for a heard weigh-in.

#### Scenario: Weigh-in heard
- **WHEN** the trial hears a stable weight
- **THEN** nothing is saved anywhere except the log and the last-heard line in Asetukset

### Requirement: Record what was heard
The trial SHALL log each heard packet raw (as the dialog does, marked as coming from the trial)
with its signal strength and whether the screen was on, but SHALL log a packet identical to the
previous one from the trial only as a count. At the end of each window it SHALL log a summary:
packets heard, distinct readings, strongest and weakest signal, first and last time heard, and the
stable weights heard. Asetukset SHALL show when the scale was last heard and its signal strength,
e.g. "Kuultu viimeksi tänään 7.12, signaali -97".

#### Scenario: Lingering broadcast
- **WHEN** the scale repeats the same stable packet for two minutes
- **THEN** the log has the first packet raw and a count of the repeats, not hundreds of lines

#### Scenario: Nothing heard
- **WHEN** a window ends without a packet from the scale
- **THEN** the summary says zero packets

### Requirement: Trial status
The section SHALL show one status: "Kuuntelee nyt" inside the window; "Lepää, alkaa klo HH.MM"
outside it; or "Estetty" with the reason when Bluetooth is off, the Bluetooth permission is
missing, or the phone's battery optimisation applies to the app. Each blocked reason SHALL offer a
button that opens the matching system screen. The status SHALL be up to date whenever Asetukset is
shown.

#### Scenario: Battery optimisation on
- **WHEN** the battery optimisation applies to the app
- **THEN** the status says "Estetty" with the reason and a button to the battery setting

#### Scenario: Bluetooth off
- **WHEN** Bluetooth is off inside the window
- **THEN** the status says "Estetty: Bluetooth pois päältä", and listening resumes in the next
  window once Bluetooth is on
