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
3. **history-import** (done 2026-11-08 in autopilot, archived; menu ⋮ → Tuo painot; the real
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

- ~~**Before scale-reading-archive: decode the unknown scale bytes**~~: done 2026-11-09 from
  openScale's decoders (bytes 4-5 product id, 7-12 the scale's address; see product.md). Still to
  confirm from the next shared weigh-in log (`addr=` against bytes 7-12). Log 2026-11-09:
  bytes 4-12 are zeros on this scale, so the address is only in `addr=`.
- ~~**scale-reading-archive**~~: done 2026-11-09 (raw packet fields per weigh-in in app storage and
  in the export/backup `raw` column).
- ~~**okok-xls-import**~~: done 2026-11-09 (Tuo painot reads the OKOK `.xls`); the real file
  still to try on the phone.
- ~~**weigh-in-discard**~~: done 2026-11-09 (Poista on the result, Punnitse uudelleen dropped,
  the scale's lingering broadcast ignored). From the 2026-11-09 log: `name=Yoda1` is in every
  packet; the company id changes per weighing, so a background scan filters by name or address.
- **Phone owner's details** (user 2026-11-09: next change after weigh-in-discard): one user per phone, no
  profiles; a setting for gender, age and height so derived values (BMI ...) can later be computed
  from the raw data.
- ~~Own weight view in the app~~: done 2026-11-09 (weight-chart: chart, periods, stats; own weights only).
- ~~**main-screen-refresh**~~: done 2026-11-09 (simple home per mockup B, Historia and Asetukset
  screens, theme choice; import, export, backup and log moved from the ⋮ menu to Asetukset).
- ~~**chart-gaps-and-years**~~: done 2026-11-09 (line breaks at gaps over 30 days, "9/2022" labels
  over a year, year in old weights' point detail and list rows).
- ~~**history-list-delta**~~: done 2026-11-09 (change from the previous weight on every Historia
  row, arrows on every change number, gain/loss in two neutral palette tones).
- **Goal weight and forecast** on the chart (user 2026-11-09: later).
- Steps from Garmin through Health Connect next to weight (dropped for now, see product.md).
- ~~Readable CSV export~~: done 2026-11-08 (weight-export, menu ⋮ → Vie painot; the import reads
  it back, so it also works as a manual move to a new phone until phase 4 is decided).
- ~~Body composition from impedance~~: dropped (user 2026-11-09, Q4: not needed).
