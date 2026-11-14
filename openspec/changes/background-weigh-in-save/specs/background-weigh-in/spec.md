## MODIFIED Requirements

### Requirement: Trial setting
Asetukset SHALL have a section "Automaattinen punnitus" with: an on/off switch, a start and an end
time (time pickers, whole minutes, defaults 06:00 and 10:00), a listening mode "Säästö" or
"Tasapaino" (default as chosen from the phase 1 trial), a switch "Kuuntele myös
virransäästötilassa" (default on) and "Hyväksyntäraja" (0.5 to 20 kg in 0.5 kg steps, default
5 kg). The switch SHALL be off on a first install and SHALL keep its state over app updates, also
over the update from the trial. An end earlier than the start SHALL mean the window runs over
midnight; start equal to end SHALL be refused. Turning the switch on SHALL ask for the
notification permission when it is missing; a denial SHALL NOT keep the switch off. The choices
SHALL survive app and phone restarts and SHALL NOT be restored to another phone from a system
backup.

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
- **WHEN** the user changes the window, the mode or the power-saving switch while it is on
- **THEN** listening is re-planned at once (stopped with a summary if now is outside the new
  window, restarted with the new mode if inside)

#### Scenario: Limit changed
- **WHEN** the user changes the limit from 5 kg to 3 kg
- **THEN** later automatic weights are checked against 3 kg and weights already saved keep their
  mark

### Requirement: Trial status
The section SHALL show one status, the first that applies: "Estetty: Bluetooth-lupa puuttuu",
"Estetty: Health Connect -lupa puuttuu" (weight write access missing), "Estetty: Bluetooth pois
päältä", "Tauolla: virransäästötila" (power saving on and the switch for it off), "Kuuntelee nyt",
or "Lepää, alkaa klo HH.MM". When the app's battery use is restricted by the phone, a warning line
SHALL be shown under the status, "Akunkäyttö rajoitettu: kuuntelu voi katketa", while listening
still runs. When notifications are not allowed, a warning line "Ilmoitukset estetty" SHALL be
shown. Each blocked reason and each warning SHALL have a button that opens the matching system
screen or permission request. If the system stopped the app by force, the status SHALL say to open
the app once.

#### Scenario: Battery use restricted
- **WHEN** the phone restricts the app's battery use and the window is on
- **THEN** the status says "Kuuntelee nyt", the warning line is shown with a button to the app's
  battery setting, and listening runs

#### Scenario: Bluetooth off
- **WHEN** Bluetooth is off inside the window
- **THEN** the status says "Estetty: Bluetooth pois päältä" with a button to turn it on

#### Scenario: Health Connect access missing
- **WHEN** weight write access to Health Connect has been taken away
- **THEN** the status says "Estetty: Health Connect -lupa puuttuu" with a button that asks for it,
  and no scan runs

## REMOVED Requirements

### Requirement: Save nothing
**Reason**: Phase 2 saves the heard weight.
**Migration**: Replaced by "Save the automatic weight".

## ADDED Requirements

