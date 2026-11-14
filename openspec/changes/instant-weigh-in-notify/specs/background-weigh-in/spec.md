## MODIFIED Requirements

### Requirement: Save the automatic weight
While the feature is on, a weight heard from the remembered scale inside the window SHALL be
written to Health Connect as a weight automatically recorded by a scale, without the app open and
with the screen off. The background SHALL decide the weight at the first stable packet of a
weighing, without waiting for a quiet time; the saved time SHALL be the time of that packet. The
raw reading SHALL be kept as for a dialog weigh-in, with the extra field `via=auto`. A weight
outside 20-300 kg SHALL NOT be saved. The background SHALL NOT save while the weigh-in dialog is in
the foreground.

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

### Requirement: Timely background decision
The background scan SHALL report each packet from the scale at once, without batching, and the
weight SHALL be saved and notified within a few seconds of the phone hearing the scale's first
stable packet, while the user may still stand on the scale.

#### Scenario: Single stable packet
- **WHEN** the scale's stable 99.05 kg is heard once at 16.23.02 and nothing after it
- **THEN** the weight is saved and notified within a few seconds, at the time 16.23.02

### Requirement: Weigh-in notification
After every automatic weigh-in the app SHALL show a notification with the weight and, when a
reference exists, the change from it ("82,5 kg tallennettu", "−0,3 kg edellisestä"). A normal
weigh-in's notification SHALL have the actions OK and Poista punnitus. A "?" weight's notification
SHALL say it needs checking ("61,3 kg – tarkista", "Poikkeaa 20,7 kg edellisestä hyväksytystä") and
SHALL have Hyväksy and Poista punnitus. With no reference the text SHALL be only "82,5 kg
tallennettu". A weight saved late from the pending queue SHALL add its weighing time ("82,5 kg
tallennettu klo 7.12"). Tapping the notification SHALL open Historia. All the notifications SHALL
be in one group in one alerting channel family, and none SHALL disappear by itself. Without the
notification permission the weight SHALL still be saved.

#### Scenario: Normal weigh-in
- **WHEN** 82.5 kg is saved automatically without a mark
- **THEN** an alerting notification shows 82,5 kg, the change, OK and Poista punnitus, and stays
  until dismissed

#### Scenario: Questioned weigh-in
- **WHEN** 61.3 kg is saved automatically and marked "?"
- **THEN** an alerting notification asks to check it and offers Hyväksy and Poista punnitus, and
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
Poista punnitus SHALL delete the weight and its raw data without a confirmation; Hyväksy SHALL
accept the weight (with chained acceptance); OK SHALL only close the notification. Each SHALL work
without opening the app and SHALL close the notification. Swiping a "?" notification away SHALL
leave the mark.

#### Scenario: Delete from the notification
- **WHEN** the user taps Poista punnitus on the notification
- **THEN** the weight and its raw data are removed from the phone and the notification closes

#### Scenario: Accept from the notification
- **WHEN** the user taps Hyväksy on a "?" weight's notification
- **THEN** the mark is removed and the notification closes

#### Scenario: OK
- **WHEN** the user taps OK on a normal weigh-in's notification
- **THEN** the notification closes and the weight stays

#### Scenario: Swiped away
- **WHEN** the user swipes a "?" weight's notification away
- **THEN** the weight keeps its "?" mark

#### Scenario: Deletion fails
- **WHEN** deleting from the notification fails
- **THEN** the notification stays and says the deletion failed, and the cause is logged

#### Scenario: Handled elsewhere
- **WHEN** the user accepts or deletes the weight in Historia while its notification is shown
- **THEN** the notification closes
