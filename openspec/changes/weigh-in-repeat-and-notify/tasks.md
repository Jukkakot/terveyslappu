## 1. Same weight repeated

- [ ] 1.1 `WeighingKeys`: store last kg, address and last-heard time; `sameWeight` (0.1 kg, sliding 30 s) and `remember`; unit tests
- [ ] 1.2 Dialog (`WeighIn`) and background (`AutoWeights.decided`) skip a same weight before claiming, log `scale.ignore reason=same kg=`
- [ ] 1.3 Saves and deletes (dialog save/discard, background save, notification Poista, Historia delete) call `remember`; replay test with a re-broadcast after delete (made-up weights)

## 2. Timely background decision

- [ ] 2.1 `ListenAlarm`: second PendingIntent `tick`, set/cancel; `ListenReceiver` routes it
- [ ] 2.2 `ListenController.onResults` arms the tick when a stable reading is pending, the tick decides it as the hourly check does; tests with a fake alarm

## 3. Notifications alert

- [ ] 3.1 New channel `weighins_alert` (default importance), delete `weighins`; normal notes use it

## 4. Stop after the window's weight

- [ ] 4.1 `ListenPrefs` window-done record; add/remove from dialog save, manual entry save, background `check=ok` save, accept, delete and unmark
- [ ] 4.2 `ListenController.replan`: done record for the current window stops listening till the next start (`listen.stop reason=done`); record change replans; tests
- [ ] 4.3 `ListenStatusRule` `Done` state "Tämän päivän paino tallennettu, kuuntelee taas klo HH.MM", `WidgetStateRule` → Lepää; tests

## 5. Finish

- [ ] 5.1 `appVersion` 1.21, CLAUDE.md log events (`scale.ignore reason=same`, `listen.stop reason=done`, tick), `./gradlew test lint assembleDebug`