### Requirement: Save the automatic weight
While the feature is on, a weight heard from the remembered scale inside the window and decided by
the stable-weight rule SHALL be written to Health Connect as a weight automatically recorded by a
scale, with the time of the stable reading, without the app open and with the screen off. Its raw
reading SHALL be kept as for a dialog weigh-in, with the extra field `via=auto`. A weight outside
20-300 kg SHALL NOT be saved. A weighing already saved or discarded, by the dialog or in the
background, SHALL NOT be saved again (same rule as the scale's lingering broadcast).

#### Scenario: Morning weigh-in
- **GIVEN** the feature is on with a 06:00-10:00 window and the phone lies in another room
- **WHEN** the user steps on the scale at 07.12 and it settles at 82.5 kg
- **THEN** 82.5 kg at 07.12 is in Health Connect, as automatically recorded by a scale, and in
  Historia, without any tap

#### Scenario: Dialog open at the same time
- **WHEN** the user weighs with the dialog open while the background also hears the scale
- **THEN** Health Connect holds one weight for that weighing

#### Scenario: Outside the window
- **WHEN** the user weighs at 12.00 with a 06:00-10:00 window and no dialog open
- **THEN** nothing is saved

### Requirement: Keep a weight whose write failed
When writing an automatic weight fails, the weight SHALL be kept on the phone and written the next
time it can be (the next weigh-in, the hourly check or when the app opens), and SHALL NOT be lost;
its notification SHALL come when it is written.

#### Scenario: Write fails
- **WHEN** Health Connect refuses the background write
- **THEN** the weight is kept on the phone, the cause is logged, and it is written when the app is
  next opened

### Requirement: Questioned weight
An automatic weight further from the reference than the limit (strictly more) SHALL be saved and
marked "?". The reference SHALL be the latest weight before it in time, written by this app, that
is not marked "?" (a manual, dialog or accepted automatic weight). With no such weight the new
weight SHALL be accepted. Only automatic weights SHALL ever be marked.

#### Scenario: Within the limit
- **GIVEN** the reference is 82.0 kg and the limit 5 kg
- **WHEN** 86.5 kg is saved automatically
- **THEN** it is saved without a mark

#### Scenario: Exactly the limit
- **GIVEN** the reference is 82.0 kg and the limit 5 kg
- **WHEN** 87.0 kg is saved automatically
- **THEN** it is saved without a mark

#### Scenario: Over the limit
- **GIVEN** the reference is 82.0 kg and the limit 5 kg
- **WHEN** 61.3 kg is saved automatically
- **THEN** it is saved and marked "?"

#### Scenario: Earlier "?" skipped
- **GIVEN** 82.0 kg accepted on Monday and 61.3 kg marked "?" on Tuesday
- **WHEN** 82.4 kg is saved automatically on Wednesday
- **THEN** it is compared with 82.0 kg and saved without a mark

#### Scenario: First weight ever
- **GIVEN** this app has no weights
- **WHEN** 82.5 kg is saved automatically
- **THEN** it is saved without a mark

### Requirement: Confirm a questioned weight
Confirming a "?" weight SHALL remove the mark, so it becomes the reference for later weights;
deleting it SHALL remove it like any weight. The mark SHALL be kept in the app's own storage,
SHALL be removed with the weight, and SHALL travel in the data file.

#### Scenario: Re-calibrate after a long break
- **GIVEN** the reference is 92.0 kg from last year and 85.0 kg is saved and marked "?"
- **WHEN** the user confirms 85.0 kg
- **THEN** the mark is gone and the next automatic weight is compared with 85.0 kg

#### Scenario: Mark after reinstall
- **WHEN** a data file with a "?" weight is imported
- **THEN** that weight is marked "?" on the phone again

### Requirement: Weigh-in notification
After every automatic weigh-in the app SHALL show a notification with the weight and the change
from the reference ("82,5 kg tallennettu", "−0,3 kg edellisestä") and the action Poista. A "?"
weight's notification SHALL say it needs checking ("61,3 kg – tarkista", "Poikkeaa 20,7 kg
edellisestä hyväksytystä") and SHALL also have Hyväksy. Tapping the notification SHALL open
Historia. A notification without "?" SHALL be silent and disappear by itself after an hour; a "?"
notification SHALL use the phone's normal notification sound and stay until handled. Without the
notification permission the weight SHALL still be saved.

#### Scenario: Normal weigh-in
- **WHEN** 82.5 kg is saved automatically without a mark
- **THEN** a silent notification shows 82,5 kg, the change and Poista

#### Scenario: Questioned weigh-in
- **WHEN** 61.3 kg is saved automatically and marked "?"
- **THEN** a notification with sound asks to check it and offers Hyväksy and Poista

### Requirement: Notification actions
Poista SHALL delete the weight and its raw data without a confirmation; Hyväksy SHALL remove the
mark. Either SHALL work without opening the app and SHALL close the notification.

#### Scenario: Delete from the notification
- **WHEN** the user taps Poista on the notification
- **THEN** the weight and its raw data are removed from the phone and the notification closes

#### Scenario: Confirm from the notification
- **WHEN** the user taps Hyväksy on a "?" weight's notification
- **THEN** the mark is removed and the notification closes

#### Scenario: Deletion fails
- **WHEN** deleting from the notification fails
- **THEN** the notification stays and says the deletion failed, and the cause is logged

#### Scenario: Handled elsewhere
- **WHEN** the user confirms or deletes the weight in Historia while its notification is shown
- **THEN** the notification closes

### Requirement: Widget status
While the feature is on, the home-screen widget SHALL show one of three states next to Punnitse:
"Päällä" (inside the window and nothing blocks), "Lepää" (outside the window) or "Estetty"
(inside the window and a blocked reason of the status, or the phone restricts the app's battery
use). The states SHALL differ by a symbol as well as by colour and SHALL be read out by a screen
reader. The widget SHALL change state at the window start and end and catch up with a new blocked
reason within about an hour. With the feature off the widget SHALL look as before.

#### Scenario: Morning
- **GIVEN** the window is 06:00-10:00 and nothing blocks
- **WHEN** the window starts
- **THEN** the widget shows "Päällä"

#### Scenario: Night
- **WHEN** the time is outside the window
- **THEN** the widget shows "Lepää", also when Bluetooth is off

#### Scenario: Battery restricted
- **WHEN** the phone restricts the app's battery use inside the window
- **THEN** the widget shows "Estetty"

#### Scenario: Feature off
- **WHEN** the feature is off
- **THEN** the widget shows only Punnitse as before
