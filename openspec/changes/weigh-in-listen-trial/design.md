# Design

## Context

The dialog's scan (`scale/ScaleScanner.kt`) is a low-latency scan with no filter, collected
only while the dialog is open; packets are picked in code by name `Yoda*` or company id low byte
`0xC0`, and every scale-like packet is logged as `scale.packet` (NFR-7). minSdk is 34. The 2026-11-14
logs show `name=Yoda1` in nearly every packet and one stable address over all weigh-ins; the company
id high byte counts weighings, so it cannot be a filter. The scale repeats its last stable packet
for at least 13 s at about 10 packets/s. Samsung's battery rules make scheduled background work
unreliable (`openspec/context/knowledge.md`, Android).

## Goals / Non-Goals

**Goals:** measure range, scan survival and battery cost with the settings phase 2 would use;
keep the code reusable for phase 2 (the listener and its state stay, phase 2 adds saving).

**Non-Goals:** saving, notifications, the "?" rule, the widget status, Health Connect background
limits (phase 2).

## Decisions

1. **Scan with a PendingIntent, not a service.** `BluetoothLeScanner.startScan(filters, settings,
   PendingIntent)` delivers results to a manifest `BroadcastReceiver`; the scan lives in the
   Bluetooth stack, so the app process may die. Alternative, a foreground service with a callback
   scan: survives Samsung better but needs a permanent notification; rejected for the trial (it
   measures exactly the cheap path).
2. **Hardware filters: address, plus name when known (two ScanFilters, OR).** The receiver
   accepts only packets from the remembered address; a name-only match is logged as
   `listen.other` and not counted. If the remembered name is null, only the address filter is
   used. Never an empty filter list (Android stops unfiltered scans with the screen off). Log
   `isOffloadedFilteringSupported` and `isOffloadedScanBatchingSupported` at `listen.start`.
3. **Scan settings:** mode from the setting (`SCAN_MODE_LOW_POWER` for Säästö,
   `SCAN_MODE_BALANCED` for Tasapaino), `CALLBACK_TYPE_ALL_MATCHES`, report delay 5 s only when
   offloaded batching is supported, else 0. The PendingIntent is explicit to our receiver with
   `FLAG_MUTABLE` (the system fills in results); stop uses an identical PendingIntent. Results
   come as `EXTRA_LIST_SCAN_RESULT`, errors as `EXTRA_ERROR_CODE` (logged `listen.failed code=`).
4. **Alarms: `setAndAllowWhileIdle`** (inexact, fires in Doze) for window start, window end and
   an hourly check inside the window. The check logs `listen.check bt= powerSave= restricted=` and
   registers the scan again (heals Bluetooth off/on and a scan the system dropped; there is no
   manifest receiver for Bluetooth state). `listen.start late=<min>` logs the delay against the set
   start. No exact-alarm permission. Power saving mode is read at each start and check.
5. **Re-arm on `BOOT_COMPLETED` and `MY_PACKAGE_REPLACED`**; any settings change re-plans at once.
   A force stop cancels alarms and scan until the app is opened; on app start, "on, but nothing
   armed" re-arms and logs it.
6. **Accumulator in app-private preferences** (the process may die between deliveries): last
   packet bytes (company id + data, not rssi) for dedupe, repeat count with its rssi min/max,
   summary counters, window start time, battery level at start, `elapsedRealtimeNanos` at start.
   `listen.summary reason=end|off|stale` logs and clears it; a leftover accumulator found at start
   or turn-on is logged as `stale` first.
7. **Stable weights in the summary** = the existing stable-weight rule run over the trial's packets.
   A dialog weigh-in already logs its company id, so the two can be matched in the log.
8. **Pure core.** Window maths (inside, next start/end, over midnight, DST via `java.time` in the
   phone's zone), status choice and the accumulator are pure classes with unit tests; receivers and
   the Asetukset section are thin.
9. **Remember the scale** in the existing `weighin` preferences at save time (name, address).
   Trial prefs live in their own file excluded from Android backup and device transfer.
10. **Log events:** `listen.enable|disable`, `listen.settings window= mode= powerSave=`,
    `listen.arm next= action=start|stop|check`, `listen.start late= mode= offloadFilter=
    offloadBatch=`, `listen.stop reason=`, `listen.check ...`, `listen.failed code=`,
    `scale.packet ... src=bg screen=on|off` (`t=` = ms since `listen.start`; `screen` read when the
    batch arrives), `listen.repeat count= rssiMin= rssiMax=`, `listen.other addr= name= rssi=`,
    `listen.summary reason= mode= packets= distinct= rssiMax= rssiMin= first= last= stable=
    batteryStart= batteryEnd=`. `ScaleReplayTest` and the replay routine skip `src=bg` lines.

## Risks / Trade-offs

- [Samsung stops the scan or the alarm] → the summary (or its absence) and `listen.check` lines
  show it; the status warns about restricted battery use; the hourly check heals a lost scan.
- [Säästö too weak at -98] → this is what the trial measures; Tasapaino is one setting away.
- [Log rotation at 1 MB] → dedupe keeps one morning to tens of lines.
- [Address filter fails with a random address type on some chips] → the name filter and the
  logged offload flags show it.

## Open Questions

- Whether Säästö hears the scale from the bedroom: answered by the trial's log.
