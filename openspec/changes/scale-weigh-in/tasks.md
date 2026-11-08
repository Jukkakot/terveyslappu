# Tasks

## 1. Scale protocol core (pure Kotlin)

- [x] 1.1 `scale/ScalePacket`: recognise a Yoda/nameless advertisement with a 0xC0 company id and decode weight, stable flag, decimals, unit (kg, jin, lb, st:lb) and impedance; JVM tests for each unit, short data and other devices pass
- [x] 1.2 `scale/StableWeight`: the stability rule (two equal stable packets, or one stable and 2 s quiet; reset on a different reading; 20–300 kg); JVM tests for the spec scenarios pass
- [x] 1.3 `scale/PacketLog`: format a packet as the `scale.packet` log fields and parse log lines back; replay test from a made-up log reaches the same weight

## 2. Saving and scanning

- [ ] 2.1 `HealthConnectWeights.writeScale`: scale device metadata and a client id from the stable reading's second; unit test for the id
- [ ] 2.2 `scale/ScaleScanner`: low-latency unfiltered scan that emits decoded packets with ms since start, logs each raw, logs scan failures; compiles (`./gradlew assembleDebug`)
- [ ] 2.3 Manifest: `BLUETOOTH_SCAN` (neverForLocation), `bluetooth_le` feature not required; lint passes

## 3. Weigh-in dialog

- [ ] 3.1 `WeighInViewModel`: states (needs permission, Bluetooth off, no Health Connect access, scanning, live weight, saving, saved, save failed, not found), 60 s timeout, save once, timings logged; JVM tests with a fake scanner and fake saver pass
- [ ] 3.2 `WeighInActivity` + dialog UI: permission request, enable-Bluetooth request, live weight, retry, auto-close 3 s after saving; Robolectric smoke test renders the scanning state
- [ ] 3.3 Strings in Finnish; light and dark follow the system theme

## 4. One-tap starts

- [ ] 4.1 Quick Settings tile `WeighInTileService` opening the dialog with `startActivityAndCollapse`
- [ ] 4.2 Home-screen widget (one button) and a static launcher shortcut opening the dialog
- [ ] 4.3 "Punnitse" button on the main screen; existing main-screen test still passes and checks the button exists
- [ ] 4.4 `./gradlew test lint assembleDebug` passes

## 5. Docs

- [ ] 5.1 Update `.claude/CLAUDE.md` map and log events, `product.md` findings/decisions, roadmap (phase 2 in progress → done after implementation)

## Workflow follow-up

- Archive after the change is merged and CI is green.
