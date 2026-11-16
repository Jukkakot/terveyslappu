# Tasks

## 1. Model and storage

- [x] 1.1 `chart/CustomCharts`: `ChartDraft.styleOf(WEIGHT)` always returns LINE whatever its map holds (covers `toChart` and the editor's saved draft state, `DraftSaver`); `setStyle(draft, WEIGHT, …)` is a no-op.
- [x] 1.2 `chart/ChartStore` decode: a stored `{"kind":"weight","style":"bars"}` reads as line; other kinds and fields unchanged. Do not rewrite storage on read; the next save writes line.
- [x] 1.3 Tests (`CustomChartsTest`, the ChartStore test): an old chart with Paino bars decodes as line with name, other series, layout, period, colours and Keskiarvo unchanged; `setStyle` on Paino keeps LINE; Syöty, Kulutus and Askeleet can still be bars.

## 2. Editor

- [x] 2.1 `ui/ChartEditor`: the Paino row shows a plain "Viiva" text (existing `chart_style_line`, `labelLarge`, onSurfaceVariant; 38 % alpha while Paino is off) start-aligned in a `weight(1f)` box at least 40 dp high, in the space the segmented choice takes in the other rows, so the colour dots line up; test tag `chartStyle-WEIGHT-label`; TalkBack reads "Paino: viiva" (merged, not a button, no "valittu"). No `chartStyle-WEIGHT-BARS`/`-LINE` buttons.
- [x] 2.2 Editor smoke test: `chartStyle-WEIGHT-label` exists, no `chartStyle-WEIGHT-BARS` or `chartStyle-WEIGHT-LINE` node, `chartStyle-ENERGY-BARS` exists.

## 3. Copy and cleanup

- [x] 3.1 `strings.xml`: `help_charts_body` → "… Syöty, Kulutus ja Askeleet voivat olla pylväinä tai viivana ja paino aina viivana, …" (rest unchanged); `chart_new_intro` → drop "pylväinä tai viivana" so it no longer implies weight bars ("…sekä askeleet yhdessä, omalla jaksollaan.").
- [x] 3.2 Remove the "weight bars rise from the scale's bottom (Q16)" comment in `ui/CustomChartView` and any WEIGHT-specific bar baseline code (bars always from zero); if `openspec/context/questions.md` has Q16 on weight bars, mark it superseded by weight-line-only.

## 4. Finish

- [x] 4.1 `./gradlew test lint assembleDebug`
- [x] 4.2 Mark the roadmap item "Weight bars mislead in own charts" done (strike-through, "done 2026-11-16 (weight-line-only, v1.51, …)"); bump `appVersion` to 1.51.

Notes: Q16 is not in questions.md (3.2 only needed the code comment). TalkBack text via new string chart_weight_line_desc ("%1$s: viiva").
