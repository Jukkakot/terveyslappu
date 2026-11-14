# Tasks

## 1. Weigh-in survives the dialog
- [x] 1.1 `WeighIn` runs save, retry and Poista in a separate (app-wide) scope; `start`/`show` are no-ops once a weight is decided; a second Poista is ignored; tests (restart during Saving does not re-save, save and delete finish when the dialog scope is cancelled)
- [x] 1.2 `WeighInViewModel` passes `TerveysApp.appScope` and keeps "prepared"; the activity uses it

## 2. Dialog closing
- [x] 2.1 Close (backdrop, Back) ignored while Saving; Poista sets keep-open; auto-close rule as a pure function with a virtual-time test of the Poista/timer race

## 3. Checks
- [x] 3.1 `./gradlew test lint assembleDebug` green
