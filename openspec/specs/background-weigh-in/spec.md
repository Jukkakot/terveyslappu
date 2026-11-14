# background-weigh-in Specification

## Purpose
Listens for the user's own scale without any tap, inside a time window the user sets, so a
weigh-in can be caught while the phone lies elsewhere. In this phase it only listens and records
what it heard; saving the weight comes later.

## Requirements

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
"Estetty: Health Connect -lupa puuttuu" (weight write access missing), "Estetty: Bluetooth pois
päältä", "Tauolla: virransäästötila" (power saving on and the switch for it off), "Kuuntelee nyt",
"Pysäytetty" (the system stopped the app by force), or "Lepää, alkaa klo HH.MM". When the app's battery use is restricted by the phone, a warning line
SHALL be shown under the status, "Akunkäyttö rajoitettu: kuuntelu voi katketa", while listening
still runs. When notifications are not allowed, a warning line "Ilmoitukset estetty" SHALL be
shown. Each blocked reason and each warning SHALL have a button that opens the matching system
screen or permission request. "Pysäytetty" SHALL say to open the app once. While weights wait in the pending queue the
section SHALL also show "N punnitusta odottaa tallennusta".

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

### Requirement: Save the automatic weight
While the feature is on, a weight heard from the remembered scale inside the window and decided by
the stable-weight rule SHALL be written to Health Connect as a weight automatically recorded by a
scale, without the app open and with the screen off. The background SHALL use the same
stable-weight rule as the weigh-in dialog, fed with the packets' timestamps, so both decide the
same kg; the saved time SHALL be the time of the deciding packet. The raw reading SHALL be kept as
for a dialog weigh-in, with the extra field `via=auto`. A weight outside 20-300 kg SHALL NOT be
saved. The background SHALL NOT save while the weigh-in dialog is in the foreground.

#### Scenario: Morning weigh-in
- **GIVEN** the feature is on with a 06:00-10:00 window and the phone lies in another room
- **WHEN** the user steps on the scale at 07.12 and it settles at 82.5 kg
- **THEN** 82.5 kg is in Health Connect at the time of the deciding packet, as automatically
  recorded by a scale, and in Historia, without any tap

#### Scenario: Dialog open at the same time
- **WHEN** the user weighs with the dialog open while the background also hears the scale
- **THEN** the dialog saves the weighing, the background saves nothing, and Health Connect holds
  one weight for that weighing

#### Scenario: Outside the window
- **WHEN** the user weighs at 12.00 with a 06:00-10:00 window and no dialog open
- **THEN** nothing is saved

