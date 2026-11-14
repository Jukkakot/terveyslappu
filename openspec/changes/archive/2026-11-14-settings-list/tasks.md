## 1. Navigation

- [x] 1.1 `SettingsPage` (LIST, PROFILE, LISTEN, DATA, APPEARANCE, SUPPORT) and the origin screen in `MainUiState`; `openSettings(page, from)` replaces `profileFocus`; back from a sub-screen goes to LIST when opened from it, else to the origin; Ohje's back goes HOME
- [x] 1.2 Links: BMI reason on home and in Historia → PROFILE, backup notice → DATA, gear → LIST
- [x] 1.3 Unit tests for the back rule (`MainUiStateTest` or a pure helper)

## 2. Asetukset list and sub-screens

- [x] 2.1 Pure state-line rules per row (`SettingsSummary`): Omat tiedot = set fields joined with " · " or "Ei asetettu"; Automaattinen punnitus = "Pois" / "Päällä H.MM–H.MM", warning text when blocked or weighings wait; Tiedot ja varmuuskopio = backup off / on / failed / late (warning when `needsAttention`); Ulkoasu = "Teema: …"; Tuki = "Loki · v<version>"; unit tests
- [x] 2.2 Main list rows (icon, name, state line, chevron, warning tint), DATA row only with the Health Connect permission
- [x] 2.3 Sub-screens with `SubScreenBar` holding today's sections unchanged (owner fields, listen group, data + backup with the export format dialog, theme choice, share/copy log with the version)
- [x] 2.4 Remove the Ohje row from Tuki; help (?) icon in the home top bar left of the gear opens Ohje

## 3. Tests and release

- [x] 3.1 Smoke tests: list shows the rows; a row opens its sub-screen and back returns to the list; BMI link opens Omat tiedot and back returns home; help icon opens Ohje
- [x] 3.2 Update existing UI tests that find settings by tag in the long page
- [x] 3.3 Remove section 6.1, 6.2, 6.4 and design 10 from food-log (keep 6.3 Ruokakirjaus as a sixth row); update its home-screen and help-page deltas and version
- [x] 3.4 `.claude/CLAUDE.md` map if needed; `appVersion=1.23`; `./gradlew test lint assembleDebug` green
