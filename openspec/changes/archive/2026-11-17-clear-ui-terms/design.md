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

## Review decisions (2026-11-17)

- Status order = the code's `ListenStatusRule` order (delta matches it).
- Widget: keep the four-value `WidgetState` enum, rule, prefs and log values; map to two symbols
  (ON/DONE/RESTING → dot, BLOCKED → triangle) only when drawing (`WeighInWidget.views`,
  `ListenStatus.symbol()`). `shown()` also turns a stored BLOCKED into the dot after the stored end.
- Widget readout: "Punnitse, automaattinen punnitus päällä" / "… ei toimi". Asetukset symbol stays
  decorative.
- Texts the delta did not name: switch "Tallenna punnitus automaattisesti"; its hint "Sovellus
  tunnistaa vaa'an valitsemanasi aikana ja tallentaa painon itse, myös sovelluksen ollessa
  kiinni."; the Tarkistusraja line replaces `listen_limit_hint`; "Pienennä/Suurenna
  tarkistusrajaa"; delete unused widget state strings.
- Last heard: "Vaaka tunnistettu viimeksi tänään 7.12, signaali -97" / "Vaakaa ei vielä
  tunnistettu" (add a MODIFIED "Last heard" to the delta).
- The food feature's name is "Ruoka" (Ohje title, permission texts); only the Asetukset row and
  sub-screen are "Omat ruuat".
- The ban covers the nouns kirjaus/ruokakirjaus in every form; the verb "kirjata" is allowed.
  "energia" is allowed in prose, never as a name or title. The risk grep is case-insensitive.
- Steps: row "Askeleet: Garmin Connect (automaattinen)", "Askeleet: kaikki sovellukset
  yhdistettynä"; "Vaihda, mistä askeleet luetaan"; "Valinta koskee myös Kulutusta"; Garmin line
  "…kun askeleet luetaan Garmin Connectista tai kaikista sovelluksista".
- Both "Tiedot" and "Tiedot ja varmuuskopio" → "Tiedostot ja varmuuskopio"; Ohje title "Tuo
  tiedostosta ja vie tiedostoon"; "kohdasta Tuo tiedostosta".
- Requirements whose behaviour changes go into the delta as MODIFIED (not in-place):
  background-weigh-in "Stop after the window's weight", "Listen in the background" and "Last
  heard"; scale-weigh-in's widget requirement (Päällä/Lepää/Estetty).
- Tunnistusnopeus default: whatever `ListenMode.of(null)` gives today, named in the spec.
- "huomenna" before "klo" whenever the time is on the next date, in "Alkaa", "Odottaa vaakaa" and
  "Tänään punnittu"; add the window end to the Listening status. Time format "H.mm".
- A saved or queued weight is "paino": "Poista paino", "N painoa odottaa tallennusta"; "punnitus"
  only for weighing on the scale.

## Implementation notes (2026-11-17)

- The stored name of a quick-added item, `QUICK_ADD` = "Pikalisäys", stays: it is data in Health Connect
  and matched by code; only the row "Lisää pelkkä N kcal" changed.
- "N painoa odottaa tallennusta" is now a plural ("1 paino odottaa tallennusta").
- Notification channel names: "Tallennetut painot" / "Tarkistettavat painot"; the notification
  "Tarkista paino" title; the notification action "Poista paino".
- `ListenStatus.Listening` and `Resting` carry the window end / start and whether it is tomorrow
  (the delta scenario "Before the window" now reads "Alkaa huomenna klo 6.00").
- Own-chart and Syöty legend/readout name is "syöty" (was "ruoka"); Kulutus "yhteensä" (was "kokonais").
- The AI instruction says `per` is "100g" or "kpl" only; the reader still accepts "annos".
