## MODIFIED Requirements

### Requirement: Trial setting
Asetukset SHALL have a section "Automaattinen punnitus" with: an on/off switch, a start and an end
time (time pickers, whole minutes, defaults 06:00 and 10:00), "Tunnistusnopeus" with the choices
"Normaali" (the lowest-power scan) and "Nopea" (the balanced scan) and the line "Nopea tunnistaa
vaa'an varmemmin, mutta kuluttaa enemmän akkua." (default as chosen from the phase 1 trial), a
switch "Toimii myös virransäästötilassa" (default on) and "Tarkistusraja" (0.5 to 20 kg in 0.5 kg
steps, default 5 kg) with the line "Jos uusi paino poikkeaa edellisestä hyväksytystä yli tämän, se
jää tarkistettavaksi." No text of the section SHALL say "kuuntelu" or "kuuntele". The switch SHALL
be off on a first install and SHALL keep its state over app updates, also over the update from the
trial. An end earlier than the start SHALL mean the window runs over midnight; start equal to end
SHALL be refused. Turning the switch on SHALL ask for the notification permission when it is
missing; a denial SHALL NOT keep the switch off. The choices SHALL survive app and phone restarts
and SHALL NOT be restored to another phone from a system backup.

#### Scenario: Turn on
- **WHEN** the user turns the switch on and allows notifications
- **THEN** listening and saving follow the window from then on, also when the app is closed

#### Scenario: Notifications denied
- **WHEN** the user turns the switch on and denies the notification permission
- **THEN** the switch stays on, weights are saved, and the section shows "Ilmoitukset estetty"
  with a button to the app's notification settings

#### Scenario: Update from the trial
- **GIVEN** the trial was on with a 06:00-10:00 window
- **WHEN** the app is updated to this version
- **THEN** the section is "Automaattinen punnitus", it is on with the same window, and the limit is
  5 kg

#### Scenario: Window over midnight
- **WHEN** the start is 22:00 and the end 02:00
- **THEN** the phone listens from 22:00 to 02:00 the next day

#### Scenario: Same start and end
- **WHEN** the user picks an end equal to the start
- **THEN** the choice is refused with a short message and the previous end stays

#### Scenario: Settings changed while on
- **WHEN** the user changes the window, the Tunnistusnopeus or the power-saving switch while it is
  on
- **THEN** listening is re-planned at once (stopped with a summary if now is outside the new
  window, restarted with the new scan if inside)

#### Scenario: Limit changed
- **WHEN** the user changes the Tarkistusraja from 5 kg to 3 kg
- **THEN** later automatic weights are checked against 3 kg and weights already saved keep their
  mark

### Requirement: Trial status
While the feature is on, the section SHALL show one explanation line under the switch, the first
that applies: "Ei toimi: vaakaa ei vielä tunneta", "Ei toimi: Bluetooth-lupa puuttuu", "Ei toimi:
Health Connect -lupa puuttuu" (weight write access missing), "Ei toimi: Bluetooth pois päältä",
"Ei toimi: avaa sovellus kerran" (the system stopped the app by force), "Ei toimi:
virransäästötila päällä" (power saving on and the switch for it off), "Tänään punnittu, seuraavan kerran klo
HH.MM" (see "Status symbol in Asetukset"), "Odottaa vaakaa klo HH.MM asti" (inside the window), or
"Alkaa klo HH.MM". While the feature is off the line SHALL be absent. When the app's battery use is
restricted by the phone, a warning line SHALL be shown under it, "Akunkäyttö rajoitettu: punnitus
voi jäädä tunnistamatta", while scanning still runs. When notifications are not allowed, a warning
line "Ilmoitukset estetty" SHALL be shown. Each "Ei toimi" reason (except "avaa sovellus kerran"
and "vaakaa ei vielä tunneta") and each warning SHALL have a button that opens the matching system screen or permission request. While weights wait in the
pending queue the section SHALL also show "N painoa odottaa tallennusta".

#### Scenario: Battery use restricted
- **WHEN** the phone restricts the app's battery use and the window is on
- **THEN** the line says "Odottaa vaakaa klo 10.00 asti", the warning line is shown with a button
  to the app's battery setting, and scanning runs

#### Scenario: Bluetooth off
- **WHEN** Bluetooth is off inside the window
- **THEN** the line says "Ei toimi: Bluetooth pois päältä" with a button to turn it on

#### Scenario: Health Connect access missing
- **WHEN** weight write access to Health Connect has been taken away
- **THEN** the line says "Ei toimi: Health Connect -lupa puuttuu" with a button that asks for it,
  and no scan runs

#### Scenario: Before the window
- **WHEN** the window is 06:00-10:00 and the time is 23.00
- **THEN** the line says "Alkaa huomenna klo 6.00"

### Requirement: Widget status
While the feature is on, the 1x1 home-screen widget SHALL show one small symbol only (no text, no
times), read out by a screen reader: "Automaattinen punnitus päällä" (green filled dot) or
"Automaattinen punnitus ei toimi" (amber warning triangle: an "Ei toimi" reason inside the
window). The warnings (battery use restricted, notifications off) SHALL NEVER show the triangle.
The symbol SHALL change within about 15 minutes of the window start and end, and of a reason
appearing or clearing inside the window; outside the window, and at a redraw after the stored
window end, it SHALL be the dot. With the feature off the widget SHALL show no
symbol.

