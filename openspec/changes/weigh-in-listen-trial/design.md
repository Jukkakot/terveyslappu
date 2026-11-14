# Design

## Context

The dialog's scan (`scale/ScaleScanner.kt`) is a low-latency scan with no filter, collected
only while the dialog is open; packets are picked in code by name `Yoda*` or company id low byte
`0xC0`, and every scale-like packet is logged as `scale.packet` (NFR-7). minSdk is 34. The 2026-11-14
logs show `name=Yoda1` in every packet and one stable address over all weigh-ins; the company id
high byte counts weighings, so it cannot be a filter. Samsung's battery rules make scheduled
background work unreliable (`openspec/context/knowledge.md`, Android).

## Goals / Non-Goals

**Goals:** measure range, scan survival and battery cost with the settings phase 2 would use;
keep the code reusable for phase 2 (the listener and its state machine stay, phase 2 adds saving).

**Non-Goals:** saving, notifications, the "?" rule, the widget status, Health Connect background
limits (phase 2).

## Decisions

1. **Scan with a PendingIntent, not a service.** `BluetoothLeScanner.startScan(filters, settings,
   PendingIntent)` delivers results to a manifest `BroadcastReceiver`; the scan lives in the
   Bluetooth stack, so the app process may die. Alternative, a foreground service with a
   callback scan: survives Samsung better but needs a permanent notification and keeps the
   process alive; rejected for the trial (the trial is meant to measure exactly the cheap path).
2. **Hardware filters: two ScanFilters (OR): device address, and device name exact.** Address is
   the reliable one; the name filter covers a scale that changes its address. Both values come from
   the remembered scale. An empty filter list is never used (Android stops unfiltered scans with the
   screen off).
3. **Settings: `SCAN_MODE_LOW_POWER`, `CALLBACK_TYPE_ALL_MATCHES`, report delay 5 s when
   `isOffloadedScanBatchingSupported`, else 0.** Batching makes one wake per 5 s instead of one per
   packet. If phase 0's edge reception proves too weak in low power, the trial log shows it and a
   later change can try `BALANCED`.
4. **Window with inexact alarms.** `AlarmManager.setWindow` (or `setAndAllowWhileIdle`) at window
   start and end, window length 15 min, delivered to a receiver that starts or stops the scan and
   schedules the next alarm. No exact-alarm permission (Android 14 makes it user-granted).
   Each window start re-registers the scan, which also heals a scan dropped by Bluetooth toggling
   or the system.
5. **Re-arm on `BOOT_COMPLETED` and `MY_PACKAGE_REPLACED`**: if the switch is on, schedule the
   next alarm, and start the scan at once when now is inside the window.
6. **Dedupe in the receiver.** The receiver keeps the last logged packet data per trial window in
   app-private preferences (process may die between deliveries); identical data only bumps a
   counter. Summary counters (packets, distinct readings, rssi min/max, first/last time, stable
   weights) live in the same preferences and are logged and cleared at window end.
7. **Pure core.** Window maths (inside? next start/end, over midnight, DST via `java.time` with
   the phone's zone), status choice and the dedupe/summary accumulator are pure classes with unit
   tests; the receivers and the Asetukset section are thin.
8. **Remember the scale** in the existing `weighin` preferences at save time (name, address).
9. **Log events:** `listen.enable|disable window=`, `listen.arm next= action=start|stop`,
   `listen.start` / `listen.stop reason=window|off|boot`, `listen.failed code=`, `scale.packet ...
   src=bg screen=on|off` (same fields as the dialog's line plus these two), `listen.repeat
   count=` when a run of identical packets ends, `listen.summary packets= distinct= rssiMax=
   rssiMin= first= last= stable=`.

## Risks / Trade-offs

- [Samsung stops the scan or the alarm] → the summary line (or its absence) shows it; the status
  checks battery optimisation; the daily re-arm heals one lost scan.
- [Reception at -98 too weak in low power] → this is what the trial measures; phase 2 is not
  built if the log shows misses.
- [Log rotation at 1 MB pushes out older lines] → dedupe keeps one morning to tens of lines.
- [Hardware filter by name not supported on the chip] → the address filter alone still works;
  the trial log tells.

## Open Questions

- Whether `BALANCED` mode is needed: answered by the trial's log.
