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
  from the latest accepted weight of this app is marked "?". A "?" weight is confirmed or deleted in
  Historia (tap the row) or from the notification; a confirmed one becomes the new reference. With
  no earlier weight everything is accepted. No time factor.
- A notification after every automatic weigh-in: the weight, the change, and Poista; a "?" weight
  also gets Hyväksy. The app asks for the notification permission when the feature is turned on.
- The home-screen widget shows the feature's state: on (inside the window and nothing blocks),
  resting (outside the window) or blocked (battery restriction, Bluetooth off, a missing
  permission). Without the feature on it looks as today.
- "?" weights are left out of the chart, the period statistics, the home card and the change
  values until confirmed; Historia lists them with a "?" mark, and the home screen shows one notice
  "Punnitus odottaa tarkistusta" while one waits.
- The backup of a change made while the app was closed runs the next time the app opens (reading
  all weights for the backup is a Health Connect read, which the background may not do).
- The "?" mark lives in the app's own storage (Health Connect has no field for it) and travels in
  the data file: a new `check` field on weights in CSV and JSON, read back on import.
- The dialog weigh-in and the background save never both save the same weighing.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
- `background-weigh-in`: from listen-only to saving: section name, plausibility limit, automatic
  save, "?" rule and confirmation, notification, widget status; the "save nothing" rule goes.
- `scale-weigh-in`: the widget shows the background state; the lingering-broadcast rule also covers
  weighings saved in the background.
- `weight-entry`: Historia's list shows "?" weights with a mark and confirms or deletes them on tap.
- `weight-chart`: "?" weights are left out of the chart and the period statistics.
- `home-screen`: "?" weights are left out of the home card and its changes; a notice while one
  waits.
- `data-file`: weights carry the `check` field in both formats.
- `weight-export`: the CSV weight table gets the `check` column.
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

## Open questions for the user

1. Should "?" weights stay out of the chart, statistics and home card until confirmed?
   Recommended: yes (a wrong 20 kg jump would ruin the chart's scale); Historia lists them with "?".
2. Is the reference "the latest accepted weight written by this app" (manual, dialog or confirmed
   automatic), ignoring other apps' weights? Recommended: yes, the same set Historia lists.
3. Outside the window, should the widget say "resting" even when Bluetooth is off, and check what
   blocks only inside the window? Recommended: yes (Bluetooth off at night is normal, and it needs
   no extra wake-ups).
4. Should a normal (not "?") notification disappear by itself? Recommended: after one hour,
   silently; a "?" notification stays until handled and makes the phone's normal notification sound.
5. If the notification permission is denied, should the feature still save? Recommended: yes,
   save; Asetukset shows a warning line with a button to the notification settings, and the widget
   is not shown as blocked.
