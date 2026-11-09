# Proposal

## Why

Moving to a new phone (roadmap phase 4, Q3): the weights move with the backup file and Tuo
tiedot, but everything else the app keeps on the phone is lost: owner details, theme, chart
periods and the raw-data archive behind each scale weight. Android already moves app data to a
new phone (Samsung Smart Switch, Google's cloud backup); the app currently opts out of both
(`allowBackup=false`, every domain excluded). Letting Android carry the app's own data is a
small change that complements the backup file instead of replacing it.

## What Changes

- The app takes part in Android's app-data backup and device-to-device transfer.
- Carried: owner details, appearance (theme), chart and home periods, the raw-data archive.
- Not carried: the app log, the last weigh-in's lingering-broadcast state, and the backup file
  setting (the file access permission does not move with it, so the user picks the file again
  on the new phone).
- The cloud backup is only made when Android can encrypt it end to end (screen lock set);
  device-to-device transfer always works.
- Weights are not app data: they stay in Health Connect, the backup file is still how they move.

## Capabilities

### New Capabilities
- `app-data-transfer`: which app data moves to a new phone through Android's own backup and
  transfer, and what stays behind.

### Modified Capabilities
<!-- none -->

## Impact

Touches the build only: the app manifest and its data-extraction rules. No screen, Health
Connect, scale or widget behaviour changes. Health Connect permissions are not app data; on the
new phone the app asks for them again as on any first start.
