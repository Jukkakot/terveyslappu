## MODIFIED Requirements

### Requirement: Save the scale weight
The app SHALL write the decided weight to Health Connect as a weight measured by a scale, with the
time of the stable reading, and SHALL show the saved weight with the choices "Poista" and "Valmis"
for about ten seconds before the dialog closes; the ten seconds SHALL be lengthened when Android's
accessibility timeout ("Time to take action") asks for longer, and Valmis SHALL show the seconds
left ("Valmis (8)"), counting down from the lengthened time. A tap on the dialog SHALL keep it open until the user closes it, and the
seconds then leave the button. Once a weight is decided, the save and what follows it SHALL finish
even when the dialog is recreated (rotation, theme or language change) or closed, and a recreated
dialog SHALL show the same result instead of weighing again. While the weight is being saved,
tapping outside the dialog and Back SHALL not close it.

#### Scenario: Saved
- **WHEN** the stable weight is decided
- **THEN** it is written to Health Connect within a second
- **AND** the dialog shows the weight, "Tallennettu", "Poista" and "Valmis (10)" counting down, and
  closes after about ten seconds

#### Scenario: Longer accessibility timeout
- **GIVEN** Android's accessibility timeout is set to one minute
- **WHEN** a weight is saved and the user does nothing
- **THEN** the result stays for about a minute before the dialog closes

#### Scenario: Keep the result open
- **WHEN** the user taps the dialog while it shows the saved weight
- **THEN** it no longer closes by itself and Valmis shows no seconds

#### Scenario: Done
- **WHEN** the user taps Valmis
- **THEN** the dialog closes at once

#### Scenario: Same weigh-in seen twice
- **WHEN** the same stable reading would be saved a second time in the same weigh-in
- **THEN** Health Connect keeps one weight, not two

#### Scenario: Turned while saving
- **GIVEN** the dialog is saving 82.5 kg
- **WHEN** the phone is turned and the dialog is recreated
- **THEN** 82.5 kg is saved once and the dialog shows it as saved, without scanning again

#### Scenario: Tap outside while saving
- **WHEN** the user taps outside the dialog or presses Back while it says it is saving
- **THEN** the dialog stays open until the weight is saved

#### Scenario: Save fails
- **WHEN** writing to Health Connect fails
- **THEN** the dialog stays open, says the save failed and offers to retry, and the cause is logged

