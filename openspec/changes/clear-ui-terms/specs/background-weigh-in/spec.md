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
that applies: "Ei toimi: Bluetooth-lupa puuttuu", "Ei toimi: Health Connect -lupa puuttuu" (weight
write access missing), "Ei toimi: Bluetooth pois päältä", "Ei toimi: virransäästötila päällä"
(power saving on and the switch for it off), "Ei toimi: avaa sovellus kerran" (the system stopped
the app by force), "Ei toimi: vaakaa ei vielä tunneta", "Tänään punnittu, seuraavan kerran klo
HH.MM" (see "Status symbol in Asetukset"), "Odottaa vaakaa klo HH.MM asti" (inside the window), or
"Alkaa klo HH.MM". While the feature is off the line SHALL be absent. When the app's battery use is
restricted by the phone, a warning line SHALL be shown under it, "Akunkäyttö rajoitettu: punnitus
voi jäädä tunnistamatta", while scanning still runs. When notifications are not allowed, a warning
line "Ilmoitukset estetty" SHALL be shown. Each "Ei toimi" reason and each warning SHALL have a
button that opens the matching system screen or permission request. While weights wait in the
pending queue the section SHALL also show "N punnitusta odottaa tallennusta".

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
- **THEN** the line says "Alkaa klo 6.00"

### Requirement: Widget status
While the feature is on, the 1x1 home-screen widget SHALL show one small symbol only (no text, no
times), read out by a screen reader: "Automaattinen punnitus päällä" (green filled dot) or
"Automaattinen punnitus ei toimi" (amber warning triangle: an "Ei toimi" reason inside the
window). The warnings (battery use restricted, notifications off) SHALL NEVER show the triangle.
The symbol SHALL change within about 15 minutes of a reason appearing or clearing at the window
start; outside the window it SHALL be the dot. With the feature off the widget SHALL show no
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
The explanation line of "Automaattinen punnitus" SHALL start with the same symbol and colour as the
widget (no symbol while the feature is off). While the window's weight is saved, from that save
until the next window start, the line SHALL read "Tänään punnittu, seuraavan kerran klo HH.MM",
with "huomenna" before "klo" when the next start is tomorrow; it comes after the "Ei toimi"
reasons and before "Odottaa vaakaa" and "Alkaa".

#### Scenario: Weighed this morning
- **GIVEN** a 06:00-10:00 window and the weight saved at 07.12
- **WHEN** Asetukset is opened at 08.00
- **THEN** the line shows the green dot and "Tänään punnittu, seuraavan kerran huomenna klo 6.00"
