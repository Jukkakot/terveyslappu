# Design: food-ai-import

## Context

The user estimates a day's food with an AI chat (Claude on the phone, also their own calorie
skill) and wants the result in the food log without retyping. The interface between the chat and
the app is a JSON block in the chat's answer; the user shares or copies the answer to the app.

## Goals / Non-Goals

**Goals:** one share (or paste) logs the day's items; a strict, documented shape the AI can follow;
a lenient reader in the app; the user fine-tunes before saving; nothing existing is changed.

**Non-Goals:** choosing another day (backlog); portion sizes pieni/normaali/iso; an MCP service or
any server (dropped by the user 2026-11-14); meal names or times.

## Decisions

- **Share target, not a server.** `ACTION_SEND` with `text/plain` reaches the app from the chat's
  share button; the data never leaves the phone (no-backend decision). Paste stays as the fallback
  for apps whose share sends something else.
- **Strict prompt, lenient reader.** The prompt asks for one fenced JSON block with plain numbers;
  the reader still accepts surrounding text, a bare array, numbers as strings, a decimal comma and
  unknown keys, because chats drift. Only `name` is required, so a partial answer still helps.
- **Field names**: English keys like the data file, except `herkku`, which is the app's own word
  for the category (no English term would mean the same to the AI or the user).
- **`kcal` is the item's total**, not per 100 g: that is what a chat estimates and what the day
  sum needs; the app derives kcal per 100 g or per piece from `amount` for the product.
- **`products` replaces, items only fill.** `products` is the chat's explicit "keep this" list, so
  it may overwrite a product; an item only teaches a product that has no energy yet, so a one-off
  estimate never silently changes a product the user tuned.
- **Always today.** The user shares in the evening for the same day; no date in the shape, so the
  AI cannot put items on a wrong day. A date key is ignored.
- **The prompt below is the contract.** Kopioi ohje tekoälylle copies it word for word (Finnish UI
  text, a string resource); changing the shape means changing this text and the reader together.

## The prompt (copied by Kopioi ohje tekoälylle)

````text
Kun arvioit syömiseni kalorit, anna vastauksen lopuksi tulos Terveyslappu-sovellukseen alla olevassa JSON-muodossa yhtenä koodilohkona. Jaan vastauksesi sovellukseen, joka lukee koodilohkon ja kirjaa jokaisen rivin tämän päivän ruokiin. Muu teksti saa olla lohkon ympärillä.

```json
{
  "items": [
    {"name": "Kaurapuuro", "amount": 300, "unit": "g", "kcal": 210},
    {"name": "Lohikeitto", "amount": 1, "unit": "kpl", "kcal": 380},
    {"name": "Pulla", "amount": 1, "unit": "kpl", "kcal": 250, "herkku": true}
  ],
  "products": [
    {"name": "Lohikeitto", "kcal": 380, "per": "annos"}
  ]
}
```

Kentät ja mihin niitä käytetään:

items = mitä söin, syömisjärjestyksessä. Jokaisesta tulee oma rivi päivän ruokalistaan, ja sen kcal lasketaan päivän summaan.
- name (pakollinen): lyhyt suomenkielinen ruuan nimi ilman määrää, esim. "Kaurapuuro", ei "Kaurapuuro 3 dl". Sovellus tekee nimistä tuotteita ja ehdottaa niitä myöhemmin, joten käytä samasta ruuasta aina samaa nimeä.
- amount ja unit: syöty määrä. unit on "g" (valmiin ruuan paino grammoina) tai "kpl" (kappaleet tai annokset). Sovellus laskee näistä tuotteelle kalorit per 100 g tai per kpl, joten saman ruuan voi myöhemmin kirjata yhdellä napautuksella ilman tekoälyä.
- kcal: tämän rivin kokonaiskalorit kokonaislukuna (ei per 100 g). Jätä pois, jos et osaa arvioida; rivi kirjataan silloin ilman kaloreita, ja täydennän sen itse.
- herkku: true makeisille, leivonnaisille, jälkiruuille, sipseille, limsoille ja alkoholille. Sovellus näyttää herkkujen osuuden päivän kaloreista erikseen. Jätä pois tavalliselta ruualta.

products = ateriat ja ruuat, jotka tallennetaan sovellukseen valmiiksi, esim. usein syömäni kotiruuat. Sama ruoka voi olla myös items-listassa. Tuote korvaa sovelluksessa saman nimisen tuotteen aiemmat arvot, joten listaa vain ruuat, joiden arvon haluan pysyvän.
- name (pakollinen): kuten yllä.
- kcal (pakollinen): kalorit per yksikkö.
- per: "100g", "kpl" tai "annos". Annos tallentuu kappaleena.
- amount ja unit (vapaaehtoinen): tavallinen annokseni, esim. 250 ja "g".
- herkku: kuten yllä.

Säännöt: yksi JSON-koodilohko vastausta kohden. Numerot ilman yksiköitä ja lainausmerkkejä, desimaalit pisteellä. Ei kommentteja JSONin sisällä. Älä lisää päivämäärää: sovellus kirjaa kaiken aina kuluvalle päivälle.
````

## Risks / Trade-offs

- The chat's share may send only part of the answer or formatted text → the reader searches the
  whole text for the block; paste is the fallback.
- A name drift ("Kaurapuuro" vs "kaurapuuro kaurahiutaleista") makes duplicate products → the
  prompt asks for stable names; the products list in Asetukset can rename or remove.
