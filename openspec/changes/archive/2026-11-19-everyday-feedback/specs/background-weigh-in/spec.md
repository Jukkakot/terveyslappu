## MODIFIED Requirements

### Requirement: Save the automatic weight
While the feature is on, a weight heard from the remembered scale inside the window SHALL be
written to Health Connect as a weight automatically recorded by a scale, without the app open and
with the screen off. The background SHALL decide the weight at the first stable packet of a
weighing, without waiting for a quiet time; the saved time SHALL be the time of that packet. The
raw reading SHALL be kept as for a dialog weigh-in, with the extra field `via=auto`. A weight
outside 20-300 kg SHALL NOT be saved. The background SHALL NOT save while the weigh-in dialog is in
the foreground checking access, scanning, showing a live weight or saving; while the dialog shows
a result (saved, the recent automatic weight, deleted), the end view or an access problem, the
background SHALL work as usual.

#### Scenario: Morning weigh-in
- **GIVEN** the feature is on with a 06:00-10:00 window and the phone lies in another room
- **WHEN** the user steps on the scale at 07.12 and it settles at 82.5 kg
- **THEN** 82.5 kg is in Health Connect at the time of the deciding packet, as automatically
  recorded by a scale, and in Historia, without any tap

#### Scenario: Dialog open at the same time
- **WHEN** the user weighs with the dialog open while the background also hears the scale
- **THEN** the dialog saves the weighing, the background saves nothing, and Health Connect holds
  one weight for that weighing

#### Scenario: Dialog left on its end view
- **GIVEN** the dialog timed out and shows why nothing was saved
- **WHEN** the user steps on the scale inside the window without tapping anything
- **THEN** the background saves the weight as usual

#### Scenario: Outside the window
- **WHEN** the user weighs at 12.00 with a 06:00-10:00 window and no dialog open
- **THEN** nothing is saved

