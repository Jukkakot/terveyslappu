# Proposal

## Why

After a weigh-in the dialog only says "Tallennettu". Seeing the change from the previous weigh-in
is the one number that makes the morning habit feel worth it, a short vibration confirms the save
when the phone is not in hand, and a widget or tile that shows the latest weight answers "did I
weigh today?" without opening anything.

## What Changes

- Saved weigh-in dialog shows the change from the previous weight in Health Connect (any app),
  e.g. "−0,3 kg edellisestä (ti 7.11.)".
- The phone gives a short confirmation vibration when the scale weight is saved.
- The home-screen widget shows the latest weight and its day ("Tänään", "Eilen", or the date)
  under the Punnitse action; the Quick Settings tile shows it as its subtitle.
- The latest weight is refreshed whenever the app saves, imports, deletes or reads weights.

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- `scale-weigh-in`: new requirements for the change from the previous weight, the confirmation
  vibration and the latest weight on the widget and tile.

## Impact

- Parts: weigh-in dialog, widget layout, Quick Settings tile, main screen view model (updates the
  cached latest weight), Health Connect (one read of the previous weight).
- No new permissions (haptic feedback through the view needs none) or libraries.
