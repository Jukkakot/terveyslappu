## 1. Implementation

- [x] 1.1 `addTyped`: a name that `EnterRow`'s rule calls unknown opens the draft editor (`editDraft`) with the field text kept for Peruuta; known names and Pikalisäys add as before
- [x] 1.2 Enter row line "täytä tiedot" for an unknown name; help text mentions it
- [x] 1.3 Unit test: Enter on an unknown name opens the editor and writes nothing; Lisää then logs it; Enter on a known name adds at once
- [x] 1.4 Checks `./gradlew test lint assembleDebug`, bump `appVersion` MINOR
