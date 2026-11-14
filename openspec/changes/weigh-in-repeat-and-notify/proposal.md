# Proposal

## Why

The first real use of the automatic save (v1.20.56, 2026-11-14) showed three problems: the scale
keeps re-broadcasting the final weight with an incremented company id, so one weighing was saved
again right after Poista (three saves of the same kg in a minute); a stable reading heard alone
was decided only when the next packet came, almost two minutes late; and the normal notification
is silent, so it never reaches the user's watch. The user also wants listening to stop for the day
once the day's weight is in, to save battery.

## What Changes

- After a weigh-in is saved or deleted (dialog, background, notification, Historia), the same kg
  (±0.1 kg) from the same scale is ignored for 30 seconds whatever its company id; each new
  hearing of it restarts the 30 seconds. The company-id rule stays as is.
- A stable reading waiting in the background is decided by a short wake-up alarm a few seconds
  after it, not only by the next packet or the hourly check.
- All weigh-in notifications alert (sound/vibration per the phone's settings), so a watch
  forwards them. A new notification channel replaces the silent one (Android does not let an app
  raise an existing channel's importance).
- Once a weight not marked "?" is saved inside the current listening window, by the dialog, a
  manual entry or the background, listening stops for the rest of that window. Deleting that
  weight starts listening again if the window is still on. Asetukset and the widget show it as
  Valmis (green check).
- Widget symbols (1x1, symbol only): Kuuntelee green dot, Valmis green check, Lepää grey moon,
  Estetty amber triangle; none while the feature is off. Asetukset's status line starts with the
  same symbol.
- `appVersion` 1.21.

## Capabilities

### New Capabilities

### Modified Capabilities
- `background-weigh-in`: Listen in the background (stop after the window's weight), Status
  (new state), Save the automatic weight (timely decision), Weigh-in notification (all alert).
- `scale-weigh-in`: Ignore the scale's lingering broadcast (same kg from the same scale, 30 s).

## Impact

Scale (packet dedupe in `scale/WeighingKeys`, `listen/HeardPackets`), background listening
(`listen/ListenController`, `ListenAndroid` second alarm, `ListenStatusRule`, `WidgetStateRule`),
automatic save and notifications (`auto/AutoWeights`, `auto/AutoAndroid` channel), dialog and
manual entry save/delete paths (`ui/` view models), widget text, Asetukset status. No Health
Connect permission changes; no CI change.
