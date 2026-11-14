## Why

The app has grown several features (widget, tile, shortcut, Historia, owner details, import/export,
backup, log sharing) that are easy to miss. A light in-app guide presents them.

## What Changes

- New screen "Ohje", opened from a new row "Ohje" in Asetukset's Tuki section; back returns to
  Asetukset.
- Content: one intro line and one short Finnish section per feature. Automatic weigh-in is not
  described (still a trial).

## Capabilities

### New Capabilities
- `help-page`: the Ohje screen and its entry in Asetukset.

### Modified Capabilities
(none; the new Tuki row is described in the help-page spec)

## Impact

- `ui/`: new `HelpScreen`, `Screen.HELP`, one row in `SettingsScreen`, string resources.
