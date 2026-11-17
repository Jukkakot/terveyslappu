# Design

## Decisions

1. **Glossary first.** `openspec/context/product.md` "Terms" is the source of truth. The
   implementer goes through every string in `res/values/strings.xml` (all Finnish UI text lives
   there; no hard-coded Finnish in `ui/*.kt`) and `assets/ai-food-prompt.txt` against it. String
   keys, code names (`Product`, `listen/`), log events, JSON keys (`products`, `per: annos`) and
   prefs keys stay. Inflection: ruoka, ruoan, ruokaa, ruuat, ruokien, ruokia; annos, annoksen,
   annosta, annokset.
2. **Main specs' quoted texts.** Behaviour does not change outside background-weigh-in, so those
   specs get no delta; instead the implementer updates the quoted Finnish UI texts in
   `openspec/specs/*/spec.md` in place in the same change (wording only, scenario logic
   untouched), so archive leaves every spec true. background-weigh-in's other requirements
   (notification texts, "Kuuntelee"-style words in scenarios) are updated the same way.
3. **Mapping** (audit 2026-11-17; not exhaustive, the glossary decides):
   - tuote → ruoka in every form; "Tuotteet" list title → "Omat ruuat"; "Tallennettavat tuotteet"
     → "Tallennettavat ruuat"; "+ N tuotetta" → "+ N ruokaa".
   - kirjaus/ruoka-annos → annos ("Tuo ruoka-annokset" → "Tuo annokset"; "ei kirjauksia" → "ei
     annoksia"; import counts "N ruokaa" meaning rows → "N annosta"); Energia-statistics "Kirjattu"
     (days) → "Päiviä".
   - Asetukset "Ruokakirjaus" → "Omat ruuat" (row, sub-screen title, Ohje).
   - Eaten kcal: "Energia" titles and "Ruoka" as a card or chart name → "Syöty"; the ruoka field
     "Energia" → "Kalorit". The day screen "Ruoka" stays.
   - "Pikalisäys %1$s kcal" → "Lisää pelkkä %1$s kcal".
   - Kulutus legend "kokonais" → "yhteensä".
   - Automaattinen punnitus texts per the delta; widget symbols per the delta.
   - "Tuo tiedot"/"Vie tiedot" → "Tuo tiedostosta"/"Vie tiedostoon"; Asetukset "Tiedot" →
     "Tiedostot ja varmuuskopio".
   - Steps "Lähde" → "Mistä askeleet luetaan"; "Kaikki (Health Connectin järjestys)" → "Kaikki
     sovellukset yhdistettynä".
   - The AI instruction: "tuote" → "ruoka"; the `per` explanation says a portion is saved per
     "kpl" (not "annos", which is a logged row).
4. **Widget**: `WidgetStateRule` keeps its inputs; its result collapses to ON / BLOCKED / OFF
   (Valmis and Lepää map to ON). The widget log line `widget.state state=` keeps its old values
   for the log (`on|done|resting|blocked|off`) so logs stay comparable; only the drawn symbol
   changes. The Asetukset symbol follows the same mapping.
5. **Text checks**: no test asserts exact copy (project rule); a unit test of the status rule
   checks which line kind applies, as today.

## Risks

- Many strings at once: a missed one keeps an old word. Mitigation: after the edit, grep
  strings.xml and the prompt for "tuote", "kirjaus", "ruoka-annos", "kuunte", "Hyväksyntä",
  "Energia", "Lepää", "Estetty", "Pikalisäys", "Lähde".
