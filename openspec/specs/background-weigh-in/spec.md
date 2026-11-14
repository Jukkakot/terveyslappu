# background-weigh-in Specification

## Purpose
Listens for the user's own scale without any tap, inside a time window the user sets, so a
weigh-in can be caught while the phone lies elsewhere. In this phase it only listens and records
what it heard; saving the weight comes later.

## Requirements

### Requirement: Trial setting
Asetukset SHALL have a section "Automaattinen punnitus (kokeilu)" with: an on/off switch, a start
and an end time (time pickers, whole minutes, defaults 06:00 and 10:00), a listening mode "Säästö"
or "Tasapaino" (default Säästö), and a switch "Kuuntele myös virransäästötilassa" (default on).
The trial switch SHALL be off on a first install and SHALL keep its state over app updates. An
end earlier than the start SHALL mean the window runs over midnight; start equal to end SHALL be
refused. The choices SHALL survive app and phone restarts and SHALL NOT be restored to another
phone from a system backup.

#### Scenario: Turn on
- **WHEN** the user turns the switch on
- **THEN** listening follows the window from then on, also when the app is closed

#### Scenario: Window over midnight
- **WHEN** the start is 22:00 and the end 02:00
- **THEN** the phone listens from 22:00 to 02:00 the next day

#### Scenario: Same start and end
- **WHEN** the user picks an end equal to the start
- **THEN** the choice is refused with a short message and the previous end stays

#### Scenario: Settings changed while on
- **WHEN** the user changes the window, the mode or the power-saving switch while the trial is on
- **THEN** listening is re-planned at once (stopped with a summary if now is outside the new
  window, restarted with the new mode if inside)

### Requirement: Which scale
The trial SHALL listen only to the scale of the latest weigh-in saved from the weigh-in dialog,
identified by its Bluetooth address (and its name when the packet had one). Before such a
weigh-in exists the trial switch SHALL be disabled with the text "Punnitse kerran widgetistä tai
sovelluksesta, niin sovellus oppii vaakasi." A later weigh-in from another scale SHALL replace the
remembered one. A packet from another address SHALL NOT count as heard, even with the same name.

#### Scenario: No scale yet
- **WHEN** no weigh-in has been saved from the dialog since this update
- **THEN** the switch cannot be turned on and the section shows the hint

#### Scenario: Same-named scale nearby
- **WHEN** a scale with the same name but another address advertises in the window
- **THEN** it is logged as another device and not counted as heard

### Requirement: Listen in the background
While the trial is on and the current time is inside the window, the phone SHALL listen for the
remembered scale with the chosen mode (Säästö = the lowest-power scan, Tasapaino = the balanced
scan), with the screen off and the app not open, waking the app only for the scale's packets.
Outside the window it SHALL NOT scan. When the phone's power saving mode is on and "Kuuntele myös
virransäästötilassa" is off, it SHALL NOT scan. The window start may be late because of the
phone's idle mode; how late SHALL be logged. While inside the window the app SHALL check about
once an hour that listening is still possible and register it again, so a scan lost to Bluetooth
being turned off and on, or to the system, comes back within about an hour. Listening SHALL resume
after a phone restart or an app update without opening the app. Turning the switch off SHALL stop
the scan at once.

#### Scenario: Window starts
- **WHEN** the window starts and the trial is on
- **THEN** the scan starts and `listen.start` is logged with how late it started

#### Scenario: Window ends
- **WHEN** the window ends
- **THEN** the scan stops and the window summary is logged

#### Scenario: Phone restarted inside the window
- **WHEN** the phone restarts at 07:00 with a 06:00-10:00 window
- **THEN** listening resumes after boot without the user opening the app

#### Scenario: Bluetooth turned off and on
- **WHEN** Bluetooth is turned off and on again inside the window
- **THEN** listening is running again within about an hour

#### Scenario: Power saving mode
- **WHEN** the phone's power saving mode is on and "Kuuntele myös virransäästötilassa" is off
- **THEN** no scan runs and the status says so

#### Scenario: Weigh-in dialog open
- **WHEN** the user opens the weigh-in dialog while the trial listens
- **THEN** the dialog works as before and the trial's packets are logged as its own

### Requirement: Save nothing
The trial SHALL NOT write anything to Health Connect, the raw-reading archive, the backup or the
export, and SHALL NOT show a notification for a heard weigh-in.

#### Scenario: Weigh-in heard
- **WHEN** the trial hears a stable weight
- **THEN** nothing is saved anywhere except the log and the last-heard line in Asetukset

### Requirement: Record what was heard
The trial SHALL log each heard packet raw, marked as coming from the trial, with its signal
strength. A packet identical to the trial's previous one SHALL only be counted.

#### Scenario: Lingering broadcast
- **WHEN** the scale repeats the same stable packet for two minutes
- **THEN** the log has the first packet raw and one line with the count of repeats

### Requirement: Window summary
At the end of each window, and when the trial is turned off or an unfinished window is found
later, the trial SHALL log one summary: why it ended, the mode, packets heard, distinct readings,
strongest and weakest signal, first and last time heard, stable weights heard, and the battery
level at the start and at the end.

#### Scenario: Nothing heard
- **WHEN** a window ends without a packet from the scale
- **THEN** the summary says zero packets

#### Scenario: Window not closed
- **WHEN** the end of a window was missed (phone off, alarm lost)
- **THEN** the next start or turn-on logs that window's summary as unfinished first

### Requirement: Last heard
The section SHALL show when the scale was last heard by the trial and the signal strength:
"Ei vielä kuultu", "Kuultu viimeksi tänään 7.12, signaali -97", "eilen 7.12" or "12.11. klo
7.12". It SHALL refresh when Asetukset is shown and every minute while it is open.

#### Scenario: Heard this morning
- **WHEN** the trial heard the scale at 7.12 today with signal -97
- **THEN** the line reads "Kuultu viimeksi tänään 7.12, signaali -97"

### Requirement: Trial status
The section SHALL show one status, the first that applies: "Estetty: Bluetooth-lupa puuttuu",
"Estetty: Bluetooth pois päältä", "Tauolla: virransäästötila" (power saving on and the switch for it
off), "Kuuntelee nyt", or "Lepää, alkaa klo HH.MM". When the app's battery use is restricted by the
phone, a warning line SHALL be shown under the status, "Akunkäyttö rajoitettu: kuuntelu voi
katketa", while listening still runs. Each blocked reason and the warning SHALL have a button that
opens the matching system screen. If the system stopped the app by force, the status SHALL say to
open the app once.

#### Scenario: Battery use restricted
- **WHEN** the phone restricts the app's battery use and the window is on
- **THEN** the status says "Kuuntelee nyt", the warning line is shown with a button to the app's
  battery setting, and listening runs

#### Scenario: Bluetooth off
- **WHEN** Bluetooth is off inside the window
- **THEN** the status says "Estetty: Bluetooth pois päältä" with a button to turn it on
