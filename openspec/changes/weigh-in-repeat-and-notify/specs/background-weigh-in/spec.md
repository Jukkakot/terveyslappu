## ADDED Requirements

### Requirement: Stop after the window's weight
Once a weight not marked "?" whose time is inside the current listening window has been saved by
this app (weigh-in dialog, manual entry or the background), listening SHALL stop for the rest of
that window and SHALL start again at the next window's start. When that weight is deleted
(dialog Poista, notification Poista, Historia) or later marked "?", and no other such weight of
this app remains in the window, listening SHALL start again at once if the window is still on.
Accepting a "?" weight of the window SHALL count as saving it. Weights written by other apps SHALL
NOT count. While stopped, the status SHALL say "Tämän päivän paino tallennettu, kuuntelee taas klo
HH.MM" (the next window start) and the widget SHALL show "Lepää". Stopping SHALL be logged
(`listen.stop reason=done`).

#### Scenario: Morning weight saved
- **GIVEN** a 06:00-10:00 window
- **WHEN** 82.5 kg is saved automatically at 07.12 without a mark
- **THEN** the scan stops, the status says "Tämän päivän paino tallennettu, kuuntelee taas klo
  6.00", and the widget shows "Lepää"

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

## MODIFIED Requirements

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
