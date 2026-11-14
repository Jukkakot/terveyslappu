## 1. Parsing and plan (pure)

- [x] 1.1 `food/AiPaste.kt`: find the JSON in text (code fence, first `{`/`[` to its matching end),
      parse object with `items`/`products` or a bare list (unknown keys ignored), tolerant numbers ("300", "1,5"),
      skip nameless or unusable entries; result = items + products + skipped count, or a
      failure reason.
- [x] 1.2 Plan the import with the existing add/product rules: items at the end of the chosen day in
      order, product matching by name key, energy learnt only by products without one, known
      Herkku wins, `products` list creates or replaces, Pikalisäys never a product; the plan keeps
      the products' previous state for Kumoa. Append only: no existing item in the plan's changes.
- [x] 1.3 Unit tests: fence + surrounding text, bare list, no JSON, nameless item, unit default,
      `annos` as kpl, learnt energy, known product untouched by an item, product list replaces,
      undo restores products, existing items untouched (made-up foods).

## 2. Share, sheet and preview

- [x] 2.1 Share target for `text/plain` (`ACTION_SEND`) that opens the app on the import preview;
      text without JSON shows the reason.
- [x] 2.2 Lisää annos sheet: Liitä tekoälyltä (clipboard) and Kopioi ohje tekoälylle: copies
      `assets/ai-food-prompt.txt` as is (already written); a test that its example parses.
- [x] 2.3 Preview: day row (today by default; tap opens a single-day calendar, no future days),
      items and products
      editable and removable, sum, skipped count; Lisää / Peruuta; "N annosta lisätty · Kumoa".
- [x] 2.4 Log `food.import from=share|paste ok count= products= skipped=` / `food.import failed
      reason=` (never names or kcal).
- [x] 2.5 One smoke test of the preview.

## 4. Clipboard offer (user 2026-11-14)

- [ ] 4.1 Pure rule: offer only for a clip newer than the last offered one (clip description
      timestamp, remembered in prefs) whose text parses as food JSON; unit tests.
- [ ] 4.2 Ruoka screen and Lisää annos sheet: on open (resume), check the clip description first
      and read the text only when it is new text; show "Leikepöydällä ruoka-arvio: N annosta ·
      Näytä · Ohita"; Näytä opens the preview; any answer marks the clip offered.
- [ ] 4.3 Log `food.import.offer shown|open|skip count=`; CLAUDE.md log events; one smoke test.

## 3. Docs and release

- [x] 3.1 One line in Ohje; bump MINOR in `gradle.properties`.
- [x] 3.2 Update `.claude/CLAUDE.md` map (AiPaste, share target) and log events; roadmap backlog
      line done.
