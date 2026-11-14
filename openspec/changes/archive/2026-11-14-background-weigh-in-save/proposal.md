## Why

Phase 1 (`weigh-in-listen-trial`) only listens. Phase 2 makes the weigh-in happen without any
tap: the phone hears the user's scale in the morning window and saves the weight by itself.
Because nobody looks at the scale's reading at that moment, a weight that is far from the last
accepted one (another person on the scale, a bag in hand) is saved but marked "?" until the user
confirms it.

**Waits for the phase 1 trial results.** This change is not started until the trial's mornings
show that the background scan hears the scale from where the phone lies (range), that Samsung keeps
the scan alive through the window, and that the battery cost is acceptable. The trial also picks
the default listening mode (Säästö or Tasapaino). If the range is not good enough, this change is
dropped or rescoped. It also needs phase 1 archived first, because it modifies phase 1's
`background-weigh-in` spec.

## What Changes

- The Asetukset section loses "(kokeilu)" and becomes "Automaattinen punnitus". The window, the
  scale identity, the mode and the listening rules stay as phase 1 built them.
- Every stable weight heard from the user's scale inside the window is written to Health Connect
  as "automatically recorded" from a scale, with its raw reading, also with the app closed and the
  screen off.
- Plausibility: a new setting "Hyväksyntäraja" (default 5 kg). A weight further than the limit
  from the reference (the latest accepted weight written by this app) is marked "?". With no
  earlier weight everything is accepted, and so is any weight while the reference is unknown (right
  after the update, before the app was opened): it is saved and marked "?". No time factor. The
  rule only marks automatic weights; import restores marks as they are in the file.
- Acceptance: a "?" weight is accepted or deleted by the user in Historia (tap the row) or from the
  notification. Chained acceptance: accepting a "?" weight also accepts the later "?" weights (time
  order) one by one while each is within the limit of the previous accepted weight in the chain;
  the chain stops at the first one outside the limit.
- Notifications: one after every automatic weigh-in (weight, change, Poista; a "?" weight also
  Hyväksy), all in one group, none disappears by itself; "?" ones may make a sound, normal ones are
  silent. Without a reference the text is only "82,5 kg tallennettu"; a late save from the pending
  queue adds the weighing time ("klo 7.12"). The app asks for the notification permission when the
  feature is turned on; a denial still saves, with a warning line in Asetukset.
- The widget follows the Asetukset status: "Päällä", "Lepää" (always outside the window) or
  "Estetty" (a blocked reason, or Tauolla / Pysäytetty); warnings (battery restricted,
  notifications off) never mean Estetty. State changes within about 15 minutes of the window start
  and end. Without the feature on it looks as today.
- "?" weights are left out of every chart, statistic, the home card and the change values until
  accepted; Historia lists them with a "?" mark whatever the period filter, and the home screen
  shows one notice "Punnitus odottaa tarkistusta" (opens Historia at the newest "?" weight) while
  one waits.
- A weight whose write fails waits in a pending queue ("N punnitusta odottaa tallennusta" in
  Asetukset), retried at the start of the first foreground read.
- Background work is time-boxed (give up at about 8 s, fall back to the queue) and logged per step.
- The backup of a change made while the app was closed runs the next time the app opens (reading
  all weights for the backup is a Health Connect read, which the background may not do).
- The "?" mark lives in the app's own storage, keyed by the app's own record (Health Connect has no
  field for it), and travels in the data file: a new `check` field on weights in CSV (last column,
  `...;raw;check`) and JSON, read back on import.
- The dialog weigh-in and the background save never both save the same weighing (the weighing key
  is claimed atomically; the background does not save while the dialog is in the foreground). Both
  use the same stable-weight rule, so they decide the same kg.
- A process-wide "weights changed" signal makes the main screen reload.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `background-weigh-in`: from listen-only to saving: section name, plausibility limit, automatic
  save, "?" rule and confirmation, notification, widget status; the "save nothing" rule goes.
- `scale-weigh-in`: the widget shows the background state; the lingering-broadcast rule also covers
  weighings saved in the background.
- `weight-entry`: Historia's list shows "?" weights with a mark (whatever the period filter) and
  accepts or deletes them on tap, with chained acceptance.
- `weight-chart`: "?" weights are left out of the chart and the period statistics.
- `home-screen`: "?" weights are left out of the home card and its changes; a notice while one
  waits that opens Historia at the newest "?" weight; reload on the weights-changed signal.
- `data-file`: weights carry the `check` field in both formats.
- `weight-export`: the CSV weight table gets the `check` column (after `raw`, time with offset).
- `weight-backup`: confirming a "?" weight also rewrites the backup; changes made with the app
  closed are backed up when it next opens.

## Impact

- Parts: Asetukset (section renamed, limit setting, notification state), Historia list (mark and
  confirmation), chart, statistics and home card (filtering), widget (status mark), scale
  (background save next to the dialog's), Health Connect (writes and deletes from the background),
  notifications (new channel, permission, actions), data file / export / backup / import (new
  field), log (new events). Android manifest: notification permission, a receiver for notification
  actions.
- No new library: notifications, alarms and the widget use the platform and the existing core
  library.
- Battery: no more than phase 1 (one Health Connect write per weigh-in).
