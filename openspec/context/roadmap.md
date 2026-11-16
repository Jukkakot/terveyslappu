# Roadmap

Phases in order; one OpenSpec change each unless noted.

1. **poc-manual-weight** (done 2026-11-08; Q1 closed 2026-11-09: Samsung Health follows Health Connect, deletions too, see product.md): Android app with manual weight entry written to Health
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

- ~~**food-sheet-compact**~~ done 2026-11-16 (v1.54, user 2026-11-16): the add panel folds to a handle, food rows drop the logging time and keep the name on one line.
- **Later idea: rows with name and kcal only** (user 2026-11-16): a food row shows only name and kcal with a details button; not planned yet.
- ~~**Bug: Lisää annos widget opens the running app**~~ not reproducing 2026-11-16 (user): removing the widget from the home screen and adding it again fixed it (likely a stale widget PendingIntent from an older build). Reopen with the app log if it returns.
- ~~**Treats in their own colour**~~ done 2026-11-16 (treat-text-colour, v1.49, user 2026-11-16): the treat share ("herkkuja 230 kcal") in a colour of its own in text, matching the treat tone of the Energia bars.
- ~~**Weight bars mislead in own charts**~~ done 2026-11-16 (weight-line-only, v1.51, user 2026-11-16, unambiguous-data): own charts show Paino as a line only; the Pylväät choice is gone for Paino and old charts read as Viiva.
- ~~**Shared y-axis for the same unit**~~ done 2026-11-16 (shared-unit-axis, v1.50, user 2026-11-16): in own charts, series with the same unit (ruoka and kulutus, kcal) share one scale and one value axis (neutral tone) in both layouts, also on the front page card; different units keep their own. Per-series axis choice dropped (user 2026-11-16): the shared-unit rule covers it.
- ~~**show-units-everywhere**~~ done 2026-11-16 (v1.47, user 2026-11-16): every number carries its unit (kcal, kg, askelta, kcal/pv), treats read "herkkuja N kcal", the add panel gets its own tone and the add confirmation line goes.
- ~~**fit-weight-scale**~~ done 2026-11-16 (v1.48, user 2026-11-16): weight and BMI charts fit the data (0.2 kg room, at least 1 kg span, half-kilo rounding), the same rule on the home card, Historia and own charts.
- ~~**food-log**~~ done 2026-11-14 (v1.24): Lisää annos with learnt products and base products, Ruoka day screen, nutrition records in Health Connect, Energia tab next to Paino/BMI, Ruokakirjaus in Asetukset, foods and products in the data file.
- ~~**home-compact**~~ done 2026-11-14 (v1.27, user 2026-11-14): the front page fits one screen: weight card with Punnitse inside, food card full width (number → Ruoka, bars → Energia), no BMI or automatic weigh-in card; Ruoka gets Viikko; the clipboard food offer also on the front page; import wording without the AI.
- ~~**home-overview**~~ done 2026-11-14 (v1.26, user 2026-11-14): the front page as one card per feature (Paino; Ruoka with its own bars and period; Automaattinen punnitus while on), the latest-weights rows go. Parity rule: every feature gets the same parts on the front page and in its details unless a part means nothing for it.
- ~~**food-ai-import**~~ done 2026-11-14 (v1.25, user 2026-11-14): share or paste the AI chat's day estimate as JSON into the food log (today by default, calendar for another day), preview with fine-tuning, append only.
- ~~**Reorder the front page cards**~~ done 2026-11-15 (home-card-layout, v1.31, user 2026-11-14/15): a long-press on a card starts edit mode; cards are dragged into the user's order, each small, wide or big, and can be hidden and added back; Valmis saves, Peruuta and back discard.
- ~~**food widget**~~ done 2026-11-14 (v1.29, user 2026-11-14): a 1x1 Lisää annos widget and launcher shortcut open the add sheet over the home screen (add-only, no food shown); Muuta's editor returns to the open add field, also in the app; import from the sheet opens the app on the preview.
- ~~**steps**~~ done 2026-11-14 (v1.28, user 2026-11-14): read-only steps from Health Connect's merged daily totals; the front page row holds the food card and the steps card side by side; Historia's fourth tab Askeleet (bars, 7-day line, weight under them, stats, day list); not in the data file or backup; no goal.
- ~~**steps source and chart axes**~~ done 2026-11-15 (steps-source-and-axes, v1.30): steps from one app (Automaattinen = most steps in 30 days, an app, or Kaikki), Asetukset → Askeleet; Historia's charts get three labelled y values with units, 3–5 dates and a legend.
- ~~**food sheet clip offer and suggestion rows**~~ done 2026-11-15 (food-sheet-clip-and-suggestions, v1.33, user 2026-11-15): the clipboard food JSON is offered on every open of the front page, Ruoka and the Lisää annos sheet until imported or skipped, as a "Tuo leikepöydältä: N annosta · kcal" button; suggestions are rows with amount and kcal, each with an edit button to set the amount before adding.
- ~~**food sheet layout**~~ done 2026-11-15 (food-sheet-layout, v1.34, user 2026-11-15): the Lisää annos sheet gets a header with the day and ⋮ (Kopioi tuontiohje), a "Leikepöydällä" card with the copied items and one-tap Lisää kaikki (also on Ruoka and the front page), a search field, headed lists, an Enter row, a red delete on own products; Kumoa bars last about 4 s and swipe away.
- **consistency pass** (user 2026-11-15, after food-sheet-layout): rows, buttons, delete and Kumoa the same on every screen (e.g. Asetukset → Ruokakirjaus product removal gets a Kumoa; the sheet's ⋮ menu may gain entries). **Done (`ui-consistency`, v1.36):** one delete rule (at once + Kumoa, red controls, no long-press delete, no chart confirm), filled main buttons, 56 dp rows at a 16 dp edge.
- ~~**period-average-line**~~ done 2026-11-15 (user 2026-11-15): a flat line at the period average replaces the rolling 7-day line on Energia and Askeleet; own charts get a Keskiarvo switch. Later idea: make the weight chart's ups and downs more visible (scale span or height) while keeping real daily values.
- ~~**garmin-steps-history**~~ done 2026-11-15 (v1.41, user 2026-11-15): Garmin's export ZIP in Tuo tiedot fills Askeleet's history before Health Connect (Garmin Connect or Kaikki as the source); Asetukset -> Askeleet shows and deletes it.
- **combined chart** (user 2026-11-14, later, not in food-log or steps): weight, kcal and steps in one chart, the user picks which are shown; avoid a two-axis plot (hard to read), e.g. stacked panels on one time axis. Grew into user-made charts, bars or line per data, stacked or overlaid, in Historia and as front page cards (user 2026-11-15). **Done (`custom-charts`, v1.32).** Fixed Historia tabs show one data kind only; combinations live in own charts (user 2026-11-15, `fixed-tabs-single-data`, v1.35).
- ~~**charts on a chart library**~~ done 2026-11-16 (charts-on-mpandroidchart, v1.46, user 2026-11-16): every Historia chart on MPAndroidChart 4 with library defaults (marker card, tap/drag, highlight, zoom and pan, x label placement).
- ~~**front page cards on the chart library**~~ done 2026-11-16 (home-cards-on-chart-library, v1.52): the weight, food, steps, Kulutus and own chart cards draw with the same library chart and chart definitions as Historia in a card mode (no marker, zoom or labels; the card's tap, long-press and drag unchanged); the Canvas drawing is gone and the cards look as before.

- ~~**Priority 1: plain weigh-in button widget + result with redo**~~: done 2026-11-09
  (weigh-in-button-redo, autopilot). Original note (user 2026-11-09): The current
  widget shows the previous result right away, which the user does not want there. Either the
  widget stops showing it or a second widget variant is added: a 1x1 button that only starts the
  weigh-in, with no previous result. After a weigh-in the result is shown (open: in the dialog,
  or for a while in the widget itself) and from there the user can delete it, i.e. start the
  weigh-in again. Same change family as the redo in the next item; do this first.

- ~~**Weigh-in without any tap**~~ done 2026-11-14 (phases 1-2 and plausibility, v1.19-v1.20). (reopened by the user 2026-11-14; dropped 2026-11-09 on an
  unmeasured battery guess). A hardware-filtered background scan (PendingIntent, name/address
  filter, low power) in a time window. Each phase only if the previous one looked good:
  - Phase 0 done 2026-11-14 (phone in the bedroom, one wall, 5-10 m): only 2 packets in 18 s,
    rssi -98 (at the receiver's edge; next to the scale -50...-68, 17-58 packets per weigh-in).
    The scale address stayed the same over all weigh-ins.
  - Phase 1, listen-only (small change): background scan with the real settings, screen off,
    time window from Asetukset, saves nothing, logs what it heard and the rssi. A few mornings show
    range, whether Samsung stops the scan, and battery use. Implemented 2026-11-14
    (weigh-in-listen-trial, v1.19); waiting for the trial's mornings, including one morning with
    the phone next to the scale.
  - Phase 2 implemented 2026-11-14 (background-weigh-in-save, v1.20): the scan receiver saves
    the weight itself, "?" marks, notifications with Poista/Hyväksy, widget state. The feature:
    time window in Asetukset (no hard-coded values); listens to the scale
    of the last manual weigh-in; widget shows on / resting (outside the window) / blocked (battery
    optimisation, Bluetooth, permission); a notification after every automatic weigh-in; Health
    Connect recording method "automatically recorded", the "?" mark and its confirmation in our own
    storage and the export. Check Health Connect's background write limits first.
  - Plausibility (user 2026-11-14, simple on purpose): every automatic weight is saved. One
    further from the latest confirmed weight (not "?") than a limit in Asetukset (default 5 kg,
    no time factor) is marked "?"; the user confirms or deletes it in Historia or from the
    notification, and a confirmed one becomes the new reference (re-calibrates after a long
    break). A notification after every automatic weigh-in, with Poista.
- ~~**Help page**~~ done 2026-11-14 (user 2026-11-14): a light in-app guide that presents the features; its own
  small change.

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
- ~~**chart-gap-lines**~~: done 2026-11-10 (Historia's line no longer breaks at gaps over 30
  days: one straight line through every weight).
- ~~Trend line~~ and ~~goal weight and forecast~~: dropped (user 2026-11-09: not wanted).
- ~~**derived-values**~~: done 2026-11-09 (BMI with class, normal-weight range and basal metabolic
  rate on the home row and as Historia tabs Paino / BMI / Perusaineenvaihdunta; plot flush with the
  y-axis).
- ~~**history-list-groups**~~: done 2026-11-09 (Historia list grouped year → month → week → day,
  single-child levels skipped, lists of up to 14 weights flat, newest path open, gap rows over 30
  days; no change across a gap).
- ~~Steps from Garmin through Health Connect next to weight~~: done 2026-11-14 (steps, v1.28); the combined chart stays in the backlog.
- ~~Readable CSV export~~: done 2026-11-08 (weight-export, menu ⋮ → Vie painot; the import reads
  it back, so it also works as a manual move to a new phone until phase 4 is decided).
- ~~Body composition from impedance~~: dropped (user 2026-11-09, Q4: not needed).
