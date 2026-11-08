# Design

## Context

The PoC app writes manual weights through `weight/HealthConnectWeights` and logs through
`log/AppLog`. The scale broadcasts its reading in BLE manufacturer data; the layout is in
`openspec/context/product.md` (Findings) and matches openScale's OKOK "C0" path. The user tests on
the phone only; the cloud session can compile and run JVM tests, so all decision logic must be
pure Kotlin. Planned in autopilot (user 2026-11-08: "plan and implement as far as you get"); every
choice below was taken on the user's behalf and can be revisited.

## Goals / Non-Goals

**Goals:** one tap → step on → saved, in a few seconds; every packet replayable from the log.

**Non-Goals:** body composition from impedance (logged, not saved; backlog), background or
automatic weigh-ins without a tap, pairing a specific scale by address, other OKOK variants
(V20, V11, VF0), history import.

## Decisions

1. **A dialog activity, not a foreground service.** The tile, widget and shortcut open
   `WeighInActivity`, a translucent dialog-themed activity in its own task
   (`excludeFromRecents`, `taskAffinity=""`), so the main screen never appears. A visible
   activity may scan freely; a foreground service started from a tile or widget needs a
   `connectedDevice` FGS type, a notification and Android 14+ start exemptions that are fragile.
   Alternative considered: scan in a service and show only a notification → more moving parts and
   nothing to look at while stepping on.
2. **Unfiltered low-latency scan, filtering in code.** The name may only be in the scan response
   and Android scan filters cannot match "company id low byte 0xC0". The scan runs only while the
   dialog is open (max 60 s), so the battery cost is small. `BLUETOOTH_SCAN` with
   `neverForLocation`, so no location permission is needed (openScale does the same).
3. **Pure core, thin Android shell.** `scale/ScalePacket` (decode bytes), `scale/StableWeight`
   (decision state machine fed with packets and elapsed ms) and `scale/PacketLog` (format/parse
   the log line) are plain Kotlin with JVM tests. `scale/ScaleScanner` only adapts
   `BluetoothLeScanner` callbacks into packets with elapsed times.
4. **Stability rule:** the scale's own stable flag is the truth; accept when two consecutive
   stable packets agree, or one stable packet is followed by 2 s without a different weight
   (checked on each packet and on a 250 ms tick). A different reading resets the candidate.
   Range 20–300 kg (the manual-entry rule) keeps 0.0 and junk out. This is a general rule, not a
   check for one capture (memory: general fixes only).
5. **Health Connect metadata:** `Metadata.autoRecorded(Device(type = SCALE, manufacturer =
   "Prego", model = "P814B"), clientRecordId = "terveyslappu-scale-<epoch second of the stable
   reading>")`. A stable client id makes a repeated write an upsert instead of a duplicate (the
   lesson openScale-sync documents). Manual entries keep their random ids.
6. **Log line:** `scale.packet` at DEBUG with `t=<ms since scan start> key=<company id hex>
   data=<payload hex> rssi=<dBm> name=<name or ->`. One line per callback, no de-duplication, so
   the replay sees exactly what the decision saw. Then `scale.stable kg= t=` and
   `weighin.save ok kg= ms=` plus `weighin.total ms=` (dialog open → saved). Address is not logged
   (not needed for replay).
7. **Tile and widget:** `WeighInTileService` uses `startActivityAndCollapse(PendingIntent)` (the
   API 34 form; minSdk is 34). The widget is a plain `RemoteViews` layout with one button and a
   `PendingIntent.getActivity`; no Glance dependency. A static launcher shortcut does the same.
8. **After saving:** the dialog shows the weight for 3 s and finishes. The main screen refreshes
   on resume, so it shows the new weight when it is the screen underneath.

## Risks / Trade-offs

- [The real packet stream differs from the one capture] → raw packet log from the first build;
  the user shares the log of a failing weigh-in and it becomes a replay test.
- [Samsung throttles or filters unfiltered scans] → the scan runs in the foreground only; if it
  fails, `onScanFailed` is logged with its code and shown (NFR-9).
- [Stale stable weight from the previous person still on the display] → accepted for now; the
  log will show whether the scale repeats an old reading when scanning starts.
- [`neverForLocation` hides some beacons] → this scale is not a beacon format; revisit only if the
  log shows no packets while the scale is on.

## Open Questions

- Does the scale keep broadcasting the final weight after stepping off, and for how long? Answered
  by the first shared log; does not change the design.