### Requirement: One weighing, one save
The weighing key (the scale packet's company id) SHALL be claimed atomically before anything is
written, by one store shared by the dialog and the background. Whoever claims it first saves; the
other SHALL ignore packets with that key. A claim whose write failed and was queued stays claimed.

#### Scenario: Two claimers
- **WHEN** the dialog and the background both decide the weighing with key `61c0` at once
- **THEN** exactly one of them saves it

### Requirement: Keep a weight whose write failed
When writing an automatic weight fails, the weight SHALL be kept on the phone in a pending queue
and written the next time it can be, and SHALL NOT be lost. The queue SHALL be retried at the next
weigh-in, the hourly check, and at the start of the first foreground read after the app opens,
before the list is shown. Asetukset SHALL show "N punnitusta odottaa tallennusta" while the queue is
not empty. The notification of a queued weight SHALL come when it is written.

#### Scenario: Write fails
- **WHEN** Health Connect refuses the background write
- **THEN** the weight is kept, the cause is logged, Asetukset shows "1 punnitusta odottaa
  tallennusta", and it is written when the app is next opened, before Historia shows its list

### Requirement: Background time budget
The background save SHALL log the duration of each step (`ms=`) and SHALL give up at about 8 seconds
and put the weight in the pending queue.

#### Scenario: Slow write
- **WHEN** the save has not finished after about 8 seconds
- **THEN** the weight is queued and the receiver ends

### Requirement: Weights changed signal
A change made to the weights by the app itself, in the background or in the foreground, SHALL send
a process-wide "weights changed" signal. The main screen SHALL reload its weights on the signal
and when the app resumes.

#### Scenario: Notification action while the app is open
- **WHEN** the user taps Hyväksy on a notification while Historia is shown
- **THEN** the list updates without a manual refresh

### Requirement: Reference weight
The reference SHALL be the latest accepted weight written by this app before the new one (a
manual, dialog or accepted automatic weight; not "?"). The app SHALL keep its latest ten accepted
own (time, kg) pairs in its own storage and recompute the reference from them after every save,
delete, undo, edit, import and acceptance; a foreground read of the weights SHALL replace the list
and seed the store. The reference SHALL have an explicit "unknown" state, e.g. right after the
update from the trial before the app was opened. Weights written by other apps SHALL NOT count.

#### Scenario: Reference after a delete
- **GIVEN** accepted weights 82.0 kg (Monday) and 82.4 kg (Tuesday)
- **WHEN** the user deletes the 82.4 kg weight
- **THEN** the reference is 82.0 kg without a Health Connect read

#### Scenario: Unknown reference
- **GIVEN** the update was installed and the app has not been opened since
- **WHEN** an automatic weight of 82.5 kg is decided in the morning
- **THEN** it is saved and marked "?", and after the user opens the app the reference store is
  seeded from the read weights

### Requirement: Questioned weight
An automatic weight further from the reference than the limit (strictly more) SHALL be saved and
marked "?". While the reference is unknown the weight SHALL be saved and marked "?". With no
accepted own weight at all (a known, empty reference) the new weight SHALL be accepted. The rule
only marks automatic weights; import restores marks as they are in the file. A "?" weight SHALL NOT
be used as a reference.

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
- **GIVEN** this app has no weights and the reference is known to be empty
- **WHEN** 82.5 kg is saved automatically
- **THEN** it is saved without a mark

### Requirement: Questioned mark storage
The "?" marks SHALL be kept in the app's own storage keyed by the weight's own record
(`clientRecordId`), not by its second. A mark SHALL be removed with its weight and restored by undo.
Everything that filters or counts "?" weights (charts, statistics, the home card, the home notice)
SHALL use only marks that match a read weight, and a foreground read SHALL drop marks that match no
weight.

#### Scenario: Orphan mark
- **GIVEN** a weight marked "?" was deleted in another app
- **WHEN** the app reads its weights in the foreground
- **THEN** its mark is dropped and no notice about it is shown

#### Scenario: Two weights in the same second
- **GIVEN** two own weights share the same second and only one is marked "?"
- **THEN** only that one is treated as questioned

### Requirement: Accept a questioned weight
Accepting a "?" weight, in Historia or from its notification, SHALL remove its mark so it becomes
the reference for later weights. Chained acceptance: the later "?" weights in time order whose
difference to the previous accepted weight in the chain is within the limit SHALL then be accepted
automatically, one after another, stopping at the first one outside the limit. Deleting a "?"
weight SHALL remove it like any weight.

#### Scenario: Re-calibrate after a long break
- **GIVEN** the reference is 92.0 kg from last year and 85.0 kg is saved and marked "?"
- **WHEN** the user accepts 85.0 kg
- **THEN** the mark is gone and the next automatic weight is compared with 85.0 kg

#### Scenario: Chain
- **GIVEN** the limit is 5 kg and "?" weights 85.0, 84.2, 83.9 and 61.3 kg in time order
- **WHEN** the user accepts 85.0 kg
- **THEN** 84.2 and 83.9 kg are accepted too and 61.3 kg stays "?"

#### Scenario: Chain from a notification
- **WHEN** the user taps Hyväksy on the notification of the first "?" weight of such a chain
- **THEN** the same later weights are accepted

#### Scenario: Mark after reinstall
- **WHEN** a data file with a "?" weight is imported
- **THEN** that weight is marked "?" on the phone again

### Requirement: Weigh-in notification
After every automatic weigh-in the app SHALL show a notification with the weight and, when a
reference exists, the change from it ("82,5 kg tallennettu", "−0,3 kg edellisestä") and the action
Poista. With no reference the text SHALL be only "82,5 kg tallennettu". A "?" weight's
notification SHALL say it needs checking ("61,3 kg – tarkista", "Poikkeaa 20,7 kg edellisestä
hyväksytystä") and SHALL also have Hyväksy. A weight saved late from the pending queue SHALL add its
weighing time ("82,5 kg tallennettu klo 7.12"). Tapping the notification SHALL open Historia. All
the notifications SHALL be in one group in one channel family, and none SHALL disappear by itself:
each stays until the user dismisses it. Every weigh-in notification SHALL alert (default
importance: sound and vibration as the phone is set), so a connected watch can show it. The
earlier silent channel SHALL be replaced by an alerting one on update. Without the notification
permission the weight SHALL still be saved.

#### Scenario: Normal weigh-in
- **WHEN** 82.5 kg is saved automatically without a mark
- **THEN** an alerting notification shows 82,5 kg, the change and Poista, and stays until dismissed

#### Scenario: Questioned weigh-in
- **WHEN** 61.3 kg is saved automatically and marked "?"
- **THEN** an alerting notification asks to check it and offers Hyväksy and Poista, and stays until
  dismissed

#### Scenario: No reference
- **WHEN** a weight is saved while the reference is unknown
- **THEN** the notification says only "82,5 kg tallennettu" and, being marked, offers Hyväksy

#### Scenario: Late save
- **WHEN** a queued weight from 07.12 is written at 09.30
- **THEN** its notification includes "klo 7.12"

#### Scenario: Update from the silent channel
- **WHEN** the app updates from a version with the silent channel
- **THEN** the silent channel is removed and the next normal weigh-in alerts

### Requirement: Notification actions
Poista SHALL delete the weight and its raw data without a confirmation; Hyväksy SHALL accept the
weight (with chained acceptance). Either SHALL work without opening the app and SHALL close the
notification.

#### Scenario: Delete from the notification
- **WHEN** the user taps Poista on the notification
- **THEN** the weight and its raw data are removed from the phone and the notification closes

#### Scenario: Accept from the notification
- **WHEN** the user taps Hyväksy on a "?" weight's notification
- **THEN** the mark is removed and the notification closes

#### Scenario: Deletion fails
- **WHEN** deleting from the notification fails
- **THEN** the notification stays and says the deletion failed, and the cause is logged

#### Scenario: Handled elsewhere
- **WHEN** the user accepts or deletes the weight in Historia while its notification is shown
- **THEN** the notification closes

### Requirement: Widget status
While the feature is on, the 1x1 home-screen widget SHALL show the automatic weigh-in's state as a
small symbol only (no text, no times), each state with its own shape and colour and read out by a
screen reader by its name: Kuuntelee (green filled dot: inside the window and listening), Valmis
(green check: the window's weight is saved, from that save until the next window start, also after
the window has ended), Lepää (grey moon: outside the window with no weight of the window saved)
and Estetty (amber warning triangle: a blocked reason, Tauolla or Pysäytetty, inside the window).
The warnings (battery use restricted, notifications off) SHALL NEVER mean Estetty. The state SHALL
change within about 15 minutes of the window start and end; the widget SHALL store the window end
and show Lepää instead of Kuuntelee once it has passed when it is next redrawn. With the feature
off the widget SHALL show no symbol.

#### Scenario: Morning
- **GIVEN** the window is 06:00-10:00 and nothing blocks
- **WHEN** the window starts
- **THEN** the widget shows Kuuntelee within about 15 minutes

#### Scenario: Weighed
- **WHEN** the morning's weight is saved at 07.12 without a mark
- **THEN** the widget shows Valmis, still at 20.00, and Kuuntelee from the next 06:00

#### Scenario: Night
- **WHEN** the time is outside the window and no weight of the window was saved
- **THEN** the widget shows Lepää, also when Bluetooth is off

#### Scenario: Battery restricted
- **WHEN** the phone restricts the app's battery use inside the window
- **THEN** the status shows its warning and the widget still shows Kuuntelee

#### Scenario: Power saving pause
- **WHEN** power saving is on and the switch for listening in it is off, inside the window
- **THEN** the widget shows Estetty

#### Scenario: Window ended
- **GIVEN** the widget showed Kuuntelee and the window end has passed
- **WHEN** the widget is next redrawn
- **THEN** it shows Lepää

#### Scenario: Feature off
- **WHEN** the feature is off
- **THEN** the widget shows only Punnitse as before

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
- **THEN** the scan stops, and the status and the widget show Valmis

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

### Requirement: Timely background decision
When a stable reading is waiting in the background for the stable-weight rule's quiet time, the
app SHALL wake itself a few seconds after that quiet time and decide it then, unless a later
packet decided it first, so the weight is saved and notified within about half a minute of the
scale settling, also when no further packet comes. The deciding time SHALL still be the reading's
own packet time.

#### Scenario: Single stable packet
- **WHEN** the scale's stable 99.05 kg is heard once at 16.23.02 and nothing after it
- **THEN** the weight is saved and notified by about 16.23.30, at the time 16.23.02

### Requirement: Status symbol in Asetukset
The status line of "Automaattinen punnitus" SHALL start with the same symbol and colour as the
widget's state (no symbol while the feature is off). While the window's weight is saved the status
SHALL read "Valmis: tämän päivän paino tallennettu, kuuntelee taas klo HH.MM", with "huomenna"
before "klo" when the next start is tomorrow; it comes after the blocked reasons and before
"Kuuntelee nyt" and "Lepää".

#### Scenario: Weighed this morning
- **GIVEN** a 06:00-10:00 window and the weight saved at 07.12
- **WHEN** Asetukset is opened at 08.00
- **THEN** the status shows the green check and "Valmis: tämän päivän paino tallennettu, kuuntelee
  taas huomenna klo 6.00"
