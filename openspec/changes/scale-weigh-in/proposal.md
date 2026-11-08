# Proposal

## Why

The app exists to make the morning weigh-in effortless, and the proof of concept showed that
weights written to Health Connect reach Samsung Health. The next step is the actual point of the
app: step on the Prego P814B scale and the weight is saved, started with one tap and without
going through the main screen.

## What Changes

- The app reads the scale's Bluetooth broadcast (advertises as `Yoda1`, weight in manufacturer
  data, no pairing or connection) and decodes weight, stable flag, decimals, unit and impedance.
- A small weigh-in dialog: scans, shows the live weight while the scale settles, saves the stable
  weight to Health Connect as a scale measurement, shows it for a moment and closes itself.
- One-tap starts for the dialog: a Quick Settings tile, a home-screen widget and a launcher
  shortcut (long-press the app icon). The main screen also gets a "Punnitse" button.
- Bluetooth handling: asks for the nearby-devices permission, offers to turn Bluetooth on, gives
  up with a clear message when no scale is heard within a minute.
- Every received scale packet is logged raw with its timing, and the stable reading and the save
  log their durations (NFR-7, NFR-8). Logged packets can be replayed as JVM tests.

## Capabilities

### New Capabilities
- `scale-weigh-in`: reading the scale broadcast, deciding the stable weight, saving it, the
  weigh-in dialog and its one-tap starts (tile, widget, shortcut, main-screen button), and the raw
  packet log.

### Modified Capabilities
- (none) The manual entry, list and log sharing work as before.

## Impact

- Parts: scale (Bluetooth LE scan), Health Connect (write with scale device metadata), widget,
  Quick Settings tile, launcher shortcut, main screen (one button), log.
- New permission: nearby devices (`BLUETOOTH_SCAN`, declared as not used for location).
- No new libraries: the platform Bluetooth LE scanner, `TileService` and `AppWidgetProvider`.
- Protocol reference: openScale's OKOK handler (GPL); decoding is rewritten here from the
  documented byte layout, no code copied.
