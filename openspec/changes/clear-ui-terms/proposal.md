# Proposal

## Why

A terms audit found the same thing named two or three ways in the UI (a logged row is "annos",
"kirjaus" or "ruoka-annos"; eaten kcal is "Ruoka", "Syöty" or "Energia"), catch-all words
("tuote") and technical words ("kuuntelu", "Hyväksyntäraja", "Lähde"). The user's rule: two names
for one thing is a bug. One glossary, and every UI text follows it.

## What Changes

- A glossary in `openspec/context/product.md` (section "Terms") is the source of truth; a new
  `ui-terms` requirement says the UI uses only its words.
- Food: tuote → ruoka (own: "Omat ruuat", base: "Valmiit ruuat"); one logged row is always
  "annos"; the Asetukset sub-screen "Ruokakirjaus" → "Omat ruuat"; eaten kcal is always "Syöty"
  (tab, home card, chart series, statistics), burned kcal "Kulutus"; the day screen where annokset
  are added stays "Ruoka"; the energy field of a ruoka is "Kalorit"; "Pikalisäys N kcal" → "Lisää
  pelkkä N kcal"; Kulutus legend "kokonais" → "yhteensä".
- Automaattinen punnitus: no "kuuntelu" in any UI text; the status is the on/off switch plus one
  explanation line; the widget symbol is only "on" or "ei toimi" (warning); "Kuuntelutapa"
  Säästö/Tasapaino → "Tunnistusnopeus" Normaali/Nopea with a line about battery use;
  "Kuuntele myös virransäästötilassa" → "Toimii myös virransäästötilassa"; "Hyväksyntäraja" →
  "Tarkistusraja" with an explanation line.
- Data: "Tuo tiedot"/"Vie tiedot" → "Tuo tiedostosta"/"Vie tiedostoon"; the Asetukset section
  "Tiedot" → "Tiedostot ja varmuuskopio".
- Steps: "Lähde" → "Mistä askeleet luetaan"; "Kaikki (Health Connectin järjestys)" → "Kaikki
  sovellukset yhdistettynä".
- Wording only elsewhere: code names, log events, JSON keys and stored prefs stay.

## Capabilities

### New Capabilities
- `ui-terms`: the glossary rule (one name per thing, texts follow `product.md` "Terms").

### Modified Capabilities
- `background-weigh-in`: "Trial setting", "Trial status", "Widget status", "Status symbol in
  Asetukset" (new names, status lines, widget symbols).
- Quoted UI texts in the other main specs (food-log, energy-burn, steps, data-file,
  weight-backup, history-import, weight-export, help-page, home-screen, app-data-transfer) are
  updated in place to the new words (design 2); their behaviour does not change.

## Impact

- App: almost every screen's texts (`res/values/strings.xml`), Ohje, the AI import instruction
  (`assets/ai-food-prompt.txt`, wording only), the widget's symbols (`WidgetStateRule`, widget
  layout), the Automaattinen punnitus section in Asetukset.
- Health Connect, scale, data file format, logs: unchanged.
