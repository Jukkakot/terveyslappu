## ADDED Requirements

### Requirement: One name per thing
Every user-visible text (screens, dialogs, notifications, widgets, Ohje, accessibility labels and
the import instruction copied by Kopioi tuontiohje) SHALL name each thing with the word given for
it in the glossary ("Terms" in `openspec/context/product.md`) and SHALL NOT use another word for
the same thing or the same word for another thing. A new text whose thing is not in the glossary
SHALL add the word to the glossary first.

#### Scenario: Logged row
- **WHEN** any screen, message or help text refers to one logged row of food
- **THEN** it calls it "annos", never "kirjaus" or "ruoka-annos"

#### Scenario: Eaten energy
- **WHEN** the Historia tab, the home card or a chart shows eaten kcal
- **THEN** each is titled with "Syöty"

#### Scenario: Reusable food
- **WHEN** Asetukset lists the user's own reusable foods
- **THEN** the sub-screen is "Omat ruuat" and no text says "tuote"
