# Roadmap

Phases in order; one OpenSpec change each unless noted.

1. **poc-manual-weight** (done 2026-11-08; Samsung Health check Q1 still open): Android app with manual weight entry written to Health
   Connect, recent weights read back, shareable log; CI builds a signed APK for Obtainium. Used to
   test what Samsung Health does with the weight (display, delay, CSV export, Samsung Cloud).
2. **scale-weigh-in** (done 2026-11-08 in autopilot, archived; first real weigh-in log still to
   come): read the `Yoda1` broadcast and save the stable weight; then one-tap
   weigh-in from a widget or Quick Settings tile without opening the app. Log every received
   packet raw with the scan's timings (NFR-7, NFR-8), and turn shared failing logs into replay
   tests.
3. **history-import** (done 2026-11-08 in autopilot, archived; menu ⋮ → Tuo historia; the real
   OKOK file still to try): import the OKOK export into Health Connect with the original timestamps.
4. **Moving to a new phone**: interim answer done and archived 2026-11-08 (weight-backup, autopilot): an
   opt-in backup file in the user's own cloud, rewritten after every change and shown on the
   main screen; the history import restores it. Still open: whether to keep this or rely on
   Samsung Cloud / Health Connect export. Original note: decide from phase 1 findings (Samsung Cloud, Health Connect export
   or an app-managed cloud file with visible sync status).

## Backlog

- ~~**Priority 1: plain weigh-in button widget + result with redo**~~: done 2026-11-09
  (weigh-in-button-redo, autopilot). Original note (user 2026-11-09): The current
  widget shows the previous result right away, which the user does not want there. Either the
  widget stops showing it or a second widget variant is added: a 1x1 button that only starts the
  weigh-in, with no previous result. After a weigh-in the result is shown (open: in the dialog,
  or for a while in the widget itself) and from there the user can delete it, i.e. start the
  weigh-in again. Same change family as the redo in the next item; do this first.

- **Next: weigh-in without any tap + redo** (user 2026-11-09: wanted). A background Bluetooth
  scan with a filter saves the weight when the user just steps on the scale (the tap starts
  stay). Plus an easy way to start a weigh-in again and delete the previous result when needed
  (the dialog's "Punnitse uudelleen" from weigh-in-button-redo can be reused, e.g. from a
  notification).
  Needs from the first weigh-in log: whether `name=Yoda1` is in the advertisement itself and
  whether the company id (`key=`) is stable, since background scans must be filtered.

- **Before scale-reading-archive: decode the unknown scale bytes** (user 2026-11-09). Packet bytes
  4-5 and 7-12 are not understood yet; check openScale's `OkOkHandler` and similar decoders and the
  shared packet logs, so the archive stores named fields where known (the raw hex is kept anyway).
- **Phone owner's details** (user 2026-11-09, later, not first priority): one user per phone, no
  profiles; a setting for gender, age and height so derived values (BMI ...) can later be computed
  from the raw data.
- Own weight view in the app: planned as weight-chart (2026-11-09).
- Steps from Garmin through Health Connect next to weight (dropped for now, see product.md).
- ~~Readable CSV export~~: done 2026-11-08 (weight-export, menu ⋮ → Vie painot; the import reads
  it back, so it also works as a manual move to a new phone until phase 4 is decided).
- ~~Body composition from impedance~~: dropped (user 2026-11-09, Q4: not needed).
