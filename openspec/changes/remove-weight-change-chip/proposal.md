# Proposal

## Why

The front page weight card shows two changes: a chip with the change from the previous weigh-in
(↑ +0,2 kg) and the period's change under the chart (↓ −0,1 kg). Neither says what it compares, and
they often point in opposite directions, which confuses (user 2026-11-17). Day-to-day changes are
mostly noise; the user wants only the period's change on the front page.

## What Changes

- Remove the "change from the previous weight" chip from the weight card at every size (small,
  wide, big).
- The period row (`97,3 → 97,2 kg` and the period change with its arrow) stays as it is.
- The small card's chart gets the room the chip took under the number (the chart already takes the
  free height).
- The change from the previous weight is still shown in the weigh-in dialog and in Historia's list;
  nothing else changes.

## Capabilities

### New Capabilities

### Modified Capabilities
- `home-screen`: Simple home screen and Card sizes drop the change chip.

## Impact

- `ui/HomeScreen.kt` (`LatestValues`, `ChangeChip`), string `home_change`.
- `chart/HomeSummary.kt` field `change` (and its tests) if no other user remains.
