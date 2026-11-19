## MODIFIED Requirements

### Requirement: Weigh-in notification
After every automatic weigh-in the app SHALL show a notification with the weight and, when a
reference exists, the change from it ("82,5 kg tallennettu", "−0,3 kg edellisestä"). A normal
weigh-in's notification SHALL have the one action Poista paino. A "?" weight's notification
SHALL say it needs checking ("61,3 kg – tarkista", "Poikkeaa 20,7 kg edellisestä hyväksytystä") and
SHALL have Hyväksy and Poista paino. With no reference the text SHALL be only "82,5 kg
tallennettu". A weight saved late from the pending queue SHALL add its weighing time ("82,5 kg
tallennettu klo 7.12"). The change and the deviation SHALL each be the difference of the two
weights as shown with one decimal (see weight-chart "Changes from shown weights"). Tapping the
notification SHALL open Historia. All the notifications SHALL
be in one group in one alerting channel family, and none SHALL disappear by itself except the
"Paino poistettu" state of "Notification actions". Every notification SHALL be dismissable with a
swipe. Without the notification permission the weight SHALL still be saved.

#### Scenario: Normal weigh-in
- **WHEN** 82.5 kg is saved automatically without a mark
- **THEN** an alerting notification shows 82,5 kg, the change and Poista paino (no OK), and stays
  until swiped away

#### Scenario: Rounded as shown
- **GIVEN** the reference is 82.34 kg and 81.76 kg is saved
- **THEN** the notification reads "81,8 kg tallennettu" and "−0,5 kg edellisestä"

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
