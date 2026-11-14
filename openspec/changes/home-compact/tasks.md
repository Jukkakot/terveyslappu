# Tasks

## 1. Front page cards

- [ ] 1.1 Weight card: drop the derived-values row and its owner-details link; add the filled "Punnitse" button inside the card (opens the weigh-in dialog, not Historia); remove the bottom "Punnitse vaa'alla" button; the no-weights state invites with the same button
- [ ] 1.2 Remove the automatic weigh-in card from the front page (its Asetukset row and the widget stay); drop the home's now unused derived/listen state
- [ ] 1.3 Food card: full-width row built to take a second card later; title and today's kcal open Ruoka on today, the bars and what is under them open Historia on Energia, the period label keeps its menu; "+ Annos" outlined inside; remove the "Tänään" button
- [ ] 1.4 Lower both small charts and tighten paddings so the cards and notices fit a ~6" phone in portrait without scrolling (check with one screenshot at a phone size, light and dark)
- [ ] 1.5 Smoke tests: Punnitse in the weight card opens the weigh-in, no BMI or automatic weigh-in card on the front page, food card's number opens Ruoka and bars open Energia

## 2. Food screens and import wording

- [ ] 2.1 Ruoka day screen: "Viikko" in the top bar opens Historia on Energia with 1 vk; back returns to Ruoka
- [ ] 2.2 Clipboard offer also on the front page (on open and resume, above the cards with the other notices), sharing the offered-once memory with Ruoka and Lisää annos
- [ ] 2.3 Source-neutral wording: "Tuo ruoka-annokset" (paste button and preview title), "Kopioi tuontiohje", offer line "Leikepöydällä ruoka-annoksia: N · Näytä · Ohita", the no-JSON message without the AI; check `assets/ai-food-prompt.txt` needs no change
- [ ] 2.4 Tests: Viikko opens Energia (smoke), the front page shows the offer for food JSON on the clipboard and only once

## 3. Docs and release

- [ ] 3.1 Update Ohje's home and food texts to the new cards and wording
- [ ] 3.2 Update `.claude/CLAUDE.md` map where the home/food pieces moved; mark home-compact done in `openspec/context/roadmap.md`
- [ ] 3.3 Bump `appVersion` to 1.27; run `./gradlew test lint assembleDebug`
