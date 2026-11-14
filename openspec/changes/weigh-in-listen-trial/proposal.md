## Why

Weighing without any tap needs the phone to hear the scale while it lies elsewhere (bedroom,
one wall, 5-10 m). The 2026-11-14 test heard only 2 packets in 18 s at rssi -98, the edge of
reception, with the screen on and a full-power scan. Before building the feature, a listen-only
trial measures in real mornings whether a low-power background scan hears the scale often enough,
whether Samsung keeps the scan alive, and what it costs in battery.

## What Changes

- New opt-in setting in Asetukset: "Automaattinen punnitus (kokeilu)" with a switch and a
  time window (start and end time chosen by the user).
- While on and inside the window, the phone listens for the user's scale in the background with
  a low-power, hardware-filtered Bluetooth scan that runs with the screen off and the app closed.
- The trial saves no weight. It logs what it heard (raw packets per NFR-7, signal strength) and
  a summary at the end of each window, and Asetukset shows when the scale was last heard.
- Asetukset shows the trial's status: listening, resting until the window starts, or blocked and
  why (Bluetooth off, permission missing, battery optimisation on), with a way to fix it.
- The scale to listen to is the one of the last weigh-in saved from the dialog; its name and
  address are remembered from then on.

## Capabilities

### New Capabilities
- `background-weigh-in`: listening for the user's scale without a tap: setting, time window,
  status, which scale, what is heard and logged. Phase 2 (saving the weight) extends it.

### Modified Capabilities
- `scale-weigh-in`: a saved weigh-in also remembers the scale's name and address.

## Impact

- Parts: Asetukset screen, scale (a new background scan beside the dialog's scan), log,
  Android manifest (receivers for scan results, alarms and boot). No Health Connect writes, no
  widget change (the widget's status comes in phase 2), no export change.
- Battery: one low-power scan inside the window only; measured by the user in Samsung's battery
  statistics during the trial.