### Requirement: Weigh-in dialog
The app SHALL offer a small weigh-in dialog over whatever is on screen, which starts scanning at
once, tells the user to step on the scale and shows the live weight while it settles. From the start
of scanning the dialog SHALL show how many seconds are left, counting down from 30, also while a
changing weight is shown. When no weight has been saved when the count reaches zero, the app SHALL
stop scanning and the dialog SHALL stay open on an end view that says why nothing was saved, with
"Valmis" (text button, closes) and "Punnitse uudelleen" (filled button, scans again with a new
count of 30). The end view SHALL NOT close by itself; Back and a tap outside close it. The reason
SHALL be the first that applies:
- packets were ignored as repeats ("Ignore the scale's lingering broadcast", "Ignore the same
  weight repeated") and either nothing else was heard or a stable reading was among the ignored
  ones (live readings that settled on the repeated weight): the repeat line of those
  requirements, ending "…, joten sitä ei tallennettu uudelleen."
- a reading other than an ignored repeat was heard but never became stable: "Paino ei
  vakiintunut 30 sekunnissa."
- nothing was heard: "Vaakaa ei tunnistettu 30 sekunnissa. Tarkista, että vaa'an näyttö syttyy
  ja puhelin on lähellä."

Punnitse uudelleen SHALL check access again as an opening does (Health Connect, nearby devices,
Bluetooth) but SHALL NOT show the recent automatic weight ("Open on a recent automatic weight").

When Bluetooth is off, the dialog SHALL first guide the user to turn it on; the count starts only
when scanning starts.

#### Scenario: Step on the scale
- **WHEN** the dialog is open and the user steps on the scale
- **THEN** the changing weight is shown until the stable weight is saved

#### Scenario: Countdown
- **WHEN** the dialog has been scanning for 2 seconds
- **THEN** it shows 28 seconds left

#### Scenario: No scale heard
- **WHEN** nothing is heard from the scale within 30 seconds of scanning
- **THEN** scanning stops and the dialog stays open with "Vaakaa ei tunnistettu 30 sekunnissa…",
  Valmis and Punnitse uudelleen

#### Scenario: Weight never settled
- **WHEN** the scale sends changing weights but no stable one within 30 seconds
- **THEN** the dialog says "Paino ei vakiintunut 30 sekunnissa." with Valmis and Punnitse uudelleen

#### Scenario: Weigh again from the end view
- **GIVEN** the dialog shows why nothing was saved
- **WHEN** the user taps Punnitse uudelleen
- **THEN** scanning starts again with "Astu vaa'alle" and 30 seconds left

#### Scenario: Weigh again with Bluetooth turned off meanwhile
- **GIVEN** the dialog shows why nothing was saved and the user has turned Bluetooth off
- **WHEN** the user taps Punnitse uudelleen
- **THEN** the dialog asks to turn Bluetooth on before scanning

#### Scenario: Live weight that settles on the repeat
- **GIVEN** 82.5 kg was saved at 7.12 a moment ago
- **WHEN** the dialog shows a changing weight that settles at 82.5 kg from the same scale and the
  30 seconds run out
- **THEN** the end view gives the repeat line, not "Paino ei vakiintunut"

#### Scenario: End view stays
- **GIVEN** the dialog shows why nothing was saved
- **WHEN** the user does nothing for a minute
- **THEN** the dialog is still open

#### Scenario: Bluetooth off
- **GIVEN** Bluetooth is off
- **WHEN** the dialog opens
- **THEN** it asks to turn Bluetooth on and shows no countdown until scanning starts

#### Scenario: Closed early
- **WHEN** the user closes the dialog before a weight is saved
- **THEN** scanning stops and nothing is saved

### Requirement: One-tap start
The weigh-in dialog SHALL open with one tap from a Quick Settings tile, a home-screen widget and a
launcher shortcut on the app icon; inside the app it is "Punnitse" in the ⋮ menu of the front page
and Historia (two taps, the one exception to one tap). The widget and the tile SHALL
show only the Punnitse action, never a weight; while automatic weigh-in is on, the widget SHALL
also show its symbol (the dot, the ✓ or the warning triangle, see "Widget status" in
background-weigh-in). The widget SHALL fit one home-screen cell.

#### Scenario: From the tile
- **WHEN** the user taps the Punnitse tile in Quick Settings
- **THEN** the panel collapses and the weigh-in dialog opens without the main screen

#### Scenario: From the widget
- **WHEN** the user taps the Punnitse widget on the home screen
- **THEN** the weigh-in dialog opens without the main screen, whatever state the widget shows

#### Scenario: No weight before weighing
- **GIVEN** weights have been saved earlier
- **WHEN** the user looks at the widget or the tile
- **THEN** neither shows a weight or a day

#### Scenario: From the main screen
- **WHEN** the user taps ⋮ and then Punnitse on the main screen
- **THEN** the weigh-in dialog opens and, after it closes, the front page card or Historia's list
  shows the new weight

### Requirement: Ignore the scale's lingering broadcast
For two minutes after a weigh-in is saved or discarded, from the dialog or in the background, a
new weigh-in in either SHALL ignore scale packets carrying that weigh-in's company id, so a reading
the scale is still repeating is not saved again. While the dialog is ignoring such packets and has
heard nothing else, it SHALL show the repeat line instead of "Astu vaa'alle": "Vaaka toistaa
äskeistä punnitusta (82,5 kg, tallennettu klo 7.12)." with the countdown going on. While a live
weight shows and its stable reading is being ignored, the repeat line SHALL show under the weight.
The brackets SHALL show only when the ignored packet decodes to a stable weight within 0.1 kg of
the remembered last weight (from the same scale address, or either address unknown); the time is
when that weight was saved or deleted ("poistettu" when deleted). Otherwise the line has no
brackets. With several ignored packets the newest decides.

#### Scenario: Reopened right after Poista
- **GIVEN** a weigh-in with company id `61c0` was discarded 10 seconds ago
- **WHEN** the dialog is opened again and the scale still sends the old stable packet with `61c0`
- **THEN** nothing is saved and the dialog says the scale repeats the previous weigh-in, deleted
  at its time, instead of "Astu vaa'alle"
- **AND** a new weighing with company id `62c0` is saved as usual

#### Scenario: Saved in the background first
- **GIVEN** the background saved the weighing with company id `61c0` a moment ago
- **WHEN** the dialog is open and receives `61c0`
- **THEN** the dialog does not save it again

#### Scenario: Old enough
- **GIVEN** the last weigh-in with company id `61c0` ended three minutes ago
- **WHEN** a packet with `61c0` arrives
- **THEN** it is used as usual

### Requirement: Ignore the same weight repeated
After a weigh-in is saved or deleted, from the dialog, the background, a notification or Historia,
a stable reading from the same scale address within 0.1 kg of that weight SHALL be ignored, by the
dialog and the background, for 30 seconds whatever its company id. Each time such a reading is
heard and ignored, the 30 seconds SHALL start again. A different weight, or the same weight after
30 seconds without hearing it, SHALL be used as usual. Ignoring SHALL be logged
(`scale.ignore reason=same kg=`). The app SHALL remember with that weight when it was saved or
deleted and which of the two (a Kumoa that brings a weight back, in Historia or a notification,
remembers it as saved; a weight still waiting in the pending queue counts as saved), and the
dialog SHALL show the same repeat line as for a lingering broadcast while it ignores such
readings. Restarting the 30 seconds SHALL NOT change the remembered save or delete time.

#### Scenario: Re-broadcast after Poista
- **GIVEN** 99.05 kg from the remembered scale was saved automatically and deleted 5 seconds ago
- **WHEN** the scale sends 99.05 kg again with a new company id
- **THEN** nothing is saved and the 30 seconds start again

#### Scenario: Keeps repeating
- **GIVEN** the scale repeats 99.05 kg every 20 seconds with a new company id after a save
- **WHEN** two minutes pass
- **THEN** none of the repeats is saved

#### Scenario: Another weight
- **GIVEN** 99.05 kg was deleted 5 seconds ago
- **WHEN** the scale sends a stable 98.6 kg
- **THEN** it is used as usual

#### Scenario: Quiet long enough
- **GIVEN** 99.05 kg was last heard 40 seconds ago after a delete
- **WHEN** the user steps on the scale and it settles at 99.05 kg
- **THEN** it is used as usual

#### Scenario: Dialog says it is ignoring
- **GIVEN** 82.5 kg was saved at 7.12 and the scale repeats it
- **WHEN** the dialog hears only that repeat
- **THEN** it shows "Vaaka toistaa äskeistä punnitusta (82,5 kg, tallennettu klo 7.12)." instead of
  "Astu vaa'alle", and after 30 seconds the end view ends with "…, joten sitä ei tallennettu
  uudelleen."

## ADDED Requirements

### Requirement: Open on a recent automatic weight
When the weigh-in dialog opens, from any entry, and Health Connect holds a weight this app saved
automatically whose weighing time is at most 2 minutes ago and which is not marked "?", the dialog
SHALL show the latest such weight instead of scanning: the weight, "Tallennettu automaattisesti klo
7.12", the change from the previous weight, and Poista and Valmis, behaving as the saved result.

#### Scenario: Widget tapped after the automatic save
- **GIVEN** 82.5 kg was saved automatically at 7.12 without a mark
- **WHEN** the user taps the Punnitse widget at 7.13
- **THEN** the dialog shows 82,5 kg, "Tallennettu automaattisesti klo 7.12", the change, Poista and
  Valmis, and no scan starts

#### Scenario: Poista on the recent weight
- **GIVEN** the dialog shows the automatic 82.5 kg of 7.12
- **WHEN** the user taps Poista
- **THEN** the weight and its raw data are removed, its notification closes, listening restarts as
  after any delete, and the dialog closes

#### Scenario: Poista fails
- **GIVEN** the dialog shows the recent automatic weight
- **WHEN** the user taps Poista and the delete fails
- **THEN** the view stays with the weight and says the deletion failed, and the cause is logged

#### Scenario: Weigh again after Poista
- **GIVEN** the user deleted the automatic weight from the dialog
- **WHEN** the user opens the dialog again
- **THEN** it scans as usual

#### Scenario: Older than two minutes
- **GIVEN** 82.5 kg was saved automatically at 7.12
- **WHEN** the user opens the dialog at 7.15
- **THEN** it scans as usual

#### Scenario: Questioned weight
- **GIVEN** 61.3 kg was saved automatically and marked "?" at 7.12
- **WHEN** the user opens the dialog at 7.13
- **THEN** it scans as usual

### Requirement: Which weight counts as recent
A weight SHALL count as automatic only when its raw data says it was saved in the background
(`via=auto`) and it is one of this app's scale weights; a weight saved from the dialog never does.
The check SHALL take at most about 1.5 seconds; when it is slower, fails, or the app may not read
weights, the dialog SHALL scan as usual (without read access silently). A background weight still
waiting in the pending queue is not in Health Connect and SHALL NOT be shown; the repeat line
covers it.

#### Scenario: Saved from the dialog
- **GIVEN** 82.5 kg was saved from the dialog at 7.12
- **WHEN** the user opens the dialog again at 7.13
- **THEN** it scans as usual (and the repeat line shows while the scale still repeats it)

#### Scenario: Slow read
- **WHEN** Health Connect has not answered within about 1.5 seconds of the access check
- **THEN** the dialog scans as usual and the log has `weighin.recent failed`

#### Scenario: No read access
- **GIVEN** the app may write weights but not read them
- **WHEN** the dialog opens
- **THEN** it scans as usual without a message, and the log has `weighin.recent none reason=noread`

### Requirement: Weigh-in end in the log
Every weigh-in SHALL log how it ended, once, when the dialog closes for good or Punnitse uudelleen
starts a new scan (not when the dialog is only recreated, e.g. turned):
`weighin.end reason=saved|discarded|recent|recentdeleted|notheard|unsettled|repeat|cancelled|failed`
with `heard=`, `ignored=` (packets ignored as repeats) and `ms=` since the dialog opened.
Punnitse uudelleen SHALL log `weighin.open from=again`.

#### Scenario: Recent automatic weight
- **WHEN** the dialog showed the recent automatic weight and closed without Poista
- **THEN** the log has `weighin.end reason=recent`, and `reason=recentdeleted` after Poista

#### Scenario: Nothing heard
- **WHEN** the end view for "Vaakaa ei tunnistettu" is closed with Valmis
- **THEN** the log has one `weighin.end reason=notheard heard=false ignored=0 ms=…`

#### Scenario: Repeats only
- **WHEN** the dialog ignored four repeated packets and timed out
- **THEN** the log has `weighin.end reason=repeat heard=false ignored=4`

#### Scenario: Turned, not closed
- **WHEN** the phone is turned while the dialog scans
- **THEN** no `weighin.end` is logged

### Requirement: Cancelled and failed ends in the log
A dialog closed while checking access, asking for Bluetooth, scanning or showing a live weight
SHALL log `weighin.end reason=cancelled`; one closed on missing Health Connect access, a denied
permission, a scan error, a failed save or a failed delete SHALL log `weighin.end reason=failed`.

#### Scenario: Closed before a result
- **WHEN** the user closes the dialog while the weight is still changing
- **THEN** the log has `weighin.end reason=cancelled heard=true`

#### Scenario: Problem left open
- **WHEN** the dialog is closed on a failed save, a scan error or a missing permission
- **THEN** the log has `weighin.end reason=failed`
