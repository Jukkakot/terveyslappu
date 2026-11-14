## 1. Parsing and plan (pure)

- [ ] 1.1 `food/AiPaste.kt`: find the JSON in clipboard text (code fence, first `{`/`[` to its
      matching end), parse object-with-`items` or bare list, tolerant numbers (also "300" and
      "1,5"), skip nameless items; result = items + skipped count, or a failure reason.
- [ ] 1.2 Plan the paste with the existing add/product rules: items at the end of the day in order,
      product matching by name key, energy learnt only by products without one, known Herkku wins,
      Pikalisäys name never a product.
- [ ] 1.3 Unit tests: fence + surrounding text, bare list, no JSON, nameless item, unit default,
      learnt energy, known product untouched, treat rule (made-up foods).

## 2. Sheet

- [ ] 2.1 Lisää annos sheet: Liitä tekoälyltä (clipboard read) → preview (items, sum, skipped) →
      Lisää / Peruuta; "N annosta lisätty · Kumoa" undoes all; failure line for no JSON.
- [ ] 2.2 Kopioi ohje tekoälylle: Finnish instruction text with the shape and an example to the
      clipboard.
- [ ] 2.3 Log `food.paste ok count= skipped=` / `food.paste failed reason=` (never names or kcal).
- [ ] 2.4 One smoke test of the preview.

## 3. Docs and release

- [ ] 3.1 One line in Ohje; bump MINOR in `gradle.properties`.
- [ ] 3.2 Update `.claude/CLAUDE.md` map (AiPaste) and log events; roadmap backlog line done.
