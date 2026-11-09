## 1. Owner details

- [ ] 1.1 Pure validation and age logic (birth year 1900..current year, height 50..250 cm, age from birth year) with unit tests
- [ ] 1.2 Preferences store for the details (singleton in `TerveysApp`), empty by default, a cleared field removed
- [ ] 1.3 Asetukset "Omat tiedot" section: birth year + age, height, gender chips (Mies / Nainen / Ei valittu); range message on invalid input; `profile.set field=` log line
- [ ] 1.4 Smoke test: the section shows and a saved height survives recreating the screen

## 2. Docs and release

- [ ] 2.1 product.md decision, roadmap item done, CLAUDE.md log events (`profile.set`) and map; bump MINOR in `gradle.properties`
- [ ] 2.2 `./gradlew test lint assembleDebug`