### Requirement: Trial status
While the feature is on, the section SHALL show one explanation line under the switch, the first
that applies: the weighed line (from the save of the window's weight until the next window start,
see "Status symbol in Asetukset"), "Ei toimi: vaakaa ei vielä tunneta", "Ei toimi:
Bluetooth-lupa puuttuu", "Ei toimi: Health Connect -lupa puuttuu" (weight write access missing),
"Ei toimi: Bluetooth pois päältä", "Ei toimi: avaa sovellus kerran" (the system stopped the app
by force), "Ei toimi: virransäästötila päällä" (power saving on and the switch for it off),
"Odottaa vaakaa klo HH.MM asti" (inside the window), or "Alkaa klo HH.MM". At the next window
start the weighed line ends and an "Ei toimi" reason shows if one applies. While the feature is
off the line SHALL be absent. When the app's battery use is restricted by the phone, a warning
line SHALL be shown under it, "Akunkäyttö rajoitettu: punnitus voi jäädä tunnistamatta", while
scanning still runs. When notifications are not allowed, a warning line "Ilmoitukset estetty"
SHALL be shown. Each "Ei toimi" reason (except "avaa sovellus kerran" and "vaakaa ei vielä
tunneta") and each warning SHALL have a button that opens the matching system screen or
permission request. While weights wait in the pending queue the section SHALL also show "N painoa
odottaa tallennusta".

#### Scenario: Battery use restricted
- **WHEN** the phone restricts the app's battery use and the window is on
- **THEN** the line says "Odottaa vaakaa klo 10.00 asti", the warning line is shown with a button
  to the app's battery setting, and scanning runs

#### Scenario: Bluetooth off
- **WHEN** Bluetooth is off inside the window
- **THEN** the line says "Ei toimi: Bluetooth pois päältä" with a button to turn it on

#### Scenario: Bluetooth off after weighing
- **GIVEN** the window's weight was saved at 07.12
- **WHEN** Bluetooth is turned off at 08.00 and Asetukset is opened
- **THEN** the line still says "Tänään punnittu, seuraavan kerran huomenna klo 6.00", and at 6.00
  the next morning it says "Ei toimi: Bluetooth pois päältä"

#### Scenario: Health Connect access missing
- **WHEN** weight write access to Health Connect has been taken away
- **THEN** the line says "Ei toimi: Health Connect -lupa puuttuu" with a button that asks for it,
  and no scan runs

#### Scenario: Before the window
- **WHEN** the window is 06:00-10:00 and the time is 23.00
- **THEN** the line says "Alkaa huomenna klo 6.00"

### Requirement: Weigh-in notification
After every automatic weigh-in the app SHALL show a notification with the weight and, when a
reference exists, the change from it ("82,5 kg tallennettu", "−0,3 kg edellisestä"). A normal
weigh-in's notification SHALL have the one action Poista paino. A "?" weight's notification
SHALL say it needs checking ("61,3 kg – tarkista", "Poikkeaa 20,7 kg edellisestä hyväksytystä") and
SHALL have Hyväksy and Poista paino. With no reference the text SHALL be only "82,5 kg
tallennettu". A weight saved late from the pending queue SHALL add its weighing time ("82,5 kg
tallennettu klo 7.12"). Tapping the notification SHALL open Historia. All the notifications SHALL
be in one group in one alerting channel family, and none SHALL disappear by itself except the
"Paino poistettu" state of "Notification actions". Every notification SHALL be dismissable with a
swipe. Without the notification permission the weight SHALL still be saved.

#### Scenario: Normal weigh-in
- **WHEN** 82.5 kg is saved automatically without a mark
- **THEN** an alerting notification shows 82,5 kg, the change and Poista paino (no OK), and stays
  until swiped away

#### Scenario: Questioned weigh-in
- **WHEN** 61.3 kg is saved automatically and marked "?"
- **THEN** an alerting notification asks to check it and offers Hyväksy and Poista paino, and
  stays until dismissed

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
Poista paino SHALL delete the weight and its raw data at once, without a confirmation, and turn the
notification silently into "Paino poistettu" (no weight shown) with the action Kumoa; that state
SHALL go away by itself after about 10 seconds, lengthened when Android's accessibility timeout
asks for longer; when it was the group's last notification, the group goes with it. Kumoa SHALL
write the weight back exactly as it was (time, weight, raw data, its "?" mark when it had one),
count as a change for the automatic backup file, and show the original notification again
silently. A failed Kumoa SHALL only say so in the notification; the weight is not put in the
pending queue. Hyväksy SHALL accept
the weight (with chained acceptance) and close the notification. Each SHALL work without opening
the app. Swiping a "?" notification away SHALL leave the mark.

#### Scenario: Delete from the notification
- **WHEN** the user taps Poista paino on the notification
- **THEN** the weight and its raw data are removed from the phone and the notification reads
  "Paino poistettu" with Kumoa, without a sound

#### Scenario: Undo time over
- **WHEN** the user does nothing after Poista paino
- **THEN** "Paino poistettu" is gone after about 10 seconds and the weight stays deleted

#### Scenario: Kumoa
- **GIVEN** the user deleted 82.5 kg of 7.12 from its notification
- **WHEN** the user taps Kumoa
- **THEN** 82.5 kg at 7.12 is back in Health Connect and Historia with its raw data, and the
  notification shows "82,5 kg tallennettu" with Poista paino again, without a sound

#### Scenario: Kumoa of a questioned weight
- **GIVEN** the user deleted a "?" weight from its notification
- **WHEN** the user taps Kumoa
- **THEN** the weight is back with its "?" mark and its notification offers Hyväksy and Poista paino

#### Scenario: Kumoa fails
- **WHEN** writing the weight back fails
- **THEN** the notification says "Painoa ei voitu palauttaa", keeps Kumoa, stays until dismissed,
  and the cause is logged

#### Scenario: OK
- **WHEN** a normal weigh-in's notification is shown
- **THEN** it has no OK action; a swipe closes it and the weight stays

#### Scenario: Accept from the notification
- **WHEN** the user taps Hyväksy on a "?" weight's notification
- **THEN** the mark is removed and the notification closes

#### Scenario: Swiped away
- **WHEN** the user swipes a "?" weight's notification away
- **THEN** the weight keeps its "?" mark

#### Scenario: Deletion fails
- **WHEN** deleting from the notification fails
- **THEN** the notification stays and says the deletion failed, and the cause is logged

#### Scenario: Handled elsewhere
- **WHEN** the user accepts or deletes the weight in Historia or the weigh-in dialog while its
  notification is shown
- **THEN** the notification closes

### Requirement: Widget status
While the feature is on, the 1x1 home-screen widget SHALL show one small symbol only (no text, no
times, never a weight), read out by a screen reader: "Automaattinen punnitus päällä" (green filled
dot), "Tänään punnittu" (a ✓ in the same green: from the save of the window's weight until the next
window start, see "Stop after the window's weight"; "Punnittu" once the date has changed since,
so after midnight) or "Automaattinen punnitus ei toimi" (amber warning triangle: an "Ei toimi"
reason inside the window). The ✓ SHALL come before the triangle, in the same order as the
Asetukset line. The warnings (battery use restricted, notifications off) SHALL NEVER show the triangle.
The symbol SHALL change within about 15 minutes of the window start and end, and of a reason
appearing or clearing inside the window, and at once when the window's weight is saved, deleted or
brought back; outside the window, and at a redraw after the stored window end, it SHALL be the dot
unless the ✓ applies. With the feature off the widget SHALL show no symbol.

#### Scenario: Morning
- **GIVEN** the window is 06:00-10:00 and nothing blocks
- **WHEN** the window starts
- **THEN** the widget shows the dot

#### Scenario: Weighed
- **WHEN** the morning's weight is saved at 07.12 without a mark
- **THEN** the widget shows the ✓ at once, still at 20.00, and the dot again from 6.00 the next
  morning

#### Scenario: Weight deleted
- **GIVEN** the widget shows the ✓ for the 07.12 weight
- **WHEN** the user deletes that weight at 07.13 inside the window
- **THEN** the widget shows the dot

#### Scenario: Screen reader
- **WHEN** TalkBack reads the widget showing the ✓ for the 07.12 weight at 08.00
- **THEN** it says "Punnitse, tänään punnittu"

#### Scenario: Screen reader after midnight
- **WHEN** TalkBack reads the widget showing the ✓ for yesterday evening's weight at 00.30
- **THEN** it says "Punnitse, punnittu"

#### Scenario: Bluetooth off after weighing
- **GIVEN** the widget shows the ✓ for the 07.12 weight
- **WHEN** Bluetooth is turned off at 08.00 inside the window
- **THEN** the widget keeps the ✓

#### Scenario: Window ended
- **GIVEN** the widget showed the triangle because Bluetooth was off and the window end has passed
- **WHEN** the widget is next redrawn
- **THEN** it shows the dot

#### Scenario: Night
- **WHEN** the time is outside the window and no weight of the last window is saved
- **THEN** the widget shows the dot, also when Bluetooth is off

#### Scenario: Battery restricted
- **WHEN** the phone restricts the app's battery use inside the window
- **THEN** the section shows its warning and the widget still shows the dot

#### Scenario: Power saving pause
- **WHEN** power saving is on and the switch for it is off, inside the window
- **THEN** the widget shows the triangle

#### Scenario: Feature off
- **WHEN** the feature is off
- **THEN** the widget shows only Punnitse as before, also after a weigh-in

### Requirement: Stop after the window's weight
Once a weight not marked "?" whose time is inside the current listening window has been saved by
this app (weigh-in dialog, manual entry or the background), listening SHALL stop for the rest of
that window and SHALL start again at the next window's start. When that weight is deleted
(dialog Poista, notification Poista, Historia) or later marked "?", and no other such weight of
this app remains in the window, listening SHALL start again at once if the window is still on.
Accepting a "?" weight of the window, and Kumoa of a notification delete, SHALL count as saving
it. Weights written by other apps SHALL NOT count. Stopping SHALL be logged
(`listen.stop reason=done`).

#### Scenario: Morning weight saved
- **GIVEN** a 06:00-10:00 window
- **WHEN** 82.5 kg is saved automatically at 07.12 without a mark
- **THEN** the scan stops, the line reads "Tänään punnittu, seuraavan kerran huomenna klo
  6.00" and the widget shows the ✓

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

#### Scenario: Deleted and brought back
- **GIVEN** the user deleted the 07.12 weight from its notification and listening started again
- **WHEN** the user taps Kumoa
- **THEN** listening stops again and the widget shows the ✓

#### Scenario: Next day
- **GIVEN** listening stopped yesterday after the day's weight
- **WHEN** today's window starts
- **THEN** listening starts as usual

### Requirement: Status symbol in Asetukset
The explanation line of "Automaattinen punnitus" SHALL start with the green dot, the green ✓ for
the "Tänään punnittu" line, or the amber triangle for every "Ei toimi" line at any time of day (no
symbol while the feature is off); the symbol is decorative for screen readers, the line carries the
meaning. While the window's weight is saved, from that save until the next window start, the line
SHALL read "Tänään punnittu, seuraavan kerran huomenna klo HH.MM" while the next start is
tomorrow, and "Punnittu, seuraavan kerran klo HH.MM" once the next start is today (after
midnight); it comes before the "Ei toimi" reasons, "Odottaa vaakaa" and "Alkaa" ("Trial
status").

#### Scenario: Weighed this morning
- **GIVEN** a 06:00-10:00 window and the weight saved at 07.12
- **WHEN** Asetukset is opened at 08.00
- **THEN** the line shows the green ✓ and "Tänään punnittu, seuraavan kerran huomenna klo 6.00"

#### Scenario: After midnight
- **GIVEN** a 06:00-10:00 window and the weight saved at 07.12 yesterday
- **WHEN** Asetukset is opened at 00.30
- **THEN** the line shows the green ✓ and "Punnittu, seuraavan kerran klo 6.00"

## ADDED Requirements

### Requirement: Notification actions in the log
Poista paino SHALL log `auto.action delete ok from=notification ageS=` with the seconds since the
weighing time (`from=dialog` for the weigh-in dialog's Poista on the recent automatic weight), and
Kumoa SHALL log `auto.action undo ok ageS=` or `auto.action undo failed cause=`, never the
weight.

#### Scenario: Deleted and undone
- **WHEN** the user taps Poista paino 40 seconds after the weighing and then Kumoa
- **THEN** the log has `auto.action delete ok from=notification ageS=40` and `auto.action undo ok`
