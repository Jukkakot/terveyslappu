# Roadmap

Phases in order; one OpenSpec change each unless noted.

1. **poc-manual-weight** (done 2026-11-08; Q1 answered 2026-11-09: Samsung Health keeps its own copy, see product.md): Android app with manual weight entry written to Health
   Connect, recent weights read back, shareable log; CI builds a signed APK for Obtainium. Used to
   test what Samsung Health does with the weight (display, delay, CSV export, Samsung Cloud).
2. **scale-weigh-in** (done 2026-11-08 in autopilot, archived; first real weigh-in log still to
   come): read the `Yoda1` broadcast and save the stable weight; then one-tap
   weigh-in from a widget or Quick Settings tile without opening the app. Log every received
   packet raw with the scan's timings (NFR-7, NFR-8), and turn shared failing logs into replay
   tests.
3. **history-import** (done 2026-11-08 in autopilot, archived; the history is imported, closed): import the OKOK export into Health Connect with the original timestamps.
4. **Moving to a new phone**: interim answer done and archived 2026-11-08 (weight-backup, autopilot): an
   opt-in backup file in the user's own cloud, rewritten after every change and shown on the
   main screen; the history import restores it. Decided 2026-11-09 (Q3): keep it, and let
   Android's backup and Smart Switch carry the app's own data (app-data-transfer). Original note: decide from phase 1 findings (Samsung Cloud, Health Connect export
   or an app-managed cloud file with visible sync status).

## Backlog

- **Idea: deletions vs Samsung Health's copy** (2026-11-09, Q1): a weight deleted here stays in
  Samsung Health. Option: the weigh-in writes to Health Connect only when its dialog closes, so
  Poista never reaches Samsung Health. Waiting for the user's call.
- ~~**Priority 1: plain weigh-in button widget + result with redo**~~: done 2026-11-09
  (weigh-in-button-redo, autopilot). Original note (user 2026-11-09): The current
  widget shows the previous result right away, which the user does not want there. Either the
  widget stops showing it or a second widget variant is added: a 1x1 button that only starts the
  weigh-in, with no previous result. After a weigh-in the result is shown (open: in the dialog,
  or for a while in the widget itself) and from there the user can delete it, i.e. start the
  weigh-in again. Same change family as the redo in the next item; do this first.

- ~~**Weigh-in without any tap**~~: dropped (user 2026-11-09: a background scan costs battery;
  not worth it).

- ~~**Before scale-reading-archive: decode the unknown scale bytes**~~: done 2026-11-09 from
  openScale's decoders (bytes 4-5 product id, 7-12 the scale's address; see product.md). Log 2026-11-09:
  bytes 4-12 are zeros on this scale, so the address is only in `addr=`.
- ~~**scale-reading-archive**~~: done 2026-11-09 (raw packet fields per weigh-in in app storage and
  in the export/backup `raw` column).
- ~~**okok-xls-import**~~: done 2026-11-09 (Tuo painot reads the OKOK `.xls`); the history is imported, nothing
  left to try.
- ~~**weigh-in-discard**~~: done 2026-11-09 (Poista on the result, Punnitse uudelleen dropped,
  the scale's lingering broadcast ignored). From the 2026-11-09 log: `name=Yoda1` is in every
  packet; the company id changes per weighing, so a background scan filters by name or address.
- ~~**owner-profile**~~: done 2026-11-09 (Asetukset → Omat tiedot: birth year, height, gender; height
  also to Health Connect; carried in the export/backup `#profile;` line and restored by Tuo painot).
  BMI and other derived values: done in derived-values.
- ~~Own weight view in the app~~: done 2026-11-09 (weight-chart: chart, periods, stats; own weights only).
- ~~**main-screen-refresh**~~: done 2026-11-09 (simple home per mockup B, Historia and Asetukset
  screens, theme choice; import, export, backup and log moved from the ⋮ menu to Asetukset).
- ~~**chart-gaps-and-years**~~: done 2026-11-09 (line breaks at gaps over 30 days, "9/2022" labels
  over a year, year in old weights' point detail and list rows).
- ~~**history-list-delta**~~: done 2026-11-09 (change from the previous weight on every Historia
  row, arrows on every change number, gain/loss in two neutral palette tones).
- ~~**generic-data-transfer**~~: done 2026-11-09 (Vie tiedot / Tuo tiedot in CSV or JSON, one
  part per data kind, owner details no longer carried, backup on the first open of each day).
- ~~**chart-scale**~~: done 2026-11-09 (shared 4 kg minimum scale, home line over the whole 30
  days with dates and start/end weights, kg on axes and stats).
- ~~**home-period**~~: done 2026-11-09 (home card line 1 vk by default, 1 kk / 3 kk from a
  label under it, remembered).
- ~~**chart-dots**~~: done 2026-11-09 (weights as dots on both charts when they fit).
- ~~Trend line~~ and ~~goal weight and forecast~~: dropped (user 2026-11-09: not wanted).
- ~~**derived-values**~~: done 2026-11-09 (BMI with class, normal-weight range and basal metabolic
  rate on the home row and as Historia tabs Paino / BMI / Perusaineenvaihdunta; plot flush with the
  y-axis).
- ~~**history-list-groups**~~: done 2026-11-09 (Historia list grouped year → month → week → day,
  single-child levels skipped, lists of up to 14 weights flat, newest path open, gap rows over 30
  days; no change across a gap).
- Steps from Garmin through Health Connect next to weight (dropped for now, see product.md).
- ~~Readable CSV export~~: done 2026-11-08 (weight-export, menu ⋮ → Vie painot; the import reads
  it back, so it also works as a manual move to a new phone until phase 4 is decided).
- ~~Body composition from impedance~~: dropped (user 2026-11-09, Q4: not needed).
