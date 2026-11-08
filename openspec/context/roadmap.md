# Roadmap

Phases in order; one OpenSpec change each unless noted.

1. **poc-manual-weight** (done 2026-11-08; Samsung Health check Q1 still open): Android app with manual weight entry written to Health
   Connect, recent weights read back, shareable log; CI builds a signed APK for Obtainium. Used to
   test what Samsung Health does with the weight (display, delay, CSV export, Samsung Cloud).
2. **scale-weigh-in**: read the `Yoda1` broadcast and save the stable weight; then one-tap
   weigh-in from a widget or Quick Settings tile without opening the app. Log every received
   packet raw with the scan's timings (NFR-7, NFR-8), and turn shared failing logs into replay
   tests.
3. **history-import**: import the OKOK export into Health Connect with the original timestamps.
4. **Moving to a new phone**: decide from phase 1 findings (Samsung Cloud, Health Connect export
   or an app-managed cloud file with visible sync status).

## Backlog

- Own weight view in the app (only if Samsung Health turns out awkward).
- Steps from Garmin through Health Connect next to weight (dropped for now, see product.md).
- Readable CSV export of the app's weights.
- Body composition from impedance (the scale sends it; needs height, age, sex).
