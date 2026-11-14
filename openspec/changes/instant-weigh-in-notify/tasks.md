## 1. Immediate decision

- [ ] 1.1 `HeardPackets`: decide at the first stable packet of a key (no `StableWeight` quiet time, no pending state); a later stable packet with the same key stays ignored; update `HeardPacketsTest`, background replay cases
- [ ] 1.2 Remove the tick alarm (`ListenAlarm.setTick/cancelTick`, request code 3, `ListenReceiver.ACTION_TICK`, `ListenController.onTick`, `listen.tick` log) and cancel a tick left armed by 1.21 once at start
- [ ] 1.3 Background scan `setReportDelay(0)` always; keep logging `offloadBatch`

## 2. Notification buttons

- [ ] 2.1 Normal note: OK (closes only, logs `auto.action ok`) + Poista punnitus; "?" note: Hyväksy + Poista punnitus; strings; `AutoAndroidTest`

## 3. Finish

- [ ] 3.1 `appVersion` 1.22; CLAUDE.md log events (drop `listen.tick`, add `auto.action ok`); `./gradlew test lint assembleDebug`
