## 1. Owner details

- [ ] 1.1 Pure validation and age logic (birth year 1900..current year, height 50..250 cm, age from birth year) with unit tests
- [ ] 1.2 Preferences store for the details (singleton in `TerveysApp`), empty by default, a cleared field removed
- [ ] 1.3 Asetukset "Omat tiedot" section: birth year + age, height, gender chips (Mies / Nainen / Ei valittu); range message on invalid input; `profile.set field=` log line
- [ ] 1.4 Smoke test: the section shows and a saved height survives recreating the screen

## 2. Details in export and import

- [ ] 2.1 Pure `#profile;` line encode/decode (set fields only, unknown or implausible values ignored) with unit tests
- [ ] 2.2 Export (and so the backup) writes the line before the header when any detail is set; test with made-up details
- [ ] 2.3 History parser recognises the line (not a skipped row); preview notes details to fill; on confirm fill only empty details; tests incl. "already set stays"

## 3. Height to Health Connect

- [ ] 3.1 Manifest WRITE_HEIGHT; height writer (HeightRecord, manual entry, now) separate from the weight permission set
- [ ] 3.2 Saving a height asks the permission when missing, writes, logs `profile.height.hc ok|denied|failed`; field note when not in Health Connect

## 4. Docs and release

- [ ] 4.1 product.md decision, roadmap item done, CLAUDE.md log events (`profile.set`) and map; bump MINOR in `gradle.properties`
- [ ] 4.2 `./gradlew test lint assembleDebug`