#### Scenario: Morning
- **GIVEN** the window is 06:00-10:00 and nothing blocks
- **WHEN** the window starts, and after the weight is saved
- **THEN** the widget shows the dot

#### Scenario: Weighed
- **WHEN** the morning's weight is saved at 07.12 without a mark
- **THEN** the widget shows the dot, still at 20.00

#### Scenario: Window ended
- **GIVEN** the widget showed the triangle because Bluetooth was off and the window end has passed
- **WHEN** the widget is next redrawn
- **THEN** it shows the dot

#### Scenario: Night
- **WHEN** the time is outside the window
- **THEN** the widget shows the dot, also when Bluetooth is off

#### Scenario: Battery restricted
- **WHEN** the phone restricts the app's battery use inside the window
- **THEN** the section shows its warning and the widget still shows the dot

#### Scenario: Power saving pause
- **WHEN** power saving is on and the switch for it is off, inside the window
- **THEN** the widget shows the triangle

#### Scenario: Feature off
- **WHEN** the feature is off
- **THEN** the widget shows only Punnitse as before

### Requirement: Status symbol in Asetukset
The explanation line of "Automaattinen punnitus" SHALL start with the green dot, or the amber
triangle for every "Ei toimi" line at any time of day (no symbol while the feature is off); the
symbol is decorative for screen readers, the line carries the meaning. While the window's weight is saved, from that save
until the next window start, the line SHALL read "Tänään punnittu, seuraavan kerran klo HH.MM",
with "huomenna" before "klo" when the next start is tomorrow; it comes after the "Ei toimi"
reasons and before "Odottaa vaakaa" and "Alkaa".

#### Scenario: Weighed this morning
- **GIVEN** a 06:00-10:00 window and the weight saved at 07.12
- **WHEN** Asetukset is opened at 08.00
- **THEN** the line shows the green dot and "Tänään punnittu, seuraavan kerran huomenna klo 6.00"

### Requirement: Listen in the background
While the trial is on and the current time is inside the window, the phone SHALL listen for the
remembered scale with the chosen Tunnistusnopeus (Normaali = the lowest-power scan, the default, as
`ListenMode.of(null)` gives; Nopea = the balanced scan), with the screen off and the app not open, waking the app only for the scale's packets.
Outside the window it SHALL NOT scan. When the phone's power saving mode is on and "Toimii myös
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
- **WHEN** the phone's power saving mode is on and "Toimii myös virransäästötilassa" is off
- **THEN** no scan runs and the line says "Ei toimi: virransäästötila päällä"

#### Scenario: Weigh-in dialog open
- **WHEN** the user opens the weigh-in dialog while the trial listens
- **THEN** the dialog works as before and the trial's packets are logged as its own

### Requirement: Last heard
The section SHALL show when the scale was last recognised in the background and the signal
strength: "Vaakaa ei vielä tunnistettu", "Vaaka tunnistettu viimeksi tänään 7.12, signaali -97",
"… eilen 7.12" or "… 12.11. klo 7.12". It SHALL refresh when Asetukset is shown and every minute
while it is open.

#### Scenario: Heard this morning
- **WHEN** the background heard the scale at 7.12 today with signal -97
- **THEN** the line reads "Vaaka tunnistettu viimeksi tänään 7.12, signaali -97"

### Requirement: Stop after the window's weight
Once a weight not marked "?" whose time is inside the current listening window has been saved by
this app (weigh-in dialog, manual entry or the background), listening SHALL stop for the rest of
that window and SHALL start again at the next window's start. When that weight is deleted
(dialog Poista, notification Poista, Historia) or later marked "?", and no other such weight of
this app remains in the window, listening SHALL start again at once if the window is still on.
Accepting a "?" weight of the window SHALL count as saving it. Weights written by other apps SHALL
NOT count. Stopping SHALL be logged (`listen.stop reason=done`).

#### Scenario: Morning weight saved
- **GIVEN** a 06:00-10:00 window
- **WHEN** 82.5 kg is saved automatically at 07.12 without a mark
- **THEN** the scan stops, the line reads "Tänään punnittu, seuraavan kerran huomenna klo
  6.00" and the widget shows the dot

#### Scenario: Weighed from the widget
- **WHEN** the user saves a weigh-in from the dialog at 07.12 inside the window
- **THEN** listening stops for the rest of the window

#### Scenario: Manual entry for another day
- **WHEN** the user adds a manual weight for yesterday during today's window
- **THEN** listening goes on

#### Scenario: Questioned weight
- **WHEN** a weight marked "?" is saved automatically
- **THEN** listening goes on

#### Scenario: Deleted
- **GIVEN** listening stopped after the 07.12 weight
- **WHEN** the user deletes that weight at 07.13
- **THEN** listening starts again

#### Scenario: Next day
- **GIVEN** listening stopped yesterday after the day's weight
- **WHEN** today's window starts
- **THEN** listening starts as usual
