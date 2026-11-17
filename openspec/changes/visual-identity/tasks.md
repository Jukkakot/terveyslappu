# Tasks

## 1. Screenshot harness (developer tool)

- [x] 1.1 Add Roborazzi (test only) to `gradle/libs.versions.toml` and `app/build.gradle.kts`; exclude `**/screens/**` from every `Test` task unless the Gradle property `screenshots` is set (design 9).
- [x] 1.2 Write `screens/ScreenshotTour` (Robolectric, 360 × 780 dp, density 2) rendering front page, Historia Paino/Syöty/Askeleet/Kulutus, Ruoka and Asetukset → Ulkoasu with the existing made-up fixtures, in Tumma and Vaalea; leave a parameter for the colour choice (filled in 2.x).
- [x] 1.3 Check that `./gradlew test` runs no screenshot test and that the documented command writes PNGs to `app/build/outputs/roborazzi/`; record the command in `.claude/CLAUDE.md` (Build, release, logs).
- [x] 1.4 Run the tour once on the current look and keep the PNGs in the session scratchpad as the "before" set.

## 2. Palette and colour choice

- [x] 2.1 Add MaterialKolor; build the Terveyslappu scheme from seed `#2E7D6B` with the graphite dark neutrals, mint dark primary and light neutrals (design 1).
- [x] 2.2 Add `ColourChoice` (BRAND default, PHONE) to `AppearancePrefs` (key `colours`); `TerveysTheme` picks the brand or the dynamic scheme; unit test the decode default and that an existing `theme` value is kept.
- [x] 2.3 Ulkoasu sub-screen: "Värit" chip group (Terveyslappu, Puhelimen värit) under "Teema"; the Asetukset row reads "<teema> · <värit>" (`SettingsSummary`); smoke test that picking Puhelimen värit changes the scheme.
- [x] 2.4 Add `QuantityColours` with light/dark shades and one `isDarkTheme()` helper; replace the scattered `luminance()` picks (`checkColors`, `paletteColour`, `burnColour`); test every shade ≥ 3:1 against the brand card surfaces and the surfaces `ChartPaletteTest` uses (design 3).
- [x] 2.5 Own charts: Oletus resolves to the quantity colour; Kulutus default coral; update `ChartPaletteTest` and the chart editor's Oletus dot.
- [x] 2.6 Neutral change colours: `changeColor()` and every change value/arrow (Historia statistics and rows, cards) in the neutral text tones for gain and loss; update its test.
- [x] 2.7 Add the colour choice to the screenshot tour (both palettes).

## 3. Cards and numbers

- [x] 3.1 Card look: 24 dp corners, no outline, `surfaceContainerLow` cards for front page cards, Historia containers and Asetukset groups.
- [x] 3.2 Icon tile (28 dp round, quantity colour at 16 % dark / 12 % light, icon in full colour; own chart card: neutral tile with the chart icon; chevron kept), permission state "anna lupa" under the kept title, and titles "Paino · <day time>", "Syöty", "Askeleet", "Kulutus" (own chart: its name) at every size; "tänään" under the steps number; the weight card reads "Paino" also when empty and in edit mode; smoke test titles not truncated at small size and 130 % text.
- [x] 3.3 Big number style (semibold, tabular figures, muted smaller unit, auto-shrink instead of wrap) on cards and the hero figure (design 8).
- [x] 3.4 Period label as an outlined neutral chip; the average text muted and smaller.

## 4. Chart look

- [x] 4.1 Quantity colours in every Historia tab and front page card (paino/BMI, syöty + herkku, askeleet, kulutus + lepo).
- [x] 4.2 Rounded bar tops: `RoundedBarChartRenderer` plugged into the combined chart (top stack part only); render test that a stacked bar draws without error and a 1 dp card bar stays visible.
- [x] 4.3 Today emphasis on fixed tabs and feature cards only: per-part bar colours (past daily bars at 60 %, today full; nothing dimmed without a bar today or for weekly bars); a pure colour-list builder walking each entry's parts, unit-tested with a Kulutus week mixing split, total-only and active-only days and a Syöty week with treats.
- [x] 4.4 Paino/BMI line: 2.5 dp, dark-theme glow (6 dp, 25 %), gradient fill on Paino only, ring on the last weight in range also without dots, layer order as in "Chart look"; glow and ring sets in their own list after `lines`; own charts: Allekkain only; test that tapping still picks the weight and the legend has no extra entry.
- [x] 4.5 Faint horizontal gridlines.

## 5. Historia header and statistics

- [ ] 5.1 Tab row first, five fixed tabs fitting 360 dp, own chart tabs by scrolling; smoke test the order (tabs above periods).
- [ ] 5.2 Periods as one six-segment row under the tabs (one selectable group for TalkBack); Oma stays "Oma" when chosen, reopens the calendar on every tap, and the range line shows the dates; smoke test all six visible at 360 dp and 130 % text.
- [ ] 5.3 `HeroStat` + `StatRow` on Paino, BMI, Syöty, Askeleet and Kulutus exactly as the per-tab list in weight-chart "Statistics layout" (BMI: Viimeisin with class word in the hero); "—" for missing values; a pure mapping per tab with a unit test.

## 6. Row ⋮ menu

- [ ] 6.1 Shared `RowMenu` (⋮ + menu, Muokkaa, red Poista, extra items, TalkBack name "Toiminnot: <name>"), replacing `FoodScreen`'s `RowActions`.
- [ ] 6.2 Use it on Historia weight rows (questioned rows: Hyväksy then Poista; their tap still opens the dialog), Ruoka, the sheet's day list and typing rows (base products: Muokkaa only), Omat ruuat and the AI preview; remove the pencil and trash buttons; update the affected screen tests (delete through the menu, Kumoa still restores).
- [ ] 6.3 Check Ohje (`HelpScreen` strings) for mentions of the trash or pencil and reword to "⋮ → Poista".

## 7. Check and docs

- [ ] 7.1 Run the screenshot tour (the specs win over the mockups where they differ) in both themes and palettes, compare with the AC mockups (`openspec/design/visual-identity/vertailu-*.png`) and fix gaps; keep the PNGs in the scratchpad only.
- [ ] 7.2 `./gradlew test lint assembleDebug`.
- [ ] 7.3 Update `openspec/context/product.md` (visual identity decision, glossary: Värit, Terveyslappu, Puhelimen värit, ⋮ menu), `.claude/CLAUDE.md` map (`QuantityColours`, `RowActions`, `ColourChoice`, harness), mark visual-identity done in `roadmap.md`, and bump MINOR in `gradle.properties`.
