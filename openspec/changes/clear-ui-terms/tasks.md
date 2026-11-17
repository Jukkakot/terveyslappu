# Tasks

## 1. Texts

- [x] 1.1 Go through `res/values/strings.xml` against the glossary and design 3; change the texts
      (keys stay).
- [x] 1.2 `assets/ai-food-prompt.txt`: wording per design 3 (JSON keys unchanged); check the AI
      paste tests still pass.
- [x] 1.3 Grep check from design "Risks"; no hit left except where the glossary allows it.

## 2. Automaattinen punnitus

- [x] 2.1 Status lines per the delta "Trial status" and "Status symbol in Asetukset" (the rule
      picking the line keeps its order); the Tunnistusnopeus line and the Tarkistusraja line.
- [x] 2.2 Widget and Asetukset symbol: ON / BLOCKED / OFF per design 4; update the rule's tests.

## 3. Specs and docs

- [x] 3.1 Update quoted UI texts in `openspec/specs/*/spec.md` per design 2 (wording only).
- [x] 3.2 `.claude/CLAUDE.md`: no map change expected; fix any UI words it quotes.

## 4. Finish

- [x] 4.1 Bump `appVersion` to 1.57; `./gradlew test lint assembleDebug`.
